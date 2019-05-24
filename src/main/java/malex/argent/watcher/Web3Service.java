package malex.argent.watcher;

import io.reactivex.disposables.Disposable;
import malex.argent.watcher.model.EthLogData;
import malex.argent.watcher.model.Notification;
import malex.argent.watcher.model.TokenData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.model.SymbolNameDecimalsToken;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class Web3Service {

    private final Logger logger = LoggerFactory.getLogger(Web3Service.class);

    private Web3j web3j;

    private WebSocketService webSocketService;

    private Disposable subscriptionDisposable;

    @Autowired
    private WatchedAddressesConfig config;

    private Credentials credentials;

    private ConcurrentMap<String, TokenData> cachedTokenData =  new ConcurrentHashMap<>();

    @Autowired
    private NotifyEndpointClient notifyEndpointClient;

    @PostConstruct
    private void init() throws URISyntaxException, IOException, CipherException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://ropsten.infura.io/ws"));
        webSocketService = new WebSocketService(webSocketClient, false);
        web3j = Web3j.build(webSocketService);
        credentials = WalletUtils.loadCredentials("pass1234", "/home/alex/temp2/.ethereum/testnet/keystore/UTC--2019-05-20T15-19-22.828550000Z--055155b25376c8c4bb8e0abfaef0ca83e39c6188.json");
    }

    public void start() throws ConnectException {
        webSocketService.connect();
        logger.info("Connected to infura wss");

        subscribe();
    }

    private void subscribe() {
        EthFilter filter = new EthFilter(null, null, config.getAddresses());
        subscriptionDisposable = web3j.ethLogFlowable(filter).subscribe(this::handleEthLogEvent);
        logger.info("Subscribed to ETH Log");
    }

    private void handleEthLogEvent(Log log) throws Exception {
        EthLogData logData = getEthLogData(log);

        String contractAddress = log.getAddress();
        logger.info("ETH Log Event. Contract: {}, Tx: {}", contractAddress, logData.getTxHash());
        logger.info("To: {}, Amount: {}", logData.getToAddress(), logData.getAmount());

        TokenData tokenData = getTokenData(contractAddress);

        logger.info("Token data for contract {}. Symbol: {}; Name: {}; Decimals: {};",
                contractAddress,
                tokenData.getSymbol(),
                tokenData.getName(),
                tokenData.getDecimals()
        );

        Notification notification = assembleNotification(logData, tokenData);

        notifyEndpointClient.sendNotify(notification);
    }

    private Notification assembleNotification(EthLogData logData, TokenData tokenData) {
        Notification notification = new Notification();
        String toAddress = removePadding(logData.getToAddress());
        notification.setWalletAddress(toAddress);
        String tokenValue = getTokenValue(logData.getAmount(), tokenData.getDecimals());
        notification.setTokenValue(tokenValue);
        String tokenName = StringUtils.strip(tokenData.getName());
        notification.setTokenName(tokenName);

        return notification;
    }

    private String removePadding(String toAddress) {
        return "0x" + toAddress.substring(26);
    }

    private String getTokenValue(String amount, BigInteger decimals) {
        BigDecimal pow = BigDecimal.TEN.pow(decimals.intValue());
        return new BigDecimal(amount).divide(pow, decimals.intValue(), RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private EthLogData getEthLogData(Log log) {
        String toAddress = log.getTopics().get(2);
        String strInHex = log.getData().substring(34, 66);
        String result = Numeric.toBigInt(strInHex).toString();
        String txHash = log.getTransactionHash();
        return new EthLogData(toAddress, result, txHash);
    }

    private synchronized TokenData getTokenData(String contractAddress) {
        return cachedTokenData.computeIfAbsent(contractAddress, this::computeTokenData);
    }

    private TokenData computeTokenData(String contractAddress) {
        SymbolNameDecimalsToken erc20 = SymbolNameDecimalsToken.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        try {
            String symbol = erc20.symbol().send();
            String name = erc20.name().send();
            BigInteger decimals = erc20.decimals().send();
            return new TokenData(symbol, name, decimals);
        } catch (Exception ex) {
            throw new RuntimeException("Could not perform contract request", ex);
        }
    }

    public void stop() {
        logger.info("Shutting down wss");
        subscriptionDisposable.dispose();
        web3j.shutdown();
    }
}

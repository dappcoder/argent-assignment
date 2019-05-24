package malex.argent.watcher;

import io.reactivex.disposables.Disposable;
import malex.argent.watcher.model.EthLogData;
import malex.argent.watcher.model.TokenData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Component
public class Web3Service {

    private final Logger logger = LoggerFactory.getLogger(Web3Service.class);

    private Web3j web3j;

    private WebSocketService webSocketService;

    private Disposable subscriptionDisposable;

    private List<String> addresses = Arrays.asList(
            "0x101848D5C5bBca18E6b4431eEdF6B95E9ADF82FA",
            "0xF6fF95D53E08c9660dC7820fD5A775484f77183A",
            "0x7E0480Ca9fD50EB7A3855Cf53c347A1b4d6A2FF5"
    );

    private Credentials credentials;

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
        EthFilter filter = new EthFilter(null, null, addresses);
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
    }

    private EthLogData getEthLogData(Log log) {
        String toAddress = log.getTopics().get(2);
        String strInHex = log.getData().substring(34, 66);
        String result = Numeric.toBigInt(strInHex).toString();
        String txHash = log.getTransactionHash();
        return new EthLogData(toAddress, result, txHash);
    }

    private TokenData getTokenData(String contractAddress) throws Exception {
        SymbolNameDecimalsToken erc20 = SymbolNameDecimalsToken.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        String symbol = erc20.symbol().send();
        String name = erc20.name().send();
        BigInteger decimals = erc20.decimals().send();

        return new TokenData(symbol, name, decimals);
    }

    public void stop() {
        logger.info("Shutting down wss");
        subscriptionDisposable.dispose();
        web3j.shutdown();
    }
}

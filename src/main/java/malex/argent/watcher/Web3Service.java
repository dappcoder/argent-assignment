package malex.argent.watcher;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Numeric;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    private void init() throws URISyntaxException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://ropsten.infura.io/ws"));
        webSocketService = new WebSocketService(webSocketClient, false);
        web3j = Web3j.build(webSocketService);
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

    private void handleEthLogEvent(Log log) {
        String toAddress = log.getTopics().get(2);
        String strInHex = log.getData().substring(34, 66);
        String result = Numeric.toBigInt(strInHex).toString();
        String txHash = log.getTransactionHash();

        logger.info("ETH Log Event. Contract: {}, Tx: {}", log.getAddress(), txHash);
        logger.info("To: {}, Amount: {}", toAddress, result);
    }

    public void stop() {
        logger.info("Shutting down wss");
        subscriptionDisposable.dispose();
        web3j.shutdown();
    }
}

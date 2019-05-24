package malex.argent.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class Web3Service {

    private final Logger log = LoggerFactory.getLogger(Web3Service.class);

    private Web3j web3j;

    private WebSocketService webSocketService;

    @PostConstruct
    private void init() throws URISyntaxException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://ropsten.infura.io/ws"));
        webSocketService = new WebSocketService(webSocketClient, false);
        web3j = Web3j.build(webSocketService);
    }

    public void start() throws ConnectException {
        webSocketService.connect();
        log.info("Connected to infura wss");
    }

    public void stop() {
        log.info("Shutting down wss");
        web3j.shutdown();
    }
}

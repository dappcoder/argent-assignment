package malex.argent.watcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class AppEventListener {

    @Autowired
    private Web3Service web3Service;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws ConnectException {
        web3Service.start();
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        web3Service.stop();
    }
}

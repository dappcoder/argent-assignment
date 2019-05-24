package malex.argent.watcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "watcher")
public class WatchedAddressesConfig {

    private List<String> addresses = new ArrayList<>();

    public List<String> getAddresses() {
        return addresses;
    }
}

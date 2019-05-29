package malex.argent.watcher;

import malex.argent.watcher.model.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class NotifyEndpointClient {

    @Value("${watcher.notifyUrl}")
    private URI uri;

    private RestTemplate template;

    @PostConstruct
    private void init() throws URISyntaxException {
        template = new RestTemplate();
    }

    public boolean sendNotify(Notification notification) {
        ResponseEntity<String> response = template.postForEntity(uri, notification, String.class);
        return (response.getStatusCodeValue() == HttpStatus.OK.value());
    }
}

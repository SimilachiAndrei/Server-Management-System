package cockpit.motherNode.services;

import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class ConnectionManagerService {
    private final ConcurrentHashMap<String, ConnectionService> connections = new ConcurrentHashMap<>();

    public void put(String jwt, ConnectionService connectionService) {
        connections.put(jwt, connectionService);
    }

    public ConnectionService get(String jwt) {
        return connections.get(jwt);
    }

    public void remove(String jwt) {
        connections.remove(jwt);
    }

    public int getConnectionCount() {
        return connections.size();
    }
}

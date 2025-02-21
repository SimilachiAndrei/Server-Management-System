package cockpit.motherNode.services;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class ConnectionManagerService {
    private final ConcurrentHashMap<Map<String,String>, ConnectionService> connections = new ConcurrentHashMap<>();

    public void put(Map<String,String> unique, ConnectionService connectionService) {
        connections.put(unique, connectionService);
    }

    public ConnectionService get(Map<String,String> unique) {
        return connections.get(unique);
    }

    public void remove(Map<String,String> unique) {
        connections.remove(unique);
    }

    public int getConnectionCount() {
        return connections.size();
    }
}

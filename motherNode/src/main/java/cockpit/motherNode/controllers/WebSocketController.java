package cockpit.motherNode.controllers;

import cockpit.motherNode.services.ConnectionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

@Controller
public class WebSocketController {
//TO DO : maybe extend for multiple users , by identifying the JWT
    private final ConnectionService connectionService;

    public WebSocketController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
    

    @MessageMapping("/sendInput")
    public void a(Map<String, String> payload) throws Exception {
        String input = payload.get("input");
        System.out.println(input);

        Socket childNodeSocket = connectionService.getCommandThread().getSocket();
        if (childNodeSocket != null && !childNodeSocket.isClosed()) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(childNodeSocket.getOutputStream()));
            writer.write(input);
            writer.flush();
        }
    }

    @MessageMapping("/terminateTerminal")
    public void terminateTerminal(String sessionId) throws Exception {
        connectionService.disconnect();
    }
}



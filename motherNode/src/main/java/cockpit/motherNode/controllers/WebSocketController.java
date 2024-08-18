package cockpit.motherNode.controllers;

import cockpit.motherNode.services.CommandService;
import cockpit.motherNode.services.ConnectionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
//TO DO : maybe extend for multiple users , by identifying the JWT
    private final ConnectionService connectionService;

    public WebSocketController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }


    @MessageMapping("/sendCommand")
    @SendTo("/topic/commandResponse")
    public String processCommand(String command) {
        return connectionService.getCommandService().sendCommand(command);
    }

    @MessageMapping("/disconnect")
    public void handleDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        connectionService.disconnect();

    }
}



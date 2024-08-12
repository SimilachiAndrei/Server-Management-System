package cockpit.motherNode.controllers;

import cockpit.motherNode.services.CommandService;
import cockpit.motherNode.services.ConnectionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final CommandService commandService;

    public WebSocketController(ConnectionService connectionService) {
        this.commandService = connectionService.getCommandService();
    }

    @MessageMapping("/sendCommand")
    @SendTo("/topic/commandResponse")
    public String processCommand(String command) {
        return commandService.sendCommand(command); // Forward the command to the CommandThread and return the response
    }
}



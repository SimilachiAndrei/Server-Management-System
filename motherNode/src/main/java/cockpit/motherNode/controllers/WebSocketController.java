package cockpit.motherNode.controllers;

import cockpit.motherNode.services.CommandService;
import cockpit.motherNode.services.ConnectionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final ConnectionService connectionService;

    public WebSocketController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }


    @MessageMapping("/sendCommand")
    @SendTo("/topic/commandResponse")
    public String processCommand(String command) {
        return connectionService.getCommandService().sendCommand(command);
    }

//    @MessageMapping("/sendCommand")
//    @SendTo("/topic/commandResponse")
//    public String handleCommand(String command) {
//        // Process the command and return a response
//        String response = processCommand(command);
//        return response;
//    }
//
//    private String processCommand(String command) {
//        // Implement your command processing logic here
//        return "Processed: " + command;
//    }
}



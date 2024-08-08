package cockpit.motherNode.controllers;


import cockpit.motherNode.dtos.ConnectDto;
import cockpit.motherNode.services.AppService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor

@RestController
@RequestMapping("/api/endpoint")
public class WebSocketController {

    private AppService appService;

    @MessageMapping("/connect")
    public void connectToApp(@Payload ConnectDto request) {
        String ipAddress = request.getAddress();
        int port = request.getPort();

        appService.connectToApp(ipAddress, port);
    }

    @MessageMapping("/disconnect")
    public void disconnectFromApp() {
        appService.disconnectFromApp();
    }
}
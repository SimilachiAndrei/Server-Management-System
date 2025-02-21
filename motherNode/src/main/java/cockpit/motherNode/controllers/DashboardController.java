package cockpit.motherNode.controllers;

import cockpit.motherNode.dtos.ConnectDto;
import cockpit.motherNode.dtos.EndpointDto;
import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.responses.EndpointResponse;
import cockpit.motherNode.services.ConnectionManagerService;
import cockpit.motherNode.services.ConnectionService;
import cockpit.motherNode.services.DashboardService;
import cockpit.motherNode.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cockpit.motherNode.utilities.IpAddressUtil.inetAddressToString;

@AllArgsConstructor

@RequestMapping("/api/endpoint")
@RestController
public class DashboardController {
    private DashboardService dashboardService;
    @Autowired
    private ConnectionManagerService connectionManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@RequestBody ConnectDto connectDto, @RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.substring(7);

        ConnectionService connectionService = new ConnectionService(messagingTemplate);
        boolean success = connectionService.initiateConnection(connectDto.getAddress(), connectDto.getPort(),connectDto.getName(), jwt);

        Map<String, String> response = new HashMap<>();
        if (success) {
            Map<String,String> unique = new HashMap<>();
            unique.put(jwt, connectDto.getName());
            connectionManager.put(unique, connectionService);

            response.put("message", "Connection established successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to establish connection.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<EndpointResponse>> getAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jwt = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(jwt);

        List<EndpointResponse> endpointResponses = new ArrayList<>();
        List<Endpoint> endpoints = dashboardService.getAll(username);
        endpoints.forEach(endpoint -> {
            EndpointResponse endpointResponse = new EndpointResponse();
            endpointResponse.setDescription(endpoint.getDescription());
            endpointResponse.setName(endpoint.getName());
            endpointResponse.setAddress(inetAddressToString(endpoint.getIpV4()));
            endpointResponse.setId(endpoint.getId());
            endpointResponse.setPort(endpoint.getPort());
            endpointResponses.add(endpointResponse);
        });

        return ResponseEntity.ok(endpointResponses);
    }

    @PostMapping("/add")
    public ResponseEntity<EndpointResponse> add(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody EndpointDto endpointDto) throws UnknownHostException {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract JWT from the Authorization header
        String jwt = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        Endpoint endpoint = dashboardService.add(endpointDto, username);
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setAddress(inetAddressToString(endpoint.getIpV4()));
        endpointResponse.setName(endpoint.getName());
        endpointResponse.setDescription(endpoint.getDescription());
        endpointResponse.setPort(endpoint.getPort());
        return ResponseEntity.ok(endpointResponse);
    }
}

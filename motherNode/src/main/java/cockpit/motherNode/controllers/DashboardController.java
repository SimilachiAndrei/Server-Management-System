package cockpit.motherNode.controllers;

import cockpit.motherNode.dtos.ConnectDto;
import cockpit.motherNode.dtos.EndpointDto;
import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.responses.EndpointResponse;
import cockpit.motherNode.services.ConnectionService;
import cockpit.motherNode.services.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cockpit.motherNode.utilities.IpAddressUtil.inetAddressToString;

@AllArgsConstructor

@RequestMapping("/api/endpoint")
@RestController
public class DashboardController {
    private DashboardService dashboardService;
    private ConnectionService connectionService;

    @PostMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@RequestBody ConnectDto connectDto) {
        boolean success = connectionService.initiateConnection(connectDto.getAddress(), connectDto.getPort());

        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Connection established successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to establish connection.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<EndpointResponse>> getAll() {
        List<EndpointResponse> endpointResponses = new ArrayList<>();
        List<Endpoint> endpoints = dashboardService.getAll();
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
    public ResponseEntity<EndpointResponse> add(@RequestBody EndpointDto endpointDto) throws UnknownHostException {
        Endpoint endpoint = dashboardService.add(endpointDto);
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setAddress(inetAddressToString(endpoint.getIpV4()));
        endpointResponse.setName(endpoint.getName());
        endpointResponse.setDescription(endpoint.getDescription());
        endpointResponse.setPort(endpoint.getPort());
        return ResponseEntity.ok(endpointResponse);
    }
}

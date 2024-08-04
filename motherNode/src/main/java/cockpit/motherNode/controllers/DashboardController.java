package cockpit.motherNode.controllers;

import cockpit.motherNode.dtos.EndpointDto;
import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.responses.EndpointResponse;
import cockpit.motherNode.services.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static cockpit.motherNode.utilities.IpAddressUtil.inetAddressToString;

@AllArgsConstructor

@RequestMapping("/api/endpoint")
@RestController
public class DashboardController {
    private DashboardService dashboardService;

    @PostMapping("/connect")
    public ResponseEntity<String> connect(@RequestBody String address)
    {
        return ResponseEntity.ok("mock response");
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
        return ResponseEntity.ok(endpointResponse);
    }
}

package cockpit.motherNode.services;

import cockpit.motherNode.dtos.EndpointDto;
import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.repositories.EndpointRepository;
import cockpit.motherNode.responses.EndpointResponse;
import cockpit.motherNode.utilities.IpAddressUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static cockpit.motherNode.utilities.IpAddressUtil.stringToInetAddress;

@AllArgsConstructor

@Service
public class DashboardService {
    private final EndpointRepository endpointRepository;

    public List<Endpoint> getAll() {
        return endpointRepository.findAll();
    }

    public Endpoint add(EndpointDto input) throws UnknownHostException {
        Endpoint endpoint = new Endpoint();
        endpoint.setDescription(input.getDescription());
        endpoint.setName(input.getName());
        endpoint.setIpV4(stringToInetAddress(input.getAddress()));
        return endpointRepository.save(endpoint);
    }
}

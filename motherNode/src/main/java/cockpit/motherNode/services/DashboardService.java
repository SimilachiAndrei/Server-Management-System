package cockpit.motherNode.services;

import cockpit.motherNode.dtos.EndpointDto;
import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.entities.User;
import cockpit.motherNode.repositories.EndpointRepository;
import cockpit.motherNode.repositories.UserRepository;
import cockpit.motherNode.responses.EndpointResponse;
import cockpit.motherNode.utilities.IpAddressUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cockpit.motherNode.utilities.IpAddressUtil.stringToInetAddress;

@AllArgsConstructor

@Service
public class DashboardService {
    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;

    public List<Endpoint> getAll(String username) {
        return endpointRepository.findAll(userRepository.findByUsername(username).orElseThrow()).orElseThrow();
    }

    public Endpoint add(EndpointDto input, String username) throws UnknownHostException {
        Endpoint endpoint = new Endpoint();
        endpoint.setDescription(input.getDescription());
        endpoint.setName(input.getName());
        endpoint.setIpV4(stringToInetAddress(input.getAddress()));
        endpoint.setPort(input.getPort());
        endpoint.setUser(userRepository.findByUsername(username).orElseThrow());
        return endpointRepository.save(endpoint);
    }
}

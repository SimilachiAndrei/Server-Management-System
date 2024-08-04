package cockpit.motherNode.repositories;

import cockpit.motherNode.entities.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.Optional;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, Integer> {
}


package cockpit.motherNode.repositories;

import cockpit.motherNode.entities.Endpoint;
import cockpit.motherNode.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import java.util.Optional;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, Integer> {

    @Query("SELECT e FROM Endpoint e WHERE e.user.id = :userId")
    List<Endpoint> findByUserId(Integer userId);

    default Optional<List<Endpoint>> findAll(User user){
        return Optional.of(findByUserId(user.getId()));
    }
}


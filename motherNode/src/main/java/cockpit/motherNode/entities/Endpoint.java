package cockpit.motherNode.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "endpoints")
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "inet", nullable = false, unique = true)
    private InetAddress ipV4;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Endpoint(Integer id, String name, InetAddress ipV4) {
        this.id = id;
        this.name = name;
        this.ipV4 = ipV4;
    }
}

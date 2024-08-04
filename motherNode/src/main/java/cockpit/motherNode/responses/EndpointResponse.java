package cockpit.motherNode.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndpointResponse {
    private Integer id;
    private String name;
    private String description;
    private String address;
}

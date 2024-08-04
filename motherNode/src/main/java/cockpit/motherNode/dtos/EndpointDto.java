package cockpit.motherNode.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndpointDto {
    private String name;
    private String description;
    private String address;
}


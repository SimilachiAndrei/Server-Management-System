package cockpit.motherNode.dtos;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConnectDto {
    private String address;
    private Integer port;
}

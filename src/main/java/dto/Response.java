package dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Response {

    String description;

    String url;
}

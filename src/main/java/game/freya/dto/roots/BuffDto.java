package game.freya.dto.roots;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class BuffDto {
    @NotNull
    private UUID uid;

    @NotNull
    private String name;

    public void activate(CharacterDto characterDto) {

    }

    public void deactivate(CharacterDto characterDto) {

    }
}

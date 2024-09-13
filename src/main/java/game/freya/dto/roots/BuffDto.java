package game.freya.dto.roots;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true, fluent = true)
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

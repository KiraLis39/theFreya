package game.freya.dto.roots;

import game.freya.interfaces.root.iBuff;
import io.swagger.v3.oas.annotations.media.Schema;
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
public abstract class BuffDto implements iBuff {
    @NotNull
    @Schema(description = "UUID бафа/дебаффа", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID uid;

    @NotNull
    @Schema(description = "Имя/название баффа/дебаффа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Override
    public void activate(CharacterDto characterDto) {
        characterDto.addBuff(this);
    }

    @Override
    public void deactivate(CharacterDto characterDto) {
        characterDto.removeBuff(this);
    }
}

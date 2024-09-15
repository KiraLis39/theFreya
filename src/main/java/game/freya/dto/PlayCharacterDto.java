package game.freya.dto;

import game.freya.dto.roots.CharacterDto;
import game.freya.entities.roots.World;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Getter
@SuperBuilder
public class PlayCharacterDto extends CharacterDto {
    @Builder.Default
    @Schema(description = "Миры, в которых данный герой активен", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Set<World> worlds = new LinkedHashSet<>(1);
}

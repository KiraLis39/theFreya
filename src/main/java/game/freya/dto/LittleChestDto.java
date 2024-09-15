package game.freya.dto;

import game.freya.dto.roots.StorageDto;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@SuperBuilder
@Accessors(chain = true, fluent = true)
public class LittleChestDto extends StorageDto {
}

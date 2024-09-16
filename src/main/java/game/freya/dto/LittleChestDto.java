package game.freya.dto;

import game.freya.dto.roots.StorageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Accessors(chain = true, fluent = true)
public class LittleChestDto extends StorageDto {
}

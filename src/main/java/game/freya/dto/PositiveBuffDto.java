package game.freya.dto;

import game.freya.dto.roots.BuffDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@RequiredArgsConstructor
@Accessors(chain = true, fluent = true)
public class PositiveBuffDto extends BuffDto {
}

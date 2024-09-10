package game.freya.dto.roots;

import game.freya.interfaces.iGameObject;
import game.freya.interfaces.iWeapon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public non-sealed abstract class WeaponDto extends AbstractEntityDto implements iGameObject, iWeapon {
}

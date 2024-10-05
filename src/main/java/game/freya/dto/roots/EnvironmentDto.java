package game.freya.dto.roots;

import game.freya.interfaces.subroot.iEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.Graphics2D;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public abstract class EnvironmentDto extends AbstractEntityDto implements iEnvironment {
    @Override
    public void draw(Graphics2D g2D) {
    }
}

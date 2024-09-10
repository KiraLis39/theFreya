package game.freya.dto.roots;

import game.freya.interfaces.iEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.*;
import java.util.Random;

@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
public non-sealed class EnvironmentDto extends AbstractEntityDto implements iEnvironment {
    @Getter
    private static final Random random = new Random();

    @Override
    public void draw(Graphics2D g2D) {

    }
}

package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.interfaces.subroot.iEnvironment;
import jakarta.persistence.Transient;
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
public abstract class EnvironmentDto extends AbstractEntityDto implements iEnvironment {
    @Getter
    @Transient
    @JsonIgnore
    private static final Random random = new Random();

    @Override
    public void draw(Graphics2D g2D) {

    }
}

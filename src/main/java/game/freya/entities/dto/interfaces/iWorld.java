package game.freya.entities.dto.interfaces;

import game.freya.entities.dto.PlayerDTO;

import java.awt.*;

public interface iWorld {

    void addPlayer(PlayerDTO playerDTO);

    void removePlayer(PlayerDTO playerDTO);

    void draw(Graphics2D g2D);
}

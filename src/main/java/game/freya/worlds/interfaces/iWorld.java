package game.freya.worlds.interfaces;

import game.freya.players.Player;
import game.freya.worlds.HardnessLevel;

import java.awt.*;

public interface iWorld {
    void setTitle(String title);

    void setDimension(Dimension dimension);

    void setHardnessLevel(HardnessLevel level);

    void addPlayer(Player player);

    void removePlayer(Player player);
}

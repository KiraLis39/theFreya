package game.freya.worlds;

import game.freya.players.Player;
import game.freya.worlds.interfaces.iWorld;
import lombok.Getter;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Getter
public class World implements iWorld {
    private final UUID uid;

    private String title;
    private Dimension dimension;
    private HardnessLevel level;

    private String passwordHash;

    private Map<String, Player> players = new HashMap<>(2);

    public World(String title) {
        this(UUID.randomUUID(), title);
    }

    public World(UUID uid, String title) {
        this.uid = uid;
        this.title = title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public void setHardnessLevel(HardnessLevel level) {
        this.level = level;
    }

    @Override
    public void addPlayer(Player player) {
        players.putIfAbsent(player.getNickName(), player);
    }

    @Override
    public void removePlayer(Player player) {
        players.remove(player.getNickName());
    }
}

package game.freya.worlds;

import game.freya.players.Player;
import game.freya.worlds.interfaces.iWorld;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class World implements iWorld {
    private final UUID uid;
    private final Map<String, Player> players = HashMap.newHashMap(2);

    private final String title;
    private final int passwordHash;

    private final Dimension dimension;
    private final HardnessLevel level;

    public World(UUID uid, String title, HardnessLevel level, Dimension dimension, int passwordHash) {
        this.uid = uid;
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
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

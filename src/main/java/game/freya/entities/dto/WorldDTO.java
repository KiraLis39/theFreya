package game.freya.entities.dto;

import game.freya.entities.dto.interfaces.iWorld;
import game.freya.enums.HardnessLevel;
import lombok.Getter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class WorldDTO implements iWorld {

    private final UUID uid;

    private final Map<String, PlayerDTO> players = HashMap.newHashMap(2);

    private final String title;
    private final int passwordHash;

    private final Dimension dimension;
    private final HardnessLevel level;

    public WorldDTO(String title, HardnessLevel level, int passwordHash) {
        this.uid = UUID.randomUUID();
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = new Dimension(128, 128);
    }

    public WorldDTO(UUID uid, String title, HardnessLevel level, Dimension dimension, int passwordHash) {
        this.uid = uid;
        this.title = title;
        this.passwordHash = passwordHash;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public void addPlayer(PlayerDTO playerDTO) {
        players.putIfAbsent(playerDTO.getNickName(), playerDTO);
    }

    @Override
    public void removePlayer(PlayerDTO playerDTO) {
        players.remove(playerDTO.getNickName());
    }

    @Override
    public void draw(Graphics2D g2D) {

    }
}

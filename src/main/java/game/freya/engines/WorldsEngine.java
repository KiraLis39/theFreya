package game.freya.engines;

import game.freya.worlds.HardnessLevel;
import game.freya.worlds.World;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldsEngine {

    private final Map<String, World> worlds = HashMap.newHashMap(3);

    public WorldsEngine() {
        // fill the worlds map from DB...
    }

    public World create(String title, String password) {
        if (worlds.containsKey(title)) {
            return worlds.get(title);
        }
        // return worldRepo.save(w);
        return new World(UUID.randomUUID(), title, HardnessLevel.EASY, new Dimension(128, 128),
                password == null ? -1 : password.hashCode());
    }

    public World save(World world) {
        return null; // return worldRepo.save(w);
    }

    public World load(String title) {
        return null; // return worldRepo.findByTitle(title);
    }

    public void delete(World world) {
        // worldRepo.deleteByTitle(title);
    }
}

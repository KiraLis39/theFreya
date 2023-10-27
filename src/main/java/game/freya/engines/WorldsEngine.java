package game.freya.engines;

import game.freya.worlds.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldsEngine {

    private final Map<String, World> worlds = new HashMap<>(3);

    public WorldsEngine() {
        // fill the worlds map from DB...
    }

    public World create(String title) {
        if (worlds.containsKey(title)) {
            return worlds.get(title);
        }
        World w = new World(UUID.randomUUID(), title);
//      w.setLevel();
//      w.setDimension();
//      w.setPasswordHash();
        return w; // return worldRepo.save(w);
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

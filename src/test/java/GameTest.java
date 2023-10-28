import game.freya.engines.WorldsEngine;
import game.freya.items.Shovel;
import game.freya.items.prototypes.Storable;
import game.freya.players.Player;
import game.freya.worlds.World;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Slf4j
public class GameTest {

    @Test
    void run() {
        WorldsEngine worldsEngine = new WorldsEngine();
        World demoWorld = worldsEngine.create("some_new", null);
        log.info("The world {} was created successfully!", demoWorld.getTitle());

        Player player01 = new Player("KiraLis39", "angelicalis39@mail.ru", null);
        demoWorld.addPlayer(player01);
        log.info("The world {} has players: {}", demoWorld.getTitle(), demoWorld.getPlayers());

        Storable shovel = new Shovel();

        log.info("The Player {} has something into his inventory: {}", player01.getNickName(), !player01.getInventory().isEmpty());
        log.info("Put the {} into player`s inventory...", shovel.getName());
        player01.getInventory().put(shovel);
        log.info("The Player {} has something into his inventory: {}", player01.getNickName(), !player01.getInventory().isEmpty());

        log.info("What kind the Player {} has into his inventory: {}", player01.getNickName(), player01.getInventory());

        log.info("Clean the inventory of the Player {}...", player01.getNickName());
        Set<Storable> removed = player01.getInventory().clear();
        log.info("The Player {} has something into his inventory: {}", player01.getNickName(), !player01.getInventory().isEmpty());

        log.info("Removing the Player {} from the World {}...", player01.getNickName(), demoWorld.getTitle());
        demoWorld.removePlayer(player01);
        log.info("The world {} has players: {}", demoWorld.getTitle(), demoWorld.getPlayers());
    }
}

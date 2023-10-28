import game.freya.engines.WorldsEngine;
import game.freya.items.Shovel;
import game.freya.items.prototypes.Tool;
import game.freya.players.Player;
import game.freya.players.interfaces.iPlayer;
import game.freya.worlds.HardnessLevel;
import game.freya.worlds.World;
import game.freya.worlds.interfaces.iWorld;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.Set;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameTest {
    private final String password = "123";
    private final int passwordHash = password.hashCode();
    private final HardnessLevel level = HardnessLevel.EASY;
    private static WorldsEngine worldsEngine;
    private static World demoWorld;
    private static Player player01;
    private static Tool shovel;

    @BeforeAll
    static void initEngine() {
        worldsEngine = new WorldsEngine();
    }

    @Test
    @Order(1)
    void createTheWorld() {
        demoWorld = worldsEngine.create("some_new", password, level);

        Assertions.assertNotNull(demoWorld);
        Assertions.assertNotNull(demoWorld.getUid());
        Assertions.assertNotNull(demoWorld.getDimension());
        Assertions.assertEquals("some_new", demoWorld.getTitle());
        Assertions.assertEquals(0, demoWorld.getPlayers().size());
        Assertions.assertEquals(passwordHash, password.hashCode());
        Assertions.assertEquals(level, demoWorld.getLevel());
        Assertions.assertInstanceOf(iWorld.class, demoWorld);
    }

    @SneakyThrows
    @Test
    @Order(2)
    void createThePlayer() {
        player01 = new Player("KiraLis39", "angelicalis39@mail.ru", "./src/test/resources/avatars_test/ava.png");
        demoWorld.addPlayer(player01);

        Assertions.assertNotNull(player01);
        Assertions.assertNotNull(player01.getUid());
        Assertions.assertNotNull(player01.getInventory());
        Assertions.assertEquals("KiraLis39", player01.getNickName());
        Assertions.assertEquals("angelicalis39@mail.ru", player01.getEmail());
//        Assertions.assertEquals(
//                ImageIO.read(new File("./src/test/resources/avatars_test/ava.png")).getData(), player01.getAvatar().getData());
        Assertions.assertEquals(1, player01.getLevel());
        Assertions.assertEquals(0, player01.getExperience());
        Assertions.assertEquals(player01.MAX_PLAYER_HEALTH, player01.getHealth());
        Assertions.assertInstanceOf(iPlayer.class, player01);
        Assertions.assertFalse(player01.isDead());

        player01.attack(player01);
        Assertions.assertEquals(player01.MAX_PLAYER_HEALTH, player01.getHealth());

        player01.hurt(10);
        Assertions.assertEquals(player01.MAX_PLAYER_HEALTH - 10, player01.getHealth());

        player01.hurt(10);
        player01.heal(100);
        Assertions.assertEquals(player01.MAX_PLAYER_HEALTH, player01.getHealth());

        player01.increaseExp(0.25f);
        Assertions.assertEquals(0.25f, player01.getExperience());

        player01.hurt(player01.MAX_PLAYER_HEALTH);
        Assertions.assertEquals(0, player01.getHealth());
        Assertions.assertTrue(player01.isDead());

        player01.heal(999);
        Assertions.assertEquals(0, player01.getHealth());
        Assertions.assertTrue(player01.isDead());

        player01.increaseExp(0.50f);
        Assertions.assertEquals(0.25f * 0.1f, player01.getExperience(), 0.001f);
    }

    @Test
    @Order(3)
    void createThePlayersStaff() {
        shovel = new Shovel("Super shovel");

        Assertions.assertNotNull(shovel);
        Assertions.assertNotNull(shovel.getUid());
        Assertions.assertEquals("Super shovel", shovel.getName());

        Assertions.assertFalse(shovel.isBroken());
        shovel.onBreak();
        Assertions.assertTrue(shovel.isBroken());
        shovel.repair();
        Assertions.assertFalse(shovel.isBroken());

        Assertions.assertEquals(16, player01.getInventory().size());
        Assertions.assertTrue(player01.getInventory().isEmpty());
        player01.getInventory().put(shovel);
        Assertions.assertTrue(player01.getInventory().has(shovel));
        player01.getInventory().get(shovel);
        Assertions.assertFalse(player01.getInventory().has(shovel));
        Assertions.assertTrue(player01.getInventory().isEmpty());

        player01.getInventory().put(shovel);
        Assertions.assertArrayEquals(Set.of(shovel).toArray(), player01.getInventory().clear().toArray());
        Assertions.assertTrue(player01.getInventory().isEmpty());


        log.info("Removing the Player {} from the World {}...", player01.getNickName(), demoWorld.getTitle());
        demoWorld.removePlayer(player01);
        log.info("The world {} has players: {}", demoWorld.getTitle(), demoWorld.getPlayers());
    }

    @Test
    @Order(4)
    void removeThePlayerFromWorld() {
        demoWorld.removePlayer(player01);

        Assertions.assertEquals(0, demoWorld.getPlayers().size());
    }
}

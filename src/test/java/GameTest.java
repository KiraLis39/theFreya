import game.freya.items.Shovel;
import game.freya.items.prototypes.Tool;
import game.freya.entities.dto.PlayerDTO;
import game.freya.entities.dto.interfaces.iPlayer;
import game.freya.enums.HardnessLevel;
import game.freya.entities.dto.WorldDTO;
import game.freya.entities.dto.interfaces.iWorld;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
    private static WorldDTO demoWorldDTO;
    private static PlayerDTO playerDTO01;

    @Test
    @Order(1)
    void createTheWorld() {
        demoWorldDTO = new WorldDTO("some_new", level, password.hashCode());

        Assertions.assertNotNull(demoWorldDTO);
        Assertions.assertNotNull(demoWorldDTO.getUid());
        Assertions.assertNotNull(demoWorldDTO.getDimension());
        Assertions.assertEquals("some_new", demoWorldDTO.getTitle());
        Assertions.assertEquals(0, demoWorldDTO.getPlayers().size());
        Assertions.assertEquals(passwordHash, password.hashCode());
        Assertions.assertEquals(level, demoWorldDTO.getLevel());
        Assertions.assertInstanceOf(iWorld.class, demoWorldDTO);
    }

    @SneakyThrows
    @Test
    @Order(2)
    void createThePlayer() {
        playerDTO01 = new PlayerDTO("KiraLis39", "angelicalis39@mail.ru", "./src/test/resources/avatars_test/ava.png");
        demoWorldDTO.addPlayer(playerDTO01);

        Assertions.assertNotNull(playerDTO01);
        Assertions.assertNotNull(playerDTO01.getUid());
        Assertions.assertNotNull(playerDTO01.getInventory());
        Assertions.assertEquals("KiraLis39", playerDTO01.getNickName());
        Assertions.assertEquals("angelicalis39@mail.ru", playerDTO01.getEmail());
//        Assertions.assertEquals(
//                ImageIO.read(new File("./src/test/resources/avatars_test/ava.png")).getData(), player01.getAvatar().getData());
        Assertions.assertEquals(1, playerDTO01.getLevel());
        Assertions.assertEquals(0, playerDTO01.getExperience());
        Assertions.assertEquals(playerDTO01.MAX_HEALTH, playerDTO01.getHealth());
        Assertions.assertInstanceOf(iPlayer.class, playerDTO01);
        Assertions.assertFalse(playerDTO01.isDead());

        playerDTO01.attack(playerDTO01);
        Assertions.assertEquals(playerDTO01.MAX_HEALTH, playerDTO01.getHealth());

        playerDTO01.hurt(10);
        Assertions.assertEquals(playerDTO01.MAX_HEALTH - 10, playerDTO01.getHealth());

        playerDTO01.hurt(10);
        playerDTO01.heal(100);
        Assertions.assertEquals(playerDTO01.MAX_HEALTH, playerDTO01.getHealth());

        playerDTO01.increaseExp(0.25f);
        Assertions.assertEquals(0.25f, playerDTO01.getExperience());

        playerDTO01.hurt(playerDTO01.MAX_HEALTH);
        Assertions.assertEquals(0, playerDTO01.getHealth());
        Assertions.assertTrue(playerDTO01.isDead());

        playerDTO01.heal(999);
        Assertions.assertEquals(0, playerDTO01.getHealth());
        Assertions.assertTrue(playerDTO01.isDead());

        playerDTO01.increaseExp(0.50f);
        Assertions.assertEquals(0.25f * 0.1f, playerDTO01.getExperience(), 0.001f);
    }

    @Test
    @Order(3)
    void createThePlayersStaff() {
        Tool shovel = new Shovel("Super shovel");

        Assertions.assertNotNull(shovel);
        Assertions.assertNotNull(shovel.getUid());
        Assertions.assertEquals("Super shovel", shovel.getName());

        Assertions.assertFalse(shovel.isBroken());
        shovel.onBreak();
        Assertions.assertTrue(shovel.isBroken());
        shovel.repair();
        Assertions.assertFalse(shovel.isBroken());

        Assertions.assertEquals(16, playerDTO01.getInventory().size());
        Assertions.assertTrue(playerDTO01.getInventory().isEmpty());
        playerDTO01.getInventory().put(shovel);
        Assertions.assertTrue(playerDTO01.getInventory().has(shovel));
        playerDTO01.getInventory().get(shovel);
        Assertions.assertFalse(playerDTO01.getInventory().has(shovel));
        Assertions.assertTrue(playerDTO01.getInventory().isEmpty());

        playerDTO01.getInventory().put(shovel);
        Assertions.assertArrayEquals(Set.of(shovel).toArray(), playerDTO01.getInventory().clear().toArray());
        Assertions.assertTrue(playerDTO01.getInventory().isEmpty());


        log.info("Removing the Player {} from the World {}...", playerDTO01.getNickName(), demoWorldDTO.getTitle());
        demoWorldDTO.removePlayer(playerDTO01);
        log.info("The world {} has players: {}", demoWorldDTO.getTitle(), demoWorldDTO.getPlayers());
    }

    @Test
    @Order(4)
    void removeThePlayerFromWorld() {
        demoWorldDTO.removePlayer(playerDTO01);

        Assertions.assertEquals(0, demoWorldDTO.getPlayers().size());
    }
}

package game.freya.players;

import game.freya.items.containers.Backpack;
import game.freya.players.interfaces.iEntity;
import game.freya.players.interfaces.iPlayer;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Getter
public class Player implements iPlayer {
    private final UUID uid;
    private final String nickName;
    private String email;
    private String avatarUrl;
    private float experience = 0f;
    private final short level = 1;
    private final float currentAttackPower = 1.0f;

    private short health;

    private Backpack inventory;
    private HurtLevel hurtLevel;

    public Player(String nickName) {
        this(UUID.randomUUID(), nickName);
    }

    public Player(UUID uid, String nickName) {
        this.uid = uid;
        this.nickName = nickName;
    }

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public void hurt(float hurtPoints) {
        this.health -= hurtPoints;
        if (this.health <= 0) {
            this.health = 0;
        }
        recheckHurtLevel();
    }

    @Override
    public void heal(float healPoints) {
        this.health += healPoints;
        if (this.health >= 100) {
            this.health = 100;
        }
        recheckHurtLevel();
    }

    @Override
    public void attack(iEntity entity) {
        entity.hurt(currentAttackPower);
    }

    private void recheckHurtLevel() {
        if (this.health <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
        } else if (this.health <= 30) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.health <= 60) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.health <= 90) {
            this.hurtLevel = HurtLevel.LIGHT_HURT;
        } else {
            this.hurtLevel = HurtLevel.HEALTHFUL;
        }
    }

    @Override
    public BufferedImage getAvatar() {
        File avatarFile = new File(avatarUrl);
        if (avatarFile.exists()) {
            try {
                return ImageIO.read(avatarFile);
            } catch (IOException ioe) {
                log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(ioe));
            }
        }
        return null;
    }

    @Override
    public void increaseExp(float increaseValue) {
        this.experience += increaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void decreaseExp(float decreaseValue) {
        this.experience -= decreaseValue;
        recheckPlayerLevel();
    }

    private void recheckPlayerLevel() {
        // level
    }
}

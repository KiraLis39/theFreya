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
    private static final short MAX_PLAYER_HEALTH = 100;

    private final UUID uid;
    private final String nickName;
    private final String email;
    private final String avatarUrl;
    private final Backpack inventory;
    private final short level = 1;
    private final float currentAttackPower = 1.0f;

    private float experience = 0f;
    private short health;

    private HurtLevel hurtLevel;

    public Player(UUID uid, String nickName, String email, String avatarUrl) {
        this.uid = uid;
        this.nickName = nickName;
        this.email = email;
        this.avatarUrl = avatarUrl;

        inventory = new Backpack("The ".concat(this.nickName).concat("`s backpack"));
    }

    @Override
    public boolean isDead() {
        return this.hurtLevel.equals(HurtLevel.DEAD);
    }

    @Override
    public void hurt(float hurtPoints) {
        this.health -= hurtPoints;
        if (this.health < 0) {
            this.health = 0;
        }
        recheckHurtLevel();
    }

    @Override
    public void heal(float healPoints) {
        this.health += healPoints;
        if (this.health > MAX_PLAYER_HEALTH) {
            this.health = MAX_PLAYER_HEALTH;
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
        } else if (this.health <= MAX_PLAYER_HEALTH * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.health <= MAX_PLAYER_HEALTH * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.health <= MAX_PLAYER_HEALTH * 0.9f) {
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

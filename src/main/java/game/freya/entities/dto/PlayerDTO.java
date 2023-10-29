package game.freya.entities.dto;

import game.freya.entities.dto.interfaces.iPlayer;
import game.freya.enums.HurtLevel;
import game.freya.items.containers.Backpack;
import game.freya.items.interfaces.iEntity;
import game.freya.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class PlayerDTO implements iPlayer {
    private final UUID uid;
    private final String nickName;
    private final String email;
    private final String avatarUrl;
    private final Backpack inventory;
    private final List<Buff> buffs = new ArrayList<>(9);

    private float currentAttackPower = 1.0f;
    private float experience = 0f;

    private short level = 1;
    private short health = MAX_HEALTH;

    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    public PlayerDTO(String nickName, String email, String avatarUrl) {
        this(UUID.randomUUID(), nickName, email, avatarUrl);
    }

    public PlayerDTO(UUID uid, String nickName, String email, String avatarUrl) {
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
        if (isDead()) {
            log.warn("Can`t heal the dead corps of player {}!", getNickName());
            return;
        }
        this.health += healPoints;
        if (this.health > MAX_HEALTH) {
            this.health = MAX_HEALTH;
        }
        recheckHurtLevel();
    }

    @Override
    public void attack(iEntity entity) {
        if (!entity.equals(this)) {
            entity.hurt(currentAttackPower);
        } else {
            log.warn("Player {} can`t attack itself!", getNickName());
        }
    }

    private void recheckHurtLevel() {
        if (this.health <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
            decreaseExp(getExperience() - getExperience() * 0.1f); // -10% exp by death
        } else if (this.health <= MAX_HEALTH * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.health <= MAX_HEALTH * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.health <= MAX_HEALTH * 0.9f) {
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
        if (isDead()) {
            return;
        }
        this.experience += increaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void decreaseExp(float decreaseValue) {
        this.experience -= decreaseValue;
        recheckPlayerLevel();
    }

    @Override
    public void addBuff(Buff buff) {
        buffs.add(buff);
        for (Buff b : buffs) {
            b.activate(this);
        }
    }

    @Override
    public void removeBuff(Buff buff) {
        buffs.remove(buff);
        buff.deactivate(this);
    }

    @Override
    public void setLevel(short level) {
        this.level = level;
    }

    private void recheckPlayerLevel() {
        // level
    }

    public void setCurrentAttackPower(float currentAttackPower) {
        this.currentAttackPower = currentAttackPower;
    }

    @Override
    public String toString() {
        return "Player{"
                + "nickName='" + nickName + '\''
                + ", email='" + email + '\''
                + ", level=" + level
                + '}';
    }
}

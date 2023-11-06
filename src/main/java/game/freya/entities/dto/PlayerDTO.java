package game.freya.entities.dto;

import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iPlayer;
import game.freya.enums.HurtLevel;
import game.freya.items.containers.Backpack;
import game.freya.items.interfaces.iEntity;
import game.freya.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static game.freya.config.Constants.FFB;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PlayerDTO implements iPlayer {
    @NotNull
    private final String nickName;
    @NotNull
    private final String email;
    @NotNull
    private UUID uid;
    @Builder.Default
    private String avatarUrl = "/images/defaultAvatar.png";

    @Builder.Default
    private Backpack inventory = new Backpack("The ".concat(Constants.getUserConfig().getUserName()).concat("`s backpack"));

    @Builder.Default
    private short level = 1;

    @Builder.Default
    private float currentAttackPower = 1.0f;

    @Builder.Default
    private float experience = 0f;

    @Builder.Default
    private short health = 100;

    @Builder.Default
    private short maxHealth = 100;

    @Builder.Default
    private Point2D.Double position = new Point2D.Double(256d, 256d);

    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Builder.Default
    private List<Buff> buffs = new ArrayList<>(9);


    // custom fields:
    @Builder.Default
    private boolean isOnline = false;

    @Setter
    private BufferedImage avatar;


    // methods:
    public PlayerDTO(String nickName, String email, String avatarUrl) {
        this(UUID.randomUUID(), nickName, email, avatarUrl);
    }

    public PlayerDTO(UUID uid, String nickName, String email, String avatarUrl) {
        this.uid = uid;
        this.nickName = nickName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        try {
            this.avatar = this.avatarUrl == null ? null : ImageIO.read(new File(avatarUrl));
        } catch (IOException io) {
            log.error("Can`t read the player`s avatar by URL '{}'!", avatarUrl);
            this.avatar = null;
        }

        this.health = maxHealth;

        buffs = new ArrayList<>(9);
        position = new Point2D.Double(128d, 128d);
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
        if (this.health > maxHealth) {
            this.health = maxHealth;
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

    @Override
    public void draw(Graphics2D g2D) {
        g2D.setFont(Constants.GAME_FONT_01);
        Rectangle2D nickBounds = FFB.getStringBounds(g2D, getNickName());

        Shape playerShape = new Ellipse2D.Double((int) getPosition().x, (int) getPosition().y, Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);

        // draw shadow:
        g2D.setColor(new Color(0, 0, 0, 31));
        g2D.fillOval(playerShape.getBounds().x - 1, playerShape.getBounds().y + 1,
                playerShape.getBounds().width - 1, playerShape.getBounds().height + 1);
        // draw border:
        g2D.setColor(Color.ORANGE);
        g2D.draw(playerShape);
        // fill body:
        g2D.setColor(Color.GREEN);
        g2D.fillOval(playerShape.getBounds().x + 1, playerShape.getBounds().y + 1,
                playerShape.getBounds().width - 2, playerShape.getBounds().height - 2);
        // draw nickname:
        g2D.drawString(getNickName(),
                (int) (playerShape.getBounds2D().getCenterX() - nickBounds.getWidth() / 2d),
                (int) (playerShape.getBounds2D().getCenterY() - 15d));
    }

    private void recheckHurtLevel() {
        if (this.health <= 0) {
            this.hurtLevel = HurtLevel.DEAD;
            decreaseExp(getExperience() - getExperience() * 0.1f); // -10% exp by death
        } else if (this.health <= maxHealth * 0.3f) {
            this.hurtLevel = HurtLevel.HARD_HURT;
        } else if (this.health <= maxHealth * 0.6f) {
            this.hurtLevel = HurtLevel.MED_HURT;
        } else if (this.health <= maxHealth * 0.9f) {
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

    public void setOnline(boolean online) {
        log.info("The Player '{}' is {} now!", getNickName(), online ? "ON-LINE" : "OFF-LINE");
        isOnline = online;
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

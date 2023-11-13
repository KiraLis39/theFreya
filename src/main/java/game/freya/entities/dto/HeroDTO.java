package game.freya.entities.dto;

import game.freya.config.Constants;
import game.freya.entities.dto.interfaces.iHero;
import game.freya.enums.HurtLevel;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.items.containers.Backpack;
import game.freya.items.interfaces.iEntity;
import game.freya.items.logic.Buff;
import game.freya.utils.ExceptionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static game.freya.config.Constants.FFB;

@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HeroDTO implements iHero {
    @NotNull
    @Setter
    private UUID uid;

    @NotNull
    private String heroName;

    @Setter
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
    private short speed = 6;

    @Builder.Default
    private Point2D.Double position = new Point2D.Double(384d, 384d);

    @Builder.Default
    private HurtLevel hurtLevel = HurtLevel.HEALTHFUL;

    @Builder.Default
    private List<Buff> buffs = new ArrayList<>(9);

    private PlayerDTO ownedPlayer;

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
            log.warn("Can`t heal the dead corps of player {}!", getHeroName());
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
            log.warn("Player {} can`t attack itself!", getHeroName());
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        Shape playerShape = new Ellipse2D.Double(
                (int) getPosition().x - Constants.MAP_CELL_DIM / 2d,
                (int) getPosition().y - Constants.MAP_CELL_DIM / 2d,
                Constants.MAP_CELL_DIM, Constants.MAP_CELL_DIM);

        // draw shadow:
        g2D.setColor(new Color(0, 0, 0, 36));
        g2D.fillOval(playerShape.getBounds().x + 3, playerShape.getBounds().y + 6,
                playerShape.getBounds().width, playerShape.getBounds().height);

        // draw border:
        g2D.setStroke(new BasicStroke(2f));
        g2D.setColor(Color.ORANGE);
        g2D.draw(playerShape);

        // fill body:
        g2D.setColor(Color.GREEN);
        g2D.fillOval(playerShape.getBounds().x + 1, playerShape.getBounds().y + 1,
                playerShape.getBounds().width - 2, playerShape.getBounds().height - 2);

        // draw nickname:
        g2D.setColor(Color.BLACK);
        g2D.setFont(Constants.GAME_FONT_01);
        g2D.drawString(getHeroName(),
                (int) (playerShape.getBounds2D().getCenterX() - FFB.getStringBounds(g2D, getHeroName()).getWidth() / 2d),
                (int) (playerShape.getBounds2D().getCenterY() + FFB.getStringBounds(g2D, getHeroName()).getHeight() / 3d));
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
        try (InputStream avatarResource = getClass().getResourceAsStream("/images/defaultAvatar.png")) {
            if (avatarResource != null) {
                try {
                    return ImageIO.read(avatarResource);
                } catch (IOException ioe) {
                    log.error("Players avatar read exception: {}", ExceptionUtils.getFullExceptionMessage(ioe));
                }
            }
            return new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        } catch (IOException e) {
            throw new GlobalServiceException(ErrorMessages.RESOURCE_READ_ERROR, "/images/defaultAvatar.png");
        }
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

    @Override
    public PlayerDTO getOwnedPlayer() {
        return this.ownedPlayer;
    }

    private void recheckPlayerLevel() {
        // level
    }

    public void setCurrentAttackPower(float currentAttackPower) {
        this.currentAttackPower = currentAttackPower;
    }

    public void moveUp() {
        position.setLocation(position.x, position.y - 1);
    }

    public void moveDown() {
        position.setLocation(position.x, position.y + 1);
    }

    public void moveLeft() {
        position.setLocation(position.x - 1, position.y);
    }

    public void moveRight() {
        position.setLocation(position.x + 1, position.y);
    }

    @Override
    public String toString() {
        return "HeroDTO{"
                + "uid=" + uid
                + ", heroName='" + heroName + '\''
                + ", level=" + level
                + ", experience=" + experience
                + ", maxHealth=" + maxHealth
                + ", speed=" + speed
                + ", position=" + position
                + ", hurtLevel=" + hurtLevel
                + '}';
    }
}

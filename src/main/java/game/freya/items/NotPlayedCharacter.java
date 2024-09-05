package game.freya.items;

import game.freya.entities.logic.Buff;
import game.freya.enums.other.MovingVector;
import game.freya.items.prototypes.GameCharacter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("npc")
public class NotPlayedCharacter extends GameCharacter {
    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public int getOil() {
        return 0;
    }

    @Override
    public void heal(float healPoints) {

    }

    @Override
    public void hurt(float hurtPoints) {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public UUID getUid() {
        return null;
    }

    @Override
    public UUID getAuthor() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Point2D.Double getCenterPoint() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public String getCacheKey() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2D) {

    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return false;
    }

    @Override
    public void increaseExp(float increaseValue) {

    }

    @Override
    public void decreaseExp(float decreaseValue) {

    }

    @Override
    public void addBuff(Buff buff) {

    }

    @Override
    public void removeBuff(Buff buff) {

    }

    @Override
    public short getLevel() {
        return 0;
    }

    @Override
    public LocalDateTime getCreateDate() {
        return null;
    }

    @Override
    public void setVector(MovingVector movingVector) {

    }
}

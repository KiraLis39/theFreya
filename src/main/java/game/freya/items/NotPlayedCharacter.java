package game.freya.items;

import game.freya.entities.logic.Buff;
import game.freya.items.prototypes.GameCharacter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.UUID;

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
    public UUID getCreator() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Point2D.Double getLocation() {
        return null;
    }

    @Override
    public Dimension getSize() {
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
    public String getImageNameInCache() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2D) {

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
}

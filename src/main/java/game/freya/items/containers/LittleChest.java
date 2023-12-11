package game.freya.items.containers;

import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iStorage;
import game.freya.items.prototypes.GameCharacter;
import game.freya.items.prototypes.Storage;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.UUID;

public class LittleChest extends Storage {
    public LittleChest(String name) {
        super(name, UUID.randomUUID());
    }

    @Override
    public UUID getUid() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public UUID getCreator() {
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
    public BufferedImage getImage() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2D) {

    }

    @Override
    public void drop() {

    }

    @Override
    public void store(iStorage storage) {

    }

    @Override
    public CurrencyVault getCurrencyType() {
        return null;
    }

    @Override
    public int getDefaultByeCost() {
        return 0;
    }

    @Override
    public int getCurrentByeCost() {
        return 0;
    }

    @Override
    public int getDefaultSellCost() {
        return 0;
    }

    @Override
    public int getCurrentSellCost() {
        return 0;
    }

    @Override
    public void setCurrentSellCost(int cost) {

    }

    @Override
    public void trade(GameCharacter owner, GameCharacter buyer, CurrencyVault vault, int value) {

    }
}

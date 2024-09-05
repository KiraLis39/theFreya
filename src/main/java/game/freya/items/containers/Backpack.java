package game.freya.items.containers;

import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iStorage;
import game.freya.items.prototypes.GameCharacter;
import game.freya.items.prototypes.Storage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.awt.*;

@SuperBuilder
@NoArgsConstructor
@Entity
@DiscriminatorValue("backpack")
public class Backpack extends Storage {
    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public void draw(Graphics2D g2D) {

    }

    @Override
    public boolean isInSector(Rectangle sector) {
        return false;
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

package game.freya.interfaces.impl;

import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.ItemDto;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iTradeable;
import game.freya.mappers.ItemsMapper;
import game.freya.services.ItemsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class TradeableImpl implements iTradeable {
    private final ItemsService itemsService;
    private final ItemsMapper itemsMapper;


    @Override
    public UUID getUid() {
        return null;
    }

    @Override
    public String getName() {
        return null;
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
    public boolean trade(CharacterDto seller, ItemDto item, CharacterDto buyer, CurrencyVault vaultType, int paySum) {
        UUID itemToSell = seller.getInventory().removeItem(item.getUid(), 1);
        if (itemToSell == null) {
            return false;
        }

        if (buyer.getInventory().tryDecreaseBalance(paySum, vaultType)) {
            // успешная торговля:
            seller.getInventory().increaseBalance(paySum, vaultType);
            buyer.getInventory().putItem(itemsMapper.toDto(itemsService.findByUid(itemToSell).get()), 1);
            return true;
        } else {
            // провал торговли:
            seller.getInventory().putItem(itemsMapper.toDto(itemsService.findByUid(itemToSell).get()), 1);
            return false;
        }
    }

    @Override
    public int compareTo(@NotNull iTradeable o) {
        // сортировка по имени:
        return o.getName().compareTo(this.getName());
    }
}

package game.freya.dto;

import game.freya.dto.roots.CharacterDto;
import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.StorageDto;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iBackpack;
import game.freya.mappers.ItemsMapper;
import game.freya.services.ItemsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
@Component
@SuperBuilder
@RequiredArgsConstructor
public class BackpackDto extends StorageDto implements iBackpack {
    private ItemsService itemsService;
    private ItemsMapper itemsMapper;

    private final Map<CurrencyVault, Integer> balance = new HashMap<>(3);

    @Autowired
    public void init(@Lazy ItemsService itemsService, @Lazy ItemsMapper itemsMapper) {
        this.itemsService = itemsService;
        this.itemsMapper = itemsMapper;
    }

    @Override
    public int increaseBalanceOf(int paySum, CurrencyVault vaultType) {
        balance.put(vaultType, balance.get(vaultType) + paySum);
        return balance.get(vaultType);
    }

    @Override
    public boolean tryDecreaseBalanceOf(int paySum, CurrencyVault vaultType) {
        try {
            if (balance.get(vaultType) < paySum) {
                return false;
            }

            balance.put(vaultType, balance.get(vaultType) + paySum);
        } catch (Exception e) {
            log.error("Не удалось пополнить баланс {} на сумму {}: {}", vaultType, paySum, e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public int getBalanceOf(CurrencyVault vaultType) {
        return 0;
    }

    @Override
    public boolean trade(CharacterDto seller, ItemDto item, CharacterDto buyer, CurrencyVault vaultType, int paySum) {
        UUID itemToSell = seller.getInventory().removeItem(item.getUid(), 1);
        if (itemToSell == null) {
            return false;
        }

        if (buyer.getInventory().tryDecreaseBalanceOf(paySum, vaultType)) {
            // успешная торговля:
            seller.getInventory().increaseBalanceOf(paySum, vaultType);
            buyer.getInventory().putItem(itemsMapper.toDto(itemsService.findByUid(itemToSell).get()), 1);
            return true;
        } else {
            // провал торговли:
            seller.getInventory().putItem(itemsMapper.toDto(itemsService.findByUid(itemToSell).get()), 1);
            return false;
        }
    }
}

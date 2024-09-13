package game.freya.dto.roots;

import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iTradeable;
import game.freya.mappers.ItemsMapper;
import game.freya.services.ItemsService;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Getter
@Component
@SuperBuilder
@RequiredArgsConstructor
public sealed abstract class TradeableImpl extends AbstractEntityDto implements iTradeable permits ItemDto {
    private final ItemsService itemsService;
    private final ItemsMapper itemsMapper;

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

package game.freya.dto;

import game.freya.dto.roots.StorageDto;
import game.freya.enums.other.CurrencyVault;
import game.freya.interfaces.iBackpack;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@SuperBuilder
public class BackpackDto extends StorageDto implements iBackpack {

    private final Map<CurrencyVault, Integer> balance = new HashMap<>(3);

    @Override
    public void increaseBalance(int paySum, CurrencyVault vaultType) {
        balance.put(vaultType, balance.get(vaultType) + paySum);
    }

    @Override
    public boolean tryDecreaseBalance(int paySum, CurrencyVault vaultType) {
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
}

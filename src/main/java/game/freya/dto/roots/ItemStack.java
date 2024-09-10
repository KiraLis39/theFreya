package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

@Setter
@Slf4j
@Accessors(chain = true, fluent = true)
public class ItemStack {
    @Getter
    private UUID itemUid;

    @Getter
    private String itemName;

    @Transient
    @JsonIgnore
    private ItemDto itemDto;

    @Getter
    private int count = 0;

    public ItemDto getItemDto() {
        if (itemDto == null) {
            if (itemUid != null) {
//                Optional<Item> itemOpt = itemsService.findByUid(itemUid);
//                if (itemOpt.isEmpty()) {
//                    throw new GlobalServiceException(ErrorMessages.ITEM_NOT_FOUND, itemUid.toString());
//                }
//                itemDto = itemsMapper.toDto(itemOpt.get());
//                itemName = itemDto.getName();
            } else {
                throw new GlobalServiceException(ErrorMessages.NOT_ENOUGH_DATA, "itemUid");
            }
        }
        return itemDto;
    }

    public int increaseCount(int increaseOn) {
        if (itemDto == null) {
            getItemDto();
        }

        if (count + increaseOn <= itemDto.getStackCount()) {
            return count += increaseOn;
        }
        return -1;
    }

    public int decreaseCount() {
        return this.decreaseCount(1);
    }

    public int decreaseCount(int decreaseOnCount) {
        if (itemDto == null) {
            getItemDto();
        }

        if (count - decreaseOnCount >= 0) {
            return count -= decreaseOnCount;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemStack stack)) {
            return false;
        }
        return Objects.equals(itemUid, stack.itemUid) && Objects.equals(itemName, stack.itemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemUid, itemName);
    }
}

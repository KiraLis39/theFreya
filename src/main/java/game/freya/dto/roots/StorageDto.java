package game.freya.dto.roots;

import com.fasterxml.jackson.annotation.JsonIgnore;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import game.freya.interfaces.subroot.iStorage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
//@Accessors(chain = true, fluent = true, prefix = {"+set"})
@RequiredArgsConstructor
public abstract class StorageDto extends AbstractEntityDto implements iStorage {

    @Schema(description = "The capacity of container", requiredMode = Schema.RequiredMode.REQUIRED)
    private short capacity;

    @Builder.Default
    @Schema(description = "Container`s content items array", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ItemStack> stacks = new ArrayList<>();

    @Override
    @JsonIgnore
    public void draw(Graphics2D g2D) {

    }

    public boolean putItem(ItemDto itemDto) {
        return this.putItem(itemDto, 1);
    }

    @Override
    @JsonIgnore
    public boolean putItem(ItemDto itemDto, int count) {
        // находим стак искомого предмета в инвентаре (где есть свободное место для нового предмета):
        Optional<ItemStack> availableStack = stacks.stream().filter(stack -> stack.itemUid().equals(itemDto.getUid())
                && stack.count() < stack.getItemDto().getStackCount()).findFirst();
        if (availableStack.isPresent()) {
            // если найден неполный стак этого предмета - дополняем его:
            return addItemToExistsStack(availableStack.get(), count);
        } else if (stacks.size() < capacity) {
            // если в хранилище ещё нет стаков этого предмета, но есть свободные ячейки:
            return addNewItemsStack(itemDto, count);
        } else {
            log.warn("Не был добавлен в инвентарь предмет '{}' ({}): Нет свободного места.", itemDto.getName(), itemDto.getUid());
            return false;
        }
    }

    private boolean addNewItemsStack(ItemDto itemDto, int count) {
        ItemStack stack = new ItemStack()
                .itemUid(itemDto.getUid())
                .itemDto(itemDto)
                .itemName(itemDto.getName())
                .count(count);

        if (itemDto.getStackCount() >= count) {
            stacks.add(stack);
        } else {
            int wantsCellCount = Math.round((float) count / itemDto.getStackCount() + 0.5f);
            if (capacity - stacks.size() >= wantsCellCount) {
                for (int i = 0; i < wantsCellCount; i++) {
                    stack = new ItemStack()
                            .itemUid(itemDto.getUid())
                            .itemDto(itemDto)
                            .itemName(itemDto.getName())
                            .count(Math.min(count, itemDto.getStackCount()));
                    stacks.add(stack);
                    count -= itemDto.getStackCount();
                    if (count < 0) {
                        throw new GlobalServiceException(ErrorMessages.UNIVERSAL_ERROR_MESSAGE_TEMPLATE,
                                "Неверные расчеты: count не может становиться меньше нуля.");
                    }
                }
            } else {
                log.warn("Не был добавлен в инвентарь предмет '{}' ({}): Нет свободного места.", itemDto.getName(), itemDto.getUid());
                return false;
            }
        }
        log.info("Добавлен в хранилище '{} ({})' предмет '{} ({})'", getName(), getUid(), stack.itemName(), stack.itemUid());
        return true;
    }

    private boolean addItemToExistsStack(ItemStack itemStack, int count) {
        if (itemStack.increaseCount(count) == -1) {
            log.error("Не был добавлен в инвентарь предмет '{}': Получено -1.", itemStack.itemUid());
            return false;
        }
        log.info("Добавлен в инвентарь предмет '{} ({})'", itemStack.itemName(), itemStack.itemUid());
        return true;
    }

    @Override
    @JsonIgnore
    public UUID removeItem(UUID itemUid, int count) {
        List<ItemStack> foundItemStacks = stacks.stream().filter(stack -> stack.itemUid().equals(itemUid)).toList();
        if (foundItemStacks.isEmpty()) {
            log.error("Невозможно достать из хранилища отсутствующий предмет '{}'", itemUid);
            return null;
        }

        int itemsCount = foundItemStacks.stream().mapToInt(ItemStack::count).sum();
        if (itemsCount < count) {
            log.error("Невозможно достать из хранилища больше предметов '{}' чем имеется.", itemUid);
            return null;
        }

        for (ItemStack stack : foundItemStacks) {
            if (stack.count() <= count) {
                count -= stack.count();
                stacks.remove(stack);
            } else {
                stack.decreaseCount(count);
//                or better? stacks.stream().filter(stk -> stk.equals(stack)).findFirst().get().decreaseCount(count);
            }

            if (stack.count() == 0) {
                log.error("del");
            }
        }
        return itemUid;
    }

    @Override
    @JsonIgnore
    public boolean translate(StorageDto dst, ItemDto itemDto, int count) {
        UUID srcRemovedItem = removeItem(itemDto.getUid(), count);
        if (srcRemovedItem == null) {
            return false;
        }
        dst.putItem(itemDto, count);
        return true;
    }

    @Override
    @JsonIgnore
    public boolean has(UUID itemUid) {
        return stacks.stream().anyMatch(stack -> stack.itemUid().equals(itemUid));
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    @JsonIgnore
    public boolean isFull() {
        return stacks.size() >= capacity;
    }

    @Override
    @JsonIgnore
    public void removeAll() {
        stacks.clear();
    }

    /**
     * Возвращает количестко указанных предметов в хранилище.
     *
     * @param itemsUid uid предмета, количество которого требуется получить.
     * @return количество этих предметов в хранилище.
     */
    @Override
    public int getItemsHaveCount(UUID itemsUid) {
        return stacks.stream()
                .filter(stack -> stack.itemUid().equals(itemsUid))
                .mapToInt(ItemStack::count)
                .sum();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return Objects.hash(getName(), getUid());
    }

    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StorageDto storage = (StorageDto) o;
        return Objects.equals(getName(), storage.getName()) && Objects.equals(getUid(), storage.getUid());
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public void onDestroy() {

    }
}

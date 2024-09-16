package game.freya.mappers;

import game.freya.dto.FoodDto;
import game.freya.dto.WeaponDto;
import game.freya.dto.roots.ItemDto;
import game.freya.entities.Food;
import game.freya.entities.Weapon;
import game.freya.entities.roots.prototypes.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ItemsMapper {
    public Item toEntity(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto) {
            case WeaponDto w -> weaponDtoToEntity(w);
            case FoodDto f -> foodDtoToEntity(f);
            default -> throw new IllegalStateException("Unexpected value: " + dto);
        };
    }

    public ItemDto toDto(Item entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case Weapon w -> weaponToDto(w);
            case Food f -> foodToDto(f);
            default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    private WeaponDto weaponToDto(Weapon entity) {
        return WeaponDto.builder()
                .uid(entity.getUid())
                .ownerUid(entity.getOwnerUid())
                .createdBy(entity.getCreatedBy())
                .worldUid(entity.getWorldUid())
                .name(entity.getName())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .location(entity.getLocation())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .stackCount(entity.getStackCount())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .durability(entity.getDurability())
                .build();
    }

    private Weapon weaponDtoToEntity(WeaponDto dto) {
        return Weapon.builder()
                .uid(dto.getUid())
                .ownerUid(dto.getOwnerUid())
                .createdBy(dto.getCreatedBy())
                .worldUid(dto.getWorldUid())
                .name(dto.getName())
                .size(dto.getSize())
                .collider(dto.getCollider())
                .location(dto.getLocation())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .stackCount(dto.getStackCount())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .durability(dto.getDurability())
                .build();
    }

    private FoodDto foodToDto(Food entity) {
        return FoodDto.builder()
                .uid(entity.getUid())
                .ownerUid(entity.getOwnerUid())
                .createdBy(entity.getCreatedBy())
                .worldUid(entity.getWorldUid())
                .name(entity.getName())
                .size(entity.getSize())
                .collider(entity.getCollider())
                .location(entity.getLocation())
                .isVisible(entity.isVisible())
                .cacheKey(entity.getCacheKey())
                .stackCount(entity.getStackCount())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .durability(entity.getDurability())
                .isPoisoned(entity.isPoisoned())
                .healthCompensation(entity.getHealthCompensation())
                .oilCompensation(entity.getOilCompensation())
                .build();
    }

    private Food foodDtoToEntity(FoodDto dto) {
        return Food.builder()
                .uid(dto.getUid())
                .ownerUid(dto.getOwnerUid())
                .createdBy(dto.getCreatedBy())
                .worldUid(dto.getWorldUid())
                .name(dto.getName())
                .size(dto.getSize())
                .collider(dto.getCollider())
                .location(dto.getLocation())
                .isVisible(dto.isVisible())
                .cacheKey(dto.getCacheKey())
                .stackCount(dto.getStackCount())
                .createdDate(dto.getCreatedDate())
                .modifyDate(dto.getModifyDate())
                .build();
    }

    public Set<Item> toEntities(Set<ItemDto> heroes) {
        return heroes.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    public Set<ItemDto> toDtos(Set<Item> heroes) {
        return heroes.stream().map(this::toDto).collect(Collectors.toSet());
    }
}

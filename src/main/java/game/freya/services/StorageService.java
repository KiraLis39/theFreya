package game.freya.services;

import game.freya.dto.roots.ItemDto;
import game.freya.dto.roots.StorageDto;
import game.freya.entities.roots.Item;
import game.freya.entities.roots.Storage;
import game.freya.mappers.StorageMapper;
import game.freya.repositories.ItemRepository;
import game.freya.repositories.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemRepository itemRepository;
    private final StorageMapper storageMapper;

    public Optional<StorageDto> createStorage(StorageDto storageDto) {
        List<ItemDto> storageItems = storageDto.getItems();
        storageDto.setItems(null);

        Storage savedWithoutItems = storageRepository.save(storageMapper.toEntity(storageDto));
        List<ItemDto> foundDtos = storageItems.stream().filter(d -> itemRepository.existsById(d.getUid())).toList();
        savedWithoutItems.setItems(itemRepository.findAllById(foundDtos.stream().map(ItemDto::getUid).collect(Collectors.toSet())));

        StorageDto result = storageMapper.toDto(storageRepository.saveAndFlush(savedWithoutItems));
        return Optional.of(result);
    }

    public ResponseEntity<HttpStatus> storeTo(UUID storageUid, UUID storedItemUid) {
        Optional<Storage> aimStorage = storageRepository.findById(storageUid);
        Optional<Item> storedItem = itemRepository.findById(storedItemUid);
        if (aimStorage.isEmpty() || storedItem.isEmpty()) {
            log.error("Не был обнаружен предмет {} или целевое хранилище {}", storedItemUid, storageUid);
            return ResponseEntity.notFound().build();
        }
        Storage s = aimStorage.get();
        s.addItem(storedItem.get());
        storageRepository.saveAndFlush(s);
        return ResponseEntity.ok().build();
    }
}

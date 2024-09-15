package game.freya.services;

import game.freya.entities.roots.prototypes.Item;
import game.freya.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemsService {
    private final ItemRepository itemRepository;

    public Optional<Item> createItem(Item item) {
        return Optional.of(itemRepository.save(item));
    }

    public Optional<Item> findByUid(UUID itemUid) {
        return itemRepository.findById(itemUid);
    }

    public ResponseEntity<HttpStatus> deleteItemByUid(UUID itemUid) {
        if (itemRepository.existsById(itemUid)) {
            itemRepository.deleteById(itemUid);
            log.info("Удалён предмет {}", itemUid);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

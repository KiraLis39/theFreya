package game.freya.services;

import game.freya.entities.roots.Item;
import game.freya.repositories.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
}

package game.freya.entities.roots;

import game.freya.entities.AbstractEntity;
import game.freya.interfaces.iStorable;
import game.freya.interfaces.iStorage;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "storage_type")
@Table(name = "storages")
public class Storage extends AbstractEntity implements iStorage {
    private short capacity;
    private final List<iStorable> items = new ArrayList<>(capacity);

    @Override
    public short capacity() {
        return capacity;
    }

    @Override
    public void put(iStorable storable) {
        if (items.size() < capacity) {
            items.add(storable);
        }
    }

    @Override
    public iStorable remove(UUID storableUid) {
        Optional<iStorable> uid = items.stream().filter(iStorable -> iStorable.getUid().equals(storableUid)).findFirst();
        return uid.map(iStorable -> items.remove(items.indexOf(iStorable))).orElse(null);
    }

    @Override
    public void translate(Storage dst, UUID storableUid) {
        Optional<iStorable> uid = items.stream().filter(iStorable -> iStorable.getUid().equals(storableUid)).findFirst();
        uid.ifPresent(iStorable -> dst.put(items.remove(items.indexOf(iStorable))));
    }

    @Override
    public boolean has(UUID storableUid) {
        return items.stream().anyMatch(iStorable -> iStorable.getUid().equals(storableUid));
    }

    @Override
    public boolean has(String storableName) {
        return items.stream().anyMatch(iStorable -> iStorable.getName().equals(storableName));
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean isFull() {
        return items.size() >= capacity;
    }

    @Override
    public void removeAll() {
        items.clear();
    }
}

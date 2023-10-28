package game.freya.items.prototypes;

import game.freya.items.interfaces.iStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
public abstract class Storage implements iStorage {
    @Getter
    private final UUID suid;
    private final short size = 16;
    private final Set<Storable> content = HashSet.newHashSet(size);
    @Getter
    @Setter
    private String name;

    protected Storage(UUID suid) {
        this.suid = suid;
    }

    protected Storage(UUID suid, String name) {
        this.suid = suid;
        this.name = name;
    }

    @Override
    public short size() {
        return this.size;
    }

    @Override
    public void put(Storable storable) {
        content.add(storable);
    }

    @Override
    public Storable get(Storable storable) {
        if (has(storable)) {
            for (Storable srbl : content) {
                if (srbl.getSuid().equals(storable.getSuid())) {
                    try {
                        content.remove(storable);
                        return srbl;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void translate(Storage aim, Storable storable) {
        aim.put(this.get(storable));
    }

    @Override
    public boolean has(Storable storable) {
        return content.contains(storable);
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public Set<Storable> clear() {
        Set<Storable> result = new HashSet<>(content);
        content.clear();
        return result;
    }

    @Override
    public String toString() {
        return "Storage{"
                + "suid=" + suid
                + ", size=" + size
                + ", name='" + name + '\''
                + ", content=" + content
                + '}';
    }
}

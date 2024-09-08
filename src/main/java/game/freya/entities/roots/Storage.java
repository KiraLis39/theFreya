package game.freya.entities.roots;

import game.freya.entities.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Контейнер для хранения предметов
 */
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "storage_type")
@Table(name = "storages")
public class Storage extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "uid", nullable = false)
    private UUID uid;

    @Column(name = "capacity")
    private short capacity;

    @NotNull
    @Size(min = 4, max = 16)
    @Builder.Default
    @OneToMany(mappedBy = "storage", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    @Transient
    public void addItem(Item item) {
        item.setStorage(this);
        this.items.add(item);
    }
}

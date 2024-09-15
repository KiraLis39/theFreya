package game.freya.entities.roots.prototypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Контейнер для хранения предметов
 */
@Getter
@Setter
@SuperBuilder
@Accessors(chain = true)
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "storage_type")
@Table(name = "storages")
public class Storage extends AbstractEntity {
    @Column(name = "capacity")
    private short capacity;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "storages_items", joinColumns = @JoinColumn(name = "storage_uid"), inverseJoinColumns = @JoinColumn(name = "item_uid"))
    private Set<Item> items = new HashSet<>();
}

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Accessors(chain = true)
@Setter
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Entity
@DiscriminatorColumn(name = "item_type")
@Table(name = "items")
public abstract class Item extends AbstractEntity {

    @Min(1)
    @Max(99)
    @Column(name = "stack_count")
    private int stackCount;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "storages_items", joinColumns = @JoinColumn(name = "item_uid"), inverseJoinColumns = @JoinColumn(name = "storage_uid"))
    private Set<Storage> storages;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.LAZY)
    @JoinTable(name = "items_buffs", joinColumns = @JoinColumn(name = "item_uid"), inverseJoinColumns = @JoinColumn(name = "buff_uid"))
    private Set<Buff> buffs;
}

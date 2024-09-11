package game.freya.entities.middles;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@RequiredArgsConstructor
@Entity
@Table(name = "storages_items")
public class StorageToItems {
    @EmbeddedId
    private StorageToItemsPK id;

    @Column(name = "storage_name")
    private String storageName;

    @NotNull
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Min(1)
    @Max(99)
    @NotNull
    @Column(name = "have_count", nullable = false)
    private Integer haveCount = 1;
}

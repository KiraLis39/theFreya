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

    @Column(name = "item_name")
    private String itemName;

    @Min(1)
    @Max(99)
    @Column(name = "have_count")
    private int haveCount = 1;
}

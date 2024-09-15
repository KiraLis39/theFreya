package game.freya.entities.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageToItems that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

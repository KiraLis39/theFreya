package game.freya.entities.middles;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class StorageToItemsPK implements Serializable {
    @Column(name = "storage_uid")
    private UUID storageUid;

    @Column(name = "item_uid")
    private UUID itemUid;
}

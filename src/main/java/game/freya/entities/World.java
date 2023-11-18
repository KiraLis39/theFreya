package game.freya.entities;

import game.freya.enums.HardnessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worlds")
public class World {
    @Id
    @Builder.Default
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private UUID uid = UUID.randomUUID();

    @Column(name = "author")
    private UUID author;

    @Column(name = "title", length = 64, unique = true, nullable = false)
    private String title;

    @Builder.Default
    @Column(name = "is_net_available")
    private boolean isNetAvailable = false;

    @Column(name = "password_hash")
    private int passwordHash;

    @Builder.Default
    @Column(name = "dimension_w")
    private int dimensionWidth = 64; // 64 = 2048 cells | 128 = 4096 cells

    @Builder.Default
    @Column(name = "dimension_h")
    private int dimensionHeight = 32; // 32 = 1024 cells | 128 = 4096 cells

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HardnessLevel level = HardnessLevel.EASY;

    @Builder.Default
    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<Hero> heroes = HashSet.newHashSet(3);

    @Builder.Default
    @CreatedDate
    @CreationTimestamp
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();
}

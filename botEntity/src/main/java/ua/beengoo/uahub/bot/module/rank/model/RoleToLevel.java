package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "r2l_links")
public class RoleToLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "role_id")
    private long roleId;

    @Column(name = "level_required")
    private long levelRequired;

    @ManyToOne
    @JoinColumn(name = "rank_settings_id", nullable = false)
    private RankSettings rankSettings;
}

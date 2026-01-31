package ua.beengoo.uahub.bot.module.rank.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** Server-level configuration for ranking behavior. */
@Entity
@Data
@Table(name = "rank_settings")
public class RankSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "server_id", unique = true, nullable = false)
    private long serverId;

    @Column(name = "chat_points_amount")
    private double chatPointsAmount = 1;

    @OneToMany(mappedBy = "rankSettings", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RoleToLevel> roleLevelMappings = new ArrayList<>();
}

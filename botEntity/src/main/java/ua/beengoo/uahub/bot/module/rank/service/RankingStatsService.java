package ua.beengoo.uahub.bot.module.rank.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.module.rank.model.*;

/** Service for resolving best multipliers by channel or role. */
@Service
public class RankingStatsService {

    private final ChannelRankRepo channelRankRepo;
    private final RoleRankRepo roleRankRepo;


    public RankingStatsService(ChannelRankRepo channelRankRepo, RoleRankRepo roleRankRepo) {
        this.channelRankRepo = channelRankRepo;
        this.roleRankRepo = roleRankRepo;
    }

    /** Returns channel rank stats or creates a default record. */
    public ChannelRankStats getChannelOrCreate(long channel_id) {
        if (channelRankRepo.existsByChannelId(channel_id)) {
            return channelRankRepo.findByChannelId(channel_id).orElse(null);
        } else {
            ChannelRankStats channelRankStats = new ChannelRankStats();
            channelRankStats.setChannelId(channel_id);
            return channelRankRepo.save(channelRankStats);
        }
    }

    /** Returns role rank stats or creates a default record. */
    public RoleRankStats getRoleOrCreate(long role_id) {
        if (roleRankRepo.existsByRoleId(role_id)) {
            return roleRankRepo.findByRoleId(role_id).orElse(null);
        } else {
            RoleRankStats roleRankStats = new RoleRankStats();
            roleRankStats.setRoleId(role_id);
            return roleRankRepo.save(roleRankStats);
        }
    }

    /** Resolves the channel multiplier using the channel or its category fallback. */
    public double getBestChannelMultiplier(StandardGuildChannel channel) {
        if (channel.getParentCategory() != null) {
            return getChannelOrCreate(channel.getIdLong()).getChannelMultiplier();
        } else {
            return getChannelOrCreate(channel.getParentCategoryIdLong()).getChannelMultiplier();
        }
    }

    /** Picks the role with the strongest absolute multiplier (positive or negative). */
    public double getBestRoleMultiplier(Member member) {
        List<Role> userRoles = new ArrayList<>(member.getRoles());

        userRoles.sort(
                (r1, r2) -> {
                    double multiplierR1 = getRoleOrCreate(r1.getIdLong()).getRoleMultiplier();
                    double multiplierR2 = getRoleOrCreate(r2.getIdLong()).getRoleMultiplier();
                    return Double.compare(multiplierR1, multiplierR2);
                });
        userRoles.reversed();

        try {
            double biggestRole = getRoleOrCreate(userRoles.getFirst().getIdLong()).getRoleMultiplier();
            double lowestRole = getRoleOrCreate(userRoles.getLast().getIdLong()).getRoleMultiplier();
            return Math.abs(biggestRole) > Math.abs(lowestRole) ? biggestRole : lowestRole;
        } catch (NoSuchElementException e) {
            return getRoleOrCreate(member.getGuild().getPublicRole().getIdLong()).getRoleMultiplier();
        }
    }
}

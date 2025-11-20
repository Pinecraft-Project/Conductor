package ua.beengoo.uahub.bot.module.rank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.beengoo.uahub.bot.module.rank.model.ChannelRankRepo;
import ua.beengoo.uahub.bot.module.rank.model.ChannelRankStats;
import ua.beengoo.uahub.bot.module.rank.model.RoleRankRepo;
import ua.beengoo.uahub.bot.module.rank.model.RoleRankStats;

@ExtendWith(MockitoExtension.class)
class RankingStatsServiceTest {

  @Mock private ChannelRankRepo channelRepo;
  @Mock private RoleRankRepo roleRepo;
  @InjectMocks private RankingStatsService svc;

  @Test
  @DisplayName("getChannelOrCreate: returns existing when present else creates")
  void channelOrCreate() {
    when(channelRepo.existsByChannelId(1L)).thenReturn(true);
    ChannelRankStats existing = new ChannelRankStats();
    existing.setChannelId(1L);
    existing.setChannelMultiplier(2.5);
    when(channelRepo.findByChannelId(1L)).thenReturn(Optional.of(existing));
    assertSame(existing, svc.getChannelOrCreate(1L));

    when(channelRepo.existsByChannelId(2L)).thenReturn(false);
    when(channelRepo.save(any(ChannelRankStats.class))).thenAnswer(inv -> inv.getArgument(0));
    ChannelRankStats created = svc.getChannelOrCreate(2L);
    assertEquals(2L, created.getChannelId());
  }

  @Test
  @DisplayName("getRoleOrCreate: returns existing when present else creates")
  void roleOrCreate() {
    RoleRankStats existing = new RoleRankStats();
    existing.setRoleId(10L);
    existing.setRoleMultiplier(3.0);
    when(roleRepo.existsByRoleId(10L)).thenReturn(true);
    when(roleRepo.findByRoleId(10L)).thenReturn(Optional.of(existing));
    assertSame(existing, svc.getRoleOrCreate(10L));

    when(roleRepo.existsByRoleId(11L)).thenReturn(false);
    when(roleRepo.save(any(RoleRankStats.class))).thenAnswer(inv -> inv.getArgument(0));
    RoleRankStats created = svc.getRoleOrCreate(11L);
    assertEquals(11L, created.getRoleId());
  }

  @Test
  @DisplayName(
      "getBestChannelMultiplier uses channel id when parent present, else parent category id")
  void bestChannelMultiplier() {
    StandardGuildChannel ch = mock(StandardGuildChannel.class);
    when(ch.getParentCategory()).thenReturn(mock(Category.class));
    when(ch.getIdLong()).thenReturn(100L);
    ChannelRankStats s = new ChannelRankStats();
    s.setChannelId(100L);
    s.setChannelMultiplier(1.5);
    when(channelRepo.existsByChannelId(100L)).thenReturn(true);
    when(channelRepo.findByChannelId(100L)).thenReturn(Optional.of(s));
    assertEquals(1.5, svc.getBestChannelMultiplier(ch));

    StandardGuildChannel ch2 = mock(StandardGuildChannel.class);
    when(ch2.getParentCategory()).thenReturn(null);
    when(ch2.getParentCategoryIdLong()).thenReturn(200L);
    ChannelRankStats p = new ChannelRankStats();
    p.setChannelId(200L);
    p.setChannelMultiplier(2.0);
    when(channelRepo.existsByChannelId(200L)).thenReturn(true);
    when(channelRepo.findByChannelId(200L)).thenReturn(Optional.of(p));
    assertEquals(2.0, svc.getBestChannelMultiplier(ch2));
  }

  @Test
  @DisplayName("getBestRoleMultiplier picks strongest absolute multiplier or public role when none")
  void bestRoleMultiplier() {
    Role r1 = mock(Role.class);
    when(r1.getIdLong()).thenReturn(1L);
    Role r2 = mock(Role.class);
    when(r2.getIdLong()).thenReturn(2L);
    Member m = mock(Member.class);
    when(m.getRoles()).thenReturn(List.of(r1, r2));
    RoleRankStats rr1 = new RoleRankStats();
    rr1.setRoleId(1L);
    rr1.setRoleMultiplier(-3.0);
    RoleRankStats rr2 = new RoleRankStats();
    rr2.setRoleId(2L);
    rr2.setRoleMultiplier(2.0);
    when(roleRepo.existsByRoleId(1L)).thenReturn(true);
    when(roleRepo.findByRoleId(1L)).thenReturn(Optional.of(rr1));
    when(roleRepo.existsByRoleId(2L)).thenReturn(true);
    when(roleRepo.findByRoleId(2L)).thenReturn(Optional.of(rr2));
    assertEquals(-3.0, svc.getBestRoleMultiplier(m));

    // No roles -> fallback to public role
    Guild g = mock(Guild.class);
    Role pub = mock(Role.class);
    when(pub.getIdLong()).thenReturn(999L);
    when(g.getPublicRole()).thenReturn(pub);
    Member m2 = mock(Member.class);
    when(m2.getRoles()).thenReturn(List.of());
    when(m2.getGuild()).thenReturn(g);
    RoleRankStats pubStats = new RoleRankStats();
    pubStats.setRoleId(999L);
    pubStats.setRoleMultiplier(0.0);
    when(roleRepo.existsByRoleId(999L)).thenReturn(true);
    when(roleRepo.findByRoleId(999L)).thenReturn(Optional.of(pubStats));
    assertEquals(0.0, svc.getBestRoleMultiplier(m2));
  }
}

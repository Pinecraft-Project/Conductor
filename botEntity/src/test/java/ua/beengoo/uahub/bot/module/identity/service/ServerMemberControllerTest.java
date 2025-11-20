package ua.beengoo.uahub.bot.module.identity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.beengoo.uahub.bot.module.identity.exceptions.MemberExistsException;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.model.ServerMemberRepo;

@ExtendWith(MockitoExtension.class)
class ServerMemberControllerTest {

  @Mock private ServerMemberRepo repo;

  @InjectMocks private ServerMemberController ctrl;

  @Test
  @DisplayName("addNewMember: creates new member with initial stats and permissions")
  void addNewMember_Creates() {
    long uid = 42L;
    when(repo.existsByDiscordId(uid)).thenReturn(false);
    when(repo.save(any(ServerMember.class))).thenAnswer(inv -> inv.getArgument(0));
    ServerMember sm = ctrl.addNewMember(uid);
    assertEquals(uid, sm.getDiscordId());
    assertNotNull(sm.getRankStats());
    assertEquals(1, sm.getRankStats().getMemberMultiplier());
    assertSame(sm, sm.getRankStats().getServerMember());
    assertNotNull(sm.getMemberPermissions());
    assertSame(sm, sm.getMemberPermissions().getServerMember());
  }

  @Test
  @DisplayName("addNewMember: throws when member exists")
  void addNewMember_ThrowsIfExists() {
    when(repo.existsByDiscordId(1L)).thenReturn(true);
    assertThrows(MemberExistsException.class, () -> ctrl.addNewMember(1L));
  }

  @Test
  @DisplayName("addMemberOrNothing: creates when missing else returns existing")
  void addMemberOrNothing_Behavior() {
    long uid = 100L;
    // First call: create
    when(repo.existsByDiscordId(uid)).thenReturn(false);
    when(repo.save(any(ServerMember.class))).thenAnswer(inv -> inv.getArgument(0));
    ServerMember created = ctrl.addMemberOrNothing(uid);
    assertEquals(uid, created.getDiscordId());

    // Second call: find existing
    when(repo.existsByDiscordId(uid)).thenReturn(true);
    when(repo.findByDiscordId(uid)).thenReturn(Optional.of(created));
    ServerMember found = ctrl.addMemberOrNothing(uid);
    assertSame(created, found);
  }

  @Test
  @DisplayName("getServerMemberByDiscordId delegates to repo")
  void getByDiscordId() {
    ServerMember sm = new ServerMember();
    when(repo.findByDiscordId(5L)).thenReturn(Optional.of(sm));
    assertSame(sm, ctrl.getServerMemberByDiscordId(5L));
  }

  @Test
  @DisplayName("updateMember saves entity")
  void updateMember_Saves() {
    ServerMember sm = new ServerMember();
    when(repo.save(sm)).thenReturn(sm);
    assertSame(sm, ctrl.updateMember(sm));
    verify(repo).save(sm);
  }

  @Test
  @DisplayName("getAll returns repo result")
  void getAll() {
    List<ServerMember> list = List.of(new ServerMember(), new ServerMember());
    when(repo.findAll()).thenReturn(list);
    assertEquals(2, ctrl.getAll().size());
  }
}

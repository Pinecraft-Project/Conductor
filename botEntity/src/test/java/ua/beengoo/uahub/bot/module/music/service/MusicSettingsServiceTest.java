package ua.beengoo.uahub.bot.module.music.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.music.model.MusicMemberSettings;
import ua.beengoo.uahub.bot.module.music.player.PlayerMode;
import ua.beengoo.uahub.bot.module.music.repository.MusicMemberSettingsRepo;

@ExtendWith(MockitoExtension.class)
class MusicSettingsServiceTest {

  @Mock private MusicMemberSettingsRepo repo;
  @Mock private ServerMemberController smc;
  @InjectMocks private MusicSettingsService svc;

  private static MusicMemberSettings msWithJson(String json) {
    MusicMemberSettings ms = new MusicMemberSettings();
    ms.setJson(json);
    ms.setOwner(new ServerMember());
    return ms;
  }

  @Test
  @DisplayName("getSavedRepeatMode: parses from JSON")
  void getSavedRepeatMode() {
    when(repo.findByOwnerDiscordId(1L))
        .thenReturn(Optional.of(msWithJson("{\"repeatMode\":\"REPEAT_ONE\"}")));
    assertEquals(Optional.of(PlayerMode.REPEAT_ONE), svc.getSavedRepeatMode(1L));
  }

  @Test
  @DisplayName("saveRepeatMode: creates or updates JSON and saves")
  void saveRepeatMode() {
    ServerMember member = new ServerMember();
    when(smc.addMemberOrNothing(2L)).thenReturn(member);
    when(repo.findByOwnerDiscordId(2L)).thenReturn(Optional.empty());
    ArgumentCaptor<MusicMemberSettings> captor = ArgumentCaptor.forClass(MusicMemberSettings.class);

    svc.saveRepeatMode(2L, PlayerMode.REPEAT_QUEUE);
    verify(repo).save(captor.capture());
    assertNotNull(captor.getValue());
    assertTrue(captor.getValue().getJson().contains("REPEAT_QUEUE"));
  }

  @Test
  @DisplayName("getRequiredVoteActions: defaults when missing or invalid JSON")
  void getRequiredVoteActions_Defaults() {
    when(repo.findByOwnerDiscordId(3L)).thenReturn(Optional.of(msWithJson("{}")));
    Set<String> actions = svc.getRequiredVoteActions(3L);
    assertTrue(actions.contains("skip"));
    assertTrue(actions.contains("stop"));
  }

  @Test
  @DisplayName("setActionRequired: toggles required action and persists")
  void setActionRequired_Toggle() {
    long uid = 4L;
    ServerMember member = new ServerMember();
    when(smc.addMemberOrNothing(uid)).thenReturn(member);
    when(repo.findByOwnerDiscordId(uid)).thenReturn(Optional.of(msWithJson("{}")));
    ArgumentCaptor<MusicMemberSettings> captor = ArgumentCaptor.forClass(MusicMemberSettings.class);

    Set<String> afterAdd = svc.setActionRequired(uid, "next", true);
    assertTrue(afterAdd.contains("next"));
    verify(repo).save(captor.capture());
    assertTrue(captor.getValue().getJson().contains("next"));
  }

  @Test
  @DisplayName("get/save bypassVoting roundtrip")
  void bypassVoting_Roundtrip() {
    long uid = 5L;
    ServerMember member = new ServerMember();
    when(smc.addMemberOrNothing(uid)).thenReturn(member);
    when(repo.findByOwnerDiscordId(uid)).thenReturn(Optional.empty());
    svc.saveBypassVoting(uid, true);
    verify(repo).save(any(MusicMemberSettings.class));
  }
}

package ua.beengoo.uahub.bot.module.identity.service;

import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import ua.beengoo.uahub.bot.module.identity.exceptions.MemberExistsException;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.model.ServerMemberRepo;
import ua.beengoo.uahub.bot.module.permissions.model.MemberPermissions;
import ua.beengoo.uahub.bot.module.rank.model.RankStats;

@Service
public class ServerMemberController {

  private ServerMemberRepo serverMemberRepo;

  public ServerMemberController(ServerMemberRepo serverMemberRepo) {
    this.serverMemberRepo = serverMemberRepo;
  }

  /**
   * Adds a guild member to the database.
   *
   * @param discord_id Discord user id
   * @return persisted {@link ServerMember}
   * @throws MemberExistsException if a record already exists
   */
  public ServerMember addNewMember(long discord_id) {
    if (!serverMemberRepo.existsByDiscordId(discord_id)) {
      ServerMember sm = new ServerMember();
      RankStats rs = new RankStats();
      rs.setMemberMultiplier(1);
      rs.setServerMember(sm);
      sm.setRankStats(rs);
      sm.setDiscordId(discord_id);

      MemberPermissions mp = new MemberPermissions();
      mp.setServerMember(sm);
      sm.setMemberPermissions(mp);

      return serverMemberRepo.save(sm);
    }
    throw new MemberExistsException("Member already exists");
  }

  /**
   * Ensures a {@link ServerMember} record exists and returns it.
   *
   * @param discord_id Discord user id
   * @return the existing or newly created entity
   */
  public ServerMember addMemberOrNothing(long discord_id) {
    try {
      return addNewMember(discord_id);
    } catch (MemberExistsException ignored) {
      return serverMemberRepo.findByDiscordId(discord_id).orElse(null);
    }
  }

  /**
   * Finds member by Discord user id.
   *
   * @param discord_id Discord user id
   * @return the member or {@code null}
   */
  public ServerMember getServerMemberByDiscordId(long discord_id) {
    return serverMemberRepo.findByDiscordId(discord_id).orElse(null);
  }

  /**
   * Persists changes to a {@link ServerMember}.
   *
   * @param entity entity to update
   * @return updated entity
   */
  public ServerMember updateMember(@NonNull ServerMember entity) {
    return serverMemberRepo.save(entity);
  }

  /** Lists all known server members. */
  public List<ServerMember> getAll() {
    return serverMemberRepo.findAll();
  }
}

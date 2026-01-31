package ua.beengoo.uahub.bot.module.rank.command.admin;

import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import ua.beengoo.uahub.bot.ContextHolder;
import ua.beengoo.uahub.bot.Lang;
import ua.beengoo.uahub.bot.layout.message.Embed;
import ua.beengoo.uahub.bot.module.identity.model.ServerMember;
import ua.beengoo.uahub.bot.module.identity.service.ServerMemberController;
import ua.beengoo.uahub.bot.module.permissions.service.PermissionService;
import ua.beengoo.uahub.bot.module.rank.model.RankSettings;
import ua.beengoo.uahub.bot.module.rank.model.RankStats;
import ua.beengoo.uahub.bot.module.rank.model.RoleToLevel;
import ua.beengoo.uahub.bot.module.rank.service.RankSettingsController;
import ua.beengoo.uahub.bot.module.rank.service.RankingService;

@Interaction
/** Administrative slash command to view and modify user ranking statistics. */
public class RankAdminCommand {
    private final ServerMemberController serverMemberController;
    private final PermissionService permissionService;
    private final RankingService rankingService;
    private final RankSettingsController rankSettingsController;


    public RankAdminCommand() {
        serverMemberController = ContextHolder.getBean(ServerMemberController.class);
        permissionService = ContextHolder.getBean(PermissionService.class);
        rankingService = ContextHolder.getBean(RankingService.class);
        rankSettingsController = ContextHolder.getBean(RankSettingsController.class);
    }

    private boolean allowed(CommandEvent event) {
        boolean hasDiscordManage =
                event.getMember() != null && event.getMember().hasPermission(Permission.MANAGE_SERVER);
        boolean hasNode =
                permissionService.has(
                        event.getUser().getIdLong(),
                        event.getMember() != null
                                ? event.getMember().getRoles().stream().map(Role::getIdLong).toList()
                                : null,
                        "rank.manage");
        return hasDiscordManage || hasNode;
    }

    @Command(value = "rankadmin r2l", desc = "Налаштування ролей до рангових рівнів")
    public void onRankAdminR2l(
        CommandEvent event,
        @Param(name = "action") @Choices({
            "Додати:add",
            "Список:list",
            "Редагувати:edit",
            "Видалити:delete"
        }) String action,
        @Param(name = "role", value = "Роль") Role role,
        @Param(name = "req_level", value = "Рівень для отримання", optional = true) Long req_level
    ){
        if (!allowed(event)) {
            event.reply(
                Embed.getError()
                    .setTitle(Lang.get("perms.error.insufficient_rights.title"))
                    .setDescription(Lang.get("rank.admin.required")));
            return;
        }
        if (role == null) {
            event.reply(Embed.getError().setTitle(Lang.get("rank.admin.invalid_role_id")));
            return;
        }

        long serverId = event.getGuild().getIdLong(); // Get the server ID

        switch (action) {
            case "add" -> {
                if (req_level == null) {
                    event.reply(
                        Embed.getError().setTitle(Lang.get("rank.admin.missing_req_level.title"))
                    );
                    return;
                }
                if (!rankingService.isRoleToLevel(role.getIdLong())) {
                    RankSettings rankSettings = rankSettingsController.getSettings(serverId);

                    RoleToLevel r2l = new RoleToLevel();
                    r2l.setRoleId(role.getIdLong());
                    r2l.setLevelRequired(req_level);
                    r2l.setRankSettings(rankSettings);

                    rankingService.saveRoleToLevel(r2l);

                    event.reply(Embed.getInfo().setTitle(Lang.get("rank.admin.r2l.success.title")));
                } else {
                    event.reply(
                        Embed.getError().setTitle(Lang.get("rank.admin.already_r2l.title").formatted(
                                role.getName(),
                                rankingService.getRoleToLevel(role.getIdLong()).getLevelRequired()
                            )
                        )
                    );
                }
            }
            case "edit" -> {
                if (req_level == null) {
                    event.reply(
                        Embed.getError().setTitle(Lang.get("rank.admin.missing_req_level.title"))
                    );
                    return;
                }
                if (rankingService.isRoleToLevel(role.getIdLong())) {
                    RoleToLevel roleToLevel = rankingService.getRoleToLevel(role.getIdLong());
                    roleToLevel.setLevelRequired(req_level);
                    rankingService.saveRoleToLevel(roleToLevel);
                    event.reply(Embed.getInfo().setTitle(Lang.get("rank.admin.r2l.edit.success.title")));
                } else {
                    event.reply(
                        Embed.getError().setTitle(Lang.get("rank.admin.not_r2l.title"))
                    );
                }
            }
            case "delete" -> {
                if (rankingService.isRoleToLevel(role.getIdLong())) {
                    rankingService.removeRoleToLevel(role.getIdLong());
                    event.reply(Embed.getInfo().setTitle(Lang.get("rank.admin.r2l.delete.success.title")));
                } else {
                    event.reply(
                        Embed.getError().setTitle(Lang.get("rank.admin.not_r2l.title"))
                    );
                }
            }
            case "list" -> {
                StringBuilder builder = new StringBuilder();
                rankingService.getAllRoleToLevel(event.getGuild().getIdLong()).forEach(
                    e -> builder.append("> <@&%s> - lvl %s\n".formatted(e.getRoleId(), e.getLevelRequired()))
                );
                if (builder.isEmpty()) {
                    builder.append("Seem's like there is nothing...");
                }
                event.jdaEvent()
                    .replyEmbeds(Embed.getInfo().setDescription(builder.toString()).build()).setEphemeral(true).queue();
            }
            default -> event.reply(
                Embed.getWarn().setTitle(Lang.get("rank.admin.unknown_action")).setDescription(action));
        }
    }

    @Command(value = "rankadmin user", desc = "Керування статистикою користувача")
    public void onRankAdminUser(
            CommandEvent event,
            @Param(name = "action")
                    @Choices({
                        "Показати:get",
                        "Додати:add",
                        "Встановити:set",
                        "Скинути:reset",
                        "Перерахувати рівень:recalc_level"
                    })
                    String action,
            @Param(name = "user", value = "Користувач") User user,
            @Param(name = "type", value = "Тип статистики", optional = true)
                    @Choices({
                        "Чат:chat",
                        "Голос:voice",
                        "Prime:prime",
                        "Множник:multiplier",
                        "Все:all",
                        "Розширена статистика:extended"
                    })
                    String type,
            @Param(name = "value", value = "Значення", optional = true) Double value) {
        if (!allowed(event)) {
            event.reply(
                    Embed.getError()
                            .setTitle(Lang.get("perms.error.insufficient_rights.title"))
                            .setDescription(Lang.get("rank.admin.required")));
            return;
        }
        if (user == null) {
            event.reply(Embed.getError().setTitle(Lang.get("rank.admin.invalid_user_id")));
            return;
        }

        ServerMember sm = serverMemberController.addMemberOrNothing(user.getIdLong());
        RankStats stats = sm.getRankStats();

        switch (action) {
            case "get" -> doGet(event, user, stats);
            case "add" -> doAdd(event, user, stats, type, value);
            case "set" -> doSet(event, user, stats, type, value);
            case "reset" -> doReset(event, user, sm, type);
            default -> event.reply(
                    Embed.getWarn().setTitle(Lang.get("rank.admin.unknown_action")).setDescription(action));
        }
    }

    /** Sends a read-only summary of a user's rank stats. */
    private void doGet(CommandEvent event, User user, RankStats stats) {
        var eb =
                Embed.getInfo()
                        .setTitle(Lang.get("rank.admin.get.title"))
                        .setThumbnail(user.getAvatarUrl())
                        .setDescription(
                                Lang.get("rank.admin.get.desc")
                                        .formatted(
                                                (int) stats.getLevel(),
                                                (long) stats.getChatPoints(),
                                                (long) stats.getVoicePoints(),
                                                (long) stats.getPrimePoints(),
                                                stats.getMemberMultiplier(),
                                                stats.getMessagesSent(),
                                                stats.getVoiceJoins()));
        event.reply(eb);
    }

    /** Adds to one of the counters or multiplier. */
    private void doAdd(CommandEvent event, User user, RankStats stats, String type, Double value) {
        if (type == null || value == null) {
            event.reply(Embed.getWarn().setTitle(Lang.get("rank.admin.add.usage")));
            return;
        }
        double v = value;
        switch (type) {
            case "chat" -> stats.addChatPoints(v);
            case "voice" -> stats.addVoicePoints(v);
            case "prime" -> stats.addPrimePoints(v);
            case "multiplier" -> stats.addMultiplier(v);
            default -> {
                event.reply(
                        Embed.getWarn().setTitle(Lang.get("rank.admin.add.invalid_type")).setDescription(type));
                return;
            }
        }
        rankingService.updateLevel(stats);
        if (event.getMember() != null)
            rankingService.upgradeRole(event.getMember(), stats);

        serverMemberController.updateMember(stats.getServerMember());
        event.reply(
                Embed.getInfo()
                        .setTitle(Lang.get("rank.admin.add.ok"))
                        .setDescription(
                                Lang.get("rank.admin.target").formatted(user.getEffectiveName(), type, value)));
    }

    /** Sets one of the counters or multiplier. */
    private void doSet(CommandEvent event, User user, RankStats stats, String type, Double value) {
        if (type == null || value == null) {
            event.reply(Embed.getWarn().setTitle(Lang.get("rank.admin.set.usage")));
            return;
        }
        double v = value;
        switch (type) {
            case "chat" -> stats.setChatPoints(v);
            case "voice" -> stats.setVoicePoints(v);
            case "prime" -> stats.setPrimePoints(v);
            case "multiplier" -> stats.setMemberMultiplier(v);
            default -> {
                event.reply(
                        Embed.getWarn().setTitle(Lang.get("rank.admin.set.invalid_type")).setDescription(type));
                return;
            }
        }
        rankingService.updateLevel(stats);
        if (event.getMember() != null)
            rankingService.upgradeRole(event.getMember(), stats);
        serverMemberController.updateMember(stats.getServerMember());
        event.reply(
                Embed.getInfo()
                        .setTitle(Lang.get("rank.admin.set.ok"))
                        .setDescription(
                                Lang.get("rank.admin.target").formatted(user.getEffectiveName(), type, value)));
    }

    /** Resets selected or all stats for a user. */
    private void doReset(CommandEvent event, User user, ServerMember sm, String type) {
        if (type == null) {
            event.reply(Embed.getWarn().setTitle(Lang.get("rank.admin.reset.usage")));
            return;
        }
        RankStats s = sm.getRankStats();
        switch (type) {
            case "all" -> {
                s.setChatPoints(0);
                s.setVoicePoints(0);
                s.setPrimePoints(0);
                s.setLevel(0);
                s.setMemberMultiplier(1);
                // extended
                s.setMessagesSent(0L);
                s.setVoiceJoins(0L);
                s.setVoiceMsAlone(0L);
                s.setVoiceMsWithOthers(0L);
                s.setVoiceMsMuted(0L);
                s.setVoiceMsDeafened(0L);
                s.setVoiceMsActive(0L);
            }
            case "chat" -> s.setChatPoints(0);
            case "voice" -> s.setVoicePoints(0);
            case "prime" -> s.setPrimePoints(0);
            case "multiplier" -> s.setMemberMultiplier(1);
            case "extended" -> {
                s.setMessagesSent(0L);
                s.setVoiceJoins(0L);
                s.setVoiceMsAlone(0L);
                s.setVoiceMsWithOthers(0L);
                s.setVoiceMsMuted(0L);
                s.setVoiceMsDeafened(0L);
                s.setVoiceMsActive(0L);
            }
            default -> {
                event.reply(
                        Embed.getWarn()
                                .setTitle(Lang.get("rank.admin.reset.invalid_type"))
                                .setDescription(type));
                return;
            }
        }
        rankingService.updateLevel(s);
        if (event.getMember() != null)
            rankingService.upgradeRole(event.getMember(), s);
        serverMemberController.updateMember(sm);
        event.reply(
                Embed.getInfo()
                        .setTitle(Lang.get("rank.admin.reset.ok"))
                        .setDescription(
                                Lang.get("rank.admin.target").formatted(user.getEffectiveName(), type)));
    }
}

package ua.beengoo.uahub.bot.module.permissions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.beengoo.uahub.bot.module.permissions.model.MemberPermissions;

@Repository
public interface MemberPermissionsRepo extends JpaRepository<MemberPermissions, Long> {}

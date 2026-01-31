package ua.beengoo.uahub.bot.module.rank.model;

import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleToLevelRepo extends JpaRepository<RoleToLevel, Long> {
    /** Finds role linked to level by role id. */
    Optional<RoleToLevel> findByRoleId(long roleId);

    /**
     * Deletes role links by role id.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoleToLevel r WHERE r.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") long roleId);

    /** Checks if role linked to level exist by id. */
    boolean existsByRoleId(long roleId);

}

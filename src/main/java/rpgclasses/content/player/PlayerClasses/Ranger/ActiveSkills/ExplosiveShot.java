package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.Projectile;
import necesse.gfx.GameResources;
import org.jetbrains.annotations.NotNull;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.projectiles.ExplosiveArrowProjectile;
import rpgclasses.utils.RPGUtils;

import java.awt.geom.Point2D;

public class ExplosiveShot extends ActiveSkill {

    public ExplosiveShot(int levelMax, int requiredClassLevel) {
        super("explosiveshot", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        Projectile projectile = getProjectile(player, playerData, activeSkillLevel);
        projectile.resetUniqueID(new GameRandom(seed));

        player.getLevel().entityManager.projectiles.addHidden(projectile);
        player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.bow, SoundEffect.effect(player));
    }

    private static @NotNull Projectile getProjectile(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        Mob target = RPGUtils.findBestTarget(player, 1000);

        float targetX;
        float targetY;

        if (target == null) {
            Point2D.Float dir = getDir(player);
            targetX = dir.x * 100 + player.x;
            targetY = dir.y * 100 + player.y;
        } else {
            targetX = target.x;
            targetY = target.y;
        }

        return new ExplosiveArrowProjectile(player.getLevel(), player, player.x, player.y, targetX, targetY, 200, 1000, new GameDamage(DamageTypeRegistry.RANGED, 5 * playerData.getLevel() + 5 * playerData.getIntelligence(player) * activeSkillLevel), 100);
    }

    @Override
    public int getBaseCooldown() {
        return 15000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"explosivearrow"};
    }
}

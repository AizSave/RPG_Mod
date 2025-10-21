package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.ProjectileRegistry;
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
import rpgclasses.utils.RPGUtils;

public class ArrowStorm extends ActiveSkill {

    public ArrowStorm(int levelMax, int requiredClassLevel) {
        super("arrowstorm", "#009900", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        RPGUtils.streamMobsAndPlayers(player, 400)
                .filter(RPGUtils.isValidTargetFilter(player))
                .forEach(
                        target -> {
                            Projectile projectile = getProjectile(player, target, playerData, activeSkillLevel);
                            projectile.resetUniqueID(new GameRandom(seed));

                            player.getLevel().entityManager.projectiles.addHidden(projectile);
                            player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
                        }
                );
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 400) ? null : "notarget";
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.bow, SoundEffect.effect(player));
        AphAreaList areaList = new AphAreaList(
                new AphArea(400, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    private static @NotNull Projectile getProjectile(PlayerMob player, Mob target, PlayerData playerData, int activeSkillLevel) {
        return ProjectileRegistry.getProjectile("stonearrow", player.getLevel(), player.x, player.y, target.x, target.y, 200, 1000, new GameDamage(DamageTypeRegistry.RANGED, 4 * playerData.getLevel() + playerData.getStrength(player) * activeSkillLevel + playerData.getSpeed(player) * activeSkillLevel), 100, player);
    }

    @Override
    public int getBaseCooldown() {
        return 15000;
    }
}

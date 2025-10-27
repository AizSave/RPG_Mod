package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

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
import rpgclasses.content.player.SkillsLogic.ActiveSkills.CastActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.projectiles.IceBallProjectile;
import rpgclasses.utils.RPGUtils;

import java.awt.geom.Point2D;

public class Iceball extends CastActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.damageParam(20)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(40);
    }

    public Iceball(int levelMax, int requiredClassLevel) {
        super("iceball", "#00ccff", levelMax, requiredClassLevel);
    }

    @Override
    public int castTime() {
        return 2000;
    }

    @Override
    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunServer(player, playerData, activeSkillLevel, seed);

        Projectile projectile = getProjectile(player, playerData, activeSkillLevel);
        projectile.resetUniqueID(new GameRandom(seed));

        player.getLevel().entityManager.projectiles.addHidden(projectile);
        player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
    }

    @Override
    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        super.castedRunClient(player, playerData, activeSkillLevel, seed);
        SoundManager.playSound(GameResources.magicbolt1, SoundEffect.effect(player));
    }

    private static Projectile getProjectile(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        Mob target = RPGUtils.findBestTarget(player, 600);

        float targetX;
        float targetY;
        int distance;

        if (target == null) {
            Point2D.Float dir = getDir(player);
            targetX = dir.x * 100 + player.x;
            targetY = dir.y * 100 + player.y;
            distance = 600;
        } else {
            targetX = target.x;
            targetY = target.y;
            distance = (int) player.getDistance(target);
        }

        return new IceBallProjectile(player.getLevel(), player, player.x, player.y, targetX, targetY, 100, distance, new GameDamage(DamageTypeRegistry.MAGIC, params[0].value(playerData.getLevel(), activeSkillLevel)), 100);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 15000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"iceball"};
    }
}

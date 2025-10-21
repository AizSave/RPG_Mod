package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.projectile.Projectile;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import org.jetbrains.annotations.NotNull;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.projectiles.ObjectProjectile;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.utils.RPGColors;

import java.awt.*;
import java.awt.geom.Point2D;

public class ObjectThrowing extends ActiveSkill {

    public ObjectThrowing(int levelMax, int requiredClassLevel) {
        super("objectthrowing", RPGColors.HEX.iron, levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.run(player, playerData, activeSkillLevel, seed, isInUSe);
        if (isInUSe) {
            if (playerData.grabbedObject != null) {
                if (player.isServer()) {
                    Projectile projectile = getProjectile(player, playerData, activeSkillLevel);
                    projectile.resetUniqueID(new GameRandom(seed));

                    player.getLevel().entityManager.projectiles.addHidden(projectile);
                    player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
                }

                playerData.grabbedObject = null;
            }

            player.buffManager.removeBuff(RPGBuffs.PASSIVES.GRABBED_OBJECT, player.isServer());
        } else {
            Point objectPoint = getObject(player);
            if (objectPoint == null) {
                for (EquippedActiveSkill equippedActiveSkill : playerData.equippedActiveSkills) {
                    if (equippedActiveSkill.getActiveSkill() == this) {
                        equippedActiveSkill.restartCooldown();
                    }
                }
                return;
            }

            playerData.grabbedObject = player.getLevel().getObject(objectPoint.x, objectPoint.y);

            player.getLevel().setObject(objectPoint.x, objectPoint.y, 0);

            player.buffManager.addBuff(new ActiveBuff(RPGBuffs.PASSIVES.GRABBED_OBJECT, player, 1000, null), player.isServer());
        }
    }

    private static @NotNull Projectile getProjectile(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        Point2D.Float dir = getDir(player);
        float targetX = dir.x * 100 + player.x;
        float targetY = dir.y * 100 + player.y;
        return new ObjectProjectile(player.getLevel(), player, playerData.grabbedObject, player.x, player.y, targetX, targetY, 100, 500, new GameDamage(DamageTypeRegistry.MELEE, 20 * playerData.getLevel() + 10 * playerData.getStrength(player) * activeSkillLevel), 300);
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        String canActive = super.canActive(player, playerData, isInUSe);
        if (canActive != null) return canActive;

        if (isInUSe) return null;

        return getObject(player) == null ? "nograbbableobject" : null;
    }

    public Point getObject(PlayerMob player) {
        Level level = player.getLevel();
        if (level.isProtected) return null;

        int tileX = player.getTileX();
        int tileY = player.getTileY();

        GameObject object = PlayerData.isGrabbableObject(level, tileX, tileY);
        if (object != null) {
            return new Point(tileX, tileY);
        }
        if (player.getDir() == 0) {
            tileY--;
        } else if (player.getDir() == 1) {
            tileX++;
        } else if (player.getDir() == 2) {
            tileY++;
        } else if (player.getDir() == 3) {
            tileX--;
        }

        object = PlayerData.isGrabbableObject(level, tileX, tileY);
        return object == null ? null : new Point(tileX, tileY);
    }

    @Override
    public int getBaseCooldown() {
        return 16000;
    }

    @Override
    public boolean isInUseSkill() {
        return true;
    }
}

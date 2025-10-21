package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.packet.PacketPlayerRespawn;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;
import necesse.level.maps.Level;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.lang.reflect.Field;

public class Resurrection extends ActiveSkill {

    public Resurrection(int levelMax, int requiredClassLevel) {
        super("resurrection", "#ff00ff", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        ServerClient lastDeath = RPGUtils.lastDeathPlayer(player.getLevel(), 10000, c -> c.isSameTeam(player.getTeam()));

        if (lastDeath != null) {
            Point spawnPos = new Point();
            spawnPos.setLocation(player.x, player.y);
            Level spawnLevel = player.getLevel();

            lastDeath.playerMob.restore();

            try {
                Field hasSpawnedField = NetworkClient.class.getDeclaredField("hasSpawned");
                hasSpawnedField.setAccessible(true);
                hasSpawnedField.set(lastDeath, false);

                Field isDeadField = NetworkClient.class.getDeclaredField("isDead");
                isDeadField.setAccessible(true);
                isDeadField.set(lastDeath, false);

                lastDeath.setLevelIdentifier(spawnLevel.getIdentifier());
                lastDeath.playerMob.setPos((float) spawnPos.x, (float) spawnPos.y, true);
                lastDeath.playerMob.dx = 0.0F;
                lastDeath.playerMob.dy = 0.0F;
                lastDeath.playerMob.setHealth(Math.max((int) (lastDeath.playerMob.getMaxHealth() * 0.1F * activeSkillLevel), 1));
                lastDeath.playerMob.setMana((float) Math.max(lastDeath.playerMob.getMaxMana(), 1));
                lastDeath.playerMob.hungerLevel = Math.max(0.5F, lastDeath.playerMob.hungerLevel);
                lastDeath.getServer().network.sendToAllClients(new PacketPlayerRespawn(lastDeath));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return (player.isClient() || RPGUtils.streamDeathPlayers(player.getLevel(), 10000, serverClient -> serverClient.isSameTeam(player.getTeam())).findAny().isPresent()) ? null : "nodeathplayers";
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return Math.max(60 + activeSkillLevel * 12, (100 - activeSkillLevel * 10) * player.getMaxMana());
    }

    @Override
    public int getBaseCooldown() {
        return 60000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -4000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"manausage"};
    }
}

package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.GameDeathPenalty;
import necesse.engine.network.NetworkClient;
import necesse.engine.network.packet.PacketPlayerRespawn;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.PlayerMob;
import necesse.level.gameObject.RespawnObject;
import necesse.level.maps.Level;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.lang.reflect.Field;

public class Resurrection extends ActiveSkill {

    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("10 x <skilllevel>").setDecimals(2, 0),
            new SkillParam("10 + <skilllevel>")
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(50, false);
    }

    public Resurrection(int levelMax, int requiredClassLevel) {
        super("resurrection", "#ff00ff", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        ServerClient lastDeath = RPGUtils.lastDeathPlayer(player.getLevel(), params[1].value(activeSkillLevel), c -> c.isSameTeam(player.getTeam()));

        if (lastDeath != null) {
            lastDeath.validateSpawnPoint(true);
            Point spawnPos;
            Level spawnLevel = lastDeath.getServer().world.getLevel(lastDeath.spawnLevelIdentifier);
            if (!lastDeath.isDefaultSpawnPoint()) {
                Point offset = RespawnObject.calculateSpawnOffset(spawnLevel, lastDeath.spawnTile.x, lastDeath.spawnTile.y, lastDeath);
                spawnPos = new Point(lastDeath.spawnTile.x * 32 + offset.x, lastDeath.spawnTile.y * 32 + offset.y);
            } else {
                spawnPos = lastDeath.getPlayerPosFromTile(spawnLevel, lastDeath.spawnTile.x, lastDeath.spawnTile.y);
            }

            lastDeath.playerMob.restore();

            try {
                Field hasSpawnedField = NetworkClient.class.getDeclaredField("hasSpawned");
                hasSpawnedField.setAccessible(true);
                hasSpawnedField.set(lastDeath, false);

                Field isDeadField = NetworkClient.class.getDeclaredField("isDead");
                isDeadField.setAccessible(true);
                isDeadField.set(lastDeath, false);


                lastDeath.setLevelIdentifier(lastDeath.spawnLevelIdentifier);
                lastDeath.playerMob.setPos((float) spawnPos.x, (float) spawnPos.y, true);
                lastDeath.playerMob.dx = 0.0F;
                lastDeath.playerMob.dy = 0.0F;
                lastDeath.playerMob.setHealth(Math.max((int) (lastDeath.playerMob.getMaxHealth() * params[0].value(activeSkillLevel)), 1));
                lastDeath.playerMob.setMana((float) Math.max(lastDeath.playerMob.getMaxMana(), 1));
                lastDeath.playerMob.hungerLevel = Math.max(0.5F, lastDeath.playerMob.hungerLevel);
                lastDeath.getServer().network.sendToAllClients(new PacketPlayerRespawn(lastDeath));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public String canActive(PlayerMob player, PlayerData playerData, int activeSkillLevel, boolean isInUSe) {
        return (player.isClient() || RPGUtils.streamDeathPlayers(player.getLevel(), params[1].value(activeSkillLevel), serverClient -> serverClient.isSameTeam(player.getTeam())).findAny().isPresent()) ? null : "nodeathplayers";
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        if (player.getWorldSettings().deathPenalty == GameDeathPenalty.HARDCORE) {
            return 12000;
        } else {
            return 40000;
        }
    }
}

package rpgclasses.data;


import necesse.entity.mobs.PlayerMob;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataList {
    public static Map<String, PlayerData> playersServer = new HashMap<>();
    public static Map<String, PlayerData> playersClient = new HashMap<>();

    public static PlayerData getPlayerData(PlayerMob player) {
        return getPlayerData(player.playerName, player.isServer());
    }

    public static PlayerData getPlayerData(String playerName, boolean isServer) {
        return (isServer ? playersServer : playersClient).get(playerName);
    }

    public static void setPlayerData(String playerName, PlayerData playerData, boolean isServer) {
        (isServer ? playersServer : playersClient).put(playerName, playerData);
    }

    public static PlayerData initPlayerData(PlayerMob player) {
        return initPlayerData(player.playerName, player.getServer().world.getUniqueID(), player.isServer());
    }

    public static PlayerData initPlayerData(String playerName, long worldUniqueID, boolean isServer) {
        PlayerData playerData = new PlayerData(playerName, worldUniqueID);
        (isServer ? playersServer : playersClient).put(playerName, playerData);
        return playerData;
    }

}

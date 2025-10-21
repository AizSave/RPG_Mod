package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.ui.CustomUIManager;
import rpgclasses.ui.RPGSkillUIManager;

import java.util.Objects;

public class LoadPlayerDataPacket extends Packet {

    public final int slot;
    public final String playerName;
    public final PlayerData playerData;

    public LoadPlayerDataPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        slot = reader.getNextInt();
        playerName = reader.getNextString();

        if (reader.hasNext()) {
            playerData = PlayerData.applyPacket(reader);
        } else {
            playerData = null;
        }
    }

    public LoadPlayerDataPacket(int slot, String playerName, PlayerData playerData) {
        this.slot = slot;
        this.playerName = playerName;
        this.playerData = playerData;


        PacketWriter writer = new PacketWriter(this);

        writer.putNextInt(slot);
        writer.putNextString(playerName);
        if (playerData != null) {
            playerData.setupPacket(writer);
        }
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerDataList.setPlayerData(playerName, playerData, false);
        if (Objects.equals(client.getPlayer().playerName, playerName) && CustomUIManager.expBar != null) {
            CustomUIManager.expBar.updateExpBar(playerData);
            RPGSkillUIManager.updateContent(playerData);
        }
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        PlayerData playerData = PlayerDataList.getPlayerData(playerName, true);

        long worldUniqueID = server.world.getUniqueID();

        if (playerData == null || playerData.worldUniqueID != worldUniqueID) {
            playerData = PlayerDataList.initPlayerData(playerName, worldUniqueID, true);
        }

        final PlayerData finalPlayerData = playerData;
        server.network.sendToAllClients(new LoadPlayerDataPacket(slot, playerName, finalPlayerData));

        ServerClient clientTarget = server.getClient(slot);
        finalPlayerData.updateAllBuffs(clientTarget.playerMob);
    }
}
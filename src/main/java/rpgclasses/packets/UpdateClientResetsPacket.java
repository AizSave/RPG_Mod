package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class UpdateClientResetsPacket extends Packet {

    public final String playerName;
    public final int resets;

    public UpdateClientResetsPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        playerName = reader.getNextString();
        resets = reader.getNextInt();
    }

    public UpdateClientResetsPacket(PlayerData playerData) {
        this.playerName = playerData.playerName;
        this.resets = playerData.getResets();

        PacketWriter writer = new PacketWriter(this);

        writer.putNextString(playerName);
        writer.putNextInt(resets);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(playerName, false);
        if (playerData != null) playerData.loadDataResets(resets);
    }
}
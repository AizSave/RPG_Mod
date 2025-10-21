package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.util.ArrayList;
import java.util.List;

public class UpdateClientMasteryPacket extends Packet {

    public final String playerName;
    public final List<Integer> masterySkills;

    public UpdateClientMasteryPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        playerName = reader.getNextString();
        masterySkills = new ArrayList<>();
        int masterySize = reader.getNextInt();
        int[] masteryArray = reader.getNextInts(masterySize);
        for (int i : masteryArray) {
            masterySkills.add(i);
        }

    }

    public UpdateClientMasteryPacket(PlayerData playerData) {
        this.playerName = playerData.playerName;
        this.masterySkills = playerData.masterySkills;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(playerName);

        int[] masteryArray = masterySkills.stream().mapToInt(Integer::intValue).toArray();
        writer.putNextInt(masteryArray.length);
        writer.putNextInts(masteryArray);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = PlayerDataList.getPlayerData(playerName, false);
        if (playerData != null) playerData.setMasterySkills(masterySkills);
    }
}
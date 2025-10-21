package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.ui.RPGSkillUIManager;

import java.util.Objects;

public class UpdateClientClassDataPacket extends Packet {

    public final PlayerClassData classData;

    public UpdateClientClassDataPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);

        classData = PlayerClassData.applyPacket(reader);
    }

    public UpdateClientClassDataPacket(PlayerClassData playerClassData) {
        this.classData = playerClassData;
        PacketWriter writer = new PacketWriter(this);
        playerClassData.setupPacket(writer);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        PlayerData playerData = classData.getPlayerData(false);
        playerData.getClassesData()[classData.playerClass.id] = classData;
        if (Objects.equals(client.getPlayer().playerName, classData.playerName)) {
            RPGSkillUIManager.updateLevels(playerData);
        }
    }
}
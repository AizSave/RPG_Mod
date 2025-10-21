package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;

public class PacketMobResetBuffTime extends Packet {
    public final int mobUniqueID;
    public final String buffStringID;

    public PacketMobResetBuffTime(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.mobUniqueID = reader.getNextInt();
        this.buffStringID = reader.getNextString();
    }

    public PacketMobResetBuffTime(int mobUniqueID, String buffStringID) {
        this.mobUniqueID = mobUniqueID;
        this.buffStringID = buffStringID;

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobUniqueID);
        writer.putNextString(buffStringID);
    }

    public void processClient(NetworkPacket packet, Client client) {
        Mob mob = GameUtils.getLevelMob(this.mobUniqueID, client.getLevel());
        if (mob != null) {
            ActiveBuff ab = mob.buffManager.getBuff(buffStringID);
            if (ab != null) {
                ab.getGndData().setInt("time", 0);
                ab.getGndData().setBoolean("ready", false);
            }
        }
    }
}
package rpgclasses.packets;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;

public class PacketMobUpdateIgniteBuff extends Packet {
    public final int mobUniqueID;
    public final String buffStringID;
    public final int duration;
    public final float damage;

    public PacketMobUpdateIgniteBuff(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.mobUniqueID = reader.getNextInt();
        this.buffStringID = reader.getNextString();
        this.duration = reader.getNextInt();
        this.damage = reader.getNextFloat();
    }

    public PacketMobUpdateIgniteBuff(int mobUniqueID, ActiveBuff ab) {
        this.mobUniqueID = mobUniqueID;
        this.buffStringID = ab.buff.getStringID();
        this.duration = ab.getDurationLeft();
        this.damage = ab.getGndData().getFloat("igniteDamage");

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(mobUniqueID);
        writer.putNextString(buffStringID);
        writer.putNextInt(duration);
        writer.putNextFloat(damage);
    }

    public void processClient(NetworkPacket packet, Client client) {
        Mob mob = GameUtils.getLevelMob(this.mobUniqueID, client.getLevel());
        if (mob != null) {
            ActiveBuff ab = mob.buffManager.getBuff(buffStringID);
            if (ab != null) {
                ab.getGndData().setFloat("igniteDamage", damage);
                ab.setModifier(BuffModifiers.FIRE_DAMAGE_FLAT, damage);
                ab.setDurationLeft(duration);
            }
        }
    }
}
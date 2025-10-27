package rpgclasses.levelevents;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import rpgclasses.buffs.MagicPoisonBuff;

import java.awt.*;

public class NecroticExplosionLevelEvent extends RPGExplosionLevelEvent {
    public float poisonDamage;
    public int poisonTime;

    public NecroticExplosionLevelEvent() {
        super();
        this.poisonDamage = 0;
        this.poisonTime = 0;
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner, float poisonDamage, int poisonTime) {
        super(x, y, range, damage, owner, false, new Color(10, 40, 10, damage == null ? 153 : 255), new Color(80, 255, 120, damage == null ? 153 : 255));
        this.poisonDamage = poisonDamage;
        this.poisonTime = poisonTime;
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, Mob owner, float poisonDamage, int poisonTime) {
        this(x, y, range, new GameDamage(0), owner, poisonDamage, poisonTime);
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner) {
        this(x, y, range, damage, owner, 0, 0);
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner, float poisonDamage, float poisonTime) {
        this(x, y, range, damage, owner, poisonDamage, (int) (poisonTime * 1000));
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, Mob owner, float poisonDamage, float poisonTime) {
        this(x, y, range, new GameDamage(0), owner, poisonDamage, poisonTime);
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextFloat(poisonDamage);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        poisonDamage = reader.getNextFloat();
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addFloat("poisonDamage", poisonDamage);
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        poisonDamage = load.getFloat("poisonDamage");
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        super.onMobWasHit(mob, distance);
        if (poisonTime > 0 && poisonDamage > 0) {
            MagicPoisonBuff.apply(ownerMob, mob, poisonDamage, poisonTime);
        }
    }
}

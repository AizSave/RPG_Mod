package rpgclasses.levelevents;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.IgnitedBuff;

import java.awt.*;

public class FireExplosionLevelEvent extends RPGExplosionLevelEvent {
    public boolean applyIgnite;

    public FireExplosionLevelEvent() {
        super();
        this.applyIgnite = false;
    }

    public FireExplosionLevelEvent(float x, float y, int range, GameDamage damage, Mob owner, boolean hitsOwner, boolean applyIgnite) {
        super(x, y, range, damage, owner, hitsOwner, new Color(255, 225, 155), new Color(200, 25, 29));
        this.applyIgnite = applyIgnite;
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextBoolean(applyIgnite);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        applyIgnite = reader.getNextBoolean();
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addBoolean("applyIgnite", applyIgnite);
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        applyIgnite = load.getBoolean("applyIgnite");
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        float changedDistance = mob == ownerMob ? distance * 2 : distance;
        if (changedDistance < range) {
            super.onMobWasHit(mob, changedDistance);
            if (applyIgnite) IgnitedBuff.apply(getAttackOwner(), mob, damage.damage * 0.2F, 5F, false);

            if (ownerMob.isHostile) {
                float duration = distance <= 32 ? 10F : Math.max(10F / (distance / 32), 2F);
                mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.BROKEN_ARMOR, mob, duration, null), true);
            }
        }
    }
}

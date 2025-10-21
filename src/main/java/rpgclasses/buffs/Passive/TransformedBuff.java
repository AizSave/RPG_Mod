package rpgclasses.buffs.Passive;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import rpgclasses.content.player.SkillsLogic.Passives.BasicPassive;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.data.PlayerClassData;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.TransformationMountMob;

public class TransformedBuff extends PassiveBuff {
    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
        PlayerMob player = null;
        if (activeBuff.owner instanceof PlayerMob) {
            player = (PlayerMob) activeBuff.owner;
        } else if (activeBuff.owner.getRider() instanceof PlayerMob) {
            player = (PlayerMob) activeBuff.owner.getRider();
        }
        if (player != null) {
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            for (PlayerClassData classData : playerData.getClassesData()) {
                for (int i = 0; i < classData.getPassiveLevels().length; i++) {
                    int passiveLevel = classData.getPassiveLevels()[i];
                    if (passiveLevel > 0) {
                        Passive passive = classData.playerClass.passivesList.get(i);
                        if (passive instanceof BasicPassive) {
                            BasicPassive basicPassive = (BasicPassive) passive;
                            if (basicPassive.onlyTransformed) basicPassive.applyBuff(activeBuff, passiveLevel);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
        super.onBeforeHit(activeBuff, event);
        if (!activeBuff.owner.isPlayer) return;
        Mob mount = activeBuff.owner.getMount();
        if (mount instanceof TransformationMountMob) {
            ((TransformationMountMob) mount).onBeforeHit((PlayerMob) activeBuff.owner, event);
        }
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        super.serverTick(activeBuff);
        if (activeBuff.owner instanceof TransformationMountMob) return;
        if (!(activeBuff.owner.getMount() instanceof TransformationMountMob)) {
            activeBuff.remove();
        }
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        if (activeBuff.owner instanceof TransformationMountMob) return;
        if (!(activeBuff.owner.getMount() instanceof TransformationMountMob)) {
            activeBuff.remove();
        }
    }
}

package rpgclasses.content.player.PlayerClasses.Druid.Passives;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Interfaces.TransformationClassBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.buffs.Skill.SecondaryPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.mount.TransformationMountMob;

public class PrimalBurst extends SimpleBuffPassive {
    public PrimalBurst(int levelMax, int requiredClassLevel) {
        super("primalburst", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrimalBurstBuff();
    }

    public class PrimalBurstBuff extends PrincipalPassiveBuff implements TransformationClassBuff {

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
            this.isVisible = false;
        }

        @Override
        public void onTransform(ActiveBuff activeBuff, PlayerMob player, Mob target) {
            giveSecondaryPassiveBuff(player, target, PlayerDataList.getPlayerData(player), getLevel(activeBuff), 5000);
        }
    }

    @Override
    public SecondaryPassiveBuff getSecondaryBuff() {
        return new SecondaryPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                int level = getLevel(activeBuff);
                activeBuff.addModifier(BuffModifiers.ALL_DAMAGE, 0.06F * level);
                activeBuff.addModifier(BuffModifiers.SPEED, 0.06F * level);
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
        };
    }
}

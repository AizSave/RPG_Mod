package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeDamageOverTimeTakenEvent;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import rpgclasses.buffs.Interfaces.TransformationClassBuff;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.buffs.Skill.SecondaryMasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;

public class Shapeshifter extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(3),
            SkillParam.staticParam(5)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public Shapeshifter(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new ShapeshifterBuff();
    }

    public class ShapeshifterBuff extends MasteryBuff implements TransformationClassBuff {

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
            this.isVisible = false;
            eventSubscriber.subscribeEvent(MobBeforeDamageOverTimeTakenEvent.class, MobBeforeDamageOverTimeTakenEvent::prevent);
        }

        @Override
        public void onTransform(ActiveBuff activeBuff, PlayerMob player, Mob target) {
            long lastUse = activeBuff.getGndData().getLong("lastUse");
            long now = player.getTime();
            if (lastUse + (int) (params[1].value() * 1000) <= now) {
                giveDatalessSecondaryPassiveBuff(player, (int) (params[0].value() * 1000));
                activeBuff.getGndData().setLong("lastUse", now);
            }
        }
    }

    @Override
    public SecondaryMasteryBuff secondaryMasteryBuff() {
        return new SecondaryMasteryBuff() {
            @Override
            public void onBeforeHitCalculated(ActiveBuff buff, MobBeforeHitCalculatedEvent event) {
                super.onBeforeHitCalculated(buff, event);
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;
            }
        };
    }
}

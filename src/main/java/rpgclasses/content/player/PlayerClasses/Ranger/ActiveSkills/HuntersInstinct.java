package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.Interfaces.DodgeClassBuff;
import rpgclasses.buffs.MarkedBuff;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.registry.RPGModifiers;
import rpgclasses.utils.RPGColors;

public class HuntersInstinct extends SimpleBuffActiveSkill {

    public HuntersInstinct(int levelMax, int requiredClassLevel) {
        super("huntersinstinct", "#ff0000", levelMax, requiredClassLevel);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new HuntersInstinctBuff();
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 12000;
    }

    @Override
    public int getBaseCooldown() {
        return 26000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"dodgechance", "marked"};
    }

    public static class HuntersInstinctBuff extends ActiveSkillBuff implements DodgeClassBuff {

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            int level = getLevel(activeBuff);
            activeBuff.setModifier(RPGModifiers.DODGE_CHANCE, level * 0.1F);
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            Mob owner = activeBuff.owner;
            if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(RPGColors.red).height(16.0F);
            }
        }

        @Override
        public void onDodge(ActiveBuff activeBuff, MobBeforeHitEvent event) {
            Mob attacker = event.attacker.getFirstAttackOwner();
            if (attacker != null) {
                MarkedBuff.markMob(((PlayerMob) activeBuff.owner), attacker, 5000);
            }
        }
    }
}

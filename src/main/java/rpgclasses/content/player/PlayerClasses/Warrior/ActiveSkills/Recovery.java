package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.utils.RPGColors;

public class Recovery extends SimpleBuffActiveSkill {

    public Recovery(int levelMax, int requiredClassLevel) {
        super("recovery", "#00ff00", levelMax, requiredClassLevel);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                int level = getLevel(activeBuff);
                float healthRegen = (20F + level * getEndurance(activeBuff)) / 10F;
                activeBuff.setModifier(BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, healthRegen);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt(2) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(RPGColors.green).height(16.0F);
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 10000;
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }
}

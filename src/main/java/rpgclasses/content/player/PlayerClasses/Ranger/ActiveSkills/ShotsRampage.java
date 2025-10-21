package rpgclasses.content.player.PlayerClasses.Ranger.ActiveSkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;

public class ShotsRampage extends SimpleBuffActiveSkill {

    public ShotsRampage(int levelMax, int requiredClassLevel) {
        super("shotsrampage", "#00ffff", levelMax, requiredClassLevel);
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                new ModifierValue<>(BuffModifiers.ATTACK_SPEED, 0.05F).apply(activeBuff);
            }

            @Override
            public int getStackSize(ActiveBuff activeBuff) {
                return 30;
            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (!event.wasPrevented && (event.target.isHostile || event.target.isHuman) && event.damage > 0 && event.damageType == DamageTypeRegistry.RANGED) {
                    activeBuff.owner.buffManager.addBuff(new ActiveBuff(getBuffStringID(), activeBuff.owner, 5000, null), true);
                }
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                Mob owner = activeBuff.owner;
                if (owner.isVisible() && GameRandom.globalRandom.nextInt((getStackSize(activeBuff) + 1) - activeBuff.getStacks()) == 0) {
                    owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(getColor()).height(16.0F);
                }
            }

            @Override
            public boolean showsFirstStackDurationText() {
                return true;
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 5000;
    }

    @Override
    public int getBaseCooldown() {
        return 60000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -6000;
    }
}

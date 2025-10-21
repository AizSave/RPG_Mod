package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.modifiers.ModifierValue;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.buffs.Skill.PassiveActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimplePassiveBuffActiveSkill;

import java.awt.*;

public class ManaRecharge extends SimplePassiveBuffActiveSkill {

    public ManaRecharge(int levelMax, int requiredClassLevel) {
        super("manarecharge", "#0099ff", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 2000;
    }

    @Override
    public PassiveActiveSkillBuff getBuff() {
        return new ArcaneOverloadBuff(this, getBuffStringID());
    }

    public static class ArcaneOverloadBuff extends PassiveActiveSkillBuff {
        public ActiveSkill skill;
        public String buffStringID;
        public ParticleTypeSwitcher particleTypeSwitcher;

        public ArcaneOverloadBuff(ActiveSkill skill, String buffStringID) {
            this.skill = skill;
            this.buffStringID = buffStringID;
            this.particleTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            super.init(activeBuff, buffEventSubscriber);
            int level = getLevel(activeBuff);
            activeBuff.setModifier(BuffModifiers.COMBAT_MANA_REGEN_FLAT, 1F * level);
            new ModifierValue<>(BuffModifiers.SLOW, 1.0F).min(1F).apply(activeBuff);
            new ModifierValue<>(BuffModifiers.SPEED, -1.0F).max(-1F).apply(activeBuff);
            activeBuff.addModifier(BuffModifiers.INCOMING_DAMAGE_MOD, 2F);
            activeBuff.addModifier(BuffModifiers.PARALYZED, true);
            activeBuff.addModifier(BuffModifiers.INTIMIDATED, true);
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            int level = getLevel(activeBuff);
            if (GameRandom.globalRandom.getChance(level * 0.2F)) {
                int angle = (int) (360.0F + GameRandom.globalRandom.nextFloat() * 360.0F);
                float dx = (float) Math.sin(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
                float dy = (float) Math.cos(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
                activeBuff.owner.getLevel().entityManager.addParticle(activeBuff.owner.x - dx, activeBuff.owner.y - dy, particleTypeSwitcher.next()).movesFriction(dx, dy, 0.8F).color(GameRandom.globalRandom.getOneOf(new Color(0, 255, 255), new Color(0, 135, 255), new Color(0, 51, 135))).heightMoves(10.0F, 20.0F, 5F, 0F, 10F, 0F).lifeTime(250);
            }
        }
    }
}

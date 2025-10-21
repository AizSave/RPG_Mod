package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import aphorea.registry.AphBuffs;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.engine.util.LineHitbox;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobDashLevelEvent;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.PlayerData;

import java.awt.*;
import java.awt.geom.Point2D;

public class BerserkerCharge extends SimpleLevelEventActiveSkill {

    public BerserkerCharge(int levelMax, int requiredClassLevel) {
        super("berserkercharge", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 14000;
    }

    @Override
    public float consumedStaminaBase() {
        return 0.5F;
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        Point2D.Float dir = getDir(player);
        return new BerserkerChargeLevelEvent(player, seed, dir.x, dir.y, 200, 200, new GameDamage(DamageTypeRegistry.MELEE, 10 * playerData.getLevel() + 2 * playerData.getStrength(player) * activeSkillLevel + 2 * playerData.getSpeed(player) * activeSkillLevel));
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return BerserkerChargeLevelEvent.class;
    }

    public static class BerserkerChargeLevelEvent extends MobDashLevelEvent {
        public BerserkerChargeLevelEvent() {
        }

        public BerserkerChargeLevelEvent(Mob owner, int seed, float dirX, float dirY, float distance, int animTime, GameDamage damage) {
            super(owner, seed, dirX, dirY, distance, animTime, damage);
        }

        @Override
        public void init() {
            super.init();
            if (this.level != null && this.level.isClient() && this.owner != null) {
                float forceMod = Math.min((float) this.animTime / 700.0F, 1.0F);
                float forceX = this.dirX * this.distance * forceMod;
                float forceY = this.dirY * this.distance * forceMod;

                for (int i = 0; i < 30; ++i) {
                    this.level.entityManager.addParticle(this.owner.x + (float) GameRandom.globalRandom.nextGaussian() * 15.0F + forceX / 5.0F, this.owner.y + (float) GameRandom.globalRandom.nextGaussian() * 20.0F + forceY / 5.0F, Particle.GType.IMPORTANT_COSMETIC).movesConstant(forceX * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F, forceY * GameRandom.globalRandom.getFloatBetween(0.8F, 1.2F) / 5.0F).color(GameRandom.globalRandom.getOneOf(new Color(255, 102, 102), new Color(204, 255, 255))).height(18.0F).lifeTime(700);
                }

                SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(this.owner).volume(0.2F).pitch(2.5F));
            }

            if (this.owner != null) {
                this.owner.buffManager.addBuff(new ActiveBuff(BuffRegistry.INVULNERABLE_ACTIVE, this.owner, this.animTime, null), false);
                this.owner.addBuff(new ActiveBuff(AphBuffs.SABER_DASH_ACTIVE, this.owner, this.animTime, null), false);
            }

        }

        @Override
        public Shape getHitBox() {
            Point2D.Float dir;
            if (this.owner.getDir() == 3) {
                dir = GameMath.getPerpendicularDir(-this.dirX, -this.dirY);
            } else {
                dir = GameMath.getPerpendicularDir(this.dirX, this.dirY);
            }

            float width = 40.0F;
            float frontOffset = 60.0F;
            float range = 40.0F;
            float rangeOffset = -30.0F;
            return new LineHitbox(this.owner.x + dir.x * rangeOffset + this.dirX * frontOffset, this.owner.y + dir.y * rangeOffset + this.dirY * frontOffset, dir.x, dir.y, range, width);
        }
    }
}

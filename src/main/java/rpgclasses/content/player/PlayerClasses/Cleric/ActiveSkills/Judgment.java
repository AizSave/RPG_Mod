package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.levelevents.RPGExplosionLevelEvent;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;

import java.awt.*;

public class Judgment extends SimpleLevelEventActiveSkill {

    public Judgment(int levelMax, int requiredClassLevel) {
        super("judgment", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new JudgmentLevelEvent(player.x, player.y, 200, new GameDamage(RPGDamageType.HOLY, 4 * playerData.getLevel() + 2 * activeSkillLevel * (playerData.getIntelligence(player) + playerData.getGrace(player))), player);
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return JudgmentLevelEvent.class;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 20 + activeSkillLevel * 4;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage", "constrained", "manausage"};
    }

    public static class JudgmentLevelEvent extends RPGExplosionLevelEvent {
        public JudgmentLevelEvent() {
            super();
        }

        public JudgmentLevelEvent(float x, float y, int range, GameDamage damage, Mob owner) {
            super(x, y, range, damage, owner, false, new Color(255, 255, 155), new Color(200, 200, 20));
        }

        @Override
        protected void playExplosionEffects() {
            SoundManager.playSound(GameResources.glyphTrapCharge, SoundEffect.effect(this.x, this.y).volume(3F).pitch(0.5F));
            this.level.getClient().startCameraShake(this.x, this.y, 200, 40, 1F, 0.8F, true);
        }

        @Override
        protected void onMobWasHit(Mob mob, float distance) {
            super.onMobWasHit(mob, distance);
            if (MobData.isWeakToHoly(mob, ownerMob)) {
                mob.buffManager.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, mob, 5000, null), mob.isServer());
            }
        }
    }

}

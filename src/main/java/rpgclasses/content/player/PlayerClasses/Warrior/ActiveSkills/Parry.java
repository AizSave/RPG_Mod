package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGColors;

public class Parry extends SimpleBuffActiveSkill {

    public Parry(int levelMax, int requiredClassLevel) {
        super("parry", RPGColors.HEX.gold, levelMax, requiredClassLevel);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(1F).pitch(1.2F));

        ParticleTypeSwitcher particleTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.CRITICAL, Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC);
        for (int i = 0; i < 20; ++i) {
            int angle = (int) (360.0F + GameRandom.globalRandom.nextFloat() * 360.0F);
            float dx = (float) Math.sin(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
            float dy = (float) Math.cos(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
            player.getLevel().entityManager.addParticle(player, particleTypeSwitcher.next()).movesFriction(dx, dy, 0.8F).color(RPGColors.gold).heightMoves(10.0F, 30.0F).lifeTime(0);
        }

    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new ActiveSkillBuff() {
            @Override
            public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
                super.onBeforeHit(activeBuff, event);
                if (!event.isPrevented() && event.damage.damage > 0 && activeBuff.owner.isServer() && event.attacker.getAttackOwner() != null) {
                    event.attacker.getAttackOwner().isServerHit(new GameDamage(DamageTypeRegistry.TRUE, 5F * event.damage.damage), activeBuff.owner.x, activeBuff.owner.y, 100, activeBuff.owner);
                    event.prevent();
                    event.showDamageTip = false;
                    event.playHitSound = false;
                }
            }
        };
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return 500;
    }

    @Override
    public int getBaseCooldown() {
        return 8000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -1000;
    }

    @Override
    public float consumedStaminaBase() {
        return 1F;
    }
}

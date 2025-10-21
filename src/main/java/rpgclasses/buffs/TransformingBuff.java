package rpgclasses.buffs;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameMath;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleTranformationActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.registry.RPGModifiers;

import java.awt.*;
import java.util.Objects;

public class TransformingBuff extends Buff {

    public ParticleTypeSwitcher particleTypeSwitcher;

    public TransformingBuff() {
        this.isImportant = true;
        this.canCancel = false;
        this.particleTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.addModifier(BuffModifiers.SPEED, -0.75F);
        activeBuff.addModifier(BuffModifiers.INTIMIDATED, true);
        activeBuff.addModifier(RPGModifiers.NO_SKILLS, true);
    }

    @Override
    public void onWasHit(ActiveBuff activeBuff, MobWasHitEvent event) {
        super.onWasHit(activeBuff, event);
        if (event.damage > 0 && !event.wasPrevented && activeBuff.owner.isPlayer) {
            PlayerMob player = (PlayerMob) activeBuff.owner;
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            playerData.removeInUseActiveSkillSlot();
            player.buffManager.removeBuff(getID(), false);
        }
    }

    @Override
    public void onRemoved(ActiveBuff activeBuff) {
        super.onRemoved(activeBuff);
        if (activeBuff.owner.isPlayer && activeBuff.owner.mount == -1) {
            if (activeBuff.owner.isServer()) {
                PlayerMob player = (PlayerMob) activeBuff.owner;

                int transformationType = activeBuff.getGndData().getInt("transformationType");
                String transformation = activeBuff.getGndData().getString("transformation");
                if (transformationType == 0) {
                    PlayerData playerData = PlayerDataList.getPlayerData(player);
                    ActiveSkill equippedActiveSkill = playerData.getInUseActiveSkill();

                    if (equippedActiveSkill instanceof SimpleTranformationActiveSkill && Objects.equals(equippedActiveSkill.stringID, transformation)) {
                        ((SimpleTranformationActiveSkill) equippedActiveSkill).transform(player);
                    }
                }
            }
            if (activeBuff.owner.isClient()) {
                SoundManager.playSound(GameResources.swoosh, SoundEffect.effect(activeBuff.owner).volume(0.35F).pitch(1F));
            }
        }
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        float chance = 2 - ((float) activeBuff.getDurationLeft() / activeBuff.getDuration()) * 2;
        int guaranteed = (int) chance;
        int times = guaranteed + (GameRandom.globalRandom.getChance(chance - guaranteed) ? 1 : 0);

        for (int i = 0; i < times; i++) {
            Color color = new Color(activeBuff.getGndData().getInt("particlesColor"));
            int angle = (int) (360.0F + GameRandom.globalRandom.nextFloat() * 360.0F);
            float dx = (float) Math.sin(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);
            float dy = (float) Math.cos(Math.toRadians(angle)) * (float) GameRandom.globalRandom.getIntBetween(30, 50);

            float variation = GameRandom.globalRandom.getFloatOffset(1, 0.2F);
            activeBuff.owner.getLevel().entityManager.addParticle(activeBuff.owner.x - dx + activeBuff.owner.dx, activeBuff.owner.y - dy + activeBuff.owner.dy, particleTypeSwitcher.next()).movesFriction(dx, dy, 0.8F).color(
                    new Color(
                            GameMath.limit((int) (color.getRed() * variation), 0, 255),
                            GameMath.limit((int) (color.getGreen() * variation), 0, 255),
                            GameMath.limit((int) (color.getBlue() * variation), 0, 255),
                            color.getAlpha()
                    )
            ).heightMoves(10.0F, 20.0F, 5F, 0F, 10F, 0F).lifeTime(250);
        }
    }
}

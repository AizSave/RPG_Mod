package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.utils.area.AphAreaList;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobAbilityLevelEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.EntityDrawable;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.regionSystem.RegionPosition;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;
import rpgclasses.utils.RPGArea;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WrathOfLight extends SimpleLevelEventActiveSkill {

    public WrathOfLight(int levelMax, int requiredClassLevel) {
        super("wrathoflight", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }


    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        Mob target = RPGUtils.findBestTarget(player, 500);

        if (target != null) {
            float damage = 5 * playerData.getLevel() + 3 * activeSkillLevel * (playerData.getIntelligence(player) + playerData.getGrace(player));
            if (MobData.isWeakToHoly(target, player)) damage *= 2;
            target.isServerHit(new GameDamage(RPGDamageType.HOLY, damage), player.x, player.y, 0, player);
            RPGBuffs.applyStun(target, 500);
            return new WrathOfLightLevelEvent(player, target.getX(), target.getY(), (2 * playerData.getLevel() + activeSkillLevel * (playerData.getIntelligence(player) + playerData.getGrace(player))) / 2);
        }
        return null;
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return WrathOfLightLevelEvent.class;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 40 + activeSkillLevel * 8;
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 500) ? null : "notarget";
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage", "constrained", "manausage"};
    }

    public static class WrathOfLightLevelEvent extends MobAbilityLevelEvent implements Attacker {
        private int lifeTime = 0;
        public int nextHit = 0;

        public int targetX;
        public int targetY;
        public float damage;
        public int hits;

        public WrathOfLightLevelEvent() {
        }

        public WrathOfLightLevelEvent(Mob owner, int targetX, int targetY, float damage) {
            super(owner, new GameRandom());
            this.targetX = targetX;
            this.targetY = targetY;
            this.damage = damage;
            this.hits = 0;
        }

        @Override
        public void init() {
            super.init();
            if (isClient()) {
                SoundManager.playSound(GameResources.glyphChargeUp, SoundEffect.effect(targetX, targetY).volume(10F).pitch(0.5F));
                owner.getLevel().entityManager.addParticle(new WrathOfLightParticle(owner.getLevel(), targetX, targetY, 5000), Particle.GType.CRITICAL);
            }
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextShortUnsigned(this.lifeTime);
            writer.putNextShortUnsigned(this.nextHit);

            writer.putNextInt(this.targetX);
            writer.putNextInt(this.targetY);
            writer.putNextFloat(this.damage);
            writer.putNextInt(this.hits);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            this.lifeTime = reader.getNextShortUnsigned();
            this.nextHit = reader.getNextShortUnsigned();

            this.targetX = reader.getNextInt();
            this.targetY = reader.getNextInt();
            this.damage = reader.getNextFloat();
            this.hits = reader.getNextInt();
        }

        @Override
        public void clientTick() {
            super.clientTick();
            lifeTime += 50;

            if (hits < 10) {
                if (lifeTime >= hits * 500) clientHit();
            } else {
                clientHit();
                this.over();
            }
        }

        @Override
        public void serverTick() {
            super.serverTick();
            lifeTime += 50;

            if (hits < 10) {
                if (lifeTime >= hits * 500) serverHit();
            } else {
                serverHit();
                this.over();
            }
        }

        public void serverHit() {
            hits++;

            RPGUtils.getAllTargets(owner, 200)
                    .forEach(
                            mob -> {
                                mob.isServerHit(new GameDamage(RPGDamageType.HOLY, damage), targetX, targetY, -20, owner);
                                if (MobData.isWeakToHoly(mob, owner)) {
                                    mob.buffManager.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, mob, 1100, null), true);
                                }
                            }
                    );
        }

        public void clientHit() {
            hits++;

            new AphAreaList(
                    new RPGArea(200, new Color(255, 255, 102))
            ).setOnlyVision(false).executeClient(owner.getLevel(), targetX, targetY);
        }

        @Override
        public void clientHit(Mob target, Packet content) {
        }
    }

    public static class WrathOfLightParticle extends Particle {
        public WrathOfLightParticle(Level level, float x, float y, long lifeTime) {
            super(level, x, y, lifeTime);
        }

        @Override
        public void clientTick() {
            super.clientTick();
            this.getLevel().lightManager.refreshParticleLightFloat(this.x, this.y, 0.13F, 1.0F);
        }

        @Override
        public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            if (!this.removed()) {
                GameTexture texture = RPGResources.PARTICLE_TEXTURES.wrathOfLight;

                int drawX = camera.getDrawX(this.x) - texture.getWidth() / 2;
                int drawY = camera.getDrawY(this.y) - texture.getHeight();

                final TextureDrawOptions options = texture.initDraw().pos(drawX, drawY);
                list.add(new EntityDrawable(this) {
                    public void draw(TickManager tickManager) {
                        options.draw();
                    }
                });
            }
        }
    }

}
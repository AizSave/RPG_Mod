package rpgclasses.content.player.PlayerClasses.Wizard.Passives;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketLevelEvent;
import necesse.engine.registries.LevelEventRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.mobAbilityLevelEvent.GroundEffectEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.particle.Particle;
import necesse.inventory.item.Item;
import necesse.level.maps.LevelObjectHit;
import rpgclasses.buffs.IgnitedBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.ArrayList;

public class FlamingSteps extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(3),
            SkillParam.damageParam(0.2F)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public static ArrayList<String> sprintBuffIDs = new ArrayList<>();

    static {
        sprintBuffIDs.add("leatherdashersactive");
        sprintBuffIDs.add("zephyrbootsactive");
        sprintBuffIDs.add("windbootsactive");
        sprintBuffIDs.add("ghostbootsactive");
        sprintBuffIDs.add("kineticbootsactive");
    }

    public FlamingSteps(int levelMax, int requiredClassLevel) {
        super("flamingsteps", "#ff6600", levelMax, requiredClassLevel);
    }

    @Override
    public void registry() {
        super.registry();
        LevelEventRegistry.registerEvent("flamingstepsevent", FlamingStepsLevelEvent.class);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.getGndData().setInt("time", 0);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                tick(activeBuff);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                tick(activeBuff);
            }

            public void tick(ActiveBuff activeBuff) {
                boolean sprinting = false;
                for (String sprintBuffID : sprintBuffIDs) {
                    if (activeBuff.owner.buffManager.hasBuff(sprintBuffID)) {
                        sprinting = true;
                        break;
                    }
                    Mob mount = activeBuff.owner.getMount();
                    if (mount != null && mount.buffManager.hasBuff(sprintBuffID)) {
                        sprinting = true;
                        break;
                    }
                }

                if (activeBuff.owner.isServer() && sprinting) {
                    LevelEvent event = new FlamingStepsLevelEvent(activeBuff.owner, activeBuff.owner.getX(), activeBuff.owner.getY(), getLevel(activeBuff), new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));
                    activeBuff.owner.getLevel().entityManager.events.addHidden(event);
                    activeBuff.owner.getServer().network.sendToClientsWithEntity(new PacketLevelEvent(event), event);
                }

                isVisible = sprinting;
            }

        };
    }

    public static class FlamingStepsLevelEvent extends GroundEffectEvent {
        protected int tickCounter;
        public int skillLevel;

        public FlamingStepsLevelEvent() {
        }

        public FlamingStepsLevelEvent(Mob owner, int x, int y, int skillLevel, GameRandom uniqueIDRandom) {
            super(owner, x, y, uniqueIDRandom);
            this.skillLevel = skillLevel;
        }

        @Override
        public void setupSpawnPacket(PacketWriter writer) {
            super.setupSpawnPacket(writer);
            writer.putNextInt(skillLevel);
        }

        @Override
        public void applySpawnPacket(PacketReader reader) {
            super.applySpawnPacket(reader);
            skillLevel = reader.getNextInt();
        }

        @Override
        public void init() {
            super.init();
            this.tickCounter = 0;
        }

        @Override
        public void clientTick() {
            super.clientTick();

            if (GameRandom.globalRandom.getChance(skillLevel * 0.1F)) {
                this.getLevel().entityManager.addParticle(x + GameRandom.globalRandom.getFloatOffset(0F, 16F), y + GameRandom.globalRandom.getFloatOffset(0F, 16F), Particle.GType.IMPORTANT_COSMETIC).color(
                        GameRandom.globalRandom.getOneOf(
                                new Color(255, 51, 0),
                                new Color(255, 102, 0),
                                new Color(255, 153, 0)
                        )
                ).heightMoves(0F, GameRandom.globalRandom.getFloatBetween(16, 24F));
            }

            ++this.tickCounter;
            if (this.tickCounter > 40) {
                this.over();
            } else {
                super.clientTick();
            }
        }

        @Override
        public void serverTick() {
            super.serverTick();
            ++this.tickCounter;
            if (this.tickCounter > 40) {
                this.over();
            } else {
                super.serverTick();
            }
        }

        @Override
        public void serverHit(Mob target, boolean clientSubmitted) {
            PlayerMob player = (PlayerMob) owner;
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            IgnitedBuff.apply(owner, target, params[1].value(playerData.getLevel(), skillLevel), params[0].value(), false);
        }

        @Override
        public void clientHit(Mob mob) {
        }

        @Override
        public Shape getHitBox() {
            int width = 24;
            int height = 24;
            return new Rectangle(this.x - width / 2, this.y - height / 2, width, height);
        }

        @Override
        public void hitObject(LevelObjectHit hit) {
        }
    }

}

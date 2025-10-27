package rpgclasses.content.player.Mastery.MasterySkills;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.MobExtraDrawBuff;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.buffs.Skill.SecondaryMasteryBuff;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.packets.PacketMobResetBuffTime;

import java.awt.*;
import java.util.LinkedList;

public class IronInvoker extends Mastery {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(5)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public IronInvoker(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return null;
    }

    @Override
    public SecondaryMasteryBuff secondaryMasteryBuff() {
        return new SummonMagicShieldBuff();
    }

    public static class SummonMagicShieldBuff extends SecondaryMasteryBuff implements MobExtraDrawBuff {
        public GameTexture starBarrierTexture;

        public void loadTextures() {
            super.loadTextures();
            this.starBarrierTexture = GameTexture.fromFile("particles/starbarrier");
        }


        public SummonMagicShieldBuff() {
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            activeBuff.getGndData().setBoolean("ready", true);
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            super.serverTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;
                if (time >= (int) (params[0].value() * 1000)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                }
                activeBuff.getGndData().setInt("time", time);
            }
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            super.serverTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                if (time == 0) {
                    SoundManager.playSound(GameResources.shatter2, SoundEffect.effect(activeBuff.owner).volume(1F).pitch(0.8F));
                }
                time += 50;
                if (time >= (int) (params[0].value() * 1000)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                    SoundManager.playSound(GameResources.cling, SoundEffect.effect(activeBuff.owner).volume(0.5F));
                    SoundManager.playSound(GameResources.jingle, SoundEffect.effect(activeBuff.owner).volume(0.5F));
                }
                activeBuff.getGndData().setInt("time", time);
            }

        }

        @Override
        public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
            super.onBeforeHitCalculated(activeBuff, event);
            if (activeBuff.getGndData().getBoolean("ready") && !event.isPrevented()) {
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;

                activeBuff.getGndData().setBoolean("ready", false);
                activeBuff.getGndData().setInt("time", 0);
                activeBuff.owner.getServer().network.sendToClientsAtEntireLevel(new PacketMobResetBuffTime(activeBuff.owner.getUniqueID(), activeBuff.buff.getStringID()), activeBuff.owner.getLevel());
            }
        }

        @Override
        public void addBackDrawOptions(ActiveBuff activeBuff, LinkedList<DrawOptions> list, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        }

        @Override
        public void addFrontDrawOptions(ActiveBuff activeBuff, LinkedList<DrawOptions> list, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
            if (perspective == null) return;
            if (activeBuff.getGndData().getBoolean("ready")) {
                Rectangle selectBox = activeBuff.owner.getSelectBox(x, y);

                int size = Math.max(selectBox.width, selectBox.height);

                int modX = size - selectBox.width;
                int modY = size - selectBox.height;

                int drawX = camera.getDrawX(selectBox.x - modX / 2F);
                int drawY = camera.getDrawY(selectBox.y - modY / 2F);

                list.add(
                        this.starBarrierTexture.initDraw().sprite((int) (perspective.getLocalTime() / 100L) % 4, 0, 64).size(size, size).color(new Color(102, 102, 102)).pos(drawX, drawY).alpha(0.6F)
                );
            }
        }
    }
}
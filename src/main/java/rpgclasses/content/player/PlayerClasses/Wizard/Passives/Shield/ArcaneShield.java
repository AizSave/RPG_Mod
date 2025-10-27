package rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield;

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
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.PacketMobResetBuffTime;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.LinkedList;

public class ArcaneShield extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("20 - <skilllevel>")
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public ArcaneShield(int levelMax, int requiredClassLevel) {
        super("arcaneshield", "#6633ff", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new MagicShieldBuff(getColor(), params[0], null);
    }

    public static class MagicShieldBuff extends PrincipalPassiveBuff implements MobExtraDrawBuff {
        public GameTexture starBarrierTexture;

        public void loadTextures() {
            super.loadTextures();
            this.starBarrierTexture = GameTexture.fromFile("particles/starbarrier");
        }

        public Color shieldColor;
        public SkillParam cooldownParam;
        public int cooldownNumber;
        public RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent;

        public MagicShieldBuff(Color shieldColor, SkillParam cooldownParam, RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent) {
            this.shieldColor = shieldColor;
            this.onPrevent = onPrevent;

            this.cooldownParam = cooldownParam;
            this.cooldownNumber = 0;
        }

        public MagicShieldBuff(Color shieldColor, int cooldown, RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent) {
            this.shieldColor = shieldColor;
            this.onPrevent = onPrevent;

            this.cooldownParam = null;
            this.cooldownNumber = cooldown;
        }

        public MagicShieldBuff(Color shieldColor, float cooldown, RPGUtils.TriRunnable<ActiveBuff, Integer, PlayerData> onPrevent) {
            this(shieldColor, (int) (cooldown * 1000), onPrevent);
        }

        public int getCooldown(ActiveBuff activeBuff) {
            return cooldownNumber == 0 ? (int) (cooldownParam.value(getLevel(activeBuff)) * 1000) : cooldownNumber;
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            activeBuff.getGndData().setInt("time", 50);
        }

        @Override
        public void serverTick(ActiveBuff activeBuff) {
            super.serverTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;
                if (time >= getCooldown(activeBuff)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                }
                activeBuff.getGndData().setInt("time", time);
            }
        }

        @Override
        public void clientTick(ActiveBuff activeBuff) {
            super.clientTick(activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0);
                if (time == 0) {
                    SoundManager.playSound(GameResources.shatter2, SoundEffect.effect(activeBuff.owner).volume(2.0F).pitch(0.8F));
                }
                time += 50;
                if (time >= getCooldown(activeBuff)) {
                    activeBuff.getGndData().setBoolean("ready", true);
                    SoundManager.playSound(GameResources.cling, SoundEffect.effect(activeBuff.owner).volume(1F));
                    SoundManager.playSound(GameResources.jingle, SoundEffect.effect(activeBuff.owner).volume(1F));
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

                PlayerMob player = (PlayerMob) activeBuff.owner;

                activeBuff.getGndData().setBoolean("ready", false);
                activeBuff.getGndData().setInt("time", 0);
                player.getServer().network.sendToClientsAtEntireLevel(new PacketMobResetBuffTime(player.getUniqueID(), activeBuff.buff.getStringID()), player.getLevel());

                if (onPrevent != null)
                    onPrevent.run(activeBuff, getLevel(activeBuff), PlayerDataList.getPlayerData(player));
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
                        this.starBarrierTexture.initDraw().sprite((int) (perspective.getLocalTime() / 100L) % 4, 0, 64).size(size, size).color(shieldColor).pos(drawX, drawY).alpha(0.6F)
                );
            }
        }

        @Override
        public void drawIcon(int x, int y, ActiveBuff activeBuff) {
            super.drawIcon(x, y, activeBuff);
            if (!activeBuff.getGndData().getBoolean("ready")) {
                int time = activeBuff.getGndData().getInt("time", 0) - 50;
                String text = EquippedActiveSkill.getTimeLeftString(getCooldown(activeBuff) - time);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }
        }
    }
}

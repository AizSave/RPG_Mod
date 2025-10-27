package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import aphorea.registry.AphModifiers;
import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameUtils;
import necesse.entity.mobs.MobBeforeHitCalculatedEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.MobExtraDrawBuff;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.buffs.Skill.ActiveSkillBuff;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleBuffActiveSkill;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.LinkedList;

public class HolyGuard extends SimpleBuffActiveSkill {
    public static SkillParam[] params = new SkillParam[]{
            new SkillParam("6 x <skilllevel>"),
            new SkillParam("10 x <skilllevel>")
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    @Override
    public SkillParam getManaParam() {
        return SkillParam.manaParam(20, false);
    }

    public HolyGuard(int levelMax, int requiredClassLevel) {
        super("holyguard", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public void giveBuffOnRun(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        super.giveBuff(player, player, activeSkillLevel);

        GameUtils.streamServerClients(player.getLevel()).forEach(targetPlayer -> {
            if (targetPlayer.isSameTeam(player.getTeam()))
                super.giveBuff(player, targetPlayer.playerMob, activeSkillLevel);
        });

        RPGUtils.streamMobsAndPlayers(player, 200)
                .filter(m -> m == player || m.isSameTeam(player))
                .forEach(
                        target -> super.giveBuff(player, target, activeSkillLevel)
                );
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.cling, SoundEffect.effect(player.x, player.y).volume(2F).pitch(1F));
        AphAreaList areaList = new AphAreaList(
                new AphArea(200, getColor())
        ).setOnlyVision(false);
        areaList.executeClient(player.getLevel(), player.x, player.y);
    }

    @Override
    public int getDuration(int activeSkillLevel) {
        return (int) (params[0].value() * 1000);
    }

    @Override
    public int getBaseCooldown(PlayerMob player) {
        return 30000;
    }

    @Override
    public int getCooldownModPerLevel() {
        return -6000;
    }

    @Override
    public ActiveSkillBuff getBuff() {
        return new HolyBuff();
    }

    public class HolyBuff extends ActiveSkillBuff implements MobExtraDrawBuff {
        public GameTexture starBarrierTexture;

        public void loadTextures() {
            super.loadTextures();
            this.starBarrierTexture = GameTexture.fromFile("particles/starbarrier");
        }

        @Override
        public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
            int level = getLevel(activeBuff);
            activeBuff.setModifier(AphModifiers.MAGIC_HEALING_RECEIVED, params[1].value(level));
        }

        @Override
        public void onBeforeHitCalculated(ActiveBuff activeBuff, MobBeforeHitCalculatedEvent event) {
            super.onBeforeHitCalculated(activeBuff, event);
            if (!event.isPrevented()) {
                event.prevent();
                event.showDamageTip = false;
                event.playHitSound = false;

                activeBuff.owner.buffManager.removeBuff(activeBuff.buff, true);
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
                        this.starBarrierTexture.initDraw().sprite((int) (perspective.getLocalTime() / 100L) % 4, 0, 64).size(size, size).color(getColor()).pos(drawX, drawY).alpha(0.6F)
                );
            }
        }
    }
}

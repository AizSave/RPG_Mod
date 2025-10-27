package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.area.AphAreaList;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.gameFont.FontManager;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.packets.PacketMobResetBuffTime;
import rpgclasses.utils.RPGArea;
import rpgclasses.utils.RPGColors;

public class VitalAura extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.staticParam(10),
            SkillParam.healingParam(1)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public VitalAura(int levelMax, int requiredClassLevel) {
        super("vitalaura", "#00ff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;

                if (time > (int) (params[0].value() * 1000)) {
                    time = 0;

                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    PlayerData playerData = PlayerDataList.getPlayerData(player);

                    AphAreaList areaList = new AphAreaList(
                            new RPGArea(150, RPGColors.green)
                                    .setAttackerHealthMod(0.5F)
                                    .setHealingArea(params[1].valueInt(playerData.getLevel(), getLevel(activeBuff)))
                    ).setOnlyVision(false);
                    areaList.execute(player, true);

                    player.getServer().network.sendToClientsAtEntireLevel(new PacketMobResetBuffTime(player.getUniqueID(), getBuffStringID()), player.getLevel());
                }

                activeBuff.getGndData().setInt("time", time);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0);
                time += 50;
                activeBuff.getGndData().setInt("time", time);
            }

            @Override
            public void drawIcon(int x, int y, ActiveBuff activeBuff) {
                super.drawIcon(x, y, activeBuff);
                int time = activeBuff.getGndData().getInt("time", 0) - 50;
                String text = EquippedActiveSkill.getTimeLeftString((params[0].valueInt(getLevel(activeBuff)) * 1000) - time);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }

        };
    }

}

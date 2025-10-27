package rpgclasses.content.player.SkillsLogic.ActiveSkills;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;

import java.util.List;

abstract public class CastActiveSkill extends ActiveSkill {

    public CastActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        super(stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void addInfoTooltips(List<String> tooltips) {
        super.addInfoTooltips(tooltips);

        float rawCast = castTime();
        float seconds = rawCast / 1000f;
        String formattedCast = (seconds == (int) seconds)
                ? Integer.toString((int) seconds)
                : String.format("%.2f", seconds);

        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "activeskillcast", "seconds", formattedCast));
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);
        if (isInUse) {
            castedRunServer(player, playerData, activeSkillLevel, seed);
        } else {
            if (player.buffManager.getModifier(RPGModifiers.CAST_TIME) > 0F) {
                ActiveBuff ab = new ActiveBuff(RPGBuffs.CASTING, player, (int) (castTime() * player.buffManager.getModifier(RPGModifiers.CAST_TIME)), null);
                ab.getGndData().setInt("particlesColor", getColorInt());
                player.buffManager.addBuff(ab, true);
            } else {
                castedRunServer(player, playerData, activeSkillLevel, seed);
                playerData.getInUseActiveSkillSlot().startCooldown(player, playerData, player.getTime(), activeSkillLevel);
            }
        }
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        if (isInUse) {
            castedRunClient(player, playerData, activeSkillLevel, seed);
        } else {
            if (player.buffManager.getModifier(RPGModifiers.CAST_TIME) <= 0F) {
                castedRunClient(player, playerData, activeSkillLevel, seed);
                playerData.getInUseActiveSkillSlot().startCooldown(player, playerData, player.getTime(), activeSkillLevel);
            }
        }
    }

    public void castedRunServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        castedRun(player, playerData, activeSkillLevel, seed);
    }

    public void castedRunClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
        castedRun(player, playerData, activeSkillLevel, seed);
    }

    public void castedRun(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed) {
    }

    @Override
    public boolean isInUseSkill() {
        return true;
    }

    public int castTime() {
        return 3000;
    }
}

package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.data.PlayerDataList;

import java.io.FileNotFoundException;

public class PrincipalPassiveBuff extends PassiveSkillBuff {
    public PrincipalPassiveBuff() {
    }

    @Override
    public void loadTextures() {
        try {
            this.iconTexture = GameTexture.fromFileRaw("passives/" + skillID());
        } catch (FileNotFoundException var2) {
            this.iconTexture = GameTexture.fromFile("buffs/positive");
        }
    }

    @Override
    public void updateLocalDisplayName() {
        this.displayName = new LocalMessage("passives", skillID());
    }

    public String skillID() {
        return this.getStringID().replace("passivebuff", "");
    }

    public int getPlayerLevel(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getLevel();
    }

    public float getEndurance(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getEndurance(player);
    }

    public float getSpeed(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getSpeed(player);
    }

    public float getStrength(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getStrength(player);
    }

    public float getIntelligence(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getIntelligence(player);
    }

    public float getGrace(PlayerMob player) {
        return PlayerDataList.getPlayerData(player).getGrace(player);
    }

}
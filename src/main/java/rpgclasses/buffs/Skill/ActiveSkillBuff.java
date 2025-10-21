package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.gfx.gameTexture.GameTexture;

import java.io.FileNotFoundException;

public class ActiveSkillBuff extends SkillBuff {
    public ActiveSkillBuff() {
        this.canCancel = false;
        this.isImportant = true;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
    }

    @Override
    public void loadTextures() {
        try {
            this.iconTexture = GameTexture.fromFileRaw("activeskills/" + skillID());
        } catch (FileNotFoundException var2) {
            this.iconTexture = GameTexture.fromFile("buffs/positive");
        }
    }

    @Override
    public void updateLocalDisplayName() {
        this.displayName = new LocalMessage("activeskills", skillID());
    }

    public String skillID() {
        return this.getStringID().replace("activeskillbuff", "");
    }

    public float getEndurance(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("endurance");
    }

    public float getSpeed(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("speed");
    }

    public float getStrength(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("strength");
    }

    public float getIntelligence(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("intelligence");
    }

    public float getGrace(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getFloat("grace");
    }
}
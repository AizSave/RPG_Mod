package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.gfx.gameTexture.GameTexture;

import java.io.FileNotFoundException;

public class MasteryBuff extends PrincipalPassiveBuff {
    public MasteryBuff() {
        isVisible = false;
    }

    @Override
    public void loadTextures() {
        try {
            this.iconTexture = GameTexture.fromFileRaw("mastery/" + masteryID());
        } catch (FileNotFoundException var2) {
            this.iconTexture = GameTexture.fromFile("buffs/positive");
        }
    }

    @Override
    public void updateLocalDisplayName() {
        this.displayName = new LocalMessage("mastery", masteryID());
    }

    public String masteryID() {
        return this.getStringID().replace("masterybuff", "");
    }

}
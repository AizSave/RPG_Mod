package rpgclasses.buffs.Skill;

import necesse.engine.localization.message.LocalMessage;
import necesse.gfx.gameTexture.GameTexture;

import java.io.FileNotFoundException;

public class SecondaryMasteryBuff extends SecondaryPassiveBuff {
    public SecondaryMasteryBuff() {
    }

    @Override
    public void loadTextures() {
        try {
            this.iconTexture = GameTexture.fromFileRaw("mastery/" + skillID());
        } catch (FileNotFoundException var2) {
            this.iconTexture = GameTexture.fromFile("buffs/positive");
        }
    }

    @Override
    public void updateLocalDisplayName() {
        this.displayName = new LocalMessage("mastery", skillID());
    }

    public String skillID() {
        return this.getStringID().replace("2masterybuff", "");
    }
}
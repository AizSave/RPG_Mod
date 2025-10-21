package rpgclasses.forms.rpgmenu.components;

import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class PlayerDataComponent extends FormContentBox {
    public PlayerDataComponent(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void updateContent(PlayerMob player) {
        this.clearComponents();
        PlayerData playerData = PlayerDataList.getPlayerData(player);

        this.addComponent(new FormLocalLabel(
                new LocalMessage("ui", "level", "level", playerData.getLevel()),
                new FontOptions(12), -1, 0, 20
        ));
        this.addComponent(new FormLocalLabel(
                new LocalMessage("ui", "experience", "exp", playerData.getExpActual(), "nextexp", playerData.getExpNext()),
                new FontOptions(12), -1, 0, 40
        ));
        this.addComponent(new FormLocalLabel(
                new LocalMessage("ui", "totalexperience", "exp", playerData.getExp()),
                new FontOptions(12), -1, 0, 60
        ));
    }
}

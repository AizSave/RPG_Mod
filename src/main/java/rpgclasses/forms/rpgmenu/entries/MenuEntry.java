package rpgclasses.forms.rpgmenu.entries;

import necesse.engine.localization.Localization;
import necesse.engine.network.client.Client;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.forms.rpgmenu.MenuContainer;
import rpgclasses.forms.rpgmenu.MenuContainerForm;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;

abstract public class MenuEntry {
    public Client client;
    public final String name;
    public PlayerMob player;
    public PlayerData playerData;

    public MenuEntry(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (name.contains(".")) {
            String[] splitName = name.split("\\.");
            return Localization.translate(splitName[0], splitName[1]);
        } else {
            return Localization.translate("ui", name);
        }
    }

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        entryForm.clearComponents();
        playerData = PlayerDataList.getPlayerData(player);
    }

    public Color getTextColor(PlayerMob player) {
        return null;
    }

    public GameTexture getTexture() {
        return null;
    }
}

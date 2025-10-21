package rpgclasses.ui;

import necesse.engine.GlobalData;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormCustomButton;
import rpgclasses.RPGResources;
import rpgclasses.forms.rpgmenu.RPGMenuPacket;

import java.awt.*;

public class RPGMenuUIForm extends Form {

    public RPGMenuUIForm(String name, int width, int height) {
        super(name, width, height);
        this.addComponent(
                new FormCustomButton(0, 0, width, height) {
                    @Override
                    public void draw(Color color, int i, int i1, PlayerMob playerMob) {
                        RPGResources.UI_TEXTURES.rpgMenu_texture.initDraw().brightness(isHovering() ? 0.9F : 1).draw(2, 2);
                    }
                }.onClicked(c -> {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    client.network.sendPacket(new RPGMenuPacket());
                })
        );
    }
}

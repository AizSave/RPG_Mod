package rpgclasses.registry;

import necesse.engine.GlobalData;
import necesse.engine.input.Control;
import necesse.engine.input.InputEvent;
import necesse.engine.input.InputID;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import rpgclasses.forms.rpgmenu.RPGMenuPacket;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

public class RPGControls {

    public static Control TRANSFORMATION_ABILITY_1;
    public static Control TRANSFORMATION_ABILITY_2;

    public static int[] defaultSlotKeys = new int[]{
            -97, // 1
            -96, // 2
            InputID.KEY_KP_0, // 3
            InputID.KEY_KP_1, // 4
            InputID.KEY_KP_2, // 5
            InputID.KEY_KP_3, // 6
            InputID.KEY_KP_4, // 7
            InputID.KEY_KP_5, // 8
            InputID.KEY_KP_6, // 9
            InputID.KEY_KP_7, // 10
            InputID.KEY_KP_8, // 11
            InputID.KEY_KP_9, // 12
    };

    public static void registerCore() {

        Control.addModControl(new Control(InputID.KEY_K, "openrpgmenu") {
            @Override
            public void activate(InputEvent event) {
                super.activate(event);
                if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                    Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                    client.network.sendPacket(new RPGMenuPacket());
                }
            }
        });

        for (int i = 0; i < 12; i++) {
            final int slot = i;
            Control.addModControl(new Control(defaultSlotKeys[i], "activeskillslot" + (slot + 1)) {
                @Override
                public void activate(InputEvent event) {
                    super.activate(event);
                    if (isPressed() && GlobalData.getCurrentState() instanceof MainGame) {
                        Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
                        PlayerData playerData = PlayerDataList.getPlayerData(client.getPlayer());
                        playerData.equippedActiveSkills[slot].tryClientRun(client.getPlayer(), slot);
                    }
                }
            });
        }

        if (Control.EXPRESSION_WHEEL.getKey() == -1) {
            Control.EXPRESSION_WHEEL.changeKey(Control.EXPRESSION_WHEEL.defaultKey);
        }

        TRANSFORMATION_ABILITY_1 = Control.addModControl(new Control(InputID.LEFT_CLICK, "transformationability1"));

        TRANSFORMATION_ABILITY_2 = Control.addModControl(new Control(InputID.RIGHT_CLICK, "transformationability2"));
    }

}

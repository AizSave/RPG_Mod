package rpgclasses.ui;

import necesse.gfx.forms.MainGameFormManager;

public class RPGMenuUIManager extends CustomUIManager<RPGMenuUIForm> {

    public void setup(MainGameFormManager mainGameFormManager) {
        mainForm = mainGameFormManager.addComponent(new RPGMenuUIForm("rpgmenuui", 68, 68));
    }

    @Override
    public void frameTick(MainGameFormManager mainGameFormManager) {
        mainForm.setHidden(mainGameFormManager.inventory.isHidden());
    }

    public void updatePosition(MainGameFormManager mainGameFormManager) {
        mainForm.setPosition(
                mainGameFormManager.crafting.getX() + mainGameFormManager.crafting.getWidth() + 11 + 17,
                mainGameFormManager.crafting.getY() + mainGameFormManager.crafting.getHeight() - mainForm.getHeight()
        );
    }
}

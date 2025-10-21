package rpgclasses.ui;

import necesse.gfx.forms.MainGameFormManager;

public class CursorCenterUIManager extends CustomUIManager<CursorCenterUIForm> {
    public CursorCenterUIManager() {
    }

    public void setup(MainGameFormManager mainGameFormManager) {
        mainForm = mainGameFormManager.addComponent(new CursorCenterUIForm(0, 0));
        mainForm.zIndex = -1;
    }

    @Override
    public void frameTick(MainGameFormManager mainGameFormManager) {
        mainForm.setHidden(mainGameFormManager.toolbar.isHidden());
    }

    @Override
    public void updatePosition(MainGameFormManager mainGameFormManager) {
    }
}

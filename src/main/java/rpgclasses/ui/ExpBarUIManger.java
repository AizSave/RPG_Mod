package rpgclasses.ui;

import necesse.engine.eventStatusBars.EventStatusBarManager;
import necesse.gfx.forms.MainGameFormManager;
import rpgclasses.data.PlayerData;

import java.util.Collection;

public class ExpBarUIManger extends CustomUIManager<ExpBarUIForm> {
    public float barPercent;
    public boolean movedByInv = false;
    public boolean anyProgressBars = false;
    public boolean vertical = false;

    @Override
    public void setup(MainGameFormManager mainGameFormManager) {
        mainForm = mainGameFormManager.addComponent(new ExpBarUIForm("expbar", 408, 6));
    }

    @Override
    public void frameTick(MainGameFormManager mainGameFormManager) {
        boolean anyUpdate = false;
        if (mainGameFormManager.isInventoryHidden() == movedByInv) {
            movedByInv = !movedByInv;
            anyUpdate = true;
        }
        boolean newAnyProgressBars = anyProgressBars(EventStatusBarManager.getStatusBars());
        if (anyProgressBars != newAnyProgressBars) {
            anyUpdate = true;
            anyProgressBars = newAnyProgressBars;
        }
        if (anyUpdate) {
            updateExpBarPosition(mainGameFormManager);
        }
        mainForm.setHidden(mainGameFormManager.toolbar.isHidden() || (mainGameFormManager.inventory.isHidden() && newAnyProgressBars));
    }

    @Override
    public void updatePosition(MainGameFormManager mainGameFormManager) {
        updateExpBarPosition(mainGameFormManager);
    }

    public void updateExpBar(PlayerData playerData) {
        updateExpBar(playerData.getExpActual(), playerData.getExpNext());
    }

    public void updateExpBar(int expActual, int expNext) {
        barPercent = (float) expActual / expNext;
    }

    public void updateExpBarPosition(MainGameFormManager mainGameFormManager) {
        if (mainGameFormManager.inventory.isHidden()) {
            vertical = false;
            mainForm.setPosition(mainGameFormManager.toolbar.getX(), mainGameFormManager.toolbar.getY() - 17);
            mainForm.setWidth(408);
            mainForm.setHeight(6);
        } else {
            vertical = true;
            mainForm.setPosition(mainGameFormManager.crafting.getX() + mainGameFormManager.crafting.getWidth() + 11, mainGameFormManager.crafting.getY());
            mainForm.setWidth(6);
            mainForm.setHeight(mainGameFormManager.crafting.getHeight());
        }
    }

    private static boolean anyProgressBars(Iterable<?> iterable) {
        if (iterable instanceof Collection) {
            return !((Collection<?>) iterable).isEmpty();
        }

        for (Object ignored : iterable) {
            return true;
        }
        return false;
    }

}

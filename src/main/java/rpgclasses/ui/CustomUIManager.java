package rpgclasses.ui;

import necesse.gfx.forms.Form;
import necesse.gfx.forms.MainGameFormManager;
import rpgclasses.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

abstract public class CustomUIManager<T extends Form> {
    public static List<CustomUIManager<? extends Form>> formManagers = new ArrayList<>();
    public static MainGameFormManager mainGameFormManager;

    public static ExpBarUIManger expBar;
    public static RPGMenuUIManager rpgMenu;
    public static List<RPGSkillUIManager> rpgSkills = new ArrayList<>();

    public static CursorCenterUIManager cursorCenter;

    public static void setupAll(MainGameFormManager mainGameFormManager) {
        CustomUIManager.mainGameFormManager = mainGameFormManager;

        expBar = (ExpBarUIManger) addCustomForm(new ExpBarUIManger());
        rpgMenu = (RPGMenuUIManager) addCustomForm(new RPGMenuUIManager());
        for (int i = 0; i < PlayerData.EQUIPPED_SKILLS_MAX; i++) {
            rpgSkills.add((RPGSkillUIManager) addCustomForm(new RPGSkillUIManager(i)));
        }
        cursorCenter = (CursorCenterUIManager) addCustomForm(new CursorCenterUIManager());
    }

    public static CustomUIManager<? extends Form> addCustomForm(CustomUIManager<? extends Form> formManager) {
        formManager.setup(mainGameFormManager);
        formManager.updatePosition(mainGameFormManager);

        formManagers.add(formManager);
        return formManager;
    }

    public T mainForm = null;

    abstract public void setup(MainGameFormManager mainGameFormManager);

    abstract public void frameTick(MainGameFormManager mainGameFormManager);

    abstract public void updatePosition(MainGameFormManager mainGameFormManager);

}

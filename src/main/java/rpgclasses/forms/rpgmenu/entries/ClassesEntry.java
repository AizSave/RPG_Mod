package rpgclasses.forms.rpgmenu.entries;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.*;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.forms.rpgmenu.MenuContainer;
import rpgclasses.forms.rpgmenu.MenuContainerForm;
import rpgclasses.forms.rpgmenu.components.ClassComponent;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerDataList;
import rpgclasses.settings.RPGSettings;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassesEntry extends MenuEntry {

    public ClassesEntry() {
        super("classes");
    }

    @Override
    public Color getTextColor(PlayerMob player) {
        playerData = PlayerDataList.getPlayerData(player);

        if (playerData.totalClassPoints() < Arrays.stream(playerData.getClassLevels()).sum()) {
            int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
            if (style == 1) {
                return new Color(255, 0, 0);
            } else {
                return new Color(102, 0, 0);
            }
        }

        return null;
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        playerData = PlayerDataList.getPlayerData(player);
        updateContent(mainForm, entryForm, container, playerData.getResets(), playerData.getClassLevels(), playerData.getClassLevels().clone());
    }

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container, final int resetPoints, final int[] classes, int[] mutableClasses) {
        super.updateContent(mainForm, entryForm, container);

        entryForm.addComponent(new FormContentIconButton(entryForm.getWidth() - 32 - 8, 8, FormInputSize.SIZE_32, ButtonColor.BASE, MenuContainerForm.showClassIcons ? Settings.UI.button_shown_big : Settings.UI.button_hidden_big, new LocalMessage("settingsui", "showClassIcons"))
                .onClicked(c -> {
                    MenuContainerForm.showClassIcons = !MenuContainerForm.showClassIcons;
                    mainForm.updateEntries(container, player, null);
                    ((FormContentIconButton) c.from).setIcon(MenuContainerForm.showClassIcons ? Settings.UI.button_shown_big : Settings.UI.button_hidden_big);
                }));


        int numItems = PlayerClass.classesList.size();

        int maxClasses = playerData.totalClassPoints();

        int classesTotal = Arrays.stream(classes).sum();
        AtomicInteger mutableClassesTotal = new AtomicInteger(Arrays.stream(mutableClasses).sum());

        FormLabel classPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 20
        ));
        updateClassPoints(classPointsLabel, mutableClassesTotal.get(), 0);

        FormLabel resetPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 38
        ));
        updateResetPoints(resetPointsLabel, classes, mutableClasses, resetPoints);


        FormButton cancelButton = entryForm.addComponent(new FormLocalTextButton("ui", "cancelbutton", 0, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> this.updateContent(mainForm, entryForm, container, resetPoints, classes, classes.clone())));
        cancelButton.setActive(false);

        FormButton confirmButton = entryForm.addComponent(new FormLocalTextButton("ui", "confirmbutton", entryForm.getWidth() / 2 + 2, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> {
                    if (!player.isInCombat()) {
                        int reducedClasses = 0;
                        for (int i = 0; i < classes.length; i++) {
                            int difference = classes[i] - mutableClasses[i];
                            if (difference > 0) reducedClasses += difference;
                        }

                        this.updateContent(mainForm, entryForm, container, resetPoints - reducedClasses * 2, mutableClasses, mutableClasses.clone());
                        mainForm.updateEntries(container, player, mutableClasses);
                    }
                    container.updateClasses.runAndSend(mutableClasses);
                }));
        confirmButton.setActive(false);

        FormContentBox classesForm = entryForm.addComponent(new FormContentBox(8, 52 + 8, entryForm.getWidth() - 16, entryForm.getHeight() - (52 + 8) - (32 + 8)));

        int maxPerRow = 5;
        int numRows = (numItems + maxPerRow - 1) / maxPerRow;

        boolean hasScrollbar = numRows >= 3;

        int contentWidth = classesForm.getWidth() - (hasScrollbar ? Settings.UI.scrollbar.active.getHeight() + 2 : 0);

        float verticalSpacing;

        if (hasScrollbar) {
            verticalSpacing = ClassComponent.height + 16;
        } else {
            verticalSpacing = (float) 1 / numRows;
        }

        for (int i = 0; i < numItems; i++) {
            int row = i / maxPerRow;
            int col = i % maxPerRow;

            int numCols = Math.min(maxPerRow, numItems - row * maxPerRow);
            float horizontalSpacing = (float) 1 / numCols;

            int x = Math.round(contentWidth * (col + 0.5F) * horizontalSpacing);
            int y = hasScrollbar ? Math.round(verticalSpacing * row + ClassComponent.height / 2F) : Math.round(classesForm.getHeight() * (row + 0.5F) * verticalSpacing);

            int finalI = i;
            PlayerClass playerClass = PlayerClass.classesList.get(i);
            ClassComponent classComponent = classesForm.addComponent(new ClassComponent(x, y, playerClass, classes[i]));
            classComponent.addOnMod(newLevel -> {
                int oldLevel = mutableClasses[finalI];
                int mod = newLevel - oldLevel;
                if ((playerClass.isEnabled() || mod < 0) && newLevel >= 0 && newLevel <= 999) {
                    if (mod != 0) {
                        mutableClasses[finalI] = newLevel;

                        int multiClass = RPGSettings.multiClass();

                        if (mod < 0 || multiClass == 0 || Arrays.stream(mutableClasses).filter(level -> level > 0).count() <= multiClass) {
                            int currentClassesTotal = mutableClassesTotal.addAndGet(mod);
                            updateFormClasses(classPointsLabel, resetPointsLabel, cancelButton, confirmButton, classesTotal, currentClassesTotal, maxClasses, resetPoints, classes, mutableClasses);
                            return true;
                        } else {
                            mutableClasses[finalI] = oldLevel;
                        }
                    }
                }
                return false;
            });
            if (playerClass.isEnabled())
                classComponent.onClick(c -> mainForm.changeEntry("classes." + playerClass.stringID, player));
        }
        if (hasScrollbar) {
            classesForm.setContentBox(new Rectangle(0, 0, classesForm.getWidth(), Math.round(verticalSpacing * numRows)));
        }
    }

    public void updateFormClasses(FormLabel classPointsLabel, FormLabel resetPointsLabel, FormButton cancelButton, FormButton confirmButton, int classesTotal, int currentClassesTotal, int maxClasses, final int resetPoints, final int[] classes, int[] mutableClasses) {
        updateClassPoints(classPointsLabel, currentClassesTotal, currentClassesTotal - classesTotal);
        updateResetPoints(resetPointsLabel, classes, mutableClasses, resetPoints);

        cancelButton.setActive(cancelEnabled(classes, mutableClasses));
        confirmButton.setActive(confirmEnabled(maxClasses, currentClassesTotal, resetPoints, classes, mutableClasses));
    }

    public void updateClassPoints(FormLabel classPointsLabel, int currentClassesTotal, int pointsDifference) {
        int totalPoints = playerData.totalClassPoints();
        int unassignedPoints = totalPoints - currentClassesTotal;

        String text = unassignedPoints + "/" + totalPoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        classPointsLabel.setText(Localization.translate("ui", "classpoints", "points", text));
    }

    public void updateResetPoints(FormLabel resetPointsLabel, final int[] classes, int[] mutableClasses, final int resetPoints) {
        int reducedClasses = 0;
        for (int i = 0; i < classes.length; i++) {
            int difference = classes[i] - mutableClasses[i];
            if (difference > 0) reducedClasses += difference;
        }

        String text = String.valueOf(resetPoints);

        if (reducedClasses > 0) {
            text = text + " (" + -reducedClasses * 2 + ")";
        }

        resetPointsLabel.setText(Localization.translate("ui", "resetpoints", "points", text));
    }


    public boolean cancelEnabled(final int[] classes, int[] mutableClasses) {
        return !Arrays.equals(classes, mutableClasses);
    }

    public boolean confirmEnabled(int maxClasses, int mutableClassesTotal, int resetPoints, final int[] classes, int[] mutableClasses) {
        int reducedClasses = 0;
        for (int i = 0; i < classes.length; i++) {
            int difference = classes[i] - mutableClasses[i];
            if (difference > 0) reducedClasses += difference;
        }

        return !Arrays.equals(classes, mutableClasses) && mutableClassesTotal <= maxClasses && (reducedClasses >= 0 || (reducedClasses * 2) <= resetPoints);
    }

}

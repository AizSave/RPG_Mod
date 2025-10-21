package rpgclasses.forms.rpgmenu.entries;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.forms.rpgmenu.MenuContainer;
import rpgclasses.forms.rpgmenu.MenuContainerForm;
import rpgclasses.forms.rpgmenu.components.ClassComponent;
import rpgclasses.forms.rpgmenu.components.MasteryComponent;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MasteryEntry extends MenuEntry {

    public MasteryEntry() {
        super("mastery");
    }

    @Override
    public Color getTextColor(PlayerMob player) {
        playerData = PlayerDataList.getPlayerData(player);

        if (playerData.totalMasteryPoints() < playerData.masterySkills.size()) {
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
        updateContent(mainForm, entryForm, container, playerData.getResets(), playerData.masterySkills, new ArrayList<>(playerData.masterySkills));
    }

    public FormLabel pointsLabel;
    public FormLabel resetPointsLabel;
    public FormButton cancelButton;
    public FormButton confirmButton;

    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container, final int resetPoints, List<Integer> masterySkills, List<Integer> mutableMasterySkills) {
        super.updateContent(mainForm, entryForm, container);
        int numItems = Mastery.masterySkillsList.size();

        pointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 20
        ));
        updatePoints(mutableMasterySkills, 0);

        resetPointsLabel = entryForm.addComponent(new FormLabel(
                "",
                new FontOptions(14), -1, 10, 38
        ));
        updateResetPoints(masterySkills, mutableMasterySkills, resetPoints);


        cancelButton = entryForm.addComponent(new FormLocalTextButton("ui", "cancelbutton", 0, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> this.updateContent(mainForm, entryForm, container, resetPoints, playerData.masterySkills, new ArrayList<>(playerData.masterySkills))));
        cancelButton.setActive(false);

        confirmButton = entryForm.addComponent(new FormLocalTextButton("ui", "confirmbutton", entryForm.getWidth() / 2 + 2, entryForm.getHeight() - 32, entryForm.getWidth() / 2 - 2, FormInputSize.SIZE_32, ButtonColor.BASE)
                .onClicked(c -> {
                    if (!player.isInCombat()) {
                        int reducedMastery = 0;
                        for (Integer mastery : masterySkills) {
                            if (!mutableMasterySkills.contains(mastery)) reducedMastery += 5;
                        }
                        this.updateContent(mainForm, entryForm, container, resetPoints - reducedMastery, mutableMasterySkills, new ArrayList<>(mutableMasterySkills));
                    }
                    container.updateMasterySkills.runAndSend(mutableMasterySkills.stream().mapToInt(Integer::intValue).toArray());
                }));
        confirmButton.setActive(false);

        FormContentBox masterySkillsForm = entryForm.addComponent(new FormContentBox(8, 52 + 8, entryForm.getWidth() - 16, entryForm.getHeight() - (52 + 8) - (32 + 8)));

        int maxPerRow = 6;
        int numRows = (numItems + maxPerRow - 1) / maxPerRow;

        boolean hasScrollbar = numRows >= 4;

        int contentWidth = masterySkillsForm.getWidth() - (hasScrollbar ? Settings.UI.scrollbar.active.getHeight() + 2 : 0);

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
            int y = hasScrollbar ? Math.round(verticalSpacing * row + ClassComponent.height / 2F) : Math.round(masterySkillsForm.getHeight() * (row + 0.5F) * verticalSpacing);

            int finalI = i;
            masterySkillsForm.addComponent(new MasteryComponent(x, y, Mastery.masterySkillsList.get(i), mutableMasterySkills.contains(i),
                    () -> { // Added
                        mutableMasterySkills.add(finalI);
                        updateForm(resetPoints, masterySkills, mutableMasterySkills);
                    },
                    () -> { // Removed
                        mutableMasterySkills.remove((Object) finalI);
                        updateForm(resetPoints, masterySkills, mutableMasterySkills);
                    })
            );
        }
        if (hasScrollbar) {
            masterySkillsForm.setContentBox(new Rectangle(0, 0, masterySkillsForm.getWidth(), Math.round(verticalSpacing * numRows)));
        }
    }

    public void updateForm(final int resetPoints, List<Integer> masterySkills, List<Integer> mutableMasterySkills) {
        updatePoints(mutableMasterySkills, mutableMasterySkills.size() - masterySkills.size());
        updateResetPoints(masterySkills, mutableMasterySkills, resetPoints);

        cancelButton.setActive(cancelEnabled(masterySkills, mutableMasterySkills));
        confirmButton.setActive(confirmEnabled(resetPoints, masterySkills, mutableMasterySkills));
    }

    public void updatePoints(List<Integer> mutableMasterySkills, int pointsDifference) {
        int totalPoints = playerData.totalMasteryPoints();
        int unassignedPoints = totalPoints - mutableMasterySkills.size();

        String text = unassignedPoints + "/" + totalPoints;

        if (-pointsDifference != 0) {
            text = text + " (" + (-pointsDifference > 0 ? "+" + -pointsDifference : String.valueOf(-pointsDifference)) + ")";
        }

        pointsLabel.setText(Localization.translate("ui", "masterypoints", "points", text));
    }

    public void updateResetPoints(List<Integer> masterySkills, List<Integer> mutableMasterySkills, final int resetPoints) {
        int reducedMastery = 0;
        for (Integer mastery : masterySkills) {
            if (!mutableMasterySkills.contains(mastery)) reducedMastery += 5;
        }

        String text = String.valueOf(resetPoints);

        if (reducedMastery > 0) {
            text = text + " (" + -reducedMastery + ")";
        }

        resetPointsLabel.setText(Localization.translate("ui", "resetpoints", "points", text));
    }

    public boolean cancelEnabled(List<Integer> masterySkills, List<Integer> mutableMasterySkills) {
        return !masterySkills.equals(mutableMasterySkills);
    }

    public boolean confirmEnabled(int resetPoints, List<Integer> masterySkills, List<Integer> mutableMasterySkills) {
        int reducedMastery = 0;
        for (Integer mastery : masterySkills) {
            if (!mutableMasterySkills.contains(mastery)) reducedMastery += 5;
        }

        return !masterySkills.equals(mutableMasterySkills) && mutableMasterySkills.size() <= playerData.totalMasteryPoints() && (reducedMastery >= 0 || reducedMastery <= resetPoints);
    }

    @Override
    public GameTexture getTexture() {
        return super.getTexture();
    }
}

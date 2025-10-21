package rpgclasses.forms.rpgmenu.entries;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.forms.rpgmenu.MenuContainer;
import rpgclasses.forms.rpgmenu.MenuContainerForm;
import rpgclasses.forms.rpgmenu.components.EquipActiveSkillComponent;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerClassData;
import rpgclasses.settings.RPGSettings;
import rpgclasses.ui.CustomUIManager;
import rpgclasses.ui.RPGSkillUIManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ActiveSkillsEntry extends MenuEntry {
    public static int showManySlots = RPGSettings.settingsGetter.getBoolean("twelveSkillSlots") ? 12 : 6;

    public ActiveSkillsEntry() {
        super("activeskills");
    }

    @Override
    public void updateContent(MenuContainerForm mainForm, FormContentBox entryForm, final MenuContainer container) {
        super.updateContent(mainForm, entryForm, container);

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        entryForm.addComponent(new FormContentIconButton(entryForm.getWidth() - 32 - 8, 8, FormInputSize.SIZE_32, ButtonColor.BASE, RPGResources.UI_TEXTURES.slot_icons[style][showManySlots - 1])
                .onClicked(c -> {
                    showManySlots = showManySlots > 6 ? 6 : 12;
                    for (RPGSkillUIManager rpgSkill : CustomUIManager.rpgSkills) {
                        rpgSkill.updatePosition(RPGSkillUIManager.mainGameFormManager);
                    }
                    mainForm.updateContent(container, player);
                }));

        FormLabel label = entryForm.addComponent(new FormLabel(
                Localization.translate("ui", "activeskillsuse") + "\n" +
                        Localization.translate("ui", "activeskillsusetip1") + "\n" +
                        Localization.translate("ui", "activeskillsusetip2") + "\n" +
                        Localization.translate("ui", "activeskillsequipinstructions"),
                new FontOptions(12), -1, 10, 20, entryForm.getWidth() - 20 - 32 - 8
        ));


        List<EquipableActiveSkill> equipableActiveSkills = new ArrayList<>();
        for (PlayerClassData classesDatum : playerData.getClassesData()) {
            for (int i = 0; i < classesDatum.getActiveSkillLevels().length; i++) {
                int activeSkillLevel = classesDatum.getActiveSkillLevels()[i];
                if (classesDatum.getLevel(false) > 0 && activeSkillLevel > 0) {
                    equipableActiveSkills.add(new EquipableActiveSkill(
                            classesDatum.playerClass,
                            classesDatum.playerClass.activeSkillsList.get(i),
                            activeSkillLevel
                    ));
                }
            }
        }

        int startY = Math.max(40, label.getY() + label.getHeight()) + 4;
        if (equipableActiveSkills.isEmpty()) {
            entryForm.addComponent(new FormLocalLabel(
                    "ui", "noactiveskills",
                    new FontOptions(16), -1, 10, startY
            ));
        } else {

            FormContentBox activeSkills = entryForm.addComponent(new FormContentBox(16, startY, entryForm.getWidth() - 32, entryForm.getHeight() - startY - 8));

            int columns = showManySlots > 6 ? 1 : 2;
            int spacingY = 8;
            int itemHeight = EquipActiveSkillComponent.height;

            int leftX = 16;
            int rightX = activeSkills.getWidth() / 2;

            for (int i = 0; i < equipableActiveSkills.size(); i++) {
                EquipableActiveSkill equipableActiveSkill = equipableActiveSkills.get(i);
                PlayerClass playerClass = equipableActiveSkill.playerClass;
                ActiveSkill activeSkill = equipableActiveSkill.activeSkill;
                int activeSkillLevel = equipableActiveSkill.activeSkillLevel;

                int column = i % columns;
                int row = i / columns;

                int x = (column == 0) ? leftX : rightX;
                int y = spacingY + row * (itemHeight + spacingY);

                equipableActiveSkill.component = activeSkills.addComponent(
                        new EquipActiveSkillComponent(x, y, activeSkill, playerClass, player, activeSkillLevel, showManySlots, showManySlots > 6 ? entryForm.getWidth() - 32 : EquipActiveSkillComponent.shortWidth, (newEquippedActiveSkills) -> {
                            for (EquipableActiveSkill skill : equipableActiveSkills) {
                                skill.component.update(newEquippedActiveSkills, playerData);
                            }
                            container.updateEquippedActiveSkills.runAndSend(newEquippedActiveSkills);
                        })
                );
            }

            int totalRows = (int) Math.ceil((float) equipableActiveSkills.size() / columns);
            activeSkills.setContentBox(new Rectangle(0, 0, activeSkills.getWidth(), spacingY + totalRows * (itemHeight + spacingY)));
        }
    }

    public static class EquipableActiveSkill {
        public final PlayerClass playerClass;
        public final ActiveSkill activeSkill;
        public final int activeSkillLevel;
        public EquipActiveSkillComponent component;

        public EquipableActiveSkill(PlayerClass playerClass, ActiveSkill activeSkill, int activeSkillLevel) {
            this.playerClass = playerClass;
            this.activeSkill = activeSkill;
            this.activeSkillLevel = activeSkillLevel;
        }
    }

}

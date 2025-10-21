package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.forms.rpgmenu.entries.ActiveSkillsEntry;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.util.function.Consumer;

public class EquipActiveSkillComponent extends FormContentBox {
    public static int height = 54;
    public static int shortWidth = 300;

    private final FormContentIconButton[] buttons = new FormContentIconButton[PlayerData.EQUIPPED_SKILLS_MAX];
    public final PlayerClass playerClass;
    public final ActiveSkill activeSkill;

    public EquipActiveSkillComponent(int x, int y, ActiveSkill activeSkill, PlayerClass playerClass, PlayerMob player, int skillLevel, int showManySlots, int width, Consumer<EquippedActiveSkill[]> onClick) {
        super(x, y, width, height);

        PlayerData playerData = PlayerDataList.getPlayerData(player);

        this.playerClass = playerClass;
        this.activeSkill = activeSkill;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        this.addComponent(new SkillIconComponent(activeSkill, skillLevel, 0, 0, 34, 54));

        this.addComponent(new FormLocalLabel(getSkillText(activeSkill, skillLevel), new FontOptions(16), -1, 34 + 10, 0));

        for (int i = 0; i < showManySlots; i++) {
            int finalI = i;
            buttons[i] = (FormContentIconButton) this.addComponent(new FormContentIconButton(34 + 10 + (32 + 4) * i, 16 + 6, FormInputSize.SIZE_32, ButtonColor.BASE, RPGResources.UI_TEXTURES.slot_icons[style][i], new StaticMessage("[input=activeskillslot" + (i + 1) + "]"))
                    .onClicked(c -> {
                        EquippedActiveSkill[] newEquippedActiveSkills = playerData.equippedActiveSkills.clone();

                        // Only if it has not equipped another skill with the same family, and it's not the same
                        boolean sameFamily = false;
                        for (int j = 0; j < PlayerData.EQUIPPED_SKILLS_MAX; j++) {
                            if (finalI != j && newEquippedActiveSkills[j].isNotSameSkillButSameFamily(playerClass, activeSkill)) {
                                sameFamily = true;
                                break;
                            }
                        }
                        if (sameFamily) {
                            return;
                        }

                        EquippedActiveSkill oldEquippedActiveSkill = newEquippedActiveSkills[finalI];

                        // Only if the player is not in combat and both skills can change
                        if (!player.isInCombat() && oldEquippedActiveSkill.canChange(player.getTime())) {

                            if (oldEquippedActiveSkill.isSameSkill(playerClass, activeSkill)) {
                                oldEquippedActiveSkill.empty();
                            } else {
                                boolean canChange = true;
                                for (int j = 0; j < PlayerData.EQUIPPED_SKILLS_MAX; j++) {
                                    EquippedActiveSkill equippedActiveSkill = newEquippedActiveSkills[j];
                                    if (finalI != j && equippedActiveSkill.isSameSkill(playerClass, activeSkill)) {
                                        if (!equippedActiveSkill.canChange(player.getTime())) {
                                            canChange = false;
                                            break;
                                        } else {
                                            equippedActiveSkill.empty();
                                        }
                                    }
                                }

                                if (canChange) {
                                    oldEquippedActiveSkill.update(playerClass, activeSkill);
                                }
                            }

                            onClick.accept(newEquippedActiveSkills);
                        }
                    }));
        }

        update(playerData.equippedActiveSkills, playerData);
    }

    public void update(EquippedActiveSkill[] newEquippedActiveSkills, PlayerData playerData) {
        for (int i = 0; i < ActiveSkillsEntry.showManySlots; i++) {
            boolean sameFamily = false;
            boolean sameSkill = false;
            for (int j = 0; j < PlayerData.EQUIPPED_SKILLS_MAX; j++) {
                if (i != j && newEquippedActiveSkills[j] != null) {
                    if (newEquippedActiveSkills[j].isSameSkill(playerClass, activeSkill)) {
                        sameSkill = true;
                        break;
                    } else if (newEquippedActiveSkills[j].isSameFamily(playerClass, activeSkill)) {
                        sameFamily = true;
                        break;
                    }
                }
            }
            EquippedActiveSkill equippedActiveSkill = newEquippedActiveSkills[i];
            if (equippedActiveSkill.isSameSkill(playerClass, activeSkill)) {
                buttons[i].color = ButtonColor.GREEN;
            } else if (sameSkill) {
                buttons[i].color = ButtonColor.BASE;
            } else if (sameFamily) {
                buttons[i].color = ButtonColor.RED;
            } else if (equippedActiveSkill.isEmpty()) {
                buttons[i].color = ButtonColor.YELLOW;
            } else {
                buttons[i].color = equippedActiveSkill.getActiveSkill().getLevel(playerData) > 0 ? ButtonColor.BASE : ButtonColor.YELLOW;
            }
        }
    }

    public GameMessage getSkillText(ActiveSkill activeSkill, int skillLevel) {
        return new StaticMessage(Localization.translate("activeskills", activeSkill.stringID) + " - " + Localization.translate("ui", "lvl", "level", skillLevel));
    }
}

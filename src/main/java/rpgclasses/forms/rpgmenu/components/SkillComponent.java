package rpgclasses.forms.rpgmenu.components;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameBackground;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormContentIconButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.GameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.content.player.SkillsLogic.SkillsList;
import rpgclasses.data.PlayerClassData;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillComponent extends FormContentBox {
    public static int width = 32 + 6 + 8;
    public static int height = 66;
    int currentSkillLevel;
    FormEventListener<FormInputEvent<FormButton>> onAdd;
    FormEventListener<FormInputEvent<FormButton>> onRemove;

    public SkillComponent(int x, int y, Skill skill, PlayerMob player, PlayerClassData classData, int currentSkillLevel, int[] mutableSkillLevels) {
        super(x, y, width, height);
        this.currentSkillLevel = currentSkillLevel;

        Color sameLevel = Settings.UI.activeTextColor;
        Color differentLevel;

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);
        if (style == 1) {
            differentLevel = new Color(255, 255, 0);
        } else {
            differentLevel = new Color(102, 102, 0);
        }

        AtomicInteger skillLevel = new AtomicInteger();
        skillLevel.set(currentSkillLevel);
        FormLocalLabel levelText = this.addComponent(new FormLocalLabel(getLevelText(skillLevel.get()), new FontOptions(12), 0, width / 2, 52 + 2));
        levelText.setColor(sameLevel);

        SkillIconComponent skillIconComponent = this.addComponent(new SkillIconComponent(skill, skillLevel.get(), 0, 0, width, 34));

        this.addComponent(new FormContentIconButton((width / 2) + 2, 34 + 2, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.addSmall_icon[style]) {
            @Override
            protected void addTooltips(PlayerMob perspective) {
                GameTooltips tooltips = null;
                if (skill.containsComplexTooltips()) {
                    int skillLevel = getSkillLevel();
                    if (skillLevel < skill.levelMax) {
                        tooltips = skill.getFinalToolTips(player, skillLevel + 1, true);
                    }
                }

                if (tooltips != null) {
                    GameTooltipManager.addTooltip(tooltips, GameBackground.itemTooltip, TooltipLocation.FORM_FOCUS);
                }
            }
        }.onClicked(c -> {

            int classLevel = classData.getLevel(false);

            int maxLevel = classData.getEffectiveSkillMaxLevel(skill, classLevel, mutableSkillLevels);

            if (skillLevel.get() >= maxLevel) return;

            boolean wellAssigned = true;

            int[] newMutableSkillLevels = mutableSkillLevels.clone();
            newMutableSkillLevels[skill.id]++;

            SkillsList<?> skillsList = skill instanceof Passive ? classData.playerClass.passivesList : classData.playerClass.activeSkillsList;
            for (int i = 0; i < newMutableSkillLevels.length; i++) {
                if (i != skill.id) {
                    Skill skill1 = skillsList.get(i);
                    maxLevel = classData.getEffectiveSkillMaxLevel(skill1, classLevel, newMutableSkillLevels);

                    if (newMutableSkillLevels[i] > maxLevel) {
                        wellAssigned = false;
                        break;
                    }
                }
            }

            if (!wellAssigned) return;

            if (skill instanceof ActiveSkill) {
                boolean hasRequired = true;
                ActiveSkill activeSkill = (ActiveSkill) skill;
                for (ActiveSkill.RequiredSkill requiredSkill : activeSkill.requiredSkills) {
                    if (mutableSkillLevels[requiredSkill.activeSkill.id] < requiredSkill.activeSkillLevel) {
                        hasRequired = false;
                        break;
                    }
                }
                if (!hasRequired) return;
            }

            if (skill instanceof Passive && skill.family != null) {
                boolean onlyInFamily = true;
                for (int i = 0; i < mutableSkillLevels.length; i++) {
                    if (mutableSkillLevels[i] > 0 && i != skill.id) {
                        Passive other = classData.playerClass.passivesList.get(i);
                        if (other.family != null && other.family.equals(skill.family)) {
                            onlyInFamily = false;
                            break;
                        }
                    }
                }
                if (!onlyInFamily) return;
            }

            int newLevel = skillLevel.incrementAndGet();
            levelText.setLocalization(getLevelText(newLevel));
            levelText.setColor(newLevel == currentSkillLevel ? sameLevel : differentLevel);
            skillIconComponent.setSkillLevel(newLevel);
            onAdd.onEvent(c);
        }));

        this.addComponent(new FormContentIconButton((width / 2) - 16 - 2, 34 + 2, FormInputSize.SIZE_16, ButtonColor.BASE, RPGResources.UI_TEXTURES.removeSmall_icon[style]) {
            @Override
            protected void addTooltips(PlayerMob perspective) {
                GameTooltips tooltips = null;
                if (skill.containsComplexTooltips()) {
                    int skillLevel = getSkillLevel();
                    if (skillLevel > 1) {
                        tooltips = skill.getFinalToolTips(player, skillLevel - 1, true);
                    }
                }

                if (tooltips != null) {
                    GameTooltipManager.addTooltip(tooltips, GameBackground.itemTooltip, TooltipLocation.FORM_FOCUS);
                }
            }
        }.onClicked(c -> {
            if (skillLevel.get() > 0) {
                if (skill instanceof ActiveSkill) {
                    int isRequirement = getIsRequirement(skill, classData, mutableSkillLevels);
                    if (skillLevel.get() <= isRequirement) {
                        return;
                    }
                }

                int newLevel = skillLevel.decrementAndGet();
                levelText.setLocalization(getLevelText(newLevel));
                levelText.setColor(newLevel == currentSkillLevel ? sameLevel : differentLevel);
                skillIconComponent.setSkillLevel(newLevel);
                onRemove.onEvent(c);
            }
        }));
    }

    private static int getIsRequirement(Skill skill, PlayerClassData classData, int[] mutableSkillLevels) {
        int isRequirement = 0;
        for (ActiveSkill activeSkill : classData.playerClass.activeSkillsList.getList()) {
            for (ActiveSkill.RequiredSkill requiredSkill : activeSkill.requiredSkills) {
                if (requiredSkill != null && requiredSkill.activeSkill.id == skill.id && mutableSkillLevels[activeSkill.id] > 0) {
                    int levelRequired = requiredSkill.activeSkillLevel;
                    if (levelRequired > isRequirement) {
                        isRequirement = levelRequired;
                    }
                }
            }
        }
        return isRequirement;
    }

    public GameMessage getLevelText(int skillLevel) {
        return new LocalMessage("ui", "lvl", "level", skillLevel);
    }

    public int getSkillLevel() {
        return currentSkillLevel;
    }

    public void addSkillLevel(int n) {
        currentSkillLevel += n;
    }

    public void setOnAdd(FormEventListener<FormInputEvent<FormButton>> onAdd) {
        this.onAdd = onAdd;
    }

    public void setOnRemove(FormEventListener<FormInputEvent<FormButton>> onRemove) {
        this.onRemove = onRemove;
    }
}

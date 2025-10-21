package rpgclasses.ui;

import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.Form;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.item.Item;
import rpgclasses.forms.rpgmenu.components.SkillIconComponent;
import rpgclasses.data.EquippedActiveSkill;
import rpgclasses.data.PlayerData;

import java.awt.*;

public class RPGSkillUIForm extends Form {

    private final int slot;
    private EquippedActiveSkill equippedActiveSkill = new EquippedActiveSkill();
    private int activeSkillLevel;
    private SkillIconComponent skillIconComponent;

    public RPGSkillUIForm(int slot, int width, int height) {
        super("rpgskill" + slot + "ui", width, height);
        this.slot = slot;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
        super.draw(tickManager, perspective, renderBox);

        FontOptions fontOptions = new FontOptions(12);
        fontOptions.color(Settings.UI.activeTextColor);
        FontManager.bit.drawString(getX() + 2, getY() + 2, String.valueOf(slot + 1), fontOptions);

        if (!this.equippedActiveSkill.isEmpty()) {
            int cooldownLeft = this.equippedActiveSkill.getCooldownLeft(perspective.getTime());
            if (cooldownLeft > 0 || this.equippedActiveSkill.isInUse()) {
                String cooldownLeftString = equippedActiveSkill.getCooldownLeftString(cooldownLeft);

                FontOptions options = new FontOptions(Item.tipFontOptions);
                options.color(new Color(255, 102, 102));
                FontManager.bit.drawString(getX() + 2, getY() + getHeight() - 12, cooldownLeftString, options);
            }
        }
    }

    public void updateContent(PlayerData playerData) {
        EquippedActiveSkill newEquippedActiveSkill = playerData.equippedActiveSkills[slot];

        if ((this.equippedActiveSkill.isEmpty() == newEquippedActiveSkill.isEmpty()) || !this.equippedActiveSkill.isSameSkill(newEquippedActiveSkill)) {
            this.equippedActiveSkill = newEquippedActiveSkill;

            this.clearComponents();
            this.skillIconComponent = null;
            if (!this.equippedActiveSkill.isEmpty()) {
                updateLevel(playerData);
                this.skillIconComponent = this.addComponent(new SkillIconComponent(this.equippedActiveSkill.getActiveSkill(), this.activeSkillLevel, 0, 0, getWidth(), getHeight()));
            }
        }
    }

    public void updateLevel(PlayerData playerData) {
        if (this.equippedActiveSkill.isEmpty()) {
            this.activeSkillLevel = 0;
        } else {
            int newActiveSkillLevel = equippedActiveSkill.getActiveSkill().getLevel(playerData);

            if (this.skillIconComponent != null) {
                this.skillIconComponent.setSkillLevel(newActiveSkillLevel);
            }

            this.activeSkillLevel = newActiveSkillLevel;
        }
    }
}

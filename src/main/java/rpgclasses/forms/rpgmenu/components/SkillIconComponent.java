package rpgclasses.forms.rpgmenu.components;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.position.FormFixedPosition;
import necesse.gfx.forms.position.FormPosition;
import necesse.gfx.forms.position.FormPositionContainer;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.SpriteTooltip;
import necesse.gfx.gameTooltips.TooltipLocation;
import rpgclasses.RPGResources;
import rpgclasses.content.player.SkillsLogic.Skill;
import rpgclasses.forms.rpgmenu.BorderFormGameBackground;

import java.awt.*;
import java.util.List;

public class SkillIconComponent extends FormButton implements FormPositionContainer {
    private FormPosition position;

    final Skill skill;
    private int skillLevel;
    final int width;
    final int height;
    boolean showLevelVersion;

    public SkillIconComponent(Skill skill, int skillLevel, int x, int y, int width, int height) {
        this.position = new FormFixedPosition(x, y);
        this.skill = skill;
        this.skillLevel = skillLevel;
        this.width = width;
        this.height = height;
        this.showLevelVersion = true;

        this.onClicked((e) -> showLevelVersion = !showLevelVersion);
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob player, Rectangle rectangle) {
        int textureWidth = skill.texture.getWidth();
        int textureHeight = skill.texture.getWidth();

        int drawX = this.getX() + (this.width - textureWidth) / 2;
        int drawY = this.getY() + (this.height - textureHeight) / 2;

        skill.texture.initDraw().draw(drawX, drawY);

        for (int i = Math.max(0, skillLevel - 5); i < skillLevel && i < stars.length; i++) {
            stars[i].draw(drawX, drawY);
        }

        if (isHovering()) {
            int showLevel = skillLevel == 0 ? 1 : skillLevel;

            ListGameTooltips tooltips;
            if (skill.containsComplexTooltips()) {
                if (showLevelVersion) {
                    tooltips = skill.getFinalToolTips(player, showLevel, false);
                    tooltips.add(" ");
                    tooltips.add(Localization.translate("ui", "clicktoseebase"));
                } else {
                    tooltips = skill.getBaseToolTips(player);
                    tooltips.add(" ");
                    tooltips.add(Localization.translate("ui", "clicktoseelevel", "level", showLevel));
                }
            } else {
                tooltips = skill.getBaseToolTips(player);
            }
            GameTooltipManager.addTooltip(tooltips, new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);

            String[] extraTooltipsString = skill.getAllExtraTooltips(player, showLevel, showLevelVersion);
            for (String extraTooltip : extraTooltipsString) {
                GameTooltipManager.addTooltip(new ListGameTooltips(extraTooltip), new BorderFormGameBackground(12), TooltipLocation.FORM_FOCUS);
            }

            GameTooltipManager.addTooltip(new SpriteTooltip(skill.texture), new BorderFormGameBackground(4), TooltipLocation.FORM_FOCUS);

        }
    }

    @Override
    public List<Rectangle> getHitboxes() {
        return singleBox(new Rectangle(this.getX(), this.getY(), this.width, this.height));
    }

    public FormPosition getPosition() {
        return this.position;
    }

    public void setPosition(FormPosition position) {
        this.position = position;
    }

    public static Star[] stars = {
            new Star(0, 26, 0),   // 1
            new Star(6, 18, 0),   // 2
            new Star(12, 26, 0),  // 3
            new Star(18, 18, 0),  // 4
            new Star(24, 26, 0),  // 5

            new Star(0, 26, 1),    // 6
            new Star(6, 18, 1),    // 7
            new Star(12, 26, 1),   // 8
            new Star(18, 18, 1),   // 9
            new Star(24, 26, 1),   // 10

            new Star(0, 26, 2),    // 11
            new Star(6, 18, 2),    // 12
            new Star(12, 26, 2),   // 13
            new Star(18, 18, 2),   // 14
            new Star(24, 26, 2),    // 15

            new Star(0, 26, 3),    // 16
            new Star(6, 18, 3),    // 17
            new Star(12, 26, 3),   // 18
            new Star(18, 18, 3),   // 19
            new Star(24, 26, 3)    // 20
    };

    public static class Star {
        int x, y, upgrades;

        public Star(int x, int y, int upgrades) {
            this.x = x;
            this.y = y;
            this.upgrades = upgrades;
        }

        public void draw(int iconDrawX, int iconDrawY) {
            RPGResources.UI_TEXTURES.star_textures[upgrades].initDraw().draw(x + iconDrawX, y + iconDrawY);
        }
    }

}

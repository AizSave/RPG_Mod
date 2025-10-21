package rpgclasses;

import necesse.engine.sound.gameSound.GameSound;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.ButtonIcon;
import necesse.gfx.ui.GameInterfaceStyle;
import necesse.gfx.ui.HoverStateTextures;
import rpgclasses.content.player.Attribute;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.content.player.SkillsLogic.Passives.Passive;
import rpgclasses.data.PlayerData;
import rpgclasses.mobs.summons.damageable.NecromancerTombMob;
import rpgclasses.mobs.summons.passive.RangerWolfMob;

import java.util.HashMap;
import java.util.Map;

public class RPGResources {

    public static Map<String, GameTexture> mobsTexture = new HashMap<>();

    public static GameTexture emptyActiveSkill_texture;

    public static void initResources() {
        emptyActiveSkill_texture = GameTexture.fromFile("activeskills/empty");

        initUIResources();
        initParticleResources();
        initMobResources();
        SOUNDS.initSoundResources();

        Mastery.masterySkillsList.forEach(Mastery::initResources);
        Attribute.attributesList.forEach(Attribute::initResources);
        PlayerClass.classesList.forEach(playerClass -> {
            playerClass.initResources();
            playerClass.passivesList.each(Passive::initResources);
            playerClass.activeSkillsList.each(ActiveSkill::initResources);
        });
    }

    public static class UI_TEXTURES {
        public static GameTexture rpgMenu_texture;
        public static GameTexture qr_texture;
        public static GameTexture[] star_textures;

        public static GameTexture[] timebar_texture;

        public static ButtonIcon[] add_icon;
        public static ButtonIcon[] remove_icon;
        public static ButtonIcon[] add10_icon;
        public static ButtonIcon[] remove10_icon;
        public static ButtonIcon[] addSmall_icon;
        public static ButtonIcon[] removeSmall_icon;
        public static ButtonIcon[][] slot_icons;
    }

    public static void initUIResources() {
        UI_TEXTURES.rpgMenu_texture = GameTexture.fromFile("ui/misc/rpgmenu");
        UI_TEXTURES.qr_texture = GameTexture.fromFile("ui/misc/qr");

        UI_TEXTURES.star_textures = new GameTexture[4];
        for (int i = 0; i < 4; i++) {
            UI_TEXTURES.star_textures[i] = GameTexture.fromFile("ui/misc/star" + (i + 1));
        }

        int styles = GameInterfaceStyle.styles.size();

        UI_TEXTURES.timebar_texture = new GameTexture[styles];

        UI_TEXTURES.add_icon = new ButtonIcon[styles];
        UI_TEXTURES.remove_icon = new ButtonIcon[styles];

        UI_TEXTURES.add10_icon = new ButtonIcon[styles];
        UI_TEXTURES.remove10_icon = new ButtonIcon[styles];

        UI_TEXTURES.addSmall_icon = new ButtonIcon[styles];
        UI_TEXTURES.removeSmall_icon = new ButtonIcon[styles];

        UI_TEXTURES.slot_icons = new ButtonIcon[styles][];

        for (int i = 0; i < styles; i++) {
            GameInterfaceStyle style = GameInterfaceStyle.styles.get(i);

            UI_TEXTURES.timebar_texture[i] = new HoverStateTextures(style, "timebar_background").active;

            UI_TEXTURES.add_icon[i] = new ButtonIcon(style, "add");
            UI_TEXTURES.remove_icon[i] = new ButtonIcon(style, "remove");

            UI_TEXTURES.add10_icon[i] = new ButtonIcon(style, "add10");
            UI_TEXTURES.remove10_icon[i] = new ButtonIcon(style, "remove10");

            UI_TEXTURES.addSmall_icon[i] = new ButtonIcon(style, "add_small");
            UI_TEXTURES.removeSmall_icon[i] = new ButtonIcon(style, "remove_small");

            UI_TEXTURES.slot_icons[i] = new ButtonIcon[PlayerData.EQUIPPED_SKILLS_MAX];
            for (int j = 0; j < PlayerData.EQUIPPED_SKILLS_MAX; j++) {
                UI_TEXTURES.slot_icons[i][j] = new ButtonIcon(style, "slot" + (j + 1));
            }
        }

    }


    public static class PARTICLE_TEXTURES {
        public static GameTexture bearTrapOpen;
        public static GameTexture bearTrapClosed;

        public static GameTexture smite;
        public static GameTexture wrathOfLight;
    }

    public static void initParticleResources() {
        PARTICLE_TEXTURES.bearTrapOpen = GameTexture.fromFile("particles/beartrap_open");
        PARTICLE_TEXTURES.bearTrapClosed = GameTexture.fromFile("particles/beartrap_closed");

        PARTICLE_TEXTURES.smite = GameTexture.fromFile("particles/smite");
        PARTICLE_TEXTURES.wrathOfLight = GameTexture.fromFile("particles/wrathoflight");
    }

    public static void initMobResources() {
        RangerWolfMob.texture = GameTexture.fromFile("mobs/rangerwolf");

        NecromancerTombMob.texture = GameTexture.fromFile("mobs/necromancertomb");
    }

    public static class SOUNDS {
        public static GameSound Zap;
        public static GameSound Bark;
        public static GameSound Rat;

        public static void initSoundResources() {
            Zap = GameSound.fromFile("zap");
            Bark = GameSound.fromFile("bark");
            Rat = GameSound.fromFile("rat");
        }
    }
}

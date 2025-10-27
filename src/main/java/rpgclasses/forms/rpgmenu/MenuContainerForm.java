package rpgclasses.forms.rpgmenu;

import necesse.engine.Settings;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.network.client.Client;
import necesse.engine.platforms.Platform;
import necesse.engine.platforms.PlatformManager;
import necesse.engine.util.GameUtils;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.*;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.presets.ExternalLinkForm;
import necesse.gfx.forms.presets.containerComponent.ContainerFormSwitcher;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.ButtonState;
import necesse.gfx.ui.GameInterfaceStyle;
import rpgclasses.RPGMod;
import rpgclasses.RPGResources;
import rpgclasses.content.player.PlayerClass;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.forms.rpgmenu.components.PlayerDataComponent;
import rpgclasses.forms.rpgmenu.entries.*;
import rpgclasses.settings.RPGSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MenuContainerForm extends ContainerFormSwitcher<MenuContainer> {
    public static String modUrl = "https://steamcommunity.com/sharedfiles/filedetails/?id=3534267732";

    public Client client;

    private final Form principalForm;

    public static boolean showClassIcons = RPGSettings.settingsGetter.getBoolean("showClassIcons");

    public static final MenuEntry[] menuEntries = new MenuEntry[]{
            new AttributesEntry(),
            new ActiveSkillsEntry(),
            new ClassesEntry()
    };

    public static final MenuEntry masteryEntry = new MasteryEntry();

    public static MenuEntry actualEntry = menuEntries[0];
    public FormContentBox entries;
    public FormContentBox entryForm;
    PlayerDataComponent playerDataComponent;

    public static FormComponentList returnForm;

    public MenuContainerForm(Client client, final MenuContainer container) {
        super(client, container);

        this.client = client;

        FormComponentList formComponents = returnForm = this.addComponent(new FormComponentList());
        this.principalForm = formComponents.addComponent(new Form(925, 500));

        entries = this.principalForm.addComponent(new FormContentBox(0, 46, 190, 274));

        GameMessage rateMessage = new LocalMessage("ui", "rate");
        this.principalForm.addComponent(new FormLocalTextButton(rateMessage, new StaticMessage(modUrl), 20, 326, 155, SIZE_20_SMALL_FONT, ButtonColor.BASE)).onClicked(e -> this.openUrlOrShowExternalLink(rateMessage, modUrl, RPGResources.UI_TEXTURES.qr_texture));

        FormBreakLine playerLevelBreakLike = this.principalForm.addComponent(new FormBreakLine(FormBreakLine.ALIGN_BEGINNING, 10, 355, 175, true));
        playerLevelBreakLike.color = Settings.UI.activeTextColor;

        FormBreakLine middleVerticalLine = this.principalForm.addComponent(new FormBreakLine(FormBreakLine.ALIGN_BEGINNING, entries.getWidth() + 10, 10, this.principalForm.getHeight() - 55, false));
        middleVerticalLine.color = Settings.UI.activeTextColor;

        playerDataComponent = this.principalForm.addComponent(new PlayerDataComponent(12, 360, 250 - 24, 100));

        FormLabel titleComponent = this.principalForm.addComponent(new FormLocalLabel("ui", "menutitle", new FontOptions(16), 0, 95, 20, 186));
        if (GameInterfaceStyle.styles.indexOf(Settings.UI) == 1) {
            titleComponent.setColor(new Color(255, 215, 0));
        } else {
            titleComponent.setColor(new Color(0, 51, 102));
        }

        FormLabel version = this.principalForm.addComponent(new FormLabel(RPGMod.currentVersion, new FontOptions(10), 1, 925 - 4, 4));
        version.setColor(Settings.UI.activeTextColor);

        entryForm = this.principalForm.addComponent(new FormContentBox(entries.getWidth() + 20, 10, 925 - entries.getWidth() - 20 - 4, this.principalForm.getHeight() - 60));

        this.principalForm.addComponent(new FormLocalTextButton("ui", "closebutton", 4, this.principalForm.getHeight() - 40, this.principalForm.getWidth() - 8)).onClicked((e) -> client.closeContainer(true));
        this.makeCurrent(formComponents);

        PlayerMob player = client.getPlayer();

        updateEntries(container, player, null);
        updateContent(container, player);

        playerDataComponent.updateContent(player);
    }

    public void updateEntries(final MenuContainer container, PlayerMob player, int[] classLevels) {
        entries.clearComponents();
        PlayerData playerData = PlayerDataList.getPlayerData(player);

        List<MenuEntry> showEntries = Arrays.stream(menuEntries).collect(Collectors.toCollection(ArrayList::new));

        if (playerData.totalMasteryPoints() > 0) {
            showEntries.add(1, masteryEntry);
        }

        for (int i = 0; i < playerData.getClassLevels().length; i++) {
            PlayerClass playerClass = PlayerClass.classesList.get(i);
            if (playerClass.isEnabled() && (classLevels == null ? playerData.getClassLevel(i) : classLevels[i]) > 0) {
                showEntries.add(new ClassEntry("classes." + playerClass.stringID, playerClass));
            }
        }

        if (showEntries.stream().noneMatch(entry -> Objects.equals(entry.name, actualEntry.name)))
            showEntries.add(actualEntry);

        int width = 190 - 12;
        int height = showClassIcons ? 34 : 28;
        int space = 4;
        for (int i = 0; i < showEntries.size(); i++) {
            MenuEntry menuEntry = showEntries.get(i);
            entries.addComponent((new FormCustomButton(6, space + i * (height + space), width, height) {

                @Override
                public void draw(Color color, int i, int i1, PlayerMob playerMob) {
                    FontOptions fontOptions = new FontOptions(16).color(this.getTextColor());

                    Color textColor = menuEntry.getTextColor(player);
                    if (textColor != null) fontOptions.color(textColor);

                    if (!Objects.equals(actualEntry.name, menuEntry.name)) {
                        if (this.isHovering()) {
                            fontOptions.alpha(204);
                        } else {
                            fontOptions.alpha(153);
                        }
                    }

                    String drawText = menuEntry.getDisplayName();
                    float drawX = width / 2F - FontManager.bit.getWidthCeil(drawText, fontOptions) / 2F + 10;
                    FontManager.bit.drawString(drawX, getY() + (height - 16) / 2F, drawText, fontOptions);

                    GameTexture gameTexture = menuEntry.getTexture();
                    if (gameTexture != null && (!menuEntry.name.startsWith("classes.") || showClassIcons)) {
                        gameTexture.initDraw().size(height).draw(6, getY());
                    }
                }

            }).onClicked((e) -> {
                changeEntry(menuEntry, player);
            }));
        }

        entries.setContentBox(new Rectangle(0, 0, entries.getWidth(), 12 + showEntries.size() * 40));
    }

    public void changeEntry(String entryName, PlayerMob player) {
        if (entryName.startsWith("classes.")) {
            String playerClassStringID = entryName.replace("classes.", "");
            PlayerClass playerClass = PlayerClass.classesList.stream().filter(pClass -> Objects.equals(pClass.stringID, playerClassStringID)).findFirst().orElse(null);
            if (playerClass != null) changeEntry(new ClassEntry(entryName, playerClass), player);
        } else {
            changeEntry(Arrays.stream(menuEntries).filter(entry -> Objects.equals(entry.name, entryName)).findAny().orElse(null), player);
        }
    }

    public void changeEntry(MenuEntry entry, PlayerMob player) {
        if (entry != null) actualEntry = entry;
        updateContent(container, player);
        updateEntries(container, player, null);
    }

    public void updateContent(final MenuContainer container, PlayerMob player) {
        entryForm.clearComponents();

        actualEntry.client = client;
        actualEntry.player = player;
        actualEntry.updateContent(this, entryForm, container);
    }

    public void onWindowResized(GameWindow window) {
        super.onWindowResized(window);
        this.principalForm.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
    }

    public boolean shouldOpenInventory() {
        return false;
    }

    public boolean shouldShowToolbar() {
        return false;
    }

    private void openUrlOrShowExternalLink(GameMessage title, String url, GameTexture qrCodeTexture) {
        if (PlatformManager.getPlatform().getOperatingSystemFamily() == Platform.OperatingSystemFamily.Windows) {
            GameUtils.openURL(url);
        } else {
            ExternalLinkForm externalLink = new ExternalLinkForm(qrCodeTexture, url, title, () -> this.makeCurrent(returnForm));
            this.addAndMakeCurrentTemporary(externalLink);
        }
    }

    public static FormInputSize SIZE_20_SMALL_FONT = new FormInputSize(20, 0, 4, 1) {
        public DrawOptions getButtonDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawWidthComponent(Settings.UI.button_20.getButtonTexture(color, state), x, y, width, drawColor);
        }

        public DrawOptions getButtonDownDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawWidthComponent(Settings.UI.button_20.getButtonDownTexture(color, state), x, y, width, drawColor);
        }

        public DrawOptions getButtonEdgeDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> {
            };
        }

        public DrawOptions getButtonDownEdgeDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> {
            };
        }

        public DrawOptions getFormTabDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawTabWidthComponentFull(state.textureGetter.apply(Settings.UI.formtab_20), x, y, width, drawColor);
        }

        public DrawOptions getFormTabDownDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawTabWidthComponentFull(state.downTextureGetter.apply(Settings.UI.formtab_20), x, y, width, drawColor);
        }

        public DrawOptions getFormTabEdgeDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawTabWidthComponentEdgeFull(state.downTextureGetter.apply(Settings.UI.formtab_20), state.textureGetter.apply(Settings.UI.formtabedge_20), x, y, width, drawColor);
        }

        public DrawOptions getFormTabDownEdgeDrawOptions(GameInterfaceStyle gameInterfaceStyle, ButtonColor color, ButtonState state, int x, int y, int width, Color drawColor) {
            return () -> this.drawTabWidthComponentEdgeFull(state.downTextureGetter.apply(Settings.UI.formtab_20), state.textureGetter.apply(Settings.UI.formtabedge_20), x, y, width, drawColor);
        }

        public Color getButtonColor(GameInterfaceStyle gameInterfaceStyle, ButtonState state) {
            return Settings.UI.button_20.getButtonColor(Settings.UI, state);
        }

        public Color getTextColor(GameInterfaceStyle gameInterfaceStyle, ButtonState state) {
            return Settings.UI.button_20.getTextColor(Settings.UI, state);
        }

        public DrawOptions getInputDrawOptions(GameInterfaceStyle gameInterfaceStyle, int x, int y, int width) {
            return () -> this.drawWidthComponent(Settings.UI.textinput_20, x, y, width, Color.WHITE);
        }

        public FontOptions getFontOptions() {
            return new FontOptions(12);
        }

        public Rectangle getContentRectangle(int width) {
            return new Rectangle(2, 2, width - 4, 16);
        }
    };
}

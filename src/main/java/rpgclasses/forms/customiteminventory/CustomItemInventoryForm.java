package rpgclasses.forms.customiteminventory;

import necesse.engine.Settings;
import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameColor;
import necesse.gfx.fairType.TypeParsers;
import necesse.gfx.forms.components.*;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.presets.containerComponent.ContainerForm;
import necesse.gfx.forms.presets.containerComponent.object.OEInventoryContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTooltips.GameTooltips;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.ui.ButtonColor;
import necesse.gfx.ui.GameInterfaceStyle;
import necesse.inventory.InventoryItem;
import necesse.inventory.container.item.ItemInventoryContainer;
import necesse.inventory.item.miscItem.InternalInventoryItemInterface;
import rpgclasses.RPGResources;

import java.awt.*;
import java.util.function.Supplier;

public class CustomItemInventoryForm<T extends ItemInventoryContainer> extends ContainerForm<T> {
    public static int shownColumns = 4;

    public FormLabelEdit label;
    public FormContentIconButton edit;
    public LocalMessage renameTip;
    public FormContainerSlot[] slots;
    public FormContentBox slotsForm;

    public FormContentIconButton addColumnButton;
    public FormContentIconButton removeColumnButton;

    public CustomItemInventoryForm(Client client, final T container) {
        super(client, 418, 100, container);
        InventoryItem inventoryItem = container.getInventoryItem();
        InternalInventoryItemInterface item = container.inventoryItem;
        FontOptions labelOptions = new FontOptions(20);
        this.label = this.addComponent(new FormLabelEdit("", labelOptions, Settings.UI.activeTextColor, 5, 5, this.getWidth() - 10, 50), -1000);
        this.label.onMouseChangedTyping((e) -> this.runEditUpdate());
        this.label.onSubmit((e) -> this.runEditUpdate());
        this.label.allowCaretSetTyping = item.canChangePouchName();
        this.label.allowItemAppend = true;
        this.label.setParsers(OEInventoryContainerForm.getParsers(labelOptions));
        this.label.setText(inventoryItem == null ? "NULL" : inventoryItem.getItemDisplayName());
        FormFlow iconFlow = new FormFlow(this.getWidth() - 4);
        this.renameTip = new LocalMessage("ui", "renamebutton");
        if (item.canChangePouchName()) {
            this.edit = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.container_rename, this.renameTip));
            this.edit.onClicked((e) -> {
                this.label.setTyping(!this.label.isTyping());
                this.runEditUpdate();
            });
        }

        FormContentIconButton lootAllButton;
        FormContentIconButton sortButton;
        if (item.canQuickStackInventory()) {
            lootAllButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.inventory_quickstack_out, new GameMessage[0]) {
                public GameTooltips getTooltips(PlayerMob perspective) {
                    GameWindow window = WindowManager.getWindow();
                    StringTooltips tooltips = new StringTooltips(Localization.translate("ui", "inventoryquickstack"));
                    if (!window.isKeyDown(340) && !window.isKeyDown(344)) {
                        tooltips.add(Localization.translate("ui", "shiftmoreinfo"), GameColor.LIGHT_GRAY);
                    } else {
                        tooltips.add(Localization.translate("ui", "inventoryquickstackinfo", "key", TypeParsers.getInputParseString(Control.INV_LOCK) + "+" + TypeParsers.getInputParseString(-100)), GameColor.LIGHT_GRAY, 400);
                    }

                    return tooltips;
                }
            });
            lootAllButton.onClicked((e) -> container.quickStackButton.runAndSend());
            lootAllButton.setCooldown(500);
            sortButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.container_loot_all, new GameMessage[0]) {
                public GameTooltips getTooltips(PlayerMob perspective) {
                    GameWindow window = WindowManager.getWindow();
                    StringTooltips tooltips = new StringTooltips(Localization.translate("ui", "inventorytransferall"));
                    if (!window.isKeyDown(340) && !window.isKeyDown(344)) {
                        tooltips.add(Localization.translate("ui", "shiftmoreinfo"), GameColor.LIGHT_GRAY);
                    } else {
                        tooltips.add(Localization.translate("ui", "inventorytransferallinfo", "key", TypeParsers.getInputParseString(Control.INV_LOCK) + "+" + TypeParsers.getInputParseString(-100)), GameColor.LIGHT_GRAY, 400);
                    }

                    return tooltips;
                }
            });
            sortButton.mirrorY();
            sortButton.onClicked((e) -> container.transferAll.runAndSend());
            sortButton.setCooldown(500);
        }

        if (item.canRestockInventory()) {
            lootAllButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.inventory_quickstack_in, new LocalMessage("ui", "inventoryrestock")));
            lootAllButton.onClicked((e) -> container.restockButton.runAndSend());
            lootAllButton.setCooldown(500);
        }

        lootAllButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.container_loot_all, new LocalMessage("ui", "inventorylootall")));
        lootAllButton.onClicked((e) -> container.lootButton.runAndSend());
        lootAllButton.setCooldown(500);
        if (item.canSortInventory()) {
            sortButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, Settings.UI.inventory_sort, new LocalMessage("ui", "inventorysort")));
            sortButton.onClicked((e) -> container.sortButton.runAndSend());
            sortButton.setCooldown(500);
        }

        if (item.canDisablePickup()) {
            final Supplier<Boolean> isDisabled = () -> {
                InventoryItem invItem = container.getInventoryItem();
                return invItem != null && container.inventoryItem.isPickupDisabled(invItem);
            };
            FormContentIconButton disablePickupButton = this.addComponent(new FormContentIconButton(iconFlow.next(-26) - 24, 4, FormInputSize.SIZE_24, ButtonColor.BASE, null, new GameMessage[0]) {
                protected void drawContent(int x, int y, int width, int height) {
                    this.setIcon(isDisabled.get() ? Settings.UI.button_escaped_20 : Settings.UI.button_checked_20);
                    super.drawContent(x, y, width, height);
                }

                public GameTooltips getTooltips(PlayerMob perspective) {
                    return container.inventoryItem.getPickupToggleTooltip(isDisabled.get());
                }
            });
            disablePickupButton.onClicked((e) -> container.setPickupDisabled.runAndSend(!(Boolean) isDisabled.get()));
        }

        this.label.setWidth(iconFlow.next() - 10);

        int style = GameInterfaceStyle.styles.indexOf(Settings.UI);

        FormFlow iconFlow2 = new FormFlow(this.getWidth() - 4);

        this.addColumnButton = (FormContentIconButton) this.addComponent(new FormContentIconButton(iconFlow2.next(-26) - 24, 0, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.add_icon[style], new LocalMessage("ui", "addcolumn")))
                .onClicked(e -> {
                    if (shownColumns < (int) Math.ceil(this.slots.length / 10F)) {
                        shownColumns++;
                        runEditColumns();
                        this.setY(this.getY() - 40);
                    }
                });

        this.removeColumnButton = (FormContentIconButton) this.addComponent(new FormContentIconButton(iconFlow2.next(-26) - 24, 0, FormInputSize.SIZE_24, ButtonColor.BASE, RPGResources.UI_TEXTURES.remove_icon[style], new LocalMessage("ui", "removecolumn")))
                .onClicked(e -> {
                    if (shownColumns > 1) {
                        shownColumns--;
                        runEditColumns();
                        this.setY(this.getY() + 40);
                    }
                });

        runEditColumns();
    }

    protected void runEditUpdate() {
        InternalInventoryItemInterface item = this.container.inventoryItem;
        if (item.canChangePouchName()) {
            if (this.label.isTyping()) {
                this.edit.setIcon(Settings.UI.container_rename_save);
                this.renameTip = new LocalMessage("ui", "savebutton");
            } else {
                InventoryItem inventoryItem = this.container.getInventoryItem();
                if (inventoryItem == null) {
                    return;
                }

                if (!this.label.getText().equals(item.getPouchName(inventoryItem))) {
                    item.setPouchName(inventoryItem, this.label.getText());
                    this.container.renameButton.runAndSend(this.label.getText());
                }

                this.edit.setIcon(Settings.UI.container_rename);
                this.renameTip = new LocalMessage("ui", "renamebutton");
                this.label.setText(inventoryItem.getItemDisplayName());
            }

            this.edit.setTooltips(this.renameTip);
        }
    }

    protected void runEditColumns() {
        int slotsBoxY = 34;
        int slotsBoxHeight = 8 + (36 + 4) * shownColumns;

        if (this.slotsForm == null) {
            this.slotsForm = this.addComponent(new FormContentBox(0, slotsBoxY, this.getWidth(), slotsBoxHeight));

            FormFlow flow = new FormFlow(2);
            this.addSlots(flow);
            flow.next(4);

            this.slotsForm.setContentBox(new Rectangle(0, 0, this.getWidth(), flow.next()));
        } else {
            this.slotsForm.setHeight(slotsBoxHeight);
        }

        int formHeight = slotsBoxY + slotsBoxHeight + 2 + 28;

        this.setHeight(formHeight);
        this.addColumnButton.setY(formHeight - 28);
        this.removeColumnButton.setY(formHeight - 28);
    }

    protected void addSlots(FormFlow flow) {
        this.slots = new FormContainerSlot[this.container.INVENTORY_END - this.container.INVENTORY_START + 1];
        int currentY = flow.next();

        for (int i = 0; i < this.slots.length; ++i) {
            int slotIndex = i + this.container.INVENTORY_START;
            int x = i % 10;
            if (x == 0) {
                currentY = flow.next(40);
            }

            this.slots[i] = slotsForm.addComponent(new FormContainerSlot(this.client, this.container, slotIndex, 4 + x * 40, currentY));
        }
    }
}

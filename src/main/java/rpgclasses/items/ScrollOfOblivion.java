package rpgclasses.items;

import necesse.engine.localization.Localization;
import necesse.engine.network.gameNetworkData.GNDItemMap;
import necesse.engine.util.GameBlackboard;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.placeableItem.consumableItem.ConsumableItem;
import necesse.level.maps.Level;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.geom.Line2D;

public class ScrollOfOblivion extends ConsumableItem {
    public ScrollOfOblivion() {
        super(100, true);
        this.rarity = Rarity.UNCOMMON;
    }

    @Override
    public String canPlace(Level level, int x, int y, PlayerMob player, Line2D playerPositionLine, InventoryItem item, GNDItemMap mapContent) {
        return null;
    }

    @Override
    public InventoryItem onPlace(Level level, int x, int y, PlayerMob player, int seed, InventoryItem item, GNDItemMap mapContent) {
        if (player.isServer()) {
            PlayerData playerData = PlayerDataList.getPlayerData(player);
            playerData.modResetsSendPacket(player.getServerClient(), 2);
        }

        if (this.isSingleUse(player)) {
            item.setAmount(item.getAmount() - 1);
        }

        return super.onPlace(level, x, y, player, seed, item, mapContent);
    }

    @Override
    public ListGameTooltips getTooltips(InventoryItem item, PlayerMob perspective, GameBlackboard blackboard) {
        ListGameTooltips tooltips = super.getTooltips(item, perspective, blackboard);
        tooltips.add(Localization.translate("itemtooltip", "scrollofoblivion"));
        tooltips.add(Localization.translate("global", "rpgmod"));
        return tooltips;
    }
}

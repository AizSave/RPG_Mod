package rpgclasses.buffs.Passive;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.HumanDrawBuff;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameSprite;
import necesse.inventory.InventoryItem;
import necesse.level.gameObject.GameObject;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.registry.RPGBuffs;

public class GrabbedObjectBuff extends PassiveBuff implements HumanDrawBuff {
    public GrabbedObjectBuff() {
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
        activeBuff.addModifier(BuffModifiers.INTIMIDATED, true);
        new ModifierValue<>(BuffModifiers.SPEED, -0.75F).max(0.5F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.SPEED_FLAT).max(0F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.STAMINA_REGEN, activeBuff.owner.buffManager.getModifier(BuffModifiers.STAMINA_CAPACITY) - 1F).apply(activeBuff);
        new ModifierValue<>(BuffModifiers.STAMINA_CAPACITY).max(1F).apply(activeBuff);

    }

    @Override
    public void addHumanDraw(ActiveBuff activeBuff, HumanDrawOptions drawOptions) {
        if (activeBuff.owner.buffManager.hasBuff(RPGBuffs.PASSIVES.GRABBED_OBJECT) && activeBuff.owner.isPlayer) {
            PlayerMob playerMob = (PlayerMob) activeBuff.owner;
            PlayerData playerData = PlayerDataList.getPlayerData(playerMob);
            if (playerData.grabbedObject != null) {
                GameObject gameObject = playerData.grabbedObject;

                drawOptions.addTopDraw(
                        (player, dir, spriteX, spriteY, spriteRes, drawX, drawY, width, height, mirrorX, mirrorY, light, alpha, mask) -> {
                            GameSprite sprite = gameObject.getObjectItem().getWorldItemSprite(new InventoryItem(gameObject.getObjectItem()), player);
                            int spriteWidth = sprite.width * 2;
                            int spriteHeight = sprite.height * 2;
                            return sprite.initDraw().size(spriteWidth, spriteHeight).light(light).pos(drawX + width / 2 - spriteWidth / 2, drawY + height / 2 - spriteHeight / 2 - 32);
                        }
                );
            }
        }

    }
}

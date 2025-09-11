package rpgclasses.content.player.PlayerClasses.Ranger.Passives;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.itemAttacker.ItemAttackSlot;
import necesse.entity.mobs.itemAttacker.ItemAttackerMob;
import necesse.gfx.gameFont.FontManager;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.toolItem.ToolItem;
import necesse.inventory.item.toolItem.projectileToolItem.gunProjectileToolItem.DeathRipperProjectileToolItem;
import necesse.inventory.item.toolItem.projectileToolItem.gunProjectileToolItem.ShardCannonProjectileToolItem;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.projectiles.PlasmaGrenadeProjectile;

import java.util.Objects;

public class PlasmaGrenade extends SimpleBuffPassive {
    public PlasmaGrenade(int levelMax, int requiredClassLevel) {
        super("plasmagrenade", "#00ffff", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                activeBuff.getGndData().setString("weapon", "");
                activeBuff.getGndData().setInt("attacks", 0);
                this.isVisible = false;
            }

            @Override
            public void onItemAttacked(ActiveBuff activeBuff, int targetX, int targetY, ItemAttackerMob attackerMob, int attackHeight, InventoryItem item, ItemAttackSlot slot, int animAttack) {
                if (item.item.type == Item.Type.TOOL) {
                    ToolItem toolItem = (ToolItem) item.item;
                    if (toolItem.getDamageType(item) == DamageTypeRegistry.RANGED && !isSpecialWeapon(toolItem)) {
                        doLogic(activeBuff, attackerMob, toolItem, item, targetX, targetY);
                    }
                }
            }

            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                PlayerMob player = (PlayerMob) activeBuff.owner;
                InventoryItem item = player.getSelectedItemSlot().getItem(player.getInv());
                if (item.item.type == Item.Type.TOOL) {
                    ToolItem toolItem = (ToolItem) item.item;
                    if (toolItem.getDamageType(item) == DamageTypeRegistry.RANGED && isSpecialWeapon(toolItem)) {
                        doLogic(activeBuff, player, toolItem, item, event.target.getX(), event.target.getY());
                    }
                }
            }

            public boolean isSpecialWeapon(ToolItem toolItem) {
                return toolItem instanceof DeathRipperProjectileToolItem
                        || toolItem instanceof ShardCannonProjectileToolItem;
            }

            public void doLogic(ActiveBuff activeBuff, Mob attacker, ToolItem toolItem, InventoryItem item, int targetX, int targetY) {
                if (!Objects.equals(activeBuff.getGndData().getString("weapon"), toolItem.getStringID())) {
                    this.isVisible = true;
                    activeBuff.getGndData().setString("weapon", toolItem.getStringID());
                    activeBuff.getGndData().setInt("attacks", 1);
                } else {
                    int attacks = activeBuff.getGndData().getInt("attacks");
                    if (attacks < 7) {
                        this.isVisible = true;
                        activeBuff.getGndData().setInt("attacks", attacks + 1);
                    } else {
                        this.isVisible = false;
                        activeBuff.getGndData().setInt("attacks", 0);

                        if (activeBuff.owner.isServer()) {
                            GameDamage damage = toolItem.getAttackDamage(item)
                                    .modDamage(getLevel(activeBuff) * 0.3F);
                            attacker.getLevel().entityManager.projectiles.add(new PlasmaGrenadeProjectile(attacker.getLevel(), attacker, attacker.x, attacker.y, targetX, targetY, 200, 2000, damage, 0));
                        }
                    }
                }
            }

            @Override
            public void drawIcon(int x, int y, ActiveBuff activeBuff) {
                super.drawIcon(x, y, activeBuff);
                int attacks = activeBuff.getGndData().getInt("attacks");
                String text = Integer.toString(attacks);
                int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
                FontManager.bit.drawString((float) (x + 28 - width), (float) (y + 30 - FontManager.bit.getHeightCeil(text, durationFontOptions)), text, durationFontOptions);
            }
        };
    }

}

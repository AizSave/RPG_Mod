package rpgclasses.buffs;

import necesse.engine.GlobalData;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import necesse.entity.particle.Particle;
import necesse.gfx.gameFont.FontManager;
import rpgclasses.content.player.Mastery.Mastery;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGModifiers;

import java.awt.*;
import java.util.Objects;

public class MarkedBuff extends Buff {
    public MarkedBuff() {
        this.isVisible = true;
        this.isImportant = true;
        this.canCancel = false;
        this.shouldSave = true;
    }

    public void init(ActiveBuff activeBuff, BuffEventSubscriber eventSubscriber) {
    }

    @Override
    public void onBeforeHit(ActiveBuff activeBuff, MobBeforeHitEvent event) {
        super.onBeforeHit(activeBuff, event);
        PlayerMob player = event.attacker.getFirstPlayerOwner();
        if (MarkedBuff.isMarked(player, event.target)) {
            event.damage = event.damage
                    .modDamage(1F + player.buffManager.getModifier(RPGModifiers.FOCUS_DAMAGE))
                    .setCritChance(event.damage.baseCritChance + 0.1F);
        }
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        float d = (float) activeBuff.owner.getSelectBox().width / 2;

        Client client = ((MainGame) GlobalData.getCurrentState()).getClient();
        if (isMarked(client.getPlayer(), activeBuff.owner)) {
            Color color = new Color(255, 0, 0);
            int particles = (int) (Math.PI * d / 2);
            for (int i = 0; i < particles; i++) {
                float angle = (float) i / particles * 360;
                float dx = (float) Math.sin(Math.toRadians(angle)) * d;
                float dy = (float) Math.cos(Math.toRadians(angle)) * d / 2;
                activeBuff.owner.getLevel().entityManager.addParticle(activeBuff.owner.x + dx, activeBuff.owner.y + dy, new ParticleTypeSwitcher(Particle.GType.CRITICAL, Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC).next()).color(color).height(0).lifeTime(100);
            }
        }
    }

    public static void markMob(PlayerMob attacker, Mob target, int duration) {
        if (attacker.isServer()) {
            ActiveBuff ab = new ActiveBuff(RPGBuffs.MARKED, target, duration, attacker);
            ab.getGndData().setString("playerAttacker", attacker.playerName);
            PlayerData playerData = PlayerDataList.getPlayerData(attacker);
            if (playerData.hasMasterySkill(Mastery.MARKSMAN)) ab.getGndData().setBoolean("markedToAll", true);
            if (playerData.hasMasterySkill(Mastery.HUNTER))
                target.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, target, 5F, null), true);
            target.addBuff(ab, true);
        }
    }

    public static boolean isMarked(PlayerMob attacker, Mob target) {
        if (attacker == null || target == null) {
            return false;
        }
        if (!target.buffManager.hasBuff(RPGBuffs.MARKED)) {
            return false;
        }
        ActiveBuff activeBuff = target.buffManager.getBuff(RPGBuffs.MARKED);
        return activeBuff.getGndData().getBoolean("markedToAll") || Objects.equals(attacker.playerName, activeBuff.getGndData().getString("playerAttacker"));
    }

    @Override
    public void drawIcon(int x, int y, ActiveBuff activeBuff) {
        super.drawIcon(x, y, activeBuff);
        if (activeBuff.getAttacker() != null && activeBuff.getAttacker().getAttackOwner() != null) {
            String text = activeBuff.getAttacker().getAttackOwner().getDisplayName();
            int width = FontManager.bit.getWidthCeil(text, durationFontOptions);
            FontManager.bit.drawString((float) (x + 16 - width / 2), (float) (y + 44), text, durationFontOptions);
        }
    }
}

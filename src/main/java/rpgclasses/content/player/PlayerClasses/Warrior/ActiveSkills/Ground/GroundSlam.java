package rpgclasses.content.player.PlayerClasses.Warrior.ActiveSkills.Ground;

import aphorea.registry.AphBuffs;
import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGColors;

import java.awt.*;

public class GroundSlam extends ActiveSkill {

    public GroundSlam(int levelMax, int requiredClassLevel) {
        super("groundslam", RPGColors.HEX.dirt, levelMax, requiredClassLevel);
    }

    @Override
    public void run(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.run(player, playerData, activeSkillLevel, seed, isInUse);

        ActiveBuff ab = new ActiveBuff(AphBuffs.STOP, player, 300, null);
        player.buffManager.addBuff(ab, false);

        Color colorArea = RPGColors.dirt;
        if (player.isClient()) {
            Color debrisColor = player.getLevel().getTile(player.getTileX(), player.getTileY()).getDebrisColor(player.getLevel(), player.getTileX(), player.getTileY());
            if (debrisColor != null) colorArea = debrisColor;
        }

        AphAreaList areaList = new AphAreaList(
                new AphArea(120, colorArea)
                        .setDebuffArea(1000 * activeSkillLevel, AphBuffs.STUN.getStringID())
                        .setDamageArea(new GameDamage(DamageTypeRegistry.MELEE, 5 * playerData.getStrength(player)))
        );
        areaList.execute(player, false);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.punch, SoundEffect.effect(player.x, player.y).volume(2F).pitch(0.5F));
        player.getClient().startCameraShake(player.x, player.y, 300, 40, 3.0F, 3.0F, true);
    }

    @Override
    public int getBaseCooldown() {
        return 12000;
    }
}

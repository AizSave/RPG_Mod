package rpgclasses.content.player.PlayerClasses.Necromancer.ActiveSkills;

import aphorea.utils.magichealing.AphMagicHealing;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.utils.RPGUtils;

public class Sacrifice extends ActiveSkill {

    public Sacrifice(int levelMax, int requiredClassLevel) {
        super("sacrifice", "#990000", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        Mob sacrifice = RPGUtils.findClosestDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player));
        if (sacrifice != null) {
            sacrifice.remove(0, 0, null, true);
            AphMagicHealing.healMob(player, player, (int) (10 + activeSkillLevel * (playerData.getEndurance(player) + playerData.getIntelligence(player)) - playerData.getGrace(player)));
        }
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyDamageableFollower(player, 1024, RPGUtils.isNecroticFollowerFilter(player)) ? null : "notargetfollower";
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.crack, SoundEffect.effect(player.x, player.y).volume(1F).pitch(0.5F));
        SoundManager.playSound(GameResources.croneLaugh, SoundEffect.effect(player.x, player.y).volume(0.5F).pitch(0.5F));
    }

    @Override
    public int getBaseCooldown() {
        return 26000;
    }
}

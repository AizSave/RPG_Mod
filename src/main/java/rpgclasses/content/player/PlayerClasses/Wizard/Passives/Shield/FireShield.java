package rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Params.SkillParam;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.levelevents.FireExplosionLevelEvent;

public class FireShield extends SimpleBuffPassive {
    public static SkillParam[] params = new SkillParam[]{
            SkillParam.damageParam(3),
            SkillParam.staticParam(26)
    };

    @Override
    public SkillParam[] getParams() {
        return params;
    }

    public FireShield(int levelMax, int requiredClassLevel) {
        super("fireshield", "#ff3300", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new ArcaneShield.MagicShieldBuff(getColor(), params[1].value(),
                (activeBuff, skillLevel, playerData) -> {
                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    player.getLevel().entityManager.events.add(new FireExplosionLevelEvent(player.x, player.y, 250, new GameDamage(DamageTypeRegistry.MAGIC, params[0].value(playerData.getLevel(), skillLevel)), player, false, true));
                }
        );
    }
}

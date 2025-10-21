package rpgclasses.content.player.PlayerClasses.Druid.Passives;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsLogic.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGTiles;

public class NaturesFavor extends SimpleBuffPassive {
    public NaturesFavor(int levelMax, int requiredClassLevel) {
        super("naturesfavor", "#00ff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
                super.init(activeBuff, buffEventSubscriber);
                updateBuff(activeBuff);
            }

            @Override
            public void clientTick(ActiveBuff activeBuff) {
                super.clientTick(activeBuff);
                updateBuff(activeBuff);
            }

            @Override
            public void serverTick(ActiveBuff activeBuff) {
                super.serverTick(activeBuff);
                updateBuff(activeBuff);
            }

            public void updateBuff(ActiveBuff activeBuff) {
                boolean inGrass = RPGTiles.isInGrassTile(activeBuff.owner) && !activeBuff.owner.isFlying();

                this.isVisible = inGrass;
                activeBuff.setModifier(
                        BuffModifiers.COMBAT_HEALTH_REGEN_FLAT, inGrass ? getLevel(activeBuff) : 0F
                );
            }
        };
    }
}

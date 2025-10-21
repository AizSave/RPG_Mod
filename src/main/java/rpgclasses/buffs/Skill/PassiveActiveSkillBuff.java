package rpgclasses.buffs.Skill;

public class PassiveActiveSkillBuff extends ActiveSkillBuff {
    public PassiveActiveSkillBuff() {
        this.isPassive = true;
    }

    @Override
    public String skillID() {
        return this.getStringID().replace("passiveactiveskillbuff", "");
    }

}
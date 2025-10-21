package rpgclasses.registry;

import necesse.engine.commands.CommandsManager;
import rpgclasses.commands.ModExp;
import rpgclasses.commands.ModResets;

public class RPGCommands {

    public static void registerCore() {
        CommandsManager.registerServerCommand(new ModExp());
        CommandsManager.registerServerCommand(new ModResets());
    }

}

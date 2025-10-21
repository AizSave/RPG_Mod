package rpgclasses.registry;

import aphorea.registry.AphRecipes;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.RecipeTechRegistry;
import necesse.inventory.recipe.Ingredient;

public class RPGRecipes {

    public static void initRecipes() {
        AphRecipes.addCraftingList("diamondbackpack", RecipeTechRegistry.WORKSTATION,
                AphRecipes.AphCraftingRecipe.showAfter("ringsbox", 1,
                        new Ingredient("anylog", 10),
                        new Ingredient("ironbar", 1)
                )
        );

        AphRecipes.addCraftingList("lifespinel", RecipeTechRegistry.FALLEN_WORKSTATION,
                AphRecipes.AphCraftingRecipe.showBefore("scrollofoblivion", 1,
                        new Ingredient("upgradeshard", 50),
                        new Ingredient("recallscroll", 2)
                ),
                AphRecipes.AphCraftingRecipe.showAfter("scrollofoblivion", 1,
                        new Ingredient("upgradeshard", 50),
                        new Ingredient("teleportationscroll", 1)
                )
        );
    }

}
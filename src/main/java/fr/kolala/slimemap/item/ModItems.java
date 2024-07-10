package fr.kolala.slimemap.item;

import fr.kolala.slimemap.SlimeMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item SLIME_MAP = register(new EmptySlimeMapItem(new FabricItemSettings()), "slime_map");
    public static final Item FILLED_SLIME_MAP = register(new FilledSlimeMapItem(new Item.Settings()), "filled_slime_map");

    public static Item register(Item item, String id) {
        Identifier itemID = Identifier.of(SlimeMap.MOD_ID, id);

        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> itemGroup.add(ModItems.SLIME_MAP));
    }
}
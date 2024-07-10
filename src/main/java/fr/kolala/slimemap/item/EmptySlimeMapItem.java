package fr.kolala.slimemap.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EmptyMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EmptySlimeMapItem extends EmptyMapItem {
    public EmptySlimeMapItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(itemStack);
        }
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        user.getWorld().playSoundFromEntity(null, user, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, user.getSoundCategory(), 1.0f, 1.0f);
        ItemStack itemStack2 = FilledSlimeMapItem.createMap(world, user.getBlockX(), user.getBlockZ(), (byte)0, true, false);
        if (itemStack.isEmpty()) {
            return TypedActionResult.consume(itemStack2);
        }
        if (!user.getInventory().insertStack(itemStack2.copy())) {
            user.dropItem(itemStack2, false);
        }
        return TypedActionResult.consume(itemStack);
    }
}
package fr.kolala.slimemap.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.kolala.slimemap.item.FilledSlimeMapItem;
import fr.kolala.slimemap.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {
    @Shadow public abstract ItemStack getHeldItemStack();

    protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
        super(type, world, pos);
    }

    @Inject(method = "getMapId", at = @At("TAIL"), cancellable = true)
    public void getMapIdInject(CallbackInfoReturnable<OptionalInt> cir) {
        Integer integer1;
        ItemStack itemStack1 = this.getHeldItemStack();
        if (itemStack1.isOf(ModItems.FILLED_SLIME_MAP) && (integer1 = FilledSlimeMapItem.getMapId(itemStack1)) != null) {
            cir.setReturnValue(OptionalInt.of(integer1));
        }
    }

    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0), cancellable = true)
    public void interactInject(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir, @Local(ordinal = 0)ItemStack itemStack) {
        MapState mapState1;
        if (itemStack.isOf(ModItems.FILLED_SLIME_MAP)) {
            if ((mapState1 = FilledSlimeMapItem.getMapState(itemStack, this.getWorld())) != null && mapState1.iconCountNotLessThan(256)) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
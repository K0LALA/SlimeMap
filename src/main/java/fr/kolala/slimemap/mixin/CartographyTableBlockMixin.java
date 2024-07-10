package fr.kolala.slimemap.mixin;

import fr.kolala.slimemap.screen.CustomCartographyTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CartographyTableBlock.class)
public class CartographyTableBlockMixin {
    @Shadow @Final private static Text TITLE;

    /**
     * @author Kolala
     * @reason Use {@link CustomCartographyTableScreenHandler}
     */
    @Overwrite
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new CustomCartographyTableScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), TITLE);
    }
}
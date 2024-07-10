package fr.kolala.slimemap.mixin;

import fr.kolala.slimemap.item.FilledSlimeMapItem;
import fr.kolala.slimemap.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Environment(value = EnvType.CLIENT)
@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends HandledScreen<CartographyTableScreenHandler> {
    @Shadow @Final private static Identifier TEXTURE;

    @Shadow @Final private static Identifier ERROR_TEXTURE;

    @Shadow protected abstract void drawMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand);

    public CartographyTableScreenMixin(CartographyTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * @author Kolala
     * @reason Drawing for slime map as well
     */
    @Overwrite
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        MapState mapState = null;
        Integer integer = null;
        int i = this.x;
        int j = this.y;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        ItemStack itemStack = this.handler.getSlot(1).getStack();
        boolean bl = itemStack.isOf(Items.MAP);
        boolean bl2 = itemStack.isOf(Items.PAPER);
        boolean bl3 = itemStack.isOf(Items.GLASS_PANE);
        ItemStack itemStack2 = this.handler.getSlot(0).getStack();
        boolean bl4 = false;
        if (itemStack2.isOf(Items.FILLED_MAP)) {
            integer = FilledMapItem.getMapId(itemStack2);
            assert this.client != null;
            mapState = FilledMapItem.getMapState(integer, this.client.world);
        } else if (itemStack2.isOf(ModItems.FILLED_SLIME_MAP)) {
            integer = FilledSlimeMapItem.getMapId(itemStack2);
            assert this.client != null;
            mapState = FilledSlimeMapItem.getMapState(integer, this.client.world);
        }

        if (mapState != null) {
            if (mapState.locked) {
                bl4 = true;
                if (bl2 || bl3) {
                    context.drawGuiTexture(ERROR_TEXTURE, i + 35, j + 31, 28, 21);
                }
            }
            if (bl2 && mapState.scale >= 4) {
                bl4 = true;
                context.drawGuiTexture(ERROR_TEXTURE, i + 35, j + 31, 28, 21);
            }
        }
        this.drawMap(context, integer, mapState, bl, bl2, bl3, bl4);
    }
}
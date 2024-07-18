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
import org.spongepowered.asm.mixin.*;

@Environment(value = EnvType.CLIENT)
@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends HandledScreen<CartographyTableScreenHandler> {
    @Unique
    private static final Identifier SCALED_SLIME_MAP_TEXTURE = new Identifier("slimemap", "container/cartography_table/scaled_slime_map");
    @Unique
    private static final Identifier DUPLICATED_SLIME_MAP_TEXTURE = new Identifier("slimemap", "container/cartography_table/duplicated_slime_map");
    @Unique
    private static final Identifier SLIME_MAP_TEXTURE = new Identifier("slimemap", "container/cartography_table/slime_map");

    @Shadow @Final private static Identifier ERROR_TEXTURE;
    @Shadow @Final private static Identifier LOCKED_TEXTURE;
    @Shadow @Final private static Identifier TEXTURE;

    @Shadow protected abstract void drawMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand);
    @Shadow protected abstract void drawMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, int x, int y, float scale);


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
        boolean bl = itemStack.isOf(Items.MAP) || itemStack.isOf(ModItems.SLIME_MAP);
        boolean bl2 = itemStack.isOf(Items.PAPER) || itemStack.isOf(Items.SLIME_BALL);
        boolean bl3 = itemStack.isOf(Items.GLASS_PANE);
        ItemStack itemStack2 = this.handler.getSlot(0).getStack();
        boolean bl4 = false;
        boolean isSlimeMap = false;
        if (itemStack2.isOf(Items.FILLED_MAP)) {
            integer = FilledMapItem.getMapId(itemStack2);
            assert this.client != null;
            mapState = FilledMapItem.getMapState(integer, this.client.world);
        } else if (itemStack2.isOf(ModItems.FILLED_SLIME_MAP)) {
            integer = FilledSlimeMapItem.getMapId(itemStack2);
            assert this.client != null;
            mapState = FilledSlimeMapItem.getMapState(integer, this.client.world);
            isSlimeMap = true;
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
        if (isSlimeMap)
            this.drawSlimeMap(context, integer, mapState, bl, bl2, bl3, bl4);
        else
            this.drawMap(context, integer, mapState, bl, bl2, bl3, bl4);
    }

    @Unique
    private void drawSlimeMap(DrawContext context, @Nullable Integer mapId, @Nullable MapState mapState, boolean cloneMode, boolean expandMode, boolean lockMode, boolean cannotExpand) {
        int i = this.x;
        int j = this.y;
        if (expandMode && !cannotExpand) {
            context.drawGuiTexture(SCALED_SLIME_MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 85, j + 31, 0.226f);
        } else if (cloneMode) {
            context.drawGuiTexture(DUPLICATED_SLIME_MAP_TEXTURE, i + 67 + 16, j + 13, 50, 66);
            this.drawMap(context, mapId, mapState, i + 86, j + 16, 0.34f);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 1.0f);
            context.drawGuiTexture(DUPLICATED_SLIME_MAP_TEXTURE, i + 67, j + 13 + 16, 50, 66);
            this.drawMap(context, mapId, mapState, i + 70, j + 32, 0.34f);
            context.getMatrices().pop();
        } else if (lockMode) {
            context.drawGuiTexture(SLIME_MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45f);
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, 0.0f, 1.0f);
            context.drawGuiTexture(LOCKED_TEXTURE, i + 118, j + 60, 10, 14);
            context.getMatrices().pop();
        } else {
            context.drawGuiTexture(SLIME_MAP_TEXTURE, i + 67, j + 13, 66, 66);
            this.drawMap(context, mapId, mapState, i + 71, j + 17, 0.45f);
        }
    }
}
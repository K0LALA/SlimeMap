package fr.kolala.slimemap.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FilledSlimeMapItem extends FilledMapItem {

    public FilledSlimeMapItem(Settings settings) {
        super(settings);
    }

    private static final String MAP_KEY = "map";

    public static ItemStack createMap(World world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
        ItemStack itemStack = new ItemStack(ModItems.FILLED_SLIME_MAP);
        FilledSlimeMapItem.createMapState(itemStack, world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
        return itemStack;
    }

    @Nullable
    public static MapState getMapState(@Nullable Integer id, World world) {
        return id == null ? null : world.getMapState(FilledSlimeMapItem.getMapName(id));
    }

    @Nullable
    public static MapState getMapState(ItemStack map, World world) {
        Integer integer = FilledSlimeMapItem.getMapId(map);
        return FilledSlimeMapItem.getMapState(integer, world);
    }

    @Nullable
    public static Integer getMapId(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.contains(MAP_KEY, NbtElement.NUMBER_TYPE) ? nbtCompound.getInt(MAP_KEY) : null;
    }

    private static int allocateMapId(World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        MapState mapState = MapState.of(x, z, (byte)scale, showIcons, unlimitedTracking, dimension);
        int i = world.getNextMapId();
        world.putMapState(FilledSlimeMapItem.getMapName(i), mapState);
        return i;
    }

    private static void setMapId(ItemStack stack, int id) {
        stack.getOrCreateNbt().putInt(MAP_KEY, id);
    }

    private static void createMapState(ItemStack stack, World world, int x, int z, int scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        int i = FilledSlimeMapItem.allocateMapId(world, x, z, scale, showIcons, unlimitedTracking, dimension);
        FilledSlimeMapItem.setMapId(stack, i);
    }

    public static String getMapName(int mapId) {
        return "map_" + mapId;
    }

    private boolean isSlimeChunk(long seed, int xPos, int zPos) {
        Random random = new Random(seed +
                ((long) xPos * xPos * 0x4c1906) +
                (xPos * 0x5ac0dbL) +
                ((long) zPos * zPos) * 0x4307a7L +
                (zPos * 0x5f24fL) ^ 0x3ad8025fL);
        return random.nextInt(10) == 0;
    }

    @Override
    public void updateColors(World world, Entity entity, MapState state) {
        if (world.getRegistryKey() != World.OVERWORLD || !(entity instanceof PlayerEntity) || world.getServer() == null) {
            return;
        }
        long seed = world.getServer().getSaveProperties().getGeneratorOptions().getSeed();
        int i = 1 << state.scale;
        int j = state.centerX;
        int k = state.centerZ;
        int l = MathHelper.floor(entity.getX() - (double)j) / i + 64;
        int m = MathHelper.floor(entity.getZ() - (double)k) / i + 64;
        int n = 128 / i;
        MapState.PlayerUpdateTracker playerUpdateTracker = state.getPlayerSyncData((PlayerEntity)entity);
        ++playerUpdateTracker.field_131;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        BlockPos.Mutable mutable2 = new BlockPos.Mutable();
        boolean bl = false;
        for (int o = l - n + 1; o < l + n; ++o) {
            if ((o & 0xF) != (playerUpdateTracker.field_131 & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int p = m - n - 1; p < m + n; ++p) {
                double f;
                if (o < 0 || p < -1 || o >= 128 || p >= 128) continue;
                int q = MathHelper.square(o - l) + MathHelper.square(p - m);
                boolean bl2 = q > (n - 2) * (n - 2);
                int r = (j / i + o - 64) * i;
                int s = (k / i + p - 64) * i;
                LinkedHashMultiset<MapColor> multiset = LinkedHashMultiset.create();
                WorldChunk worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(r), ChunkSectionPos.getSectionCoord(s));
                if (worldChunk.isEmpty()) continue;
                int t = 0;
                double e = 0.0;
                for (int u = 0; u < i; ++u) {
                    for (int v = 0; v < i; ++v) {
                        BlockState blockState;
                        mutable.set(r + u, 0, s + v);
                        int w = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, mutable.getX(), mutable.getZ()) + 1;
                        if (isSlimeChunk(seed, worldChunk.getPos().x, worldChunk.getPos().z)) {
                            // TODO: Add config to set color of slime chunks and if they are erasing what's below or not
                            blockState = Blocks.SLIME_BLOCK.getDefaultState();
                            //multiset.add(MapColor.PALE_GREEN, Integer.MAX_VALUE);
                        }
                        else if (w > world.getBottomY() + 1) {
                            do {
                                mutable.setY(--w);
                            } while ((blockState = worldChunk.getBlockState(mutable)).getMapColor(world, mutable) == MapColor.CLEAR && w > world.getBottomY());
                            if (w > world.getBottomY() && !blockState.getFluidState().isEmpty()) {
                                BlockState blockState2;
                                int x = w - 1;
                                mutable2.set(mutable);
                                do {
                                    mutable2.setY(x--);
                                    blockState2 = worldChunk.getBlockState(mutable2);
                                    ++t;
                                } while (x > world.getBottomY() && !blockState2.getFluidState().isEmpty());
                                blockState = this.getFluidStateIfVisible(world, blockState, mutable);
                            }
                        } else {
                            blockState = Blocks.BEDROCK.getDefaultState();
                        }
                        state.removeBanner(world, mutable.getX(), mutable.getZ());
                        e += (double)w / (double)(i * i);
                        multiset.add(blockState.getMapColor(world, mutable));
                    }
                }
                MapColor mapColor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.CLEAR);
                MapColor.Brightness brightness = mapColor == MapColor.WATER_BLUE ? ((f = (double) (t / (i * i)) * 0.1 + (double)(o + p & 1) * 0.2) < 0.5 ? MapColor.Brightness.HIGH : (f > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL)) : ((f = (e - d) * 4.0 / (double)(i + 4) + ((double)(o + p & 1) - 0.5) * 0.4) > 0.6 ? MapColor.Brightness.HIGH : (f < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL));
                d = e;
                if (p < 0 || q >= n * n || bl2 && (o + p & 1) == 0) continue;
                bl |= state.putColor(o, p, mapColor.getRenderColorByte(brightness));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) {
            return;
        }
        MapState mapState = FilledSlimeMapItem.getMapState(stack, world);
        if (mapState == null) {
            return;
        }
        if (entity instanceof PlayerEntity playerEntity) {
            mapState.update(playerEntity, stack);
        }
        if (!mapState.locked && (selected || entity instanceof PlayerEntity && ((PlayerEntity)entity).getOffHandStack() == stack)) {
            this.updateColors(world, entity, mapState);
        }
    }

    private BlockState getFluidStateIfVisible(World world, BlockState state, BlockPos pos) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && !state.isSideSolidFullSquare(world, pos, Direction.UP)) {
            return fluidState.getBlockState();
        }
        return state;
    }

    private static void scale(ItemStack map, World world, int amount) {
        MapState mapState = FilledSlimeMapItem.getMapState(map, world);
        if (mapState != null) {
            int i = world.getNextMapId();
            world.putMapState(FilledSlimeMapItem.getMapName(i), mapState.zoomOut(amount));
            FilledSlimeMapItem.setMapId(map, i);
        }
    }

    public static void copyMap(World world, ItemStack stack) {
        MapState mapState = FilledSlimeMapItem.getMapState(stack, world);
        if (mapState != null) {
            int i = world.getNextMapId();
            String string = FilledSlimeMapItem.getMapName(i);
            MapState mapState2 = mapState.copy();
            world.putMapState(string, mapState2);
            FilledSlimeMapItem.setMapId(stack, i);
        }
    }

    private static Text getIdText(int id) {
        return Text.translatable("filled_map.id", id).formatted(Formatting.GRAY);
    }

    public static Text getIdText(ItemStack stack) {
        return FilledSlimeMapItem.getIdText(FilledSlimeMapItem.getMapId(stack));
    }

    public static int getMapColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt("display");
        if (nbtCompound != null && nbtCompound.contains("MapColor", NbtElement.NUMBER_TYPE)) {
            int i = nbtCompound.getInt("MapColor");
            return 0xFF000000 | i & 0xFFFFFF;
        }
        return -12173266;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (blockState.isIn(BlockTags.BANNERS)) {
            MapState mapState;
            if (!context.getWorld().isClient && (mapState = FilledSlimeMapItem.getMapState(context.getStack(), context.getWorld())) != null && !mapState.addBanner(context.getWorld(), context.getBlockPos())) {
                return ActionResult.FAIL;
            }
            return ActionResult.success(context.getWorld().isClient);
        }
        return super.useOnBlock(context);
    }
}
package modernmods.modernfoundry.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.world.item.OreberryItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Age-based berry bush ported from Oreberries' native block behavior. */
public class OreberryBushBlock extends Block implements BonemealableBlock {
  public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
  private static final VoxelShape[] SHAPES = {
    Block.box(4, 0, 4, 12, 8, 12),
    Block.box(2, 0, 2, 14, 12, 14),
    Block.box(0, 0, 0, 16, 16, 16),
    Block.box(0, 0, 0, 16, 16, 16)
  };
  private static final VoxelShape[] COLLISION_SHAPES = {
    SHAPES[0],
    SHAPES[1],
    Block.box(1, 0, 1, 15, 15, 15),
    Block.box(1, 0, 1, 15, 15, 15)
  };

  private final Supplier<? extends Item> berries;
  private final boolean growsInLight;

  public OreberryBushBlock(Properties properties, Supplier<? extends Item> berries, boolean growsInLight) {
    super(properties);
    this.berries = berries;
    this.growsInLight = growsInLight;
    registerDefaultState(stateDefinition.any().setValue(AGE, 0));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPES[state.getValue(AGE)];
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return COLLISION_SHAPES[state.getValue(AGE)];
  }

  /** Left-click harvesting is part of the original Oreberries interaction contract. */
  @Override
  public void attack(BlockState state, Level level, BlockPos pos, Player player) {
    harvest(state, level, pos, player);
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (canGrowAt(level, pos, state) && random.nextDouble() < Config.COMMON.oreberriesTickGrowthChance.get()) {
      level.setBlock(pos, state.cycle(AGE), 2);
    }
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
    return canGrowAt(level, pos, state);
  }

  @Override
  public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
    return random.nextDouble() < Config.COMMON.oreberriesBonemealGrowthChance.get();
  }

  @Override
  public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    level.setBlock(pos, state.cycle(AGE), 2);
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    return harvest(state, level, pos, player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
    return harvest(state, level, pos, player)
      ? ItemInteractionResult.sidedSuccess(level.isClientSide)
      : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  private boolean harvest(BlockState state, Level level, BlockPos pos, Player player) {
    if (state.getValue(AGE) < 3) {
      return false;
    }
    if (!level.isClientSide) {
      level.setBlock(pos, state.setValue(AGE, 2), 2);
      int count = 1 + level.getRandom().nextInt(3);
      ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(berries.get(), count));
    }
    return true;
  }

  private boolean canGrowAt(LevelReader level, BlockPos pos, BlockState state) {
    return state.getValue(AGE) < 3
      && (growsInLight || level.getMaxLocalRawBrightness(pos) < 10);
  }

  @Nullable
  @Override
  public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
    return PathType.DAMAGE_OTHER;
  }

  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (entity instanceof LivingEntity && !(entity instanceof ItemEntity)) {
      entity.hurt(entity.damageSources().cactus(), 1.0F);
    }
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    if (!growsInLight && level.getMaxLocalRawBrightness(pos) >= 13) {
      return false;
    }
    BlockPos soilPos = pos.below();
    BlockState soil = level.getBlockState(soilPos);
    return soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.COARSE_DIRT)
      || soil.is(Blocks.ROOTED_DIRT) || soil.is(Blocks.SAND)
      || soil.is(this) && soil.getValue(AGE) >= 2;
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = defaultBlockState();
    return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    List<ItemStack> drops = new ArrayList<>();
    ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
    int silkTouch = 0;
    if (!tool.isEmpty()) {
      silkTouch = EnchantmentHelper.getItemEnchantmentLevel(
        params.getLevel().registryAccess().holderOrThrow(Enchantments.SILK_TOUCH), tool);
    }
    if (silkTouch >= Config.COMMON.oreberriesSilkTouchRequirement.get()) {
      drops.add(new ItemStack(this.asItem()));
    }
    return drops;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
    Item item = berries.get();
    if (item instanceof OreberryItem oreberry) {
      tooltip.add(Component.translatable(oreberry.getDescriptionId() + ".tooltip"));
    }
    super.appendHoverText(stack, context, tooltip, flag);
  }
}

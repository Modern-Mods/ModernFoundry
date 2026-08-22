package modernmods.modernfoundry.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import modernmods.hilt.network.NetworkWrapper.PacketDirection;
import modernmods.hilt.network.NetworkWrapper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.definition.UpdateMaterialsPacket;
import modernmods.modernfoundry.library.materials.stats.UpdateMaterialStatsPacket;
import modernmods.modernfoundry.library.materials.traits.UpdateMaterialTraitsPacket;
import modernmods.modernfoundry.library.modifiers.UpdateModifiersPacket;
import modernmods.modernfoundry.library.modifiers.fluid.UpdateFluidEffectsPacket;
import modernmods.modernfoundry.library.tools.definition.UpdateToolDefinitionDataPacket;
import modernmods.modernfoundry.library.tools.layout.UpdateTinkerSlotLayoutsPacket;
import modernmods.modernfoundry.shared.network.GeneratePartTexturesPacket;
import modernmods.modernfoundry.smeltery.network.ChannelFlowPacket;
import modernmods.modernfoundry.smeltery.network.FaucetActivationPacket;
import modernmods.modernfoundry.smeltery.network.FluidUpdatePacket;
import modernmods.modernfoundry.smeltery.network.SmelteryFluidClickedPacket;
import modernmods.modernfoundry.smeltery.network.SmelteryTankUpdatePacket;
import modernmods.modernfoundry.smeltery.network.StructureErrorPositionPacket;
import modernmods.modernfoundry.smeltery.network.StructureUpdatePacket;
import modernmods.modernfoundry.tables.network.StationTabPacket;
import modernmods.modernfoundry.tables.network.TinkerStationRenamePacket;
import modernmods.modernfoundry.tables.network.TinkerStationSelectionPacket;
import modernmods.modernfoundry.tables.network.UpdateCraftingRecipePacket;
import modernmods.modernfoundry.tables.network.UpdateStationScreenPacket;
import modernmods.modernfoundry.tables.network.UpdateTinkerStationRecipePacket;
import modernmods.modernfoundry.tools.network.EntityMovementChangePacket;
import modernmods.modernfoundry.tools.network.InteractWithAirPacket;
import modernmods.modernfoundry.tools.network.LevelUpPacket;
import modernmods.modernfoundry.tools.network.PushBlockRowPacket;
import modernmods.modernfoundry.tools.network.SyncProjectileModifiersPacket;
import modernmods.modernfoundry.tools.network.TinkerControlPacket;
import modernmods.modernfoundry.tools.network.ToolContainerFluidUpdatePacket;
import modernmods.modernfoundry.tools.network.YoyoCollectedDropsPacket;
import modernmods.modernfoundry.tools.network.YoyoHandSyncPacket;
import modernmods.modernfoundry.tools.network.YoyoToggleAttackPacket;
import modernmods.modernfoundry.tools.network.YoyoToggleEnchantmentPacket;
import modernmods.modernfoundry.thinking.common.networking.packet.packet.ItemStackSyncS2CPacket;
import modernmods.modernfoundry.integrations.network.ArsElementalSetData;
import modernmods.modernfoundry.integrations.network.BotaniaSetData;
import modernmods.modernfoundry.integrations.network.LaunchGhostSword;

import javax.annotation.Nullable;

/**
 * Base network class for all tinkers logic
 * <p>
 * In general, if you need to send packets you should use your own network class
 */
public class TinkerNetwork extends NetworkWrapper {
  private static TinkerNetwork instance = null;

  /*
   * Network versions:
   * 1: 3.10.1 and before
   * 2: 3.10.2 - new material stat type; item removal
   * 3: 3.11.2+ - lost track of how much changed but its a lot
   */
  private TinkerNetwork() {
    super(TConstruct.getResource("network"), "3");
  }

  /** Gets the instance of the network */
  public static TinkerNetwork getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Attempt to call network getInstance before network is setup");
    }
    return instance;
  }

  /**
   * Called during mod construction to setup the network
   */
  public static void setup() {
    if (instance != null) {
      return;
    }
    instance = new TinkerNetwork();

    // shared
    instance.registerPacket(InventorySlotSyncPacket.class, InventorySlotSyncPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateNeighborsPacket.class, UpdateNeighborsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(GeneratePartTexturesPacket.class, GeneratePartTexturesPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(SyncPersistentDataPacket.class, SyncPersistentDataPacket::new, PacketDirection.PLAY_TO_CLIENT);

    // gadgets
    instance.registerPacket(EntityMovementChangePacket.class, EntityMovementChangePacket::new, PacketDirection.PLAY_TO_CLIENT);

    // tables
    instance.registerPacket(StationTabPacket.class, StationTabPacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(TinkerStationRenamePacket.class, TinkerStationRenamePacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(UpdateCraftingRecipePacket.class, UpdateCraftingRecipePacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(TinkerStationSelectionPacket.class, TinkerStationSelectionPacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(UpdateTinkerSlotLayoutsPacket.class, UpdateTinkerSlotLayoutsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateStationScreenPacket.class, buf -> UpdateStationScreenPacket.INSTANCE, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateTinkerStationRecipePacket.class, UpdateTinkerStationRecipePacket::new, PacketDirection.PLAY_TO_CLIENT);

    // tools
    instance.registerPacket(UpdateMaterialsPacket.class, UpdateMaterialsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateMaterialStatsPacket.class, UpdateMaterialStatsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateMaterialTraitsPacket.class, UpdateMaterialTraitsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateToolDefinitionDataPacket.class, UpdateToolDefinitionDataPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(ToolContainerFluidUpdatePacket.class, ToolContainerFluidUpdatePacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(SyncProjectileModifiersPacket.class, SyncProjectileModifiersPacket::new, PacketDirection.PLAY_TO_CLIENT);

    // modifiers
    instance.registerPacket(TinkerControlPacket.class, TinkerControlPacket::read, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(InteractWithAirPacket.class, InteractWithAirPacket::read, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(UpdateModifiersPacket.class, UpdateModifiersPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(UpdateFluidEffectsPacket.class, UpdateFluidEffectsPacket::decode, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(PushBlockRowPacket.class, PushBlockRowPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(LevelUpPacket.class, LevelUpPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(YoyoCollectedDropsPacket.class, YoyoCollectedDropsPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(YoyoHandSyncPacket.class, YoyoHandSyncPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(YoyoToggleAttackPacket.class, YoyoToggleAttackPacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(YoyoToggleEnchantmentPacket.class, YoyoToggleEnchantmentPacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(ItemStackSyncS2CPacket.class, ItemStackSyncS2CPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(ArsElementalSetData.class, ArsElementalSetData::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(BotaniaSetData.class, BotaniaSetData::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(LaunchGhostSword.class, LaunchGhostSword::new, PacketDirection.PLAY_TO_SERVER);

    // smeltery
    instance.registerPacket(FluidUpdatePacket.class, FluidUpdatePacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(FaucetActivationPacket.class, FaucetActivationPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(ChannelFlowPacket.class, ChannelFlowPacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(SmelteryTankUpdatePacket.class, SmelteryTankUpdatePacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(StructureUpdatePacket.class, StructureUpdatePacket::new, PacketDirection.PLAY_TO_CLIENT);
    instance.registerPacket(SmelteryFluidClickedPacket.class, SmelteryFluidClickedPacket::new, PacketDirection.PLAY_TO_SERVER);
    instance.registerPacket(StructureErrorPositionPacket.class, StructureErrorPositionPacket::new, PacketDirection.PLAY_TO_CLIENT);
  }

  /**
   * Sends a vanilla packet to the given player
   * @param player  Player
   * @param packet  Packet
   */
  public void sendVanillaPacket(Entity player, Packet<?> packet) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(packet);
    }
  }

  /**
   * Same as {@link #sendToClientsAround(Object, ServerLevel, BlockPos)}, but checks that the world is a serverworld
   * @param msg       Packet to send
   * @param world     World instance
   * @param position  Target position
   */
  public void sendToClientsAround(Object msg, @Nullable LevelAccessor world, BlockPos position) {
    if (world instanceof ServerLevel server) {
      sendToClientsAround(msg, server, position);
    }
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  @Override
  public void sendToTrackingAndSelf(Object msg, Entity entity) {
    super.sendToTrackingAndSelf(msg, entity);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  @Override
  public void sendToTracking(Object msg, Entity entity) {
    super.sendToTracking(msg, entity);
  }

  /**
   * Sends a packet to the whole player list
   * @param targetedPlayer  Main player to target, if null uses whole list
   * @param playerList      Player list to use if main player is null
   * @param msg             Message to send
   */
  public void sendToPlayerList(@Nullable ServerPlayer targetedPlayer, PlayerList playerList, Object msg) {
    if (targetedPlayer != null) {
      sendTo(msg, targetedPlayer);
    } else {
      for (ServerPlayer player : playerList.getPlayers()) {
        sendTo(msg, player);
      }
    }
  }
}

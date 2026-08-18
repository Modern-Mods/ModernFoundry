package modernmods.modernfoundry.shared.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import modernmods.mantle.command.argument.TagSourceArgument;
import modernmods.mantle.registration.deferred.ArgumentTypeDeferredRegister;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.shared.command.argument.MaterialArgument;
import modernmods.modernfoundry.shared.command.argument.MaterialStatsArgument;
import modernmods.modernfoundry.shared.command.argument.MaterialVariantArgument;
import modernmods.modernfoundry.shared.command.argument.ModifierArgument;
import modernmods.modernfoundry.shared.command.argument.ModifierHookArgument;
import modernmods.modernfoundry.shared.command.argument.ModifierTagSource;
import modernmods.modernfoundry.shared.command.argument.SlotTypeArgument;
import modernmods.modernfoundry.shared.command.argument.ToolStatArgument;
import modernmods.modernfoundry.shared.command.subcommand.DurabilityCommand;
import modernmods.modernfoundry.shared.command.subcommand.GenerateHiddenFluidsCommand;
import modernmods.modernfoundry.shared.command.subcommand.GenerateMeltingRecipesCommand;
import modernmods.modernfoundry.shared.command.subcommand.GeneratePartTexturesCommand;
import modernmods.modernfoundry.shared.command.subcommand.MaterialsCommand;
import modernmods.modernfoundry.shared.command.subcommand.ModifierPriorityCommand;
import modernmods.modernfoundry.shared.command.subcommand.ModifierUsageCommand;
import modernmods.modernfoundry.shared.command.subcommand.ModifiersCommand;
import modernmods.modernfoundry.shared.command.subcommand.SlotsCommand;
import modernmods.modernfoundry.shared.command.subcommand.StatsCommand;

import java.util.function.Consumer;

public class TConstructCommand {
  public static final DynamicCommandExceptionType COMPONENT_ERROR = new DynamicCommandExceptionType(error -> (Component)error);
  private static final ArgumentTypeDeferredRegister ARGUMENT_TYPE = new ArgumentTypeDeferredRegister(TConstruct.MOD_ID);

  /** Registers all TConstruct command related content */
  public static void init() {
    ARGUMENT_TYPE.register(modernmods.modernfoundry.TConstruct.getModBus());
    ARGUMENT_TYPE.registerSingleton("slot_type", SlotTypeArgument.class, SlotTypeArgument::slotType);
    ARGUMENT_TYPE.registerSingleton("tool_stat", ToolStatArgument.class, ToolStatArgument::stat);
    ARGUMENT_TYPE.registerSingleton("modifier", ModifierArgument.class, ModifierArgument::modifier);
    ARGUMENT_TYPE.registerSingleton("material", MaterialArgument.class, MaterialArgument::material);
    ARGUMENT_TYPE.registerSingleton("material_variant", MaterialVariantArgument.class, MaterialVariantArgument::material);
    ARGUMENT_TYPE.registerSingleton("material_stat", MaterialStatsArgument.class, MaterialStatsArgument::stats);
    ARGUMENT_TYPE.registerSingleton("modifier_hook", ModifierHookArgument.class, ModifierHookArgument::modifierHook);

    TagSourceArgument.registerCustom(ModifierTagSource.INSTANCE);
    TagSourceArgument.registerCustom(MaterialRegistry.getTagSource());

    // add command listener
    NeoForge.EVENT_BUS.addListener(TConstructCommand::registerCommand);
  }

  /** Registers a sub command for the root Mantle command */
  private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
    LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
    consumer.accept(subCommand);
    root.then(subCommand);
  }

  /** Event listener to register the Mantle command */
  private static void registerCommand(RegisterCommandsEvent event) {
    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(TConstruct.MOD_ID);
    CommandBuildContext context = event.getBuildContext();

    // sub commands
    register(builder, "modifiers", ModifiersCommand::register);
    register(builder, "materials", MaterialsCommand::register);
    register(builder, "tool_stats", StatsCommand::register);
    register(builder, "slots", SlotsCommand::register);
    register(builder, "durability", DurabilityCommand::register);
    register(builder, "report", b -> {
      register(b, "modifier_usage", ModifierUsageCommand::register);
      register(b, "modifier_priority", ModifierPriorityCommand::register);
    });
    register(builder, "generate", b -> {
      register(b, "part_textures", GeneratePartTexturesCommand::register);
      register(b, "melting_recipes", bb -> GenerateMeltingRecipesCommand.register(bb, context));
      register(b, "hidden_fluids_tag", GenerateHiddenFluidsCommand::register);
    });

    // register final command
    event.getDispatcher().register(builder);
  }
}

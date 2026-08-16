package modernmods.modernfoundry.shared.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.stats.MaterialStatType;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.utils.IdParser;
import modernmods.modernfoundry.library.utils.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/** Argument type for a material stat type */
@NoArgsConstructor(staticName = "stats")
public class MaterialStatsArgument implements ArgumentType<MaterialStatType<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("modernfoundry:head", "modernfoundry:limb");
    private static final DynamicCommandExceptionType MODIFIER_NOT_FOUND = new DynamicCommandExceptionType(name -> TConstruct.makeTranslation("command", "material_stat.not_found", name));

    @Override
    public MaterialStatType<?> parse(StringReader reader) throws CommandSyntaxException {
      MaterialStatsId loc = new MaterialStatsId(IdParser.read(TConstruct.MOD_ID, reader));
      MaterialStatType<?> statType = MaterialRegistry.getInstance().getStatType(loc);
      if (statType == null) {
        throw MODIFIER_NOT_FOUND.create(loc);
      }
      return statType;
    }

    /** Gets a modifier from the command context */
    public static MaterialStatType<?> getStat(CommandContext<CommandSourceStack> context, String name) {
      return context.getArgument(name, MaterialStatType.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return TinkerSuggestionProvider.suggestResource(TConstruct.MOD_ID, MaterialRegistry.getInstance().getAllStatTypeIds(), builder, id -> id, id -> Util.makeTranslation("stat", id));
    }

    @Override
    public Collection<String> getExamples() {
      return EXAMPLES;
    }
  }

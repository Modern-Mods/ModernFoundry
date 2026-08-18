package modernmods.modernfoundry.library.json.predicate.tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.LoadableCodec;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

/** Variant of ItemPredicate for matching Tinker tools using {@link ToolStackItemPredicate} */
@RequiredArgsConstructor
public class ToolStackItemPredicate implements DataComponentPredicate {
  public static final Identifier ID = TConstruct.getResource("tool_stack");
  public static final Codec<ToolStackItemPredicate> CODEC = RecordCodecBuilder.create(
    instance -> instance.group(new LoadableCodec<>(ToolStackPredicate.LOADER).fieldOf("predicate").forGetter(predicate -> predicate.predicate))
                        .apply(instance, ToolStackItemPredicate::new)
  );

  private final IJsonPredicate<IToolStackView> predicate;

  public static ItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    // the MODIFIABLE tag check is enforced inside matches(), so no need to restrict the item set here
    return ItemPredicate.Builder.item()
                                .withComponents(DataComponentMatchers.Builder.components()
                                                                            .partial(TinkerCommons.toolStackItemPredicate.get(), new ToolStackItemPredicate(predicate))
                                                                            .build())
                                .build();
  }

  public static ItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return ofTool(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(DataComponentGetter components) {
    // ItemPredicate always tests the predicate against the ItemStack itself (see ItemPredicate#test)
    if (components instanceof ItemStack stack) {
      // tag check is important to prevent accidently modifying the NBT of non-tools
      return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
    }
    return false;
  }
}

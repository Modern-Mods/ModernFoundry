package modernmods.modernfoundry.library.json.predicate.tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.LoadableCodec;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags.Items;
import modernmods.modernfoundry.shared.TinkerCommons;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

/** Variant of ItemPredicate for matching Tinker tools using {@link ToolStackItemPredicate} */
@RequiredArgsConstructor
public class ToolStackItemPredicate implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  public static final Codec<ToolStackItemPredicate> CODEC = RecordCodecBuilder.create(
    instance -> instance.group(new LoadableCodec<>(ToolStackPredicate.LOADER).fieldOf("predicate").forGetter(predicate -> predicate.predicate))
                        .apply(instance, ToolStackItemPredicate::new)
  );

  private final IJsonPredicate<IToolStackView> predicate;

  public static ItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return ItemPredicate.Builder.item()
                                .of(Items.MODIFIABLE)
                                .withSubPredicate(TinkerCommons.toolStackItemPredicate.get(), new ToolStackItemPredicate(predicate))
                                .build();
  }

  public static ItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return ofTool(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidently modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }
}

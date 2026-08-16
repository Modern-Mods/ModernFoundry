package modernmods.modernfoundry.library.modifiers.modules.build;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.predicate.tool.ToolContextPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierTraitHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;

import java.util.List;

/**
 * Module for a modifier to have a nested modifier as a trait.
 * TODO 1.21: level range does not actually function when this module is applied across multiple sources, remove it.
 */
public record ModifierTraitModule(ModifierEntry modifier, boolean fixedLevel, ModifierCondition<IToolContext> condition) implements ModifierTraitHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierTraitModule>defaultHooks(ModifierHooks.MODIFIER_TRAITS);
  public static final RecordLoadable<ModifierTraitModule> LOADER = RecordLoadable.create(
    ModifierEntry.LOADABLE.directField(ModifierTraitModule::modifier),
    BooleanLoadable.INSTANCE.requiredField("fixed_level", ModifierTraitModule::fixedLevel),
    ModifierCondition.CONTEXT_FIELD,
    ModifierTraitModule::new);

  /** @deprecated use {@link #ModifierTraitModule(ModifierEntry, boolean, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  public ModifierTraitModule {}

  public ModifierTraitModule(ModifierEntry modifier, boolean fixedLevel, IJsonPredicate<IToolContext> tool) {
    this(modifier, fixedLevel, ModifierCondition.ANY_CONTEXT.with(tool));
  }

  /** @deprecated use {@link #ModifierTraitModule(ModifierId, int, boolean, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  public ModifierTraitModule(ModifierId id, int level, boolean fixedLevel, ModifierCondition<IToolContext> condition) {
    this(new ModifierEntry(id, level), fixedLevel, condition);
  }

  public ModifierTraitModule(ModifierId id, int level, boolean fixedLevel, IJsonPredicate<IToolContext> tool) {
    this(new ModifierEntry(id, level), fixedLevel, tool);
  }

  public ModifierTraitModule(ModifierEntry modifier, boolean fixedLevel) {
    this(modifier, fixedLevel, ModifierCondition.ANY_CONTEXT);
  }

  public ModifierTraitModule(ModifierId id, int level, boolean fixedLevel) {
    this(id, level, fixedLevel, ModifierCondition.ANY_CONTEXT);
  }

  /** Common usecase of a modifier only applied to specificly tagged tools */
  public static ModifierTraitModule tagCondition(ModifierId id, TagKey<Item> tag) {
    return new ModifierTraitModule(id, 1, false, ToolContextPredicate.tag(tag));
  }

  @Override
  public void addTraits(IToolContext context, ModifierEntry self, TraitBuilder builder, boolean firstEncounter) {
    if (condition.matches(context, self)) {
      if (fixedLevel) {
        // fixed levels do not need to add again if already added
        if (firstEncounter) {
          builder.add(this.modifier);
        }
      // if just 1 level, can save some object creation
      } else if (self.getLevel() == 1) {
        builder.add(this.modifier);
      } else {
        // level of the trait is based on the level of the modifier, just multiply the two
        builder.add(this.modifier.withLevel(this.modifier.getLevel() * self.getLevel()));
      }
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ModifierTraitModule> getLoader() {
    return LOADER;
  }
}

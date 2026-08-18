package modernmods.modernfoundry.library.modifiers.modules.combat;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeUniqueField;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModuleBuilder;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Adds an attribute modifier to the mob before hitting, then removes the modifier after hitting.
 * @param unique     Unique string used to generate the UUID and as the attribute name
 * @param attribute  Attribute to apply
 * @param id         Modifier ID generated from {@code unique}
 * @param operation  Attribute operation
 * @param amount     Amount of the attribute to apply
 * @param condition  Standard modifier conditions
 */
public record MeleeAttributeModule(String unique, Attribute attribute, Identifier id, Operation operation, LevelingValue amount, IJsonPredicate<LivingEntity> target, ModifierCondition<IToolStackView> condition) implements ModifierModule, MeleeHitModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MeleeAttributeModule>defaultHooks(ModifierHooks.MELEE_HIT);
  public static final RecordLoadable<MeleeAttributeModule> LOADER = RecordLoadable.create(
    new AttributeUniqueField<>(MeleeAttributeModule::unique),
    Loadables.ATTRIBUTE.requiredField("attribute", MeleeAttributeModule::attribute),
    TinkerLoadables.OPERATION.requiredField("operation", MeleeAttributeModule::operation),
    LevelingValue.LOADABLE.directField(MeleeAttributeModule::amount),
    LivingEntityPredicate.LOADER.defaultField("target", MeleeAttributeModule::target),
    ModifierCondition.TOOL_FIELD,
    MeleeAttributeModule::new);

  /** @apiNote Internal constructor, use {@link #builder(Attribute, Operation)} */
  @Internal
  public MeleeAttributeModule {}

  private MeleeAttributeModule(String unique, Attribute attribute, Operation operation, LevelingValue amount, IJsonPredicate<LivingEntity> target, ModifierCondition<IToolStackView> condition) {
    this(unique, attribute, Identifier.fromNamespaceAndPath("modernfoundry", unique.isEmpty() ? "melee_attribute" : unique.replace(':', '.')), operation, amount, target, condition);
  }

  private Holder<Attribute> holder() {
    return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float beforeMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
    if (condition.matches(tool, modifier)) {
      LivingEntity target = context.getLivingTarget();
      if (target != null) {
        AttributeInstance instance = target.getAttribute(holder());
        if (instance != null) {
          // ensure we don't already have the modifier from someone misusing melee hooks or simultaneous attacks
          instance.removeModifier(id);
          instance.addTransientModifier(new AttributeModifier(id, amount.compute(modifier.getEffectiveLevel()), operation));
        }
      }
    }
    return knockback;
  }

  private void removeAttribute(@Nullable LivingEntity target) {
    if (target != null) {
      AttributeInstance instance = target.getAttribute(holder());
      if (instance != null) {
        instance.removeModifier(id);
      }
    }
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    removeAttribute(context.getLivingTarget());
  }

  @Override
  public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageAttempted) {
    removeAttribute(context.getLivingTarget());
  }

  @Override
  public RecordLoadable<MeleeAttributeModule> getLoader() {
    return LOADER;
  }


  /** Creates a new builder instance */
  public static Builder builder(Attribute attribute, Operation operation) {
    return new Builder(attribute, operation);
  }

  public static Builder builder(Holder<Attribute> attribute, Operation operation) {
    return new Builder(attribute.value(), operation);
  }

  @Setter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Builder extends ModuleBuilder.Stack<Builder> implements LevelingValue.Builder<MeleeAttributeModule>  {
    protected final Attribute attribute;
    protected final Operation operation;
    protected String unique = "";
    protected IJsonPredicate<LivingEntity> target = LivingEntityPredicate.ANY;

    /**
     * Sets the unique string using a resource location
     */
    public Builder uniqueFrom(Identifier id) {
      return unique(id.getNamespace() + ".modifier." + id.getPath());
    }

    @Override
    public MeleeAttributeModule amount(float flat, float eachLevel) {
      return new MeleeAttributeModule(unique, attribute, operation, new LevelingValue(flat, eachLevel), target, condition);
    }
  }
}

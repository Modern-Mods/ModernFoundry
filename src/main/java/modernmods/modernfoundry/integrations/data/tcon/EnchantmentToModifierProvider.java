package modernmods.modernfoundry.integrations.data.tcon;

import org.jetbrains.annotations.NotNull;

import net.minecraft.data.PackOutput;

import dev.shadowsoffire.apotheosis.Apoth;

import modernmods.modernfoundry.library.data.tinkering.AbstractEnchantmentToModifierProvider;

import modernmods.modernfoundry.integrations.items.TciModifiers;

public class EnchantmentToModifierProvider extends AbstractEnchantmentToModifierProvider {

    public EnchantmentToModifierProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void addEnchantmentMappings() {
        addOptional(Apoth.Enchantments.CAPTURING.getId(), TciModifiers.CAPTURING_MODIFIER.getId(), true);
    }

    @Override
    public @NotNull String getName() {
        return "TciIntegration - TCon Enchantment to Modifier Mapping";
    }

}

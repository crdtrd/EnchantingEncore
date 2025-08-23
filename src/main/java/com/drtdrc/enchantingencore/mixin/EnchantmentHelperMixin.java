// src/main/java/com/drtdrc/enchantingencore/mixin/EnchantmentHelperMixin.java
package com.drtdrc.enchantingencore.mixin;

import com.drtdrc.enchantingencore.BiasContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    /** Call the real getPossibleEntries (bypasses our redirect). */
    @Invoker("getPossibleEntries")
    private static List<EnchantmentLevelEntry> ee$origGetPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
        throw new AssertionError();
    }

    /**
     * Vanilla: Weighting.getRandom(random, list, EnchantmentLevelEntry::getWeight)
     * We wrap the third arg (Function<T, Weight>) to return base + our bias.
     * <p>
     * Applies to BOTH invocations inside generateEnchantments.
     */
    @ModifyArg(
            method = "generateEnchantments",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/collection/Weighting;getRandom(Lnet/minecraft/util/math/random/Random;Ljava/util/List;Ljava/util/function/ToIntFunction;)Ljava/util/Optional;"
            ),
            index = 2 // wrap the weight function
    )
    private static ToIntFunction<EnchantmentLevelEntry> ee$biasWeightFunction(
            ToIntFunction<EnchantmentLevelEntry> original
    ) {
        return entry -> {
            // base weight from vanilla rarity:
            int base = original.applyAsInt(entry);

            // +1 per stored level across reachable chiseled bookshelves:
            int bonus = BiasContext.bonus(entry.enchantment());

            return Math.max(1, base + bonus);
        };
    }

//    /**
//     * In your Yarn 1.21.8+build.1, generateEnchantments(...) calls:
//     * getPossibleEntries(int, ItemStack, Stream<RegistryEntry<Enchantment>>)
//     * We redirect that single invocation and adjust weights.
//     */
//    @Redirect(
//            method = "generateEnchantments",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Ljava/util/stream/Stream;)Ljava/util/List;"
//            )
//    )
//    private static List<EnchantmentLevelEntry> ee$biasPossibleEntries(int level, ItemStack stack, Stream<RegistryEntry<Enchantment>> possibleEnchantments) {
//        List<EnchantmentLevelEntry> base = ee$origGetPossibleEntries(level, stack, possibleEnchantments);
//        if (base.isEmpty()) return base;
//
//        List<EnchantmentLevelEntry> out = new ArrayList<>(base.size());
//        for (EnchantmentLevelEntry e : base) {
//            // Accessors in 1.21.x Yarn are record-like: enchantment(), level(), getWeight()
//            RegistryEntry<Enchantment> ench = e.enchantment();
//            int baseWeight = e.getWeight();
//            int bonus = BiasContext.bonus(ench);
//            int total = Math.max(1, baseWeight + bonus);
//
//            out.add(new EnchantmentLevelEntry(ench, e.level(), ));
//        }
//        return out;
//    }
}

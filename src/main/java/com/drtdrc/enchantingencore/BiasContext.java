package com.drtdrc.enchantingencore;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.EnchantingTableBlock; // for POWER_PROVIDER_OFFSETS and canAccessPowerProvider

public final class BiasContext {
    private BiasContext() {}

    // Each enchantment's "count" from nearby chiseled bookshelves (reachable only)
    private static final ThreadLocal<Map<RegistryEntry<Enchantment>, Integer>> BIAS =
            ThreadLocal.withInitial(HashMap::new);

    /** Multiplier for how strongly each counted book affects weight. Tweak to taste (configurable). */
    public static int STEP = 1;

    public static void compute(World world, BlockPos tablePos) {
        var map = BIAS.get();
        map.clear();

        for (BlockPos off : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (!EnchantingTableBlock.canAccessPowerProvider(world, tablePos, off)) continue;

            BlockPos bp = tablePos.add(off);
            BlockState st = world.getBlockState(bp);
            if (!st.isOf(Blocks.CHISELED_BOOKSHELF)) continue;

            BlockEntity be = world.getBlockEntity(bp);
            if (!(be instanceof ChiseledBookshelfBlockEntity shelf)) continue;

            // for each enchanted book
            for (int i = 0; i < shelf.size(); i++) {
                ItemStack book = shelf.getStack(i);
                if (!book.isOf(Items.ENCHANTED_BOOK)) continue;

                ItemEnchantmentsComponent stored = book.get(DataComponentTypes.STORED_ENCHANTMENTS);

                // Add the *level* for each enchantment (e.g., Prot IV contributes 4)
                for (Map.Entry<RegistryEntry<Enchantment>, Integer> e : Objects.requireNonNull(stored).getEnchantmentEntries()) {
                    int level = Math.max(0, e.getValue()); // defensive
                    if (level == 0) continue;
                    map.merge(e.getKey(), level, Integer::sum);
                }
            }
        }
    }

    public static void clear() { BIAS.remove(); }

    public static int bonus(RegistryEntry<Enchantment> ench) {
        // With STEP=1, this is exactly "sum of levels across nearby books"
        return BIAS.get().getOrDefault(ench, 0) * STEP;
    }
}

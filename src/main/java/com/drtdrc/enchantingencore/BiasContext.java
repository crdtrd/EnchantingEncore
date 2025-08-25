package com.drtdrc.enchantingencore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import net.minecraft.block.EnchantingTableBlock; // for POWER_PROVIDER_OFFSETS and canAccessPowerProvider

public final class BiasContext {
    private BiasContext() {}

    // Each enchantment's "count" from nearby chiseled bookshelves (reachable only)
    private static final ThreadLocal<Map<RegistryEntry<Enchantment>, Integer>> BIAS =
            ThreadLocal.withInitial(HashMap::new);

    // Gate to avoid accidental carry-over across calls/players
    private static final ThreadLocal<Boolean> ACTIVE =
            ThreadLocal.withInitial(() -> Boolean.FALSE);


    public static void compute(World world, BlockPos tablePos) {
        Map<RegistryEntry<Enchantment>, Integer> map = BIAS.get();
        map.clear();

        ACTIVE.set(Boolean.TRUE);

        for (BlockPos off : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (!EnchantingTableBlock.canAccessPowerProvider(world, tablePos, off)) continue;

            BlockPos bp = tablePos.add(off);
            BlockState st = world.getBlockState(bp);
            if (!st.isOf(Blocks.CHISELED_BOOKSHELF)) continue;

            BlockEntity be = world.getBlockEntity(bp);
            if (!(be instanceof ChiseledBookshelfBlockEntity shelf)) continue;

            // Require: every slot is an enchanted book. If any slot is not, this shelf contributes nothing.
            int slots = shelf.size();
            boolean allEnchanted = true;
            for (int i = 0; i < slots; i++) {
                ItemStack s = shelf.getStack(i);
                if (!s.isOf(Items.ENCHANTED_BOOK)) { allEnchanted = false; break; }
            }
            if (!allEnchanted) continue;

            // Build intersection of enchant keys across ALL books in this shelf
            Set<RegistryEntry<Enchantment>> common = null;

            int bookCount = 0; // == slots, since allEnchanted==true, but keep explicit
            for (int i = 0; i < slots; i++) {
                ItemStack book = shelf.getStack(i);
                ItemEnchantmentsComponent stored = book.get(DataComponentTypes.STORED_ENCHANTMENTS);
                if (stored == null) { allEnchanted = false; break; } // defensive
                bookCount++;

                // Collect this book's enchant set and add levels
                Set<RegistryEntry<Enchantment>> thisSet = new HashSet<>();
                for (Map.Entry<RegistryEntry<Enchantment>, Integer> e : stored.getEnchantmentEntries()) {
                    thisSet.add(e.getKey());
                }

                if (common == null) {
                    common = thisSet; // first book
                } else {
                    common.retainAll(thisSet); // intersection across books so far
                    if (common.isEmpty()) break; // early out: nothing can match all books
                }
            }

            if (!allEnchanted || bookCount == 0 || common.isEmpty()) continue;

            // For each enchant present in every book on this shelf,
            for (RegistryEntry<Enchantment> ench : common) {
                    map.merge(ench, 1, Integer::sum);
            }
        }
    }


    public static void deactivate() {
        ACTIVE.set(Boolean.FALSE);
        BIAS.remove();
    }

    public static int bonus(RegistryEntry<Enchantment> ench) {
        return BIAS.get().getOrDefault(ench, 0);
    }
}

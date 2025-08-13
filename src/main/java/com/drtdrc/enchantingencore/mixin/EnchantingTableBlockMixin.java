package com.drtdrc.enchantingencore.mixin;

import com.drtdrc.enchantingencore.EnchantingEncore;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.minecraft.util.math.BlockPos.stream;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends BlockWithEntity {
    // Need to change POWER_PROVIDER_OFFSETS to new offset values
    @Shadow @Final @Mutable
    public static List<BlockPos> POWER_PROVIDER_OFFSETS;

    protected EnchantingTableBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tweakPowerProviderOffsets(CallbackInfo ci) {
        POWER_PROVIDER_OFFSETS = stream(-4, -4, -4, 4, 4, 4)
                .filter(pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1)
                .map(BlockPos::toImmutable)
                .toList();
    }

    @Inject(
            method = "canAccessPowerProvider",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void canAccessPowerProviderModified(World world, BlockPos tablePos, BlockPos providerOffset, CallbackInfoReturnable<Boolean> cir) {

        BlockPos providerPos = tablePos.add(providerOffset);
        BlockState provider = world.getBlockState(providerPos);

        boolean isProvider = provider.isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                || (provider.isOf(Blocks.CHISELED_BOOKSHELF) && isChiseledBookshelfFull(world, providerPos));

        BlockState state = world.getBlockState(tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2));
        boolean canTransmit = state.isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)
                || state.isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                || state.isOf(Blocks.CHISELED_BOOKSHELF);

        cir.setReturnValue(isProvider && canTransmit);
    }

    @Unique
    private static boolean isChiseledBookshelfFull(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ChiseledBookshelfBlockEntity shelf) {
            for (int i = 0; i < shelf.size(); i++) {
                if (shelf.getStack(i).isEmpty()) return false;
            }
            return true;
        }
        return false;
    }

    // particles
    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void onGetTicker(World world, BlockState state, BlockEntityType<T> type, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (world.isClient) return;
        if (type != BlockEntityType.ENCHANTING_TABLE) return;

        BlockEntityTicker<T> existing = cir.getReturnValue();

        BlockEntityTicker<T> serverTicker = (w, pos, s, be) -> {
            if(!(w instanceof ServerWorld sw)) return;

            Random rand = sw.getRandom();
            if (rand.nextInt(3) != 0) return; // about every 3 ticks
            spawnEnchantParticlesServer(sw, pos, rand);
        };

        if (existing != null) {
            BlockEntityTicker<T> chained = (w2, p2, s2, be2) -> {
                existing.tick(w2, p2, s2, be2);
                serverTicker.tick(w2, p2, s2, be2);
            };
            cir.setReturnValue(chained);
        } else {
            cir.setReturnValue(serverTicker);
        }
    }

    @Unique
    private static void spawnEnchantParticlesServer(ServerWorld world, BlockPos tablePos, Random random) {
        final double ox = tablePos.getX() + 0.5;
        final double oy = tablePos.getY() + 2.0;
        final double oz = tablePos.getZ() + 0.5;

        for (BlockPos offset : POWER_PROVIDER_OFFSETS) {
            if (random.nextInt(16) != 0) continue;
            if (!EnchantingTableBlock.canAccessPowerProvider(world, tablePos, offset)) continue;

            double tx = offset.getX() + random.nextFloat() - 0.5;
            double ty = offset.getY() - random.nextFloat() - 1.0f;
            double tz = offset.getZ() + random.nextFloat() - 0.5;

            double vx = tx - ox;
            double vy = ty - oy;
            double vz = tz - oz;

            // count = 1; the last param is speed (unused for ENCHANT when velocities provided)
            world.spawnParticles(ParticleTypes.ENCHANT, ox, oy, oz, 0, tx, ty, tz, 1.0);
        }
    }
}

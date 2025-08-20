package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Socket;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CCCompat {
    public static void attachPeripherals(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof Socket socket) {
            PeripheralProvider.attach(event, event.getObject(), SocketPeripheral::new);
        }
    }
    public static void postInitialize() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, CCCompat::attachPeripherals);
    }

    public static final Capability<IPeripheral> CAPABILITY_PERIPHERAL = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation PERIPHERAL = CommonSimpleRadio.id("peripheral");

    private static final class PeripheralProvider<O extends BlockEntity> implements ICapabilityProvider {
        private final O blockEntity;
        private final Function<O, IPeripheral> factory;
        private @Nullable LazyOptional<IPeripheral> peripheral;

        private PeripheralProvider(O blockEntity, Function<O, IPeripheral> factory) {
            this.blockEntity = blockEntity;
            this.factory = factory;
        }

        private static <O extends BlockEntity> void attach(AttachCapabilitiesEvent<BlockEntity> event, O blockEntity, Function<O, IPeripheral> factory) {
            var provider = new PeripheralProvider<>(blockEntity, factory);
            event.addCapability(PERIPHERAL, provider);
            event.addListener(provider::invalidate);
        }

        private void invalidate() {
            if (peripheral != null) peripheral.invalidate();
            peripheral = null;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
            if (capability != CAPABILITY_PERIPHERAL) return LazyOptional.empty();
            if (blockEntity.isRemoved()) return LazyOptional.empty();

            var peripheral = this.peripheral;
            return (peripheral == null ? (this.peripheral = LazyOptional.of(() -> factory.apply(blockEntity))) : peripheral).cast();
        }
    }
}

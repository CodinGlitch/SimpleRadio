package com.codinglitch.simpleradio.client.core.registry.models;

import com.codinglitch.simpleradio.client.core.central.ModuleOverrides;
import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModuleModel extends SimpleRadioModels.LocationHolder implements BakedModel {
    private final BakedModel baseModel;
    private final ModuleOverrides overrides;

    public ModuleModel(BakedModel baseModel) {
        this.baseModel = baseModel;
        this.overrides = new ModuleOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return baseModel.getQuads(state, direction, random);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }
    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }
    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }
    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }
    @Override
    public ItemTransforms getTransforms() {
        return baseModel.getTransforms();
    }
}

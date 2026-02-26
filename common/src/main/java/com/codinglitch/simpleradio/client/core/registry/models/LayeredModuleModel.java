package com.codinglitch.simpleradio.client.core.registry.models;

import com.codinglitch.simpleradio.central.Module;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LayeredModuleModel implements BakedModel {
    private final ModuleModel baseModel;
    private final Module upgrade;

    public LayeredModuleModel(ModuleModel baseModel, Module upgrade) {
        this.baseModel = baseModel;
        this.upgrade = upgrade;
    }

    private void buildQuads(List<BakedQuad> quads) {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = atlas.getSprite(this.upgrade.texture);

        if (sprite.contents().name().getPath().equals("missingno")) return;

        List<BlockElement> blockElements = Lists.newArrayList();
        blockElements.addAll(ModelBakery.ITEM_MODEL_GENERATOR.processFrames(1, "layer1", sprite.contents()));
        //TODO: allow usage of multiple layers

        for (BlockElement element : blockElements) {
            element.faces.forEach((side, face) -> {
                quads.add(BlockModel.bakeFace(element, face, sprite, side, BlockModelRotation.X0_Y0));
            });
        }
        //TODO: optimize quads; culling and removal of overlapping quads
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        if (direction != null)
            return baseModel.getQuads(state, direction, random);

        List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(state, direction, random));
        buildQuads(quads);
        return quads;
    }

    @Override
    public ItemOverrides getOverrides() {
        return baseModel.getOverrides();
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

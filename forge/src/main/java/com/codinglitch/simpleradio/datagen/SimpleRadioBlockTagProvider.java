package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SimpleRadioBlockTagProvider extends BlockTagsProvider {

    public SimpleRadioBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, @Nullable ExistingFileHelper fileHelper) {
        super(output, lookup, CommonSimpleRadio.ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        CommonBlockTagProvider.defineTags(key ->items -> {
            this.tag(key).add(items);
        });
    }
}

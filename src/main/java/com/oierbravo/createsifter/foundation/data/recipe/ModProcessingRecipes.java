package com.oierbravo.createsifter.foundation.data.recipe;


import com.oierbravo.createsifter.CreateSifter;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class ModProcessingRecipes extends CreateRecipeProvider {


    protected static final List<ModProcessingRecipes> PROVIDERS = new ArrayList<>();

    public static void registerAllProcessingProviders(FabricDataGenerator generator) {
        PROVIDERS.add(new SiftingRecipeGen(generator));

        generator.addProvider(new DataProvider() {
            @Override
            public void run(@NotNull HashCache pCache) throws IOException {
                PROVIDERS.forEach(generator -> {
                    try {
                        generator.run(pCache);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public @NotNull String getName() {
                return "Create: Sifter's Processing Recipes";
            }
        });
    }


    public ModProcessingRecipes(FabricDataGenerator generator) {
        super(generator);
    }


    /* Functions from Create's ProcessingRecipeGen.java */

    protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(String name,
                                                                     UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        return create(CreateSifter.asResource(name), transform);
    }

    protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(ResourceLocation name,
                                                                     UnaryOperator<ProcessingRecipeBuilder<T>> transform) {

        return createWithDeferredId(() -> name, transform);
    }

    protected <T extends ProcessingRecipe<?>> GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name,
                                                                                   UnaryOperator<ProcessingRecipeBuilder<T>> transform) {
        ProcessingRecipeSerializer<T> serializer = getSerializer();
        GeneratedRecipe generatedRecipe =
                c -> transform.apply(new ProcessingRecipeBuilder<>(serializer.getFactory(), name.get()))
                //c -> transform.apply(new ModProcessingRecipeBuilder<>(serializer.getFactory(), name.get()))
                        .build(c);
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    protected <T extends ProcessingRecipe<?>> ProcessingRecipeSerializer<T> getSerializer() {
        return getRecipeType().getSerializer();
    }

    protected abstract IRecipeTypeInfo getRecipeType();

}
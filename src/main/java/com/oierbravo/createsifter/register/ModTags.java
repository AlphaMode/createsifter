package com.oierbravo.createsifter.register;

import com.oierbravo.createsifter.CreateSifter;
import com.oierbravo.createsifter.groups.ModGroup;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.oierbravo.createsifter.register.ModTags.NameSpace.MOD;

public class ModTags {
    private static final CreateRegistrate REGISTRATE = CreateSifter.registrate()
            .creativeModeTab(() -> ModGroup.MAIN);

    public enum NameSpace {

        MOD(CreateSifter.MODID, false, true),CREATE(Create.ID, true, false), FORGE("forge");

        public final String id;
        public final boolean optionalDefault;
        public final boolean alwaysDatagenDefault;

        NameSpace(String id) {
            this(id, true, false);
        }

        NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
            this.id = id;
            this.optionalDefault = optionalDefault;
            this.alwaysDatagenDefault = alwaysDatagenDefault;
        }

    }
    public enum ModItemTags {
        MESHES;
        public final TagKey<Item> tag;

        ModItemTags() {
			this(MOD);
        }

        ModItemTags(NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        ModItemTags(NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        ModItemTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        ModItemTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
            tag = TagKey.create(Registry.ITEM_REGISTRY, id);
            if (alwaysDatagen) {
                REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag));
            }
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Item item) {
            return item.builtInRegistryHolder()
                    .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }

        public void add(Item... values) {
            REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
                    .add(values));
        }

        public void addOptional(Mods mod, String... ids) {
            REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
                TagsProvider.TagAppender<Item> builder = prov.tag(tag);
                for (String id : ids)
                    builder.addOptional(mod.asResource(id));
            });
        }

        public void includeIn(TagKey<Item> parent) {
            REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(parent)
                    .addTag(tag));
        }

        public void includeIn(AllTags.AllItemTags parent) {
            includeIn(parent.tag);
        }

        public void includeAll(TagKey<Item> child) {
            REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
                    .addTag(child));
        }
    }

    public static void register() {

    }
}

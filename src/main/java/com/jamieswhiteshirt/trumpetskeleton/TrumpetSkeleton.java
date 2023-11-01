package com.jamieswhiteshirt.trumpetskeleton;

import com.jamieswhiteshirt.trumpetskeleton.common.entity.TrumpetSkeletonEntityTypes;
import com.jamieswhiteshirt.trumpetskeleton.common.item.TrumpetSkeletonItems;
import com.jamieswhiteshirt.trumpetskeleton.common.sound.TrumpetSkeletonSoundEvents;
import com.jamieswhiteshirt.trumpetskeleton.mixin.ParrotEntityAccessor;
import com.jamieswhiteshirt.trumpetskeleton.mixin.SpawnRestrictionAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.world.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TrumpetSkeleton implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("trumpet-skeleton");

    public static double relativeSpawnWeight = 0.05D;

    @Override
    public void onInitialize() {
        TrumpetSkeletonItems.init();
        TrumpetSkeletonSoundEvents.init();
        TrumpetSkeletonEntityTypes.init();

        ParrotEntityAccessor.trumpetskeleton$getMobSounds().put(TrumpetSkeletonEntityTypes.TRUMPET_SKELETON, TrumpetSkeletonSoundEvents.ENTITY_PARROT_IMITATE_TRUMPET_SKELETON);
        SpawnRestrictionAccessor.trumpetskeleton$register(TrumpetSkeletonEntityTypes.TRUMPET_SKELETON, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        FabricDefaultAttributeRegistry.register(TrumpetSkeletonEntityTypes.TRUMPET_SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes());

        Properties configuration = new Properties();
        configuration.setProperty("relativeSpawnWeight", String.valueOf(relativeSpawnWeight));
        Path configurationFile = FabricLoader.getInstance().getConfigDir().resolve("trumpet-skeleton.properties");

        if (Files.exists(configurationFile)) {
            try (InputStream in = Files.newInputStream(configurationFile)) {
                configuration.load(in);
                LOGGER.info("Loaded configuration file \"" + configurationFile + "\"");
            } catch (IOException e) {
                LOGGER.error("Could not read configuration file \"" + configurationFile + "\"", e);
            }
        } else {
            try (OutputStream out = Files.newOutputStream(configurationFile)) {
                configuration.store(out, "Trumpet Skeleton configuration");
                LOGGER.info("Generated configuration file \"" + configurationFile + "\"");
            } catch (IOException e) {
                LOGGER.error("Could not write configuration file \"" + configurationFile + "\"", e);
            }
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.addAfter(Items.TNT_MINECART, TrumpetSkeletonItems.TRUMPET);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(TrumpetSkeletonItems.TRUMPET_SKELETON_SPAWN_EGG);
        });

        String relativeSpawnRateString = configuration.getProperty("relativeSpawnWeight");
        try {
            relativeSpawnWeight = Double.parseDouble(relativeSpawnRateString);
        } catch (NumberFormatException e) {
            LOGGER.error("Error processing configuration file \"" + configurationFile + "\".");
            LOGGER.error("Expected configuration value for relativeSpawnWeight to be a number, found \"" + relativeSpawnRateString + "\".");
            LOGGER.error("Using default value \"" + relativeSpawnWeight + "\" instead.");
        }
    }
}

package net.monacraft.bladearts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResourcePackContractTest {
    private static final List<String> MODEL_IDS = List.of(
            "blade_nameless", "blade_white", "blade_crimson", "blade_gale", "blade_void",
            "soul_fragment", "blade_soul", "soul_ingot", "soul_crystal", "soul_sphere",
            "soul_trapezohedron");

    @Test
    void everyCustomItemHasAModernItemDefinitionAndModel() throws IOException {
        Path assets = Path.of("craftengine-pack", "resourcepack", "assets", "monabladearts");

        for (String modelId : MODEL_IDS) {
            Path itemDefinition = assets.resolve(Path.of("items", modelId + ".json"));
            Path model = assets.resolve(Path.of("models", "item", modelId + ".json"));

            assertTrue(Files.isRegularFile(itemDefinition), "Missing item definition: " + modelId);
            assertTrue(Files.isRegularFile(model), "Missing item model: " + modelId);
            assertEquals(
                    "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"monabladearts:item/"
                            + modelId + "\"}}",
                    Files.readString(itemDefinition).trim());
        }
    }
}

package net.monacraft.bladearts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Test
    void everyBladeIsDetailedAndHasUniqueGeometry() throws IOException {
        Path models = Path.of("craftengine-pack", "resourcepack", "assets", "monabladearts", "models", "item");
        Set<String> geometries = new HashSet<>();

        for (String blade : List.of("blade_nameless", "blade_white", "blade_crimson", "blade_gale", "blade_void")) {
            String json = Files.readString(models.resolve(blade + ".json"));
            int elementCount = json.split("\\\"from\\\"").length - 1;
            assertTrue(elementCount >= 20, blade + " must contain at least 20 modeled elements");
            assertTrue(json.contains("monabladearts:item/blade_base"), blade + " must inherit held-item transforms");
            assertTrue(geometries.add(json), blade + " must not be a color-only duplicate");
        }

        String base = Files.readString(models.resolve("blade_base.json"));
        for (String transform : List.of("gui", "ground", "fixed", "firstperson_righthand", "thirdperson_righthand")) {
            assertTrue(base.contains("\"" + transform + "\""), "Missing display transform: " + transform);
        }
    }
}

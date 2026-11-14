package net.monacraft.bladearts.item;

import net.monacraft.bladearts.MonaBladeArtsPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class RecipeService implements Listener {
    private final MonaBladeArtsPlugin plugin;
    private final BladeItemFactory items;
    private final Map<BladeType, ItemStack[]> guide = new EnumMap<>(BladeType.class);
    private final Map<NamespacedKey, Requirement> guarded = new HashMap<>();
    public RecipeService(MonaBladeArtsPlugin plugin, BladeItemFactory items) { this.plugin = plugin; this.items = items; }

    public void registerAll() {
        addBlade(BladeType.NAMELESS, new String[]{" II", " SI", "S  "}, Map.of('I', Material.IRON_INGOT, 'S', Material.STICK));
        addBladeExact(BladeType.WHITE, BladeType.NAMELESS, SoulItem.SOUL, Material.QUARTZ);
        addBladeExact(BladeType.CRIMSON, BladeType.WHITE, SoulItem.INGOT, Material.BLAZE_ROD);
        addBladeExact(BladeType.GALE, BladeType.WHITE, SoulItem.CRYSTAL, Material.BREEZE_ROD);
        addBladeExact(BladeType.VOID, BladeType.CRIMSON, SoulItem.TRAPEZOHEDRON, Material.NETHER_STAR);
        addSoul(SoulItem.SOUL, SoulItem.FRAGMENT);
        addSoul(SoulItem.INGOT, SoulItem.SOUL);
        addSoul(SoulItem.CRYSTAL, SoulItem.INGOT);
        addSoul(SoulItem.SPHERE, SoulItem.CRYSTAL);
        addSoul(SoulItem.TRAPEZOHEDRON, SoulItem.SPHERE);
    }

    private void addBlade(BladeType type, String[] shape, Map<Character, Material> ingredients) {
        ShapedRecipe recipe = new ShapedRecipe(key("blade_" + type.id()), items.blade(type));
        recipe.shape(shape);
        ingredients.forEach(recipe::setIngredient);
        plugin.getServer().addRecipe(recipe);
        guide.put(type, toGuide(shape, ingredients));
    }

    private void addBladeExact(BladeType output, BladeType previous, SoulItem soul, Material accent) {
        ShapedRecipe recipe = new ShapedRecipe(key("blade_" + output.id()), items.blade(output));
        recipe.shape(" S ", "PAP", " S ");
        recipe.setIngredient('P', previous.material());
        recipe.setIngredient('S', soul.material());
        recipe.setIngredient('A', accent);
        plugin.getServer().addRecipe(recipe);
        guarded.put(recipe.getKey(), new Requirement(previous.id(), 2, soul.id(), 2, accent));
        guide.put(output, new ItemStack[]{null, items.soul(soul, 1), null, items.blade(previous), new ItemStack(accent), items.blade(previous), null, items.soul(soul, 1), null});
    }

    private void addSoul(SoulItem output, SoulItem input) {
        ShapedRecipe recipe = new ShapedRecipe(key(output.id()), items.soul(output, 1));
        recipe.shape("SSS", "SAS", "SSS");
        recipe.setIngredient('S', input.material());
        recipe.setIngredient('A', Material.AMETHYST_BLOCK);
        plugin.getServer().addRecipe(recipe);
        guarded.put(recipe.getKey(), new Requirement(input.id(), 8, null, 0, Material.AMETHYST_BLOCK));
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe shaped)) return;
        Requirement requirement = guarded.get(shaped.getKey());
        if (requirement == null) return;
        int first = 0, second = 0, accent = 0;
        for (ItemStack stack : event.getInventory().getMatrix()) {
            if (stack == null || stack.getType().isAir()) continue;
            String id = items.id(stack);
            if (requirement.firstId.equals(id)) first += stack.getAmount();
            else if (requirement.secondId != null && requirement.secondId.equals(id)) second += stack.getAmount();
            else if (stack.getType() == requirement.accent) accent += stack.getAmount();
        }
        if (first < requirement.firstCount || second < requirement.secondCount || accent < 1) event.getInventory().setResult(null);
    }

    public ItemStack[] guideRecipe(BladeType type) { return guide.getOrDefault(type, new ItemStack[0]); }

    private ItemStack[] toGuide(String[] shape, Map<Character, Material> ingredients) {
        ItemStack[] result = new ItemStack[9];
        for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) {
            Material material = ingredients.get(shape[y].charAt(x));
            result[y * 3 + x] = material == null ? null : new ItemStack(material);
        }
        return result;
    }
    private NamespacedKey key(String id) { return new NamespacedKey(plugin, id); }
    private record Requirement(String firstId, int firstCount, String secondId, int secondCount, Material accent) {}
}

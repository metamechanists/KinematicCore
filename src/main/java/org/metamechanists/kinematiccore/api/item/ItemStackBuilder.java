package org.metamechanists.kinematiccore.api.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemStackBuilder {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static final int LINE_LENGTH = 50;
    public static final String DIAMOND = "\u25C6";

    public static final Component INTERMEDIATE = mm.deserialize("<color:#48df56>\u1F6E0 Intermediate");
    public static final Component TOOL = mm.deserialize("<color:#00ff00>\u26CF Tool");
    public static final Component WEAPON = mm.deserialize("<color:#00ff00>\u1F5E1 Weapon");
    public static final Component ARMOR = mm.deserialize("<color:#00ff00>\u1F455 Armor");
    public static final Component VEHICLE = mm.deserialize("<color:#aa6eaa>\u2708 Vehicle");
    public static final Component MACHINE = mm.deserialize("<color:#00ff00>\u1F3ED Machine");
    public static final Component STORAGE = mm.deserialize("<color:#00ff00>\u1F9F0 Storage");
    public static final Component GENERATOR = mm.deserialize("<color:#00ff00>\u26A1 Generator");
    public static final Component ENERGY_STORAGE = mm.deserialize("<color:#00ff00>\u1F50B Energy Storage");
    public static final Component RESOURCE = mm.deserialize("<color:#00ff00>\u1FAA8 Resource");
    public static final Component FOOD = mm.deserialize("<color:#00ff00>\u1F352 Food/drink");
    public static final Component BUILDING_BLOCK = mm.deserialize("<color:#00ff00>\u1F3DB Building Block");

    private final ItemStack stack;

    public ItemStackBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    private void addLine(Component line) {
        stack.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
        });
    }

    public ItemStackBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemStackBuilder name(String name) {
        return name(mm.deserialize(name));
    }

    public ItemStackBuilder name(Component name) {
        stack.editMeta(meta -> meta.displayName(name));
        return this;
    }

    public ItemStackBuilder loreLine(String line) {
        addLine(mm.deserialize(line));
        return this;
    }

    public ItemStackBuilder loreLine(Component line) {
        addLine(line);
        return this;
    }

    public ItemStackBuilder loreLine(String key, String value, String unit) {
        return loreLine("<color:#eec250>" + DIAMOND + " <color:#b4b4b4>" + key + " <color:#2182ff>" + value + " <color:#708b8c>" + unit);
    }

    public ItemStackBuilder loreLine(String key, String value) {
        return loreLine(key, value, "");
    }

    public ItemStackBuilder enchantment(Enchantment enchantment, int level) {
        stack.addEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder flag(ItemFlag flag) {
        stack.addItemFlags(flag);
        return this;
    }

    public ItemStackBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        stack.editMeta(meta -> meta.addAttributeModifier(attribute, modifier));
        return this;
    }

    public ItemStackBuilder unbreakable() {
        stack.editMeta(meta -> meta.setUnbreakable(true));
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}

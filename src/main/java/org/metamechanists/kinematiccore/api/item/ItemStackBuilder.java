package org.metamechanists.kinematiccore.api.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemStackBuilder {
    public static final int LINE_LENGTH = 50;
    public static final String DIAMOND = "\u25C6";

    public static final Component INTERMEDIATE = Component.text("\u1F6E0 Intermediate").color(TextColor.color(130, 255, 130));
    public static final Component TOOL = Component.text("\u26CF Tool").color(TextColor.color(0, 255, 0));
    public static final Component WEAPON = Component.text("\u1F5E1 Weapon").color(TextColor.color(0, 255, 0));
    public static final Component ARMOR = Component.text("\u1F455 Armor").color(TextColor.color(0, 255, 0));
    public static final Component VEHICLE = Component.text("\u2708 Vehicle").color(TextColor.color(170, 110, 170));
    public static final Component MACHINE = Component.text("\u1F3ED Machine").color(TextColor.color(0, 255, 0));
    public static final Component STORAGE = Component.text("\u1F9F0 Storage").color(TextColor.color(0, 255, 0));
    public static final Component GENERATOR = Component.text("\u26A1 Generator").color(TextColor.color(0, 255, 0));
    public static final Component ENERGY_STORAGE = Component.text("\u1F50B Energy Storage").color(TextColor.color(0, 255, 0));
    public static final Component RESOURCE = Component.text("\u1FAA8 Resource").color(TextColor.color(241, 255, 98));
    public static final Component FOOD = Component.text("\u1F352 Food/drink").color(TextColor.color(0, 255, 0));
    public static final Component BUILDING_BLOCK = Component.text("\u1F3DB Building Block").color(TextColor.color(0, 255, 0));

    public static final TextColor NAME_COLOR = TextColor.color(255, 255, 255);
    public static final TextColor DIAMOND_COLOR = TextColor.color(210, 210, 150);
    public static final TextColor KEY_COLOR = TextColor.color(180, 180, 180);
    public static final TextColor VALUE_COLOR = TextColor.color(255, 180, 100);
    public static final TextColor UNIT_COLOR = TextColor.color(100, 100, 100);

    private final ItemStack stack;

    public ItemStackBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    private void addLine(String line) {
        stack.editMeta(meta -> {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(ChatColor.RESET + "" + ChatColor.WHITE + line);
            meta.setLore(lore);
        });
    }

    private void addLine(Component line) {
        stack.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.lore(lore);
        });
    }

    public ItemStackBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemStackBuilder name(String name) {
        stack.editMeta(meta -> meta.setDisplayName(name));
        return this;
    }

    public ItemStackBuilder name(Component name) {
        stack.editMeta(meta -> meta.displayName(name));
        return this;
    }

    public ItemStackBuilder loreLine(String line) {
        addLine(line);
        return this;
    }

    public ItemStackBuilder loreLine(Component line) {
        addLine(line);
        return this;
    }

    public ItemStackBuilder loreLine(String key, String value, String unit) {
        return loreLine(Component.text(DIAMOND + " ")
                .color(DIAMOND_COLOR)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(key + ": ")
                        .color(KEY_COLOR)
                        .decoration(TextDecoration.ITALIC, false))
                .append(Component.text(value + " ")
                        .color(VALUE_COLOR)
                        .decoration(TextDecoration.ITALIC, false))
                .append(Component.text(unit)
                        .color(UNIT_COLOR)
                        .decoration(TextDecoration.ITALIC, false)));
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

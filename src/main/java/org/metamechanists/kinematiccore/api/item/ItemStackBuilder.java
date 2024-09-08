package org.metamechanists.kinematiccore.api.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class ItemStackBuilder {
    private final ItemStack stack;

    public ItemStackBuilder(Material material) {
        this.stack = new ItemStack(material);
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
        stack.editMeta(meta -> {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.setLore(lore);
        });
        return this;
    }

    public ItemStackBuilder loreLine(Component line) {
        stack.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line);
            meta.lore(lore);
        });
        return this;
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

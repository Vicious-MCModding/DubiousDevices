package com.drathonix.dubiousdevices.guis;

import com.drathonix.dubiousdevices.inventory.gui.CustomGUIInventory;
import com.drathonix.dubiousdevices.inventory.gui.GUIElement;
import com.drathonix.dubiousdevices.recipe.ItemRecipe;
import com.drathonix.dubiousdevices.recipe.RecipeFlag;
import com.drathonix.dubiousdevices.recipe.RecipeHandler;
import com.vicious.viciouslibkit.util.map.ItemStackMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RecipeCreation {
    public static Map<UUID, RecipeBuilder> playerRecipeBuilders = new HashMap<>();
    public static <T extends ItemRecipe<T>> CustomGUIInventory inputsPage(String deviceName, RecipeHandler<T> handler){
        CustomGUIInventory gui = CustomGUIInventory.newGUI(deviceName + " Inputs",27);
        //Allow modification.
        for (GUIElement element : gui.elements) {
            element.cancel = false;
        }
        GUIElement outputsPageElement = GUIElement.loredElement(new ItemStack(Material.LIME_WOOL), ChatColor.GOLD.toString() + ChatColor.BOLD + "Outputs",ChatColor.GREEN + "Click me when you're ready to set the recipe inputs and move to the outputs");
        outputsPageElement.onLeftClick((ev)->{
            UUID plr = ev.getWhoClicked().getUniqueId();
            RecipeBuilder builder = new RecipeBuilder();
            ItemStackMap stackMap = new ItemStackMap();
            outputsPageElement.setStack(null);
            gui.updateElement(outputsPageElement);
            stackMap.addAll(Arrays.asList(gui.GUI.getContents()));
            builder.inputs=stackMap.getStacks();
            playerRecipeBuilders.put(plr,builder);
            gui.softClose();
            outputsPage(deviceName,handler).open((Player) ev.getWhoClicked());
        });
        gui.onClose = ()->{
            gui.accessors.forEach((u,p)->{
                playerRecipeBuilders.remove(u);
            });
        };
        gui.setElement(outputsPageElement,2,8);
        return gui;
    }
    public static <T extends ItemRecipe<T>> CustomGUIInventory outputsPage(String deviceName, RecipeHandler<T> handler) {
        CustomGUIInventory gui = CustomGUIInventory.newGUI(deviceName + " Outputs",27);
        //Allow modification.
        for (GUIElement element : gui.elements) {
            element.cancel = false;
        }
        GUIElement flagsPageElement = GUIElement.loredElement(new ItemStack(Material.LIME_WOOL), ChatColor.GOLD.toString() + ChatColor.BOLD + "Flags",ChatColor.GREEN + "Click me when you're ready to set the recipe outputs and move to the flags");
        flagsPageElement.onLeftClick((ev)->{
            UUID plr = ev.getWhoClicked().getUniqueId();
            RecipeBuilder builder = playerRecipeBuilders.get(plr);
            ItemStackMap stackMap = new ItemStackMap();
            flagsPageElement.setStack(null);
            gui.updateElement(flagsPageElement);
            stackMap.addAll(Arrays.asList(gui.GUI.getContents()));
            builder.outputs=stackMap.getStacks();
            gui.softClose();
            flagsPage(deviceName,handler).open((Player) ev.getWhoClicked());
        });
        gui.onClose = ()->{
            gui.accessors.forEach((u,p)->{
                playerRecipeBuilders.remove(u);
            });
        };
        gui.setElement(flagsPageElement,2,8);
        return gui;
    }
    public static <T extends ItemRecipe<T>> CustomGUIInventory flagsPage(String deviceName, RecipeHandler<T> handler){
        CustomGUIInventory gui = CustomGUIInventory.newGUI(deviceName + " Flags",54);
        //Allow modification.
        for (GUIElement element : gui.elements) {
            element.cancel = false;
        }
        List<String> activeFlags = new ArrayList<>();
        List<RecipeFlag> validFlags = handler.validFlags();
        for (int i = 0; i < validFlags.size(); i++) {
            RecipeFlag flag = validFlags.get(i);
            GUIElement flagElem = GUIElement.loredElement(new ItemStack(flag.material),flag.name,flag.description, flag.defaultState ? ChatColor.GREEN.toString() + ChatColor.BOLD + "ENABLED" : ChatColor.GREEN.toString() + ChatColor.BOLD + "DISABLED");
            if(flag.defaultState){
                flagElem.toggleEnchant();
                activeFlags.add(flag.name);
            }
            flagElem.onLeftClick((ev)->{
                flagElem.toggleEnchant();
                toggleFlag(activeFlags,flag,flagElem);
                gui.updateElement(flagElem);
            });
            gui.setElement(flagElem,i);
        }
        GUIElement done = GUIElement.loredElement(new ItemStack(Material.LIME_WOOL), ChatColor.GOLD.toString() + ChatColor.BOLD + "Flags",ChatColor.GREEN + "Click me when you're ready to set the recipe outputs and move to the flags");
        done.onLeftClick((ev)->{
            UUID plr = ev.getWhoClicked().getUniqueId();
            RecipeBuilder builder = playerRecipeBuilders.get(plr);
            builder.flags=activeFlags;
            handler.addRecipe(handler.defaultConstructor.construct(builder.inputs, builder.outputs, builder.flags));
            gui.close();
        });
        gui.onClose = ()->{
            gui.accessors.forEach((u,p)->{
                playerRecipeBuilders.remove(u);
            });
        };
        gui.setElement(done,5,8);
        return gui;
    }

    private static void toggleFlag(List<String> activeFlags, RecipeFlag flag, GUIElement elem) {
        int idx = activeFlags.indexOf(flag.name);
        if(idx == -1){
            activeFlags.add(flag.name);
            ItemStack stack = elem.getStack();
            ItemMeta metaverse = stack.getItemMeta();
            if(metaverse != null) {
                List<String> lore = metaverse.getLore();
                lore.set(lore.size()-1,ChatColor.GREEN.toString() + ChatColor.BOLD + "ENABLED");
                metaverse.setLore(lore);
            }
            stack.setItemMeta(metaverse);
        }
        else {
            activeFlags.remove(idx);
            ItemStack stack = elem.getStack();
            ItemMeta metaverse = stack.getItemMeta();
            if(metaverse != null) {
                List<String> lore = metaverse.getLore();
                lore.set(lore.size()-1,ChatColor.GREEN.toString() + ChatColor.BOLD + "DISABLED");
                metaverse.setLore(lore);
            }
            stack.setItemMeta(metaverse);
        }
    }
}

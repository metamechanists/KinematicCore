package org.metamechanists.kinematiccore.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.kinematiccore.test.MainTester;


@SuppressWarnings("unused")
@CommandAlias("ki|kinematic")
public class KinematicCommand extends BaseCommand {
    private static final TextColor HEADING_COLOR = TextColor.color(100, 255, 100);

    @HelpCommand
    public static void help(@NotNull CommandSender sender, @NotNull CommandHelp help) {
        sender.sendMessage(Component.text("[[ Kinematic Core ]]").color(HEADING_COLOR).decorate(TextDecoration.BOLD));
        help.showHelp();
    }

    @Subcommand("dev")
    @Description("Commands to help with Kinematic development. DO NOT use this command outside of a test server unless you are sure you know what you're doing.")
    public static class KinematicDevCommand extends BaseCommand {

        @Subcommand("test")
        @Description("Run Kinematic tests.")
        public static class KinematicTestCommand extends BaseCommand {
            @Subcommand("all")
            @Description("Run all 'non-destructive' tests.")
            public static void test(@NotNull Player player) {
                MainTester.TestResult result = new MainTester(player.getLocation()).allNonDestructive();

                player.sendMessage(ChatColor.GRAY + "[ "
                        + ChatColor.GREEN + result.passed() + ChatColor.WHITE + " passed"
                        + ChatColor.GRAY + " | "
                        + ChatColor.RED + result.passed() + ChatColor.WHITE + " failed"
                        + ChatColor.GRAY + " | "
                        + ChatColor.BLUE + result.passed() + ChatColor.WHITE + " total"
                        + ChatColor.GRAY + " ]"
                );

                if (result.failed() != 0) {
                    for (String failure : result.failures()) {
                        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.DARK_RED + failure);
                    }
                }
            }
        }
    }
}


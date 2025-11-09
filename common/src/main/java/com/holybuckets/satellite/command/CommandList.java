package com.holybuckets.satellite.command;

//Project imports

import com.holybuckets.foundation.HBUtil;
import com.holybuckets.foundation.event.CommandRegistry;
import com.holybuckets.satellite.LoggerProject;
import com.holybuckets.satellite.core.SatelliteManager;
import com.holybuckets.satellite.block.be.SatelliteBlockEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

public class CommandList {

    public static final String CLASS_ID = "033";
    private static final String PREFIX = "hbSatellites";

    public static void register() {
        CommandRegistry.register(LocateClusters::noArgs);
        CommandRegistry.register(LocateClusters::limitCount);
        CommandRegistry.register(LocateClusters::limitCountSpecifyBlockType);
        CommandRegistry.register(GetAllSatellites::command);
        CommandRegistry.register(GetAllChannels::command);
    }

    //**** STATIC UTILITY ****//

    static String posString(BlockPos pos) {
        return HBUtil.BlockUtil.positionToString(pos);
    }

    //1. Locate Clusters
    private static class LocateClusters
    {
        // Register the base command with no arguments
        private static LiteralArgumentBuilder<CommandSourceStack> noArgs() {
            return Commands.literal(PREFIX)
                .then(Commands.literal("locateClusters")
                    .executes(context -> execute(context.getSource(), -1, null)) // Default case (no args)
                );

        }

        // Register command with count argument
        private static LiteralArgumentBuilder<CommandSourceStack> limitCount() {
            return Commands.literal(PREFIX)
                .then(Commands.literal("locateClusters")
                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int count = IntegerArgumentType.getInteger(context, "count");
                            return execute(context.getSource(), count, null);
                        })
                    )
            );
        }

        // Register command with both count and blockType OR just blockType
        private static LiteralArgumentBuilder<CommandSourceStack> limitCountSpecifyBlockType() {
            return Commands.literal(PREFIX)
                .then(Commands.literal("locateClusters")
                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .then(Commands.argument("blockType", StringArgumentType.string())
                            .executes(context -> {
                                int count = IntegerArgumentType.getInteger(context, "count");
                                String blockType = StringArgumentType.getString(context, "blockType");
                                return execute(context.getSource(), count, blockType);
                            })
                        )
                    )
                    .then(Commands.argument("blockType", StringArgumentType.string())
                        .executes(context -> {
                            String blockType = StringArgumentType.getString(context, "blockType");
                            return execute(context.getSource(), -1, blockType);
                        })
                    )
            );
        }


        private static int execute(CommandSourceStack source, int count, String blockType)
        {

            LoggerProject.logDebug("010001", "Locate Clusters Command");
            return 0;
        }


    }
    //END COMMAND

    //2. Get All Satellites
    private static class GetAllSatellites
    {
        private static LiteralArgumentBuilder<CommandSourceStack> command() {
            return Commands.literal(PREFIX)
                .then(Commands.literal("getAllSatellites")
                    .executes(context -> execute(context.getSource()))
                );
        }

        private static int execute(CommandSourceStack source)
        {
            try {
                if (source.getLevel() == null) {
                    source.sendFailure(Component.literal("Command must be executed in a world"));
                    return 0;
                }

                SatelliteManager manager = SatelliteManager.get(source.getLevel());
                Set<SatelliteBlockEntity> satellites = manager.getAllSatellites();

                if (satellites.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No satellites found"), false);
                    return 0;
                }

                source.sendSuccess(() -> Component.literal("Found " + satellites.size() + " satellites:"), false);
                
                for (SatelliteBlockEntity satellite : satellites) {
                    BlockPos pos = satellite.getBlockPos();
                    int colorId = satellite.getColorId();
                    String posStr = posString(pos);
                    source.sendSuccess(() -> Component.literal("  Satellite at " + posStr + " (ColorID: " + colorId + ")"), false);
                }

                return satellites.size();
            } catch (Exception e) {
                source.sendFailure(Component.literal("Error executing getAllSatellites: " + e.getMessage()));
                return 0;
            }
        }
    }
    //END COMMAND

    //3. Get All Channels
    private static class GetAllChannels
    {
        private static LiteralArgumentBuilder<CommandSourceStack> command() {
            return Commands.literal(PREFIX)
                .then(Commands.literal("getAllChannels")
                    .executes(context -> execute(context.getSource()))
                );
        }

        private static int execute(CommandSourceStack source)
        {
            try {
                if (source.getLevel() == null) {
                    source.sendFailure(Component.literal("Command must be executed in a world"));
                    return 0;
                }

                SatelliteManager manager = SatelliteManager.get(source.getLevel());
                Set<SatelliteManager.SourceKey> channels = manager.getAllChannels();

                if (channels.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("No active channels found"), false);
                    return 0;
                }

                source.sendSuccess(() -> Component.literal("Found " + channels.size() + " active channels:"), false);
                
                for (SatelliteManager.SourceKey channel : channels) {
                    SatelliteBlockEntity satellite = channel.satellite;
                    BlockPos satellitePos = satellite.getBlockPos();
                    int colorId = satellite.getColorId();
                    String satellitePosStr = posString(satellitePos);
                    
                    BlockPos controllerPos = channel.controller.getBlockPos();
                    String controllerPosStr = posString(controllerPos);
                    
                    source.sendSuccess(() -> Component.literal("  Channel " + colorId + ": Satellite at " + satellitePosStr + " -> Controller at " + controllerPosStr), false);
                }

                return channels.size();
            } catch (Exception e) {
                source.sendFailure(Component.literal("Error executing getAllChannels: " + e.getMessage()));
                return 0;
            }
        }
    }
    //END COMMAND


}
//END CLASS COMMANDLIST

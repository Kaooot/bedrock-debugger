package dev.kaooot.debugger.command;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.data.command.ChainedSubCommandData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumConstraint;
import org.cloudburstmc.protocol.bedrock.data.command.CommandEnumData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOverloadData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParam;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermissionLevel;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.CommandEnumValue;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.api.command.annotation.Overloads;
import dev.kaooot.debugger.api.command.annotation.Parameter;
import dev.kaooot.debugger.api.command.annotation.Parameters;
import dev.kaooot.debugger.api.command.annotation.SubCommand;
import dev.kaooot.debugger.api.command.annotation.SubCommandValue;
import dev.kaooot.debugger.api.command.annotation.SubCommands;
import dev.kaooot.debugger.core.registry.Registry;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CommandRegistry extends Registry<CommandData, Command> {

    public Command parseCommand(String command) {
        command = command.substring(1).trim();

        final String name;
        final String[] args;
        if (command.contains(" ")) {
            name = command.split(" ")[0].toLowerCase();
            args = command.substring(name.length()).trim().split(" ");
        } else {
            name = command.toLowerCase();
            args = new String[0];
        }

        for (final Map.Entry<CommandData, Command> entry : this.registry.entrySet()) {
            boolean found = false;
            if (entry.getKey().getAliases() != null) {
                for (final String s : entry.getKey().getAliases().getValues().keySet()) {
                    if (s.equalsIgnoreCase(entry.getKey().getName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found || entry.getKey().getName().equalsIgnoreCase(name)) {
                final Command parsed = entry.getValue();
                parsed.setName(name);
                parsed.setArgs(args);
                return parsed;
            }
        }
        return null;
    }

    @Value
    public static class ParsedCommand {
        String name;
        Command command;
    }

    @Override
    public RegistryKey getKey() {
        return RegistryKey.COMMAND;
    }

    @Override
    protected void register(Command command) {
        final Class<? extends Command> clazz = command.getClass();

        if (!clazz.isAnnotationPresent(Name.class) &&
            !clazz.isAnnotationPresent(Description.class)) {
            throw new RuntimeException("The command " + clazz.getSimpleName() +
                " needs a name and a description");
        }

        final String name = clazz.getAnnotation(Name.class).value();

        if (name.isEmpty()) {
            throw new RuntimeException("The name for command " + clazz.getSimpleName() + " is " +
                "empty");
        }

        final String description = clazz.getAnnotation(Description.class).value();

        if (description.isEmpty()) {
            throw new IllegalStateException(
                "The description for command " + clazz.getSimpleName() + " is empty"
            );
        }

        final List<ChainedSubCommandData> subCommandDataList = new ObjectArrayList<>();

        if (clazz.isAnnotationPresent(SubCommands.class)) {
            final SubCommands subCommands = clazz.getAnnotation(SubCommands.class);

            for (final SubCommand subCommand : subCommands.value()) {
                final ChainedSubCommandData chainedSubCommandData =
                    new ChainedSubCommandData(subCommand.name());
                for (final SubCommandValue value : subCommand.values()) {
                    chainedSubCommandData.getValues().add(new ChainedSubCommandData.Value(
                            value.first(),
                            value.second()
                        )
                    );
                }
                subCommandDataList.add(chainedSubCommandData);
            }
        }

        final List<CommandOverloadData> overloadDataList = new ObjectArrayList<>();

        if (clazz.isAnnotationPresent(Overloads.class)) {
            for (final Parameters parameters : clazz.getAnnotation(Overloads.class).value()) {
                final List<CommandParamData> commandParamDataList = new ObjectArrayList<>();

                for (final Parameter parameter : parameters.overloads()) {
                    final CommandParamData data = new CommandParamData();
                    data.setName(parameter.name());
                    data.setOptional(parameter.optional());

                    if (!parameter.enumData().name().isEmpty()) {
                        data.setEnumData(readCommandEnumData(parameter.enumData()));
                    }

                    try {
                        final Field field = CommandParam.class
                            .getDeclaredField(parameter.type().name());
                        field.setAccessible(true);
                        final CommandParam param = (CommandParam) field.get(null);
                        data.setType(param);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (!parameter.postfix().isEmpty()) {
                        data.setPostfix(parameter.postfix());
                    }
                    data.getOptions().addAll(Arrays.asList(parameter.options()));

                    commandParamDataList.add(data);
                }
                overloadDataList.add(new CommandOverloadData(parameters.chaining(),
                    commandParamDataList.toArray(new CommandParamData[0])));
            }
        }

        // Aliases
        CommandEnumData aliases = null;
        if (clazz.isAnnotationPresent(
            dev.kaooot.debugger.api.command.annotation.CommandEnumData.class)) {
            aliases = readCommandEnumData(clazz
                .getAnnotation(
                    dev.kaooot.debugger.api.command.annotation.CommandEnumData.class));
        }

        this.registry.put(new CommandData(name, description,
            Collections.singleton(CommandData.Flag.TEST_USAGE), CommandPermissionLevel.ANY, aliases,
            subCommandDataList, overloadDataList.toArray(new CommandOverloadData[0])), command);
    }

    @Override
    protected boolean validateType(Class<?> clazz) {
        return true;
    }

    private static CommandEnumData readCommandEnumData(
        dev.kaooot.debugger.api.command.annotation.CommandEnumData enumData) {
        final Map<String, Set<CommandEnumConstraint>> constraints =
            new Object2ObjectOpenHashMap<>();
        for (final CommandEnumValue value : enumData.values()) {
            constraints.put(value.name(),
                new ObjectOpenHashSet<>(Arrays.asList(value.constraints())));
        }
        return new CommandEnumData(enumData.name(), constraints, enumData.soft());
    }
}
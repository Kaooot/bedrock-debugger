package dev.kaooot.debugger.screen;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.util.DebugElement;
import dev.kaooot.debugger.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.cloudburstmc.protocol.bedrock.data.payload.diagnostics.MemoryCategory;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ClientMemoryStatsScreen implements DebugScreen {

    @Override
    public void render(BedrockDebuggerProxy proxy) {
        proxy.getServer().sendDebugInfos(
            this.getElement(),
            this.getMemoryStats(proxy)
        );
    }

    @Override
    public int getIndex() {
        return 3;
    }

    @Override
    public DebugElement getElement() {
        return DebugElement.SMALL_INFO;
    }

    private List<String> getMemoryStats(BedrockDebuggerProxy proxy) {
        final List<String> list = new ObjectArrayList<>();
        list.add("§lClient Memory Stats§r");
        list.add(" ");
        list.add(" ");
        final ServerboundDiagnosticsPacket packet = proxy.getPlayer().getDiagnostics();
        list.add("Avg FPS: " + packet.getAvgFps());
        list.add(" ");
        list.add("Avg Client Sim Tick Time MS: " +
            Util.round(packet.getAvgClientSimTickTimeMS())
        );
        list.add(" ");
        list.add("Avg Begin Frame Time MS: " +
            Util.round(packet.getAvgBeginFrameTimeMS())
        );
        list.add(" ");
        list.add("Avg Input Time MS: " +
            Util.round(packet.getAvgInputTimeMS())
        );
        list.add(" ");
        list.add("Avg Render Time MS: " +
            Util.round(packet.getAvgRenderTimeMS())
        );
        list.add(" ");
        list.add("Avg End Time Time MS: " +
            Util.round(packet.getAvgEndFrameTimeMS())
        );
        list.add(" ");
        list.add("Avg Remainder Time Percent: " +
            Util.round(packet.getAvgRemainderTimePercent())
        );
        list.add(" ");
        list.add("Avg Unaccounted Time Percent: " +
            Util.round(packet.getAvgUnaccountedTimePercent())
        );
        list.add(" ");
        list.add(" ");
        list.add(
            "MemoryCategoryValues Count: " + proxy.getPlayer().getMemoryCategoryValuesCache().size()
        );

        final List<MemoryCategory> categories = proxy.getPlayer().getMemoryCategoryValuesCache()
            .keySet()
            .stream()
            .sorted(
                (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name(),
                    o2.name())
            )
            .toList();
        for (final MemoryCategory category : categories) {
            list.add(
                Util.CONVERTER.convert(category.name()) + " (" +
                    Util.round(((float) proxy.getPlayer().getMemoryCategoryValuesCache()
                        .getOrDefault(category, 0L) / 1048576), 3) + " MB)"
            );
        }
        return list;
    }
}
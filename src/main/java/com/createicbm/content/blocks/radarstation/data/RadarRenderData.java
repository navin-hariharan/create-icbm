package com.createicbm.content.blocks.radarstation.data;

import com.createicbm.content.blocks.radarstation.TileRadarStation;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RadarRenderData
{
    public static final int UV_SIZE = 54;
    private static final int MAP_RADIUS = UV_SIZE / 2 - 3;

    private final TileRadarStation host;

    @Getter
    private final List<RadarRenderDot> dots = new LinkedList<>();

    public RadarRenderData(TileRadarStation host)
    {
        this.host = host;
    }

    public void update()
    {
        dots.clear();
        addDot(host.getBlockPos().getX() + 0.5, host.getBlockPos().getZ() + 0.5, RadarDotType.MARKER);
        host.getDetectedThreats().forEach(entity -> addDot(entity.getX(), entity.getZ(), RadarDotType.HOSTILE));
        host.getIncomingThreats().forEach(missile -> addDot(missile.x(), missile.z(), RadarDotType.INCOMING));
    }

    public void addDot(double ex, double ez, RadarDotType type)
    {
        final double range = Math.max(1, host.getDetectionRange());
        final double deltaX = ex - (host.getBlockPos().getX() + 0.5);
        final double deltaZ = ez - (host.getBlockPos().getZ() + 0.5);
        double scaledX = deltaX / range * MAP_RADIUS;
        double scaledZ = deltaZ / range * MAP_RADIUS;
        final double length = Math.sqrt(scaledX * scaledX + scaledZ * scaledZ);
        if (length > MAP_RADIUS)
        {
            final double factor = MAP_RADIUS / length;
            scaledX *= factor;
            scaledZ *= factor;
        }
        final int x = Math.max(-MAP_RADIUS, Math.min(MAP_RADIUS, (int) Math.round(scaledX)));
        final int z = Math.max(-MAP_RADIUS, Math.min(MAP_RADIUS, (int) Math.round(scaledZ)));
        dots.add(new RadarRenderDot(x, z, type));
    }

    public void clear()
    {
        dots.clear();
    }

    public void setDots(List<RadarRenderDot> dots)
    {
        this.dots.clear();
        for (RadarRenderDot dot : dots)
        {
            final int x = Math.max(-MAP_RADIUS, Math.min(MAP_RADIUS, dot.getX()));
            final int y = Math.max(-MAP_RADIUS, Math.min(MAP_RADIUS, dot.getY()));
            this.dots.add(new RadarRenderDot(x, y, dot.getType()));
        }
    }

    public static List<RadarRenderDot> decodeDots(ByteBuf buf)
    {
        final List<RadarRenderDot> dots = new LinkedList<>();
        int dotCount = Math.max(0, Math.min(buf.readInt(), 4096));
        for (int i = 0; i < dotCount; i++)
        {
            dots.add(new RadarRenderDot(buf.readInt(), buf.readInt(), RadarDotType.get(buf.readByte())));
        }
        return dots;
    }

    public static void encodeDots(ByteBuf buf, List<RadarRenderDot> dots)
    {
        // Keep encoder and decoder limits aligned so a very busy scope cannot desynchronise the packet buffer.
        final int count = Math.min(dots.size(), 4096);
        buf.writeInt(count);
        // Iterate, the list is linked so indexed access would be quadratic
        final Iterator<RadarRenderDot> iterator = dots.iterator();
        for (int i = 0; i < count && iterator.hasNext(); i++)
        {
            final RadarRenderDot dot = iterator.next();
            buf.writeInt(dot.getX());
            buf.writeInt(dot.getY());
            buf.writeByte(dot.getType().ordinal());
        }
    }
}

package com.createicbm.content.blocks.radarstation.data;

import lombok.Data;

@Data
public class RadarRenderDot {
    private final int x;
    private final int y;
    private final RadarDotType type;
}

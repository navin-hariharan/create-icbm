package com.createicbm.lib.tracker;

import lombok.Data;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Single entry of a tracker event
 */
@Data
public class EventTrackerEntry {
    private final EventTrackerType type;
    private final Object[] data;

    public <DATA> DATA get(ResourceLocation key) {
        return get(key, null);
    }
    public <DATA> DATA get(ResourceLocation key, DATA placeholder) {
        final EventTrackerField<Object> field = type.getField(key);
        // A key this type has no column for gives the placeholder instead of a NullPointerException
        return field == null ? placeholder : (DATA) Optional.ofNullable(field.get(this)).orElse(placeholder);
    }

    public String getString(ResourceLocation key, String placeholder) {
        final EventTrackerField<Object> field = type.getField(key);
        return field == null ? placeholder : Optional.ofNullable(field.get(this)).map(Object::toString).orElse(placeholder);
    }
}

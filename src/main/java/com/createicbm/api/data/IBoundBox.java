package com.createicbm.api.data;

public interface IBoundBox<POS> {
    POS lowerBound();
    POS upperBound();
}

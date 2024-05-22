package com.pro4d.quickmc.util;

import java.util.Collection;

public class Filter<T> {

    private final Handler<T> handler;
    public Filter(Handler<T> handler) {
        this.handler = handler;
    }

    public void filter(Collection<T> input, FilterType type) {
        if(type == FilterType.NOTHING) return;

        if(type == FilterType.EXCLUSION_PASS) {
            input.removeIf(handler::filter);
        } else if(type == FilterType.EXCLUSION_FAIL) input.removeIf(item -> !handler.filter(item));
    }
    public boolean check(T input) {
        return handler.filter(input);
    }

    public enum FilterType {
        EXCLUSION_PASS,
        EXCLUSION_FAIL,
        NOTHING
    }

    @FunctionalInterface
    public interface Handler<T> {
        boolean filter(T item);
    }
}

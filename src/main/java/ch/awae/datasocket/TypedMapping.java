package ch.awae.datasocket;

import java.util.Map;

class TypedMapping<V> {

    private final Map<Class<?>, V> map;

    TypedMapping(Map<Class<?>, V> map) {
        this.map = map;
    }

    V get(Class<?> tClass) {
        return map.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(tClass))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

}

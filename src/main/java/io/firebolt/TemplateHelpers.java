package io.firebolt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TemplateHelpers {

    public static<T> Collection<T> reverse(Collection<T> c) {
        List<T> data = new ArrayList<>(c);
        Collections.reverse(data);
        return data;
    }

    public static<T> Collection<String> stringify(Collection<T> c) {
        return c.stream().map(Objects::toString).toList();
    }

}

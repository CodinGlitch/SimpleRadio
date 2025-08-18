package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public class RouterContainer<R extends Router> extends AbstractList<R> {
    private final ArrayList<R> content = new ArrayList<>();

    public boolean add(R router) {
        ServerSimpleRadioApi.getInstance().pushRouter(router);

        return content.add(router);
    }

    @Override
    public R get(int index) {
        return content.get(index);
    }

    @Override
    public R remove(int index) {
        return content.remove(index);
    }

    public boolean removeIf(Predicate<? super R> filter) {
        return content.removeIf(router -> {
            if (router == null) return true;

            if (filter.test(router)) {
                ServerSimpleRadioApi.getInstance().removeRouter(router.getIdentifier());
                return true;
            }
            return false;
        });
    }

    public ArrayList<R> getContent() {
        return content;
    }

    @NotNull
    @Override
    public Iterator<R> iterator() {
        return content.iterator();
    }

    @Override
    public int size() {
        return content.size();
    }
}

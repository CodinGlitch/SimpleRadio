package com.codinglitch.simpleradio.radio;

import java.util.ArrayList;
import java.util.function.Predicate;

public class RouterContainer<R extends RadioRouter> extends ArrayList<R> {
    @Override
    public boolean add(R router) {
        RadioManager.pushRouter(router);

        return super.add(router);
    }

    @Override
    public boolean removeIf(Predicate<? super R> filter) {
        return super.removeIf(router -> {
            if (router == null) return true;

            if (filter.test(router)) {
                RadioManager.removeRouter(router.getIdentifier());
                return true;
            }
            return false;
        });
    }
}

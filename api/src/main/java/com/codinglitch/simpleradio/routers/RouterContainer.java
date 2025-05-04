package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;

import java.util.ArrayList;
import java.util.function.Predicate;

public class RouterContainer<R extends Router> extends ArrayList<R> {
    @Override
    public boolean add(R router) {
        ServerSimpleRadioApi.getInstance().pushRouter(router);

        return super.add(router);
    }

    @Override
    public boolean removeIf(Predicate<? super R> filter) {
        return super.removeIf(router -> {
            if (router == null) return true;

            if (filter.test(router)) {
                ServerSimpleRadioApi.getInstance().removeRouter(router.getIdentifier());
                return true;
            }
            return false;
        });
    }
}

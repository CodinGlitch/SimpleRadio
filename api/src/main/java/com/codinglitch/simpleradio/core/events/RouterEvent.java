package com.codinglitch.simpleradio.core.events;

import com.codinglitch.intermediator.Phase;
import com.codinglitch.simpleradio.core.RadioEvent;
import com.codinglitch.simpleradio.radio.Message;
import com.codinglitch.simpleradio.routers.Router;

/**
 * Events relating to a {@link Router}.
 */
public class RouterEvent extends RadioEvent {

    /**
     * Called when any router receives a message.
     */
    public static class Messaged extends RadioEvent implements Phase {
        public final Router router;
        public final Message message;

        public Messaged(Router router, Message message) {
            this.router = router;
            this.message = message;
        }
    }

}

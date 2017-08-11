package fr.olived19.microgameoflife.queue;

import fr.olived19.microgameoflife.messages.Message;

public abstract class RPCAction {
    public abstract Message execute(String messageRequest);
}

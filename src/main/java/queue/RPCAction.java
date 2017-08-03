package queue;

import messages.Message;

public abstract class RPCAction {
    public abstract Message execute(String messageRequest);
}

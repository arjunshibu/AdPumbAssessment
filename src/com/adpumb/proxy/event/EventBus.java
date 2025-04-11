package com.adpumb.proxy.event;

import com.adpumb.proxy.command.Command;

import java.util.concurrent.*;

public class EventBus {
    private static EventBus instance;
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void publish(Command command) throws InterruptedException {
        commandQueue.put(command);
    }

    public Command take() throws InterruptedException {
        return commandQueue.take();
    }
}

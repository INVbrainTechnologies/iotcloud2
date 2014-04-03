package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.MessageConverter;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractConfigurator implements Configurator {
    protected Channel createChannel(String name, Map properties,
                                    Direction direction, int queueSize,
                                    MessageConverter converter) {

        BlockingQueue inMassages = new ArrayBlockingQueue(queueSize);
        BlockingQueue outMassages = new ArrayBlockingQueue(queueSize);

        Channel channel = new Channel(name, direction, inMassages, outMassages, converter);
        channel.addProperties(properties);

        return channel;
    }
}
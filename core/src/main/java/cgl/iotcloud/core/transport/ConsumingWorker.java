package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ConsumingWorker implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(ConsumingWorker.class);

    private List<Channel> channels;

    private boolean run;

    private BlockingQueue<MessageContext> messageContexts;

    private boolean singleChannel;

    public ConsumingWorker(List<Channel> channels, BlockingQueue<MessageContext> messageContexts, boolean singleChannel) {
        this.messageContexts = messageContexts;
        this.channels = channels;
        this.run = true;
        this.singleChannel = singleChannel;
    }

    public ConsumingWorker(List<Channel> channels, BlockingQueue<MessageContext> messageContexts) {
        this(channels, messageContexts, false);
    }

    @Override
    public void run() {
        while (run) {
            try {
                MessageContext message = messageContexts.take();
                // find the channel responsible for this message
                String sensorId = message.getSensorId();
                if (sensorId == null) {
                    String s = "The sensor id of a transport message should be present, discarding the message";
                    LOG.warn(s);
                    continue;
                }

                if (channels.size() == 0) {
                    String s = "There must be at least one channel";
                    LOG.error(s);
                    throw new RuntimeException(s);
                }

                Channel matchingChannel = null;
                if (!singleChannel) {
                    for (Channel channel : channels) {
                        if (channel.getSensorID().equals(sensorId)) {
                            matchingChannel = channel;
                            break;
                        }
                    }
                } else {
                    matchingChannel = channels.get(0);
                }

                if (matchingChannel != null) {
                    BlockingQueue receiver = matchingChannel.getOutQueue();
                    if (receiver == null) {
                        String msg = "A receiving channel should specify a MessageReceiver";
                        LOG.error(msg);
                        throw new RuntimeException(msg);
                    }

                    receiver.put(message);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to get the message from queue");
            }
        }
    }

    public void stop() {
        run = false;
    }
}

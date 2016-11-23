package cn.v5.mock;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * Created by piguangtao on 15/2/5.
 */
public class RabbitmqTemplateMock implements AmqpTemplate {
    /**
     * Send a message to a default exchange with a default routing key.
     *
     * @param message a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void send(Message message) throws AmqpException {

    }

    /**
     * Send a message to a default exchange with a specific routing key.
     *
     * @param routingKey the routing key
     * @param message    a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void send(String routingKey, Message message) throws AmqpException {

    }

    /**
     * Send a message to a specific exchange with a specific routing key.
     *
     * @param exchange   the name of the exchange
     * @param routingKey the routing key
     * @param message    a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void send(String exchange, String routingKey, Message message) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a default exchange with a default routing key.
     *
     * @param message a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(Object message) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a default exchange with a specific routing key.
     *
     * @param routingKey the routing key
     * @param message    a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(String routingKey, Object message) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a specific exchange with a specific routing key.
     *
     * @param exchange   the name of the exchange
     * @param routingKey the routing key
     * @param message    a message to send
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a default exchange with a default routing key.
     *
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a default exchange with a specific routing key.
     *
     * @param routingKey           the routing key
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {

    }

    /**
     * Convert a Java object to an Amqp {@link org.springframework.amqp.core.Message} and send it to a specific exchange with a specific routing key.
     *
     * @param exchange             the name of the exchange
     * @param routingKey           the routing key
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {

    }

    /**
     * Receive a message if there is one from a default queue. Returns immediately, possibly with a null value.
     *
     * @return a message or null if there is none waiting
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Message receive() throws AmqpException {
        return null;
    }

    /**
     * Receive a message if there is one from a specific queue. Returns immediately, possibly with a null value.
     *
     * @param queueName the name of the queue to poll
     * @return a message or null if there is none waiting
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Message receive(String queueName) throws AmqpException {
        return null;
    }

    /**
     * Receive a message if there is one from a default queue and convert it to a Java object. Returns immediately,
     * possibly with a null value.
     *
     * @return a message or null if there is none waiting
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object receiveAndConvert() throws AmqpException {
        return null;
    }

    /**
     * Receive a message if there is one from a specific queue and convert it to a Java object. Returns immediately,
     * possibly with a null value.
     *
     * @param queueName the name of the queue to poll
     * @return a message or null if there is none waiting
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object receiveAndConvert(String queueName) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern. Send a message to a default exchange with a default routing key and attempt to receive a
     * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
     * limited by a timeout.
     *
     * @param message a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Message sendAndReceive(Message message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern. Send a message to a default exchange with a specific routing key and attempt to receive a
     * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
     * limited by a timeout.
     *
     * @param routingKey the routing key
     * @param message    a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Message sendAndReceive(String routingKey, Message message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern. Send a message to a specific exchange with a specific routing key and attempt to receive a
     * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
     * limited by a timeout.
     *
     * @param exchange   the name of the exchange
     * @param routingKey the routing key
     * @param message    a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Message sendAndReceive(String exchange, String routingKey, Message message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a default
     * routing key and attempt to receive a response, converting that to a Java object. Implementations will normally
     * set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param message a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(Object message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a
     * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
     * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param routingKey the routing key
     * @param message    a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(String routingKey, Object message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a specific exchange with a
     * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
     * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param exchange   the name of the exchange
     * @param routingKey the routing key
     * @param message    a message to send
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(String exchange, String routingKey, Object message) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a default
     * routing key and attempt to receive a response, converting that to a Java object. Implementations will normally
     * set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a
     * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
     * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param routingKey           the routing key
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {
        return null;
    }

    /**
     * Basic RPC pattern with conversion. Send a Java object converted to a message to a specific exchange with a
     * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
     * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
     *
     * @param exchange             the name of the exchange
     * @param routingKey           the routing key
     * @param message              a message to send
     * @param messagePostProcessor a processor to apply to the message before it is sent
     * @return the response if there is one
     * @throws org.springframework.amqp.AmqpException if there is a problem
     */
    @Override
    public Object convertSendAndReceive(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException {
        return null;
    }
}

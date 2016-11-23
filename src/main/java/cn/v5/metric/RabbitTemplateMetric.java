package cn.v5.metric;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by piguangtao on 15/1/22.
 */
public class RabbitTemplateMetric extends RabbitTemplate implements InitializingBean{

    private MetricRegistry metricRegistry;

    private Timer reqTimer;

    private Meter errorMeter;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (null != metricRegistry) {
            reqTimer = metricRegistry.timer("rabbitmq-req-timer");
            errorMeter = metricRegistry.meter("rabbitmq-error-meter");
        }
    }

    public RabbitTemplateMetric(){
        super();
    }

    public RabbitTemplateMetric(ConnectionFactory connectionFactory){
       super(connectionFactory);
    }


    @Override
    public void send(final String exchange, final String routingKey,
                     final Message message, final CorrelationData correlationData)
            throws AmqpException{
       Timer.Context context = null;
       try{
           if(null != metricRegistry){
               context = reqTimer.time();
           }
           super.send(exchange,routingKey,message,correlationData);
       }
       catch (Exception e){
           if(null != errorMeter){
               errorMeter.mark();
           }
           throw e;
       }
        finally {
           if(null != context){
               context.close();
           }
       }
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
}

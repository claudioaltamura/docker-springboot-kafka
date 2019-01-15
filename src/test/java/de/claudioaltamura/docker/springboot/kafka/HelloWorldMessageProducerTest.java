package de.claudioaltamura.docker.springboot.kafka;

import static org.assertj.core.api.BDDAssertions.then;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {HelloWorldMessageProducer.class, HelloWorldMessageProducerConfig.class})
@DirtiesContext
public class HelloWorldMessageProducerTest {
  
    private static String SENDER_TOPIC = "topic";
    
    private Logger log = LoggerFactory.getLogger(HelloWorldMessageProducerTest.class);

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, SENDER_TOPIC);
    
    @Autowired
    private HelloWorldMessageProducer producer;

    private KafkaMessageListenerContainer<String, HelloWorldMessage> container;

    private BlockingQueue<ConsumerRecord<String, HelloWorldMessage>> records;

    @Before
    public void setUp() throws Exception {
      Map<String, Object> consumerProperties =
          KafkaTestUtils.consumerProps("producer", "false", embeddedKafka);
      consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
      consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

      DefaultKafkaConsumerFactory<String, HelloWorldMessage> consumerFactory =
          new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(),
              new JsonDeserializer<>(HelloWorldMessage.class));

      ContainerProperties containerProperties = new ContainerProperties(SENDER_TOPIC);
      container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
      records = new LinkedBlockingQueue<>();
      container.setupMessageListener((MessageListener<String, HelloWorldMessage>) record -> {
        log.debug(String.format("test-listener received message='%s'", record.value().toString()));
        records.add(record);
      });
      container.start();
      ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    @After
    public void tearDown() {
      container.stop();
    }

    @Test
    public void test() throws Exception {
      HelloWorldMessage helloWorldMessage = new HelloWorldMessage();
      helloWorldMessage.setMessage("Hola");
      helloWorldMessage.setTimestamp(1);;

      this.producer.send(SENDER_TOPIC, helloWorldMessage);

      ConsumerRecord<String, HelloWorldMessage> received = records.poll(10, TimeUnit.SECONDS);
      AssertionsForClassTypes.assertThat(received).isNotNull();

      HelloWorldMessage value = received.value();
      then(value).isInstanceOf(HelloWorldMessage.class);
      then(value.getMessage()).isEqualTo(helloWorldMessage.getMessage());
      then(value.getTimestamp()).isEqualTo(helloWorldMessage.getTimestamp());
    }
  }


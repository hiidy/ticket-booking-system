package com.seatwise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.booking.system.BookingRequestAvro;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {BookingStreamsApplication.class, BookingControllerTest.TestConfig.class})
@AutoConfigureMockMvc
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    topics = {"booking-request-test"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@TestPropertySource(
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
      "spring.kafka.producer.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer",
      "spring.kafka.producer.properties.schema.registry.url=mock://test-registry",
      "kafka.topics.booking-request=booking-request-test"
    })
class BookingControllerTest {

  @Configuration
  static class TestConfig {}

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  private KafkaMessageListenerContainer<String, BookingRequestAvro> container;
  private BlockingQueue<BookingRequestAvro> records;

  @BeforeEach
  void setUp() {
    records = new LinkedBlockingQueue<>();

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    consumerProps.put("schema.registry.url", "mock://test-registry");
    consumerProps.put("specific.avro.reader", "true");

    DefaultKafkaConsumerFactory<String, BookingRequestAvro> consumerFactory =
        new DefaultKafkaConsumerFactory<>(consumerProps);

    ContainerProperties containerProperties = new ContainerProperties("booking-request-test");
    container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    container.setupMessageListener(
        (MessageListener<String, BookingRequestAvro>) r -> records.add(r.value()));
    container.start();

    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @AfterEach
  void tearDown() {
    if (container != null) {
      container.stop();
    }
  }

  @Test
  void shouldSendBookingRequestToKafka() throws Exception {
    // Given
    BookingRequest request = new BookingRequest(1L, List.of(100L, 101L, 102L), 10L);
    String requestJson = objectMapper.writeValueAsString(request);

    // When
    MvcResult result =
        mockMvc
            .perform(
                post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isAccepted())
            .andReturn();

    String requestId = result.getResponse().getContentAsString();

    // Then
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(records).isNotEmpty());

    BookingRequestAvro avroMessage = records.poll();
    assertThat(avroMessage).isNotNull();
    assertThat(avroMessage.getMemberId()).isEqualTo(1L);
    assertThat(avroMessage.getTicketIds()).containsExactly(100L, 101L, 102L);
    assertThat(avroMessage.getSectionId()).isEqualTo(10L);
    assertThat(avroMessage.getRequestId()).isEqualTo(requestId);
  }
}

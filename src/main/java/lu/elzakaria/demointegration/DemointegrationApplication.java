package lu.elzakaria.demointegration;

import ch.qos.logback.core.util.StatusPrinter;
import lombok.extern.slf4j.Slf4j;
import lu.elzakaria.entities.Person;
import lu.elzakaria.entities.PersonRepo;
import org.h2.store.FileLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jpa.core.JpaExecutor;
import org.springframework.integration.jpa.inbound.JpaPollingChannelAdapter;
import org.springframework.messaging.MessageHandler;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Scanner;
import java.util.stream.Stream;


@SpringBootApplication(scanBasePackages = {"lu.elzakaria", "lu.elzakaria.entities"})
@EnableJpaRepositories(basePackages = "lu.elzakaria.entities")
@EnableIntegration
@EntityScan(basePackages = "lu.elzakaria")
@Slf4j
public class DemointegrationApplication {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PersonRepo personRepository;

    public static void main(String[] args) {

        AbstractApplicationContext context
                = new AnnotationConfigApplicationContext(DemointegrationApplication.class);
        context.registerShutdownHook();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter q and press <enter> to exit the program: ");

        while (true) {
            String input = scanner.nextLine();
            if ("q".equals(input.trim())) {
                break;
            }
        }
        System.exit(0);
    }


    /**
     * Populate the database with a bunch of persons to feed the poller
     * @return
     */
    @Bean
    ApplicationRunner populateDatabase() {
        return args ->
        {
            Stream.iterate(0, integer -> integer++)
                    .limit(100)
                    .forEach(integer -> {
                        Person person = new Person();
                        person.setUsername("user " + integer);
                        personRepository.save(person);
                    });
          };
    }

    @Bean
    public JpaExecutor jpaExecutor() {
        JpaExecutor executor = new JpaExecutor(this.entityManager);
        executor.setJpaQuery("select p from Person p");
        return executor;
    }

    @Bean
    @InboundChannelAdapter(channel = "jpaInputChannel",
            poller = @Poller(fixedDelay = "3000"))
    public MessageSource<?> jpaInbound() {
        return new JpaPollingChannelAdapter(jpaExecutor());
    }

    @Bean
    @ServiceActivator(inputChannel = "jpaInputChannel")
    public MessageHandler handler() {
        return message -> log.info(String.valueOf(message.getPayload()));
    }

}

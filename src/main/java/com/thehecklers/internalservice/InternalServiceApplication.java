package com.thehecklers.internalservice;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

//@EnableDiscoveryClient
@SpringBootApplication
public class InternalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(PersonRepo repo) {
        return args -> {
            repo.deleteAll();
            Stream.of(new Person("Mark", "Heckler"),
                            new Person("Julien", "Dubois"))
                    .forEach(repo::save);

            repo.findAll().forEach(System.out::println);
        };
    }
}

@Repository
interface PersonRepo extends CosmosRepository<Person, String> {
    public Person findByLastName(String lastName);
}

@RestController
@RequestMapping("/")
class InternalServiceController {
    private final PersonRepo repo;

    InternalServiceController(PersonRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public String status() {
        return "You've reached the internal service.";
    }

    @GetMapping("/greeting")
    public String greeting() {
        return "You know the secret handshake, welcome!";
    }

    @GetMapping("/people")
    public Iterable<Person> getAllPeople() {
        Iterable<Person> people = repo.findAll();

        people.forEach(System.out::println);

        return people;
    }

    @GetMapping("/person")
    public Person getPerson() {
//        Person person = repo.findByLastName(lastName);
        Person person = repo.findAll().iterator().next();

        System.out.println(person);

        return person;
    }

    @GetMapping("/sslcheck")
    public String getTlsSslStatus(HttpServletRequest request) {
        return "Secure connection from external service: " + request.isSecure();
    }
}

@Container(containerName = "data")
class Person {
    @Id
    private final String id;
    private String firstName;
    @PartitionKey
    private String lastName;

    public Person(String firstName, String lastName) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}

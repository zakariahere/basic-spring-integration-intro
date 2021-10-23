package lu.elzakaria.entities;


import lombok.Data;

import javax.persistence.*;

@Entity(name = "Person")
@Table(name =  "person")
@Data
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Basic
    private String username;
}

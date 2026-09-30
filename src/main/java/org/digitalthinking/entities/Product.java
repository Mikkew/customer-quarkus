package org.digitalthinking.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(
        uniqueConstraints=
        @UniqueConstraint(columnNames={"customer", "product"})
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer", referencedColumnName = "id")
    @JsonBackReference
    private Customer customer;

    @Column
    private Long product;

    @Transient
    private String name;

    @Transient
    private String code;

    @Transient
    private String description;
}

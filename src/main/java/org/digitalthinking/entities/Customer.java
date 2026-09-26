package org.digitalthinking.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String nombre;
    public String apellido;
    public String email;
    public String telefono;

    // Colección simple de IDs de productos asociados
    @ElementCollection
    private List<Long> productIds = new ArrayList<>();
}

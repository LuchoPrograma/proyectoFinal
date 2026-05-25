package com.example.proyectoFinal.config;

import com.example.proyectoFinal.entities.Cine;
import com.example.proyectoFinal.entities.Empleado;
import com.example.proyectoFinal.repositories.CineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Autowired
    private CineRepository cineRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Only seed if empty
            if (cineRepository.count() == 0) {
                System.out.println("Seeding database with Cines and Empleados...");

                Cine cine1 = new Cine();
                cine1.setNombre("Cinemarco");
                cine1.setDireccion("Av. Principal 123");
                
                Empleado emp1 = new Empleado();
                emp1.setNombre("Pepe Gomez");
                emp1.setDni(43823521);
                
                Empleado emp2 = new Empleado();
                emp2.setNombre("Juan Perez");
                emp2.setDni(43152000);
                
                cine1.getEmpleados().addAll(Arrays.asList(emp1, emp2));
                emp1.getCines().add(cine1);
                emp2.getCines().add(cine1);

                Cine cine2 = new Cine();
                cine2.setNombre("Cinelandia");
                cine2.setDireccion("Calle Falsa 456");
                
                Empleado emp3 = new Empleado();
                emp3.setNombre("Maria Lopez");
                emp3.setDni(38900123);
                
                Empleado emp4 = new Empleado();
                emp4.setNombre("Carlos Ruiz");
                emp4.setDni(41555666);

                cine2.getEmpleados().addAll(Arrays.asList(emp3, emp4));
                emp3.getCines().add(cine2);
                emp4.getCines().add(cine2);

                Cine cine3 = new Cine();
                cine3.setNombre("Mundopeli");
                cine3.setDireccion("Boulevard Central 789");
                
                Empleado emp5 = new Empleado();
                emp5.setNombre("Laura Martinez");
                emp5.setDni(39000111);
                
                Empleado emp6 = new Empleado();
                emp6.setNombre("Diego Torres");
                emp6.setDni(35000222);

                cine3.getEmpleados().addAll(Arrays.asList(emp5, emp6));
                emp5.getCines().add(cine3);
                emp6.getCines().add(cine3);

                Cine cine4 = new Cine();
                cine4.setNombre("ParadiCine");
                cine4.setDireccion("Ruta 9 Km 50");
                
                Empleado emp7 = new Empleado();
                emp7.setNombre("Ana Fernandez");
                emp7.setDni(40123456);
                
                Empleado emp8 = new Empleado();
                emp8.setNombre("Luis Sanchez");
                emp8.setDni(37888999);

                cine4.getEmpleados().addAll(Arrays.asList(emp7, emp8));
                emp7.getCines().add(cine4);
                emp8.getCines().add(cine4);

                cineRepository.saveAll(Arrays.asList(cine1, cine2, cine3, cine4));
                System.out.println("Seeding completed!");
            }
        };
    }
}

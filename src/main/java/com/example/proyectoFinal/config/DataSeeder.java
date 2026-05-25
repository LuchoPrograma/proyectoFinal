package com.example.proyectoFinal.config;

import com.example.proyectoFinal.entities.*;
import com.example.proyectoFinal.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;

@Configuration
public class DataSeeder {

    @Autowired
    private CineRepository cineRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Only seed if empty
            if (cineRepository.count() == 0) {
                System.out.println("Seeding database with rich Cine data, Empleados, Salas, Peliculas, Funciones, and Ventas...");

                // ==========================================
                // CINE 1: Cinemarco
                // ==========================================
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

                Sala sala1_1 = new Sala();
                sala1_1.setNumero(1);
                sala1_1.setCapacidad(100);

                Sala sala1_2 = new Sala();
                sala1_2.setNumero(2);
                sala1_2.setCapacidad(80);

                cine1.getSalas().addAll(Arrays.asList(sala1_1, sala1_2));

                Pelicula peli1_1 = new Pelicula();
                peli1_1.setTitulo("Avengers: Endgame");
                peli1_1.setGenero(Genero.ACCION);

                Pelicula peli1_2 = new Pelicula();
                peli1_2.setTitulo("Toy Story 4");
                peli1_2.setGenero(Genero.COMEDIA);

                cine1.getPeliculas().addAll(Arrays.asList(peli1_1, peli1_2));
                cineRepository.save(cine1); // Save to get generated IDs

                // Create Functions
                Funcion func1_1 = new Funcion();
                func1_1.setHorario("18:00");
                func1_1.setPelicula(peli1_1);
                func1_1.getSalas().add(sala1_1);
                sala1_1.getFunciones().add(func1_1);

                Funcion func1_2 = new Funcion();
                func1_2.setHorario("15:30");
                func1_2.setPelicula(peli1_2);
                func1_2.getSalas().add(sala1_2);
                sala1_2.getFunciones().add(func1_2);

                funcionRepository.saveAll(Arrays.asList(func1_1, func1_2));

                // Customers
                Cliente cli1_1 = new Cliente();
                cli1_1.setNombre("Ana Perez");
                cli1_1.setEmail("ana@gmail.com");

                Cliente cli1_2 = new Cliente();
                cli1_2.setNombre("Juan Gomez");
                cli1_2.setEmail("juan@gmail.com");

                clienteRepository.saveAll(Arrays.asList(cli1_1, cli1_2));

                // Ventas for Cine 1
                Venta v1_1 = new Venta();
                v1_1.setFecha(new Date());
                Pago pago1_1 = new Pago();
                pago1_1.setMonto(10000.0);
                pago1_1.setTipo(TipoPago.TARJETA);
                v1_1.setPago(pago1_1);
                v1_1.setCliente(cli1_1);

                Entrada ent1_1 = new Entrada();
                ent1_1.setPrecio(5000.0);
                ent1_1.setAsiento("A1");
                ent1_1.getFunciones().add(func1_1);
                func1_1.getEntradas().add(ent1_1);

                Entrada ent1_2 = new Entrada();
                ent1_2.setPrecio(5000.0);
                ent1_2.setAsiento("A2");
                ent1_2.getFunciones().add(func1_1);
                func1_1.getEntradas().add(ent1_2);

                v1_1.getEntradas().addAll(Arrays.asList(ent1_1, ent1_2));
                cine1.getVentas().add(v1_1);

                Venta v1_2 = new Venta();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -1);
                v1_2.setFecha(cal.getTime());
                Pago pago1_2 = new Pago();
                pago1_2.setMonto(12000.0);
                pago1_2.setTipo(TipoPago.EFECTIVO);
                v1_2.setPago(pago1_2);
                v1_2.setCliente(cli1_2);

                Entrada ent1_3 = new Entrada();
                ent1_3.setPrecio(4000.0);
                ent1_3.setAsiento("B3");
                ent1_3.getFunciones().add(func1_2);
                func1_2.getEntradas().add(ent1_3);

                Entrada ent1_4 = new Entrada();
                ent1_4.setPrecio(4000.0);
                ent1_4.setAsiento("B4");
                ent1_4.getFunciones().add(func1_2);
                func1_2.getEntradas().add(ent1_4);

                Entrada ent1_5 = new Entrada();
                ent1_5.setPrecio(4000.0);
                ent1_5.setAsiento("B5");
                ent1_5.getFunciones().add(func1_2);
                func1_2.getEntradas().add(ent1_5);

                v1_2.getEntradas().addAll(Arrays.asList(ent1_3, ent1_4, ent1_5));
                cine1.getVentas().add(v1_2);

                cineRepository.save(cine1);
                funcionRepository.saveAll(Arrays.asList(func1_1, func1_2));


                // ==========================================
                // CINE 2: Cinelandia
                // ==========================================
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

                Sala sala2_1 = new Sala();
                sala2_1.setNumero(1);
                sala2_1.setCapacidad(120);

                Sala sala2_2 = new Sala();
                sala2_2.setNumero(2);
                sala2_2.setCapacidad(90);

                cine2.getSalas().addAll(Arrays.asList(sala2_1, sala2_2));

                Pelicula peli2_1 = new Pelicula();
                peli2_1.setTitulo("Joker");
                peli2_1.setGenero(Genero.DRAMA);

                Pelicula peli2_2 = new Pelicula();
                peli2_2.setTitulo("Inception");
                peli2_2.setGenero(Genero.SUSPENSO);

                cine2.getPeliculas().addAll(Arrays.asList(peli2_1, peli2_2));
                cineRepository.save(cine2);

                // Create Functions
                Funcion func2_1 = new Funcion();
                func2_1.setHorario("20:30");
                func2_1.setPelicula(peli2_1);
                func2_1.getSalas().add(sala2_1);
                sala2_1.getFunciones().add(func2_1);

                Funcion func2_2 = new Funcion();
                func2_2.setHorario("22:00");
                func2_2.setPelicula(peli2_2);
                func2_2.getSalas().add(sala2_2);
                sala2_2.getFunciones().add(func2_2);

                funcionRepository.saveAll(Arrays.asList(func2_1, func2_2));

                // Customers
                Cliente cli2_1 = new Cliente();
                cli2_1.setNombre("Roberto Sanchez");
                cli2_1.setEmail("roberto@gmail.com");

                Cliente cli2_2 = new Cliente();
                cli2_2.setNombre("Laura Gomez");
                cli2_2.setEmail("laura.g@gmail.com");

                clienteRepository.saveAll(Arrays.asList(cli2_1, cli2_2));

                // Ventas for Cine 2
                Venta v2_1 = new Venta();
                v2_1.setFecha(new Date());
                Pago pago2_1 = new Pago();
                pago2_1.setMonto(6000.0);
                pago2_1.setTipo(TipoPago.TARJETA);
                v2_1.setPago(pago2_1);
                v2_1.setCliente(cli2_1);

                Entrada ent2_1 = new Entrada();
                ent2_1.setPrecio(6000.0);
                ent2_1.setAsiento("H12");
                ent2_1.getFunciones().add(func2_1);
                func2_1.getEntradas().add(ent2_1);

                v2_1.getEntradas().add(ent2_1);
                cine2.getVentas().add(v2_1);

                Venta v2_2 = new Venta();
                Calendar cal2 = Calendar.getInstance();
                cal2.add(Calendar.DAY_OF_YEAR, -2);
                v2_2.setFecha(cal2.getTime());
                Pago pago2_2 = new Pago();
                pago2_2.setMonto(10000.0);
                pago2_2.setTipo(TipoPago.EFECTIVO);
                v2_2.setPago(pago2_2);
                v2_2.setCliente(cli2_2);

                Entrada ent2_2 = new Entrada();
                ent2_2.setPrecio(5000.0);
                ent2_2.setAsiento("C4");
                ent2_2.getFunciones().add(func2_2);
                func2_2.getEntradas().add(ent2_2);

                Entrada ent2_3 = new Entrada();
                ent2_3.setPrecio(5000.0);
                ent2_3.setAsiento("C5");
                ent2_3.getFunciones().add(func2_2);
                func2_2.getEntradas().add(ent2_3);

                v2_2.getEntradas().addAll(Arrays.asList(ent2_2, ent2_3));
                cine2.getVentas().add(v2_2);

                cineRepository.save(cine2);
                funcionRepository.saveAll(Arrays.asList(func2_1, func2_2));


                // ==========================================
                // CINE 3: Mundopeli
                // ==========================================
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

                Sala sala3_1 = new Sala();
                sala3_1.setNumero(1);
                sala3_1.setCapacidad(150);

                Sala sala3_2 = new Sala();
                sala3_2.setNumero(2);
                sala3_2.setCapacidad(100);

                cine3.getSalas().addAll(Arrays.asList(sala3_1, sala3_2));

                Pelicula peli3_1 = new Pelicula();
                peli3_1.setTitulo("John Wick");
                peli3_1.setGenero(Genero.ACCION);

                Pelicula peli3_2 = new Pelicula();
                peli3_2.setTitulo("The Hangover");
                peli3_2.setGenero(Genero.COMEDIA);

                cine3.getPeliculas().addAll(Arrays.asList(peli3_1, peli3_2));
                cineRepository.save(cine3);

                // Create Functions
                Funcion func3_1 = new Funcion();
                func3_1.setHorario("19:00");
                func3_1.setPelicula(peli3_1);
                func3_1.getSalas().add(sala3_1);
                sala3_1.getFunciones().add(func3_1);

                Funcion func3_2 = new Funcion();
                func3_2.setHorario("21:30");
                func3_2.setPelicula(peli3_2);
                func3_2.getSalas().add(sala3_2);
                sala3_2.getFunciones().add(func3_2);

                funcionRepository.saveAll(Arrays.asList(func3_1, func3_2));

                // Customers
                Cliente cli3_1 = new Cliente();
                cli3_1.setNombre("Carlos Perez");
                cli3_1.setEmail("carlos.p@gmail.com");

                Cliente cli3_2 = new Cliente();
                cli3_2.setNombre("Elena Rodriguez");
                cli3_2.setEmail("elena.r@gmail.com");

                clienteRepository.saveAll(Arrays.asList(cli3_1, cli3_2));

                // Ventas for Cine 3
                Venta v3_1 = new Venta();
                v3_1.setFecha(new Date());
                Pago pago3_1 = new Pago();
                pago3_1.setMonto(9000.0);
                pago3_1.setTipo(TipoPago.TARJETA);
                v3_1.setPago(pago3_1);
                v3_1.setCliente(cli3_1);

                Entrada ent3_1 = new Entrada();
                ent3_1.setPrecio(4500.0);
                ent3_1.setAsiento("F8");
                ent3_1.getFunciones().add(func3_1);
                func3_1.getEntradas().add(ent3_1);

                Entrada ent3_2 = new Entrada();
                ent3_2.setPrecio(4500.0);
                ent3_2.setAsiento("F9");
                ent3_2.getFunciones().add(func3_1);
                func3_1.getEntradas().add(ent3_2);

                v3_1.getEntradas().addAll(Arrays.asList(ent3_1, ent3_2));
                cine3.getVentas().add(v3_1);

                Venta v3_2 = new Venta();
                Calendar cal3 = Calendar.getInstance();
                cal3.add(Calendar.DAY_OF_YEAR, -3);
                v3_2.setFecha(cal3.getTime());
                Pago pago3_2 = new Pago();
                pago3_2.setMonto(4500.0);
                pago3_2.setTipo(TipoPago.EFECTIVO);
                v3_2.setPago(pago3_2);
                v3_2.setCliente(cli3_2);

                Entrada ent3_3 = new Entrada();
                ent3_3.setPrecio(4500.0);
                ent3_3.setAsiento("D10");
                ent3_3.getFunciones().add(func3_2);
                func3_2.getEntradas().add(ent3_3);

                v3_2.getEntradas().add(ent3_3);
                cine3.getVentas().add(v3_2);

                cineRepository.save(cine3);
                funcionRepository.saveAll(Arrays.asList(func3_1, func3_2));


                // ==========================================
                // CINE 4: ParadiCine
                // ==========================================
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

                Sala sala4_1 = new Sala();
                sala4_1.setNumero(1);
                sala4_1.setCapacidad(200);

                Sala sala4_2 = new Sala();
                sala4_2.setNumero(2);
                sala4_2.setCapacidad(120);

                cine4.getSalas().addAll(Arrays.asList(sala4_1, sala4_2));

                Pelicula peli4_1 = new Pelicula();
                peli4_1.setTitulo("Interstellar");
                peli4_1.setGenero(Genero.SUSPENSO);

                Pelicula peli4_2 = new Pelicula();
                peli4_2.setTitulo("Parasite");
                peli4_2.setGenero(Genero.DRAMA);

                cine4.getPeliculas().addAll(Arrays.asList(peli4_1, peli4_2));
                cineRepository.save(cine4);

                // Create Functions
                Funcion func4_1 = new Funcion();
                func4_1.setHorario("17:45");
                func4_1.setPelicula(peli4_1);
                func4_1.getSalas().add(sala4_1);
                sala4_1.getFunciones().add(func4_1);

                Funcion func4_2 = new Funcion();
                func4_2.setHorario("23:15");
                func4_2.setPelicula(peli4_2);
                func4_2.getSalas().add(sala4_2);
                sala4_2.getFunciones().add(func4_2);

                funcionRepository.saveAll(Arrays.asList(func4_1, func4_2));

                // Customers
                Cliente cli4_1 = new Cliente();
                cli4_1.setNombre("Miguel Alvarez");
                cli4_1.setEmail("miguel.a@gmail.com");

                Cliente cli4_2 = new Cliente();
                cli4_2.setNombre("Sofia Martinez");
                cli4_2.setEmail("sofia.m@gmail.com");

                clienteRepository.saveAll(Arrays.asList(cli4_1, cli4_2));

                // Ventas for Cine 4
                Venta v4_1 = new Venta();
                v4_1.setFecha(new Date());
                Pago pago4_1 = new Pago();
                pago4_1.setMonto(24000.0);
                pago4_1.setTipo(TipoPago.TARJETA);
                v4_1.setPago(pago4_1);
                v4_1.setCliente(cli4_1);

                for (int i = 1; i <= 4; i++) {
                    Entrada ent = new Entrada();
                    ent.setPrecio(6000.0);
                    ent.setAsiento("G" + i);
                    ent.getFunciones().add(func4_1);
                    func4_1.getEntradas().add(ent);
                    v4_1.getEntradas().add(ent);
                }
                cine4.getVentas().add(v4_1);

                Venta v4_2 = new Venta();
                Calendar cal4 = Calendar.getInstance();
                cal4.add(Calendar.DAY_OF_YEAR, -4);
                v4_2.setFecha(cal4.getTime());
                Pago pago4_2 = new Pago();
                pago4_2.setMonto(11000.0);
                pago4_2.setTipo(TipoPago.TARJETA);
                v4_2.setPago(pago4_2);
                v4_2.setCliente(cli4_2);

                Entrada ent4_5 = new Entrada();
                ent4_5.setPrecio(5500.0);
                ent4_5.setAsiento("E7");
                ent4_5.getFunciones().add(func4_2);
                func4_2.getEntradas().add(ent4_5);

                Entrada ent4_6 = new Entrada();
                ent4_6.setPrecio(5500.0);
                ent4_6.setAsiento("E8");
                ent4_6.getFunciones().add(func4_2);
                func4_2.getEntradas().add(ent4_6);

                v4_2.getEntradas().addAll(Arrays.asList(ent4_5, ent4_6));
                cine4.getVentas().add(v4_2);

                cineRepository.save(cine4);
                funcionRepository.saveAll(Arrays.asList(func4_1, func4_2));

                System.out.println("Seeding completed successfully!");
            }
        };
    }
}

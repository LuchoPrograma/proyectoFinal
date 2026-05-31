# Taquilla: Sistema de Gestión de Cines

Sistema web para la gestión de sucursales de cine. Permite administrar ventas de entradas, programar funciones, gestionar empleados y consultar el historial de operaciones por sucursal.

Por Luciano Augusto Federicci - Comisión A
| Programación Orientada a Objetos - Prof. Martín Vargas

---

## Tabla de Contenidos

- [Descripción general](#descripción-general)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura del proyecto](#arquitectura-del-proyecto)
- [Modelo de dominio](#modelo-de-dominio)
- [Funcionalidades principales](#funcionalidades-principales)
- [API REST](#api-rest)
- [Frontend](#frontend)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Estructura de directorios](#estructura-de-directorios)

---

## Descripción General

**Taquilla** es una aplicación full-stack diseñada para gestionar las operaciones diarias de una cadena de cines con múltiples sucursales. Cada sucursal (denominada `Cine` en el dominio) tiene sus propias salas, funciones, empleados y películas.

Los empleados acceden al sistema seleccionando su sucursal e identificándose con su DNI. Una vez dentro del dashboard, pueden:

- Consultar y registrar ventas de entradas.
- Programar nuevas funciones en las salas de la sucursal.
- Dar de alta nuevos empleados vinculados a la sucursal.

---

## Tecnologías Utilizadas

### Backend

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 25 | Lenguaje principal |
| Spring Boot | 3.5.x | Framework web y de inyección de dependencias |
| Spring Data JPA | — | Acceso y abstracción de la base de datos |
| Hibernate Envers | — | Auditoría automática de entidades |
| MySQL | 8.x | Base de datos relacional |
| Lombok | — | Reducción de código boilerplate (getters, setters, constructores) |
| Gradle | — | Herramienta de build |

### Frontend

| Tecnología | Rol |
|---|---|
| HTML5 | Estructura de las páginas |
| CSS3 (Vanilla) | Estilos con diseño glassmorphism y animaciones |
| JavaScript (ES6+) | Lógica del cliente, llamadas a la API REST |
| Google Fonts (Inter) | Tipografía |

---

## Arquitectura del Proyecto

El proyecto sigue una arquitectura en capas estándar de Spring Boot, reforzada por un patrón de clases base genéricas:

```
┌──────────────────────────────────────────────────────┐
│                    Frontend (HTML/JS)                │
│  index.html │ dashboard.html │ programar-funciones   │
│             │  nuevo-empleado.html                   │
└────────────────────────┬─────────────────────────────┘
                         │ HTTP/REST (localhost:9000)
┌────────────────────────▼─────────────────────────────┐
│                   Capa Controller                    │
│  BaseControllerImpl<E,S>  →  implementación genérica │
│  CineController / FuncionController / ...            │
└────────────────────────┬─────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────┐
│                    Capa Service                      │
│  BaseServiceImpl<E,ID>  →  CRUD genérico transaccional│
│  CineServiceImpl / FuncionServiceImpl / VentaServiceImpl│
└────────────────────────┬─────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────┐
│                  Capa Repository                     │
│  BaseRepository<E,ID>  →  extends JpaRepository     │
│  CineRepository / FuncionRepository / ...            │
└────────────────────────┬─────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────┐
│                MySQL Database                        │
└──────────────────────────────────────────────────────┘
```

### Patrón de clases base genéricas

Para evitar la duplicación de código CRUD en cada entidad, el proyecto implementa una jerarquía de clases base parametrizadas:

- **`Base`** — Entidad JPA abstracta con el campo `id` autogenerado. Todas las entidades heredan de ella.
- **`BaseService<E, ID>`** — Interfaz genérica con los métodos CRUD estándar.
- **`BaseServiceImpl<E, ID>`** — Implementación abstracta de `BaseService` que delega en `BaseRepository`. Cada servicio concreto la extiende y puede agregar métodos específicos del dominio.
- **`BaseController<E, ID>`** — Interfaz que define el contrato REST (GET, POST, PUT, DELETE).
- **`BaseControllerImpl<E, S>`** — Implementación abstracta del controlador que inyecta el servicio genérico y expone los endpoints estándar. Cada controlador concreto la extiende para agregar endpoints específicos.

---

## Modelo de Dominio

```
Cine
 ├── nombre, dirección
 ├── List<Sala>         (OneToMany → FK fk_cine)
 ├── List<Pelicula>     (OneToMany → FK fk_cine)
 ├── List<Empleado>     (ManyToMany → tabla cine_empleado)
 └── List<Venta>        (OneToMany → FK fk_cine, solo lectura)

Sala
 ├── numero, capacidad
 └── List<Funcion>      (ManyToMany → tabla sala_funcion)

Funcion
 ├── horario
 ├── Pelicula           (ManyToOne)
 └── List<Entrada>      (ManyToMany → tabla funcion_entrada)

Venta
 ├── fecha
 ├── Cliente            (ManyToOne)
 ├── Funcion            (ManyToOne)
 └── Pago               (ManyToOne)

Empleado
 └── nombre (nombre + apellido concatenados), dni

Pelicula
 └── titulo, genero (enum: ACCION, COMEDIA, DRAMA, TERROR, CIENCIA_FICCION...)

Cliente
 └── nombre, email

Pago
 └── monto, tipo (enum: TARJETA | EFECTIVO)
```

> **Nota sobre ventas y cascades**: La relación `Cine → Venta` es de **solo lectura** en JPA (`insertable = false, updatable = false`). La FK `fk_cine` se asigna mediante una query nativa en `VentaRepository.assignToCine()` para evitar el bug de Hibernate "Multiple representations of the same entity" que ocurría al hacer cascade sobre colecciones lazy previas.

---

## Funcionalidades Principales

### 1. Selección de sucursal y empleado (`index.html`)
El empleado selecciona su sucursal entre todas las disponibles y luego elige su nombre de la lista de empleados vinculados a esa sucursal. La selección se persiste en `localStorage` antes de redirigir al dashboard.

### 2. Dashboard de ventas (`dashboard.html`)
- Muestra el historial de ventas de la sucursal, ordenadas de la más reciente a la más antigua.
- Permite registrar nuevas ventas mediante un modal de 4 pasos:
  1. **Cliente**: selección de cliente existente o alta de nuevo cliente.
  2. **Función**: selección de película y horario de función disponible en la sucursal.
  3. **Asientos**: grilla interactiva de butacas con disponibilidad en tiempo real.
  4. **Pago**: elección del método de pago y confirmación con resumen detallado.
- Al confirmar, se emite una **pantalla de tickets** con código de entrada generado.

### 3. Programar nuevas funciones (`programar-funciones.html`)
- Permite agendar una nueva función en una sala de la sucursal.
- Soporta selección de película existente **o** ingreso de película nueva (con género).
- El backend valida que no exista otra función en la misma sala con menos de **3 horas** de diferencia.

### 4. Alta de empleados (`nuevo-empleado.html`)
- Formulario con validación de nombre (solo letras), apellido (solo letras) y DNI (7 a 9 dígitos numéricos).
- Si el empleado ya existe en el sistema (mismo DNI), el backend lo reutiliza y simplemente lo vincula a la sucursal actual, sin duplicar registros.
- El nuevo empleado queda disponible inmediatamente en el login de `index.html`.

### 5. Auditoría con Hibernate Envers
Las entidades marcadas con `@Audited` (como `Cine` y `Funcion`) tienen seguimiento automático de cambios en tablas de auditoría generadas por Envers.

---

## API REST

La API corre en `http://localhost:9000`. Todos los endpoints aceptan y devuelven JSON.

### Endpoints genéricos (disponibles para todas las entidades)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/{recurso}` | Listar todos |
| `GET` | `/api/v1/{recurso}/paged` | Listar con paginación |
| `GET` | `/api/v1/{recurso}/{id}` | Obtener por ID |
| `POST` | `/api/v1/{recurso}` | Crear nuevo |
| `PUT` | `/api/v1/{recurso}/{id}` | Actualizar existente |
| `DELETE` | `/api/v1/{recurso}/{id}` | Eliminar |

Recursos disponibles: `cines`, `salas`, `funciones`, `peliculas`, `clientes`, `empleados`, `ventas`, `pagos`, `entradas`.

### Endpoints específicos de dominio

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/cines/{id}/clientes` | Clientes con ventas en la sucursal |
| `POST` | `/api/v1/cines/{id}/ventas` | Registrar venta completa (atómica) |
| `POST` | `/api/v1/cines/{id}/empleados` | Alta o vinculación de empleado |
| `POST` | `/api/v1/funciones/programar` | Programar nueva función en sala |

### Ejemplo: Registrar una venta

```http
POST /api/v1/cines/1/ventas
Content-Type: application/json

{
  "clienteId": 3,
  "funcionId": 7,
  "asientos": ["A1", "A2"],
  "precioUnitario": 8000,
  "tipoPago": "TARJETA",
  "fecha": "2026-05-31"
}
```

### Ejemplo: Programar una función nueva

```http
POST /api/v1/funciones/programar
Content-Type: application/json

{
  "salaId": 2,
  "horario": "20:00",
  "cineId": 1,
  "peliculaId": 5
}
```

### Ejemplo: Alta de empleado nuevo

```http
POST /api/v1/cines/1/empleados
Content-Type: application/json

{
  "nombre": "Luciano",
  "apellido": "García",
  "dni": 40123456
}
```

---

## Frontend

El frontend es una aplicación multi-página en HTML/CSS/JS puro, sin frameworks. Se comunica con el backend a través de `fetch()`.

| Archivo | Descripción |
|---------|-------------|
| `index.html` + `app.js` | Pantalla de login: selección de sucursal y empleado |
| `dashboard.html` + `dashboard.js` | Panel principal: historial de ventas y modal de nueva venta |
| `programar-funciones.html` + `programar-funciones.js` | Formulario de programación de funciones |
| `nuevo-empleado.html` + `nuevo-empleado.js` | Formulario de alta de empleados |
| `css/style.css` | Estilos globales (glassmorphism, dark mode, animaciones) |

### Gestión de estado en el cliente

La sesión activa se almacena en `localStorage` con las claves:
- `selectedCine`: objeto JSON con los datos de la sucursal seleccionada.
- `selectedEmpleado`: objeto JSON con los datos del empleado que inició sesión.

Cualquier página que requiera sesión activa verifica la existencia de estas claves al cargar. Si no existen, redirige a `index.html`.

---

## Cómo Ejecutar el Proyecto

### Prerrequisitos

- **Java 21+** (compatible con Java 25 configurado en el proyecto)
- **MySQL 8.x** corriendo en `localhost:3306`
- Un servidor HTTP estático para el frontend (Live Server de VS Code, por ejemplo)

### 1. Configurar la base de datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE proyectofinal_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configurar `application.properties`

El archivo se encuentra en `src/main/resources/application.properties`. Configurar las credenciales de MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/proyectofinal_db
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
spring.jpa.hibernate.ddl-auto=update
server.port=9000
```

### 3. Ejecutar el backend

Desde la raíz del proyecto, con Gradle Wrapper:

```bash
# Windows
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```
O bien ejecutar la clase main ProyectoFinalApplication usando IntelliJ. 

El servidor arranca en `http://localhost:9000`.


### 4. Servir el frontend

Abrir la carpeta `frontend/` con un servidor HTTP local. Con VS Code y la extensión **Live Server**:

1. Clic derecho sobre `frontend/index.html`
2. Seleccionar **"Open with Live Server"**

O con Node.js:

```bash
npx serve frontend/
```

Luego acceder a `http://localhost:5500` (o el puerto que indique Live Server).

### 5. Acceder a la aplicación

1. Ir a `index.html`.
2. Seleccionar una sucursal.
3. Seleccionar un empleado de la lista.
4. Hacer clic en **"Ingresar"** para acceder al dashboard.

---

## Estructura de Directorios

```
proyectoFinal/
├── frontend/                        # Aplicación web (cliente)
│   ├── css/
│   │   └── style.css                # Estilos globales
│   ├── js/
│   │   ├── app.js                   # Lógica de la pantalla de login
│   │   ├── dashboard.js             # Lógica del dashboard y modal de ventas
│   │   ├── programar-funciones.js   # Lógica del formulario de funciones
│   │   └── nuevo-empleado.js        # Lógica del formulario de empleados
│   ├── index.html                   # Pantalla de selección de sucursal/empleado
│   ├── dashboard.html               # Panel principal de ventas
│   ├── programar-funciones.html     # Formulario de programación de funciones
│   └── nuevo-empleado.html          # Formulario de alta de empleados
│
└── src/main/java/com/example/proyectoFinal/
    ├── ProyectoFinalApplication.java    # Punto de entrada Spring Boot
    ├── audit/                           # Configuración de Hibernate Envers
    ├── config/                          # Configuración de CORS y otros beans
    ├── controllers/
    │   ├── BaseController.java          # Interfaz REST genérica
    │   ├── BaseControllerImpl.java      # Implementación REST genérica
    │   ├── CineController.java          # Endpoints específicos de Cine
    │   ├── FuncionController.java       # Endpoint de programación de funciones
    │   └── ...                          # Controladores por entidad
    ├── dto/
    │   ├── RegistrarVentaRequest.java   # DTO para registrar una venta
    │   ├── ProgramarFuncionRequest.java # DTO para programar una función
    │   └── CrearEmpleadoRequest.java    # DTO para dar de alta un empleado
    ├── entities/
    │   ├── Base.java                    # Entidad base con campo id
    │   ├── Cine.java                    # Sucursal de cine
    │   ├── Sala.java                    # Sala de proyección
    │   ├── Funcion.java                 # Función/horario de una película
    │   ├── Pelicula.java                # Película con título y género
    │   ├── Venta.java                   # Registro de venta
    │   ├── Cliente.java                 # Cliente comprador
    │   ├── Empleado.java                # Empleado de la sucursal
    │   ├── Entrada.java                 # Entrada/ticket individual
    │   ├── Pago.java                    # Información de pago
    │   ├── Genero.java                  # Enum de géneros cinematográficos
    │   └── TipoPago.java                # Enum TARJETA | EFECTIVO
    ├── repositories/
    │   ├── BaseRepository.java          # Repositorio genérico (JpaRepository)
    │   └── ...                          # Repositorios por entidad
    └── services/
        ├── BaseService.java             # Interfaz de servicio genérica
        ├── BaseServiceImpl.java         # Implementación CRUD genérica
        ├── CineService.java             # Contrato del servicio de Cine
        ├── CineServiceImpl.java         # Lógica de negocio del Cine
        ├── FuncionService.java          # Contrato del servicio de Función
        ├── FuncionServiceImpl.java      # Lógica de programación y asientos
        ├── VentaService.java            # Contrato del servicio de Venta
        ├── VentaServiceImpl.java        # Lógica de registro de ventas (atómica)
        └── ...                          # Servicios por entidad
```

---

## Decisiones de Diseño Relevantes

### ¿Por qué la FK `fk_cine` de Venta se asigna con query nativa?

Cuando se hace un `repository.save(cine)` con cascades activos, Hibernate intenta gestionar todas las ventas previas de ese cine, lo que provoca el error *"Multiple representations of the same entity"* si alguna venta tiene objetos lazily loaded ya en sesión. La solución implementada es:
1. La relación `Cine → Venta` se declara con `insertable = false, updatable = false` (solo lectura en JPA).
2. La FK se asigna manualmente con un `@Query` nativo en `VentaRepository`.

### ¿Por qué el registro de ventas es un endpoint dedicado (`POST /cines/{id}/ventas`)?

Centralizar el proceso en un endpoint atómico (en lugar de múltiples PUT encadenados) garantiza consistencia: o todos los pasos (crear entradas, crear pago, crear venta, asignar FK) se completan juntos, o ninguno persiste.

### ¿Por qué los empleados se gestionan con upsert por DNI?

Un empleado puede trabajar en múltiples sucursales. Si al dar de alta un empleado su DNI ya existe en la base de datos, el sistema reutiliza el registro existente y solo añade el vínculo con la nueva sucursal. Esto evita duplicados y permite movilidad entre sucursales.

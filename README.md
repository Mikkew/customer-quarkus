# customer — Guía práctica de Quarkus, Panache, GraphQL y Blaze Persistence

Proyecto educativo en **Quarkus 3.39 / Java 21** que gestiona *clientes* y sus *productos* y, a la vez,
sirve de laboratorio para comparar **distintas formas de resolver el mismo problema**:

- tres maneras de acceder a la base de datos (Panache, Spring Data JPA, Blaze Persistence Entity Views);
- dos maneras de exponer la API (REST y GraphQL);
- tres maneras de consumir otro servicio (WebClient reactivo de Vert.x, cliente GraphQL propio y cliente
  GraphQL declarativo de Quarkus).

Está pensado para leerse con el código al lado: cada sección indica en qué clase mirar.

---

## Índice

1. [Qué hace la aplicación](#1-qué-hace-la-aplicación)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura y capas](#3-arquitectura-y-capas)
4. [Modelo de datos](#4-modelo-de-datos)
5. [Tres formas de acceder a datos](#5-tres-formas-de-acceder-a-datos)
6. [Consumo de servicios externos](#6-consumo-de-servicios-externos)
7. [Catálogo de endpoints](#7-catálogo-de-endpoints)
8. [API GraphQL](#8-api-graphql)
9. [Manejo de errores](#9-manejo-de-errores)
10. [Configuración](#10-configuración)
11. [Cómo ejecutarlo](#11-cómo-ejecutarlo)
12. [Pruebas](#12-pruebas)
13. [Estructura del repositorio](#13-estructura-del-repositorio)
14. [Ejercicios sugeridos](#14-ejercicios-sugeridos)
15. [Historia del proyecto](#15-historia-del-proyecto)

---

## 1. Qué hace la aplicación

Es un CRUD de **clientes** (`Customer`) donde cada cliente tiene una lista de **productos** (`Product`).
Los datos de un producto están repartidos en dos lugares:

| Dato | Dónde vive |
|---|---|
| `id`, `customer`, `product` (id del producto externo) | Base de datos local (PostgreSQL) |
| `name` | Columna local, pero puede enriquecerse desde el servicio externo |
| `code`, `description` | **Solo** en el servicio externo de productos (campos `@Transient` aquí) |

Esa división motiva el endpoint más interesante, `GET /customer/{id}/product`: lee el cliente de la base
de datos, pide el catálogo al servicio de productos **en paralelo** y combina ambos resultados.

La aplicación **depende de un servicio de productos externo** que debe escuchar en
`http://localhost:8081` y exponer `GET /product` (REST) y `/graphql` (GraphQL). Ese servicio **no está
en este repositorio**.

---

## 2. Stack tecnológico

| Tecnología | Para qué se usa aquí |
|---|---|
| **Quarkus** (`quarkus-rest`, `quarkus-rest-jackson`) | Framework, REST reactivo, serialización JSON |
| **Hibernate ORM + Panache** | Acceso a datos "estilo Quarkus" (`CustomerRepository`) |
| **Spring Data JPA** (`quarkus-spring-data-jpa`) | Repositorios derivados por nombre de método (`CustomerSpringRepository`) |
| **Blaze Persistence + Entity Views** | Proyecciones de solo lectura tipadas (`CustomerView`) |
| **PostgreSQL** (`quarkus-jdbc-postgresql`) | Base de datos |
| **SmallRye GraphQL** | Servidor GraphQL (`CustomerResources`) |
| **SmallRye GraphQL Client** | Cliente GraphQL dinámico (servicios de producto) |
| **Mutiny / Vert.x WebClient** | Programación reactiva y llamadas HTTP no bloqueantes |
| **SmallRye OpenAPI + Swagger UI** | Documentación interactiva de la API REST |
| **Lombok** | `@Data`, `@NoArgsConstructor`, `@Slf4j`… para reducir boilerplate |
| **JUnit 5, Mockito, REST Assured** | Pruebas |

---

## 3. Arquitectura y capas

```
        ┌────────────────────────── entrada ──────────────────────────┐
        │  controllers/        REST   (/customer, /v1/product, ...)   │
        │  graphql/            GraphQL (/graphql)                     │
        └───────────────┬─────────────────────────────────────────────┘
                        │
                ┌───────▼────────┐
                │   usecases/    │  orquesta varios servicios (CustomerProductsUseCase)
                └───────┬────────┘
                        │
                ┌───────▼────────┐
                │   services/    │  reglas de negocio, transacciones, clientes remotos
                └───┬────────┬───┘
                    │        │
        ┌───────────▼──┐  ┌──▼──────────────────────┐
        │ repositories/│  │ servicio de productos   │
        │ (BD local)   │  │ externo (:8081)         │
        └──────────────┘  └─────────────────────────┘
```

Convenciones que verás en todo el código:

- **Inyección por constructor** con `@Inject` (clases `final`-friendly y fáciles de probar con Mockito).
- **Un servicio por técnica de acceso**: `CustomerService` (Panache), `CustomerSpringService`
  (Spring Data), `CustomerViewService` (Blaze). Así se pueden comparar lado a lado.
- **Los controllers no contienen lógica**: delegan en servicios o casos de uso.
- **Los casos de uso** (`usecases/`) existen para flujos que combinan varios servicios.

---

## 4. Modelo de datos

```
Customer 1 ──────< Product
  id (PK, IDENTITY)      id (PK, IDENTITY)
  code                   customer  (FK → Customer.id)
  accountNumber          product   (id del producto en el servicio externo)
  names                  name
  surname                code        (@Transient, solo en memoria)
  phone                  description (@Transient, solo en memoria)
  address
```

Detalles que conviene entender (`entities/`):

- `Customer.products` es `@OneToMany(mappedBy = "customer", cascade = ALL, orphanRemoval = true,
  fetch = EAGER)`: guardar/borrar un cliente propaga a sus productos.
- `@JsonManagedReference` / `@JsonBackReference` rompen el ciclo `Customer ↔ Product` al serializar a
  JSON: el cliente muestra sus productos, pero el producto **no** repite al cliente.
- `Product.code` y `Product.description` son `@Transient`: no se guardan, se rellenan con datos del
  servicio externo.
- `@EqualsAndHashCode.Exclude` y `@ToString.Exclude` sobre `Product.customer` evitan recursión infinita
  en Lombok.

### Datos iniciales

`src/main/resources/import.sql` carga 10 clientes y sus relaciones con productos en cada arranque. Es
posible porque `quarkus.hibernate-orm.database.generation=drop-and-create` recrea el esquema **en cada
arranque** (¡no uses eso en producción!).

---

## 5. Tres formas de acceder a datos

Mismo dominio, tres enfoques. Compáralos.

### 5.1 Panache — `CustomerRepository` + `CustomerService`

```java
@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> { }
```

Sin escribir nada obtienes `listAll()`, `findByIdOptional()`, `persist()`, `deleteById()`…
`CustomerService` añade las reglas: limpiar IDs al crear, asociar productos con su cliente
(`product.setCustomer(customer)`), lanzar `NotFoundException`, y envolver en `@Transactional`.

**Úsalo cuando:** quieres CRUD rápido y consultas simples.

### 5.2 Spring Data JPA — `CustomerSpringRepository`

```java
Optional<Customer> findByCode(String code);
List<Customer> findBySurnameIgnoreCase(String surname);
boolean existsByAccountNumber(String accountNumber);

@Query("SELECT c FROM Customer c WHERE LOWER(c.names) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.id")
List<Customer> searchByName(@Param("name") String name);
```

Las consultas se **derivan del nombre del método** (`findBySurnameIgnoreCase`) o se declaran con
`@Query`. Quarkus genera la implementación en tiempo de build.

**Úsalo cuando:** vienes de Spring o quieres consultas derivadas sin escribir JPQL.

### 5.3 Blaze Persistence Entity Views — `CustomerView` + `CustomerViewRepository`

```java
@EntityView(Customer.class)
public interface CustomerView {
    @IdMapping Long getId();
    String getCode();
    ...
    Set<ProductView> getProducts();
}
```

Una *Entity View* es una **proyección de solo lectura**: Blaze genera la implementación y construye una
consulta que selecciona **únicamente** los atributos declarados, en lugar de cargar la entidad completa.
`ProductView` es una subvista anidada que omite la referencia de vuelta al cliente.

**Úsalo cuando:** necesitas DTOs de lectura eficientes sin escribir mapeos a mano.

> Nota: el procesador de anotaciones de Blaze está configurado en `pom.xml`
> (`blaze-persistence-entity-view-processor-jakarta`).

---

## 6. Consumo de servicios externos

El servicio de productos externo se consume de **tres formas** para que veas las diferencias.

### 6.1 WebClient reactivo de Vert.x — `CustomerService.getAllProducts()`

Llamada HTTP REST a `GET http://localhost:8081/product` con timeout de 5 s. Devuelve `Uni<List<Product>>`,
traduce respuestas no-200 y JSON inválido a `502 Bad Gateway`.

### 6.2 `ProductGraphQLServiceV1` — cliente GraphQL propio

Delega en `GraphQLClientService`, un cliente genérico construido a mano con
`VertxDynamicGraphQLClientBuilder` y la URL `graphql.client.url`.

- Los documentos GraphQL son **constantes** en `constants/Constants.java`.
- `GraphQLClientService` centraliza el manejo de errores: loguea y convierte fallos de red y errores
  GraphQL en `WebApplicationException` **502**.
- Cierra el cliente en `@PreDestroy`.

### 6.3 `ProductGraphQLServiceV2` — cliente declarativo de Quarkus

Inyecta directamente `@GraphQLClient("product-api") DynamicGraphQLClient`, configurado con
`quarkus.smallrye-graphql-client.product-api.url`.

- Los documentos se arman **inline** con `PRODUCT_FIELDS` y variables propias (`$id`, `$p`).
- No hay manejo de errores propio: lo que devuelva el cliente se propaga tal cual.
- Quarkus gestiona el ciclo de vida del cliente.

### Resumen V1 vs V2

| Aspecto | V1 | V2 |
|---|---|---|
| Cliente | `GraphQLClientService` (manual) | `@GraphQLClient("product-api")` (Quarkus) |
| Configuración | `graphql.client.url` | `quarkus.smallrye-graphql-client.product-api.url` |
| Documentos | Constantes en `Constants` | Inline |
| Errores | 502 + log centralizado | Sin tratamiento propio |
| Ciclo de vida | `@PreDestroy` propio | Gestionado por Quarkus |
| Endpoint REST | `/v1/product` | `/v2/product` |

### Detalle importante: el nombre `deletProduct`

La mutation de borrado del servicio remoto se llama **`deletProduct`** (sin la "e"). Es el nombre real del
schema remoto y se respeta en `Constants.DELETE_PRODUCT` y en V2. Si el servicio remoto lo corrige, hay
que actualizar ambos sitios.

### Caso de uso: `CustomerProductsUseCase`

```java
Uni.combine().all()
   .unis(customerService.getCustomerReactive(id), customerService.getAllProducts())
   .with((customer, products) -> { /* cruza por id y completa name/description */ });
```

`Uni.combine().all()` suscribe a ambas fuentes y espera las dos antes de combinar. Es el patrón básico
para "juntar" resultados asíncronos independientes con Mutiny.

---

## 7. Catálogo de endpoints

Base: `http://localhost:8080`

### Clientes — `CustomerController` (`/customer`)

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/customer` | Lista clientes (Panache) | 200 |
| GET | `/customer/{id}` | Cliente por id | 200 / 404 |
| POST | `/customer` | Crea cliente (con sus productos) | 201 |
| PUT | `/customer/{id}` | Actualiza cliente y reemplaza productos | 202 / 404 |
| DELETE | `/customer/{id}` | Elimina cliente | 202 / 404 |
| GET | `/customer/view` | Lista usando Entity Views (Blaze) | 200 |
| GET | `/customer/view/{id}` | Una vista por id | 200 / 404 |
| GET | `/customer/code/{code}` | Por código (Spring Data) | 200 / 404 |
| GET | `/customer/search?name=` | Búsqueda parcial por nombres | 200 |
| GET | `/customer/search?surname=` | Por apellido, sin distinguir mayúsculas | 200 |
| GET | `/customer/{id}/product` | Cliente enriquecido con el catálogo externo | 200 / 404 / 502 |

### Productos — `ProductGraphQLControllerV1` / `V2`

Mismos endpoints en `/v1/product` y `/v2/product`:

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/{v}/product` | Lista productos | 200 |
| GET | `/{v}/product/{id}` | Producto por id | 200 / 404 |
| POST | `/{v}/product` | Crea producto | 201 |
| PUT | `/{v}/product/{id}` | Actualiza producto | 202 |
| DELETE | `/{v}/product/{id}` | Elimina producto | 202 |

### Utilidades

| Ruta | Qué es |
|---|---|
| `/swaggerui` | Swagger UI (OpenAPI) |
| `/q/openapi` | Documento OpenAPI |
| `/graphql` | Endpoint GraphQL |
| `/graphql/schema.graphql` | Schema GraphQL generado |
| `/q/dev/` | Dev UI (solo modo dev) |

### Ejemplos con `curl`

```bash
# Listar
curl localhost:8080/customer

# Crear con productos
curl -X POST localhost:8080/customer -H 'Content-Type: application/json' -d '{
  "code":"CUST-100","accountNumber":"ACC-2000","names":"Ada","surname":"Lovelace",
  "phone":"555-0000","address":"Calle 1",
  "products":[{"product":101,"name":"Laptop"}]
}'

# Búsqueda
curl 'localhost:8080/customer/search?name=ana'

# Cliente + datos del catálogo externo (requiere el servicio en :8081)
curl localhost:8080/customer/1/product
```

---

## 8. API GraphQL

`graphql/CustomerResources.java` usa las anotaciones de MicroProfile GraphQL: `@GraphQLApi`, `@Query`,
`@Mutation`, `@Name`, `@Description`. Quarkus genera el schema automáticamente; hay una copia en
`customer-schema.graphql`.

```graphql
type Query {
  allCustomer: [Customer]
  customer(customerId: BigInteger): Customer
}
type Mutation {
  addCustomer(customer: CustomerInput): Customer
  updateCustomer(customer: CustomerInput): Customer
  deleteCustomer(customerId: BigInteger): Customer
}
```

Ejemplo:

```bash
curl -s localhost:8080/graphql -H 'Content-Type: application/json' -d '{
  "query": "{ allCustomer { id names surname products { id product name } } }"
}'
```

> Observa que GraphQL reutiliza **el mismo `CustomerService`** que REST: la lógica no se duplica, solo
> cambia la capa de exposición.

---

## 9. Manejo de errores

| Situación | Mecanismo | Resultado |
|---|---|---|
| Cliente inexistente | `NotFoundException` + `NotFoundExceptionMapper` | `404` con `{"status":404,"message":"..."}` |
| Producto remoto inexistente (REST V1/V2) | El controller comprueba `null` | `404` |
| Servicio de productos caído / respuesta inválida | `WebApplicationException` con `BAD_GATEWAY` | `502` |
| El servicio GraphQL remoto responde con `errors` (V1) | `GraphQLClientService.failOnErrors` | `502` |

`MyObjectMapperCustomizer` configura Jackson para **omitir campos `null`** en las respuestas.

---

## 10. Configuración

`src/main/resources/application.properties`:

| Propiedad | Valor | Nota |
|---|---|---|
| `quarkus.http.port` | `8080` | |
| `quarkus.datasource.jdbc.url` | `jdbc:postgresql://localhost:5432/customer_db` | solo perfiles `dev`/`prod` |
| `quarkus.datasource.username/password` | `sa` / `admin` | solo perfiles `dev`/`prod` |
| `quarkus.hibernate-orm.database.generation` | `drop-and-create` | recrea el esquema al arrancar |
| `quarkus.hibernate-orm.log.sql` | `true` | muestra el SQL en consola |
| `graphql.client.url` | `http://localhost:8081/graphql` | `GraphQLClientService` (V1) |
| `quarkus.smallrye-graphql-client.product-api.url` | `http://localhost:8081/graphql` | cliente V2 |

Las credenciales solo existen en `dev` y `prod` a propósito: en el perfil `test` no hay URL, de modo que
Quarkus puede levantar una base temporal (*Dev Services*, requiere Docker).

> ⚠️ Las credenciales `sa/admin` son de ejemplo. En un entorno real, usa variables de entorno o un
> gestor de secretos.

---

## 11. Cómo ejecutarlo

### Requisitos

- JDK 21
- Docker (para PostgreSQL local y para los tests con Dev Services)
- El servicio de productos externo en `localhost:8081` (solo para los endpoints de producto y
  `/customer/{id}/product`)

### Base de datos

```bash
docker run --name customer-db -e POSTGRES_USER=sa -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=customer_db -p 5432:5432 -d postgres:16
```

### Modo desarrollo (live coding)

```bash
./mvnw quarkus:dev
```

La Dev UI está en <http://localhost:8080/q/dev/>.

### Empaquetar

```bash
./mvnw package                                        # target/quarkus-app/quarkus-run.jar
java -jar target/quarkus-app/quarkus-run.jar

./mvnw package -Dquarkus.package.jar.type=uber-jar    # un único jar
java -jar target/*-runner.jar
```

### Ejecutable nativo

```bash
./mvnw package -Dnative
# sin GraalVM instalado:
./mvnw package -Dnative -Dquarkus.native.container-build=true
./target/customer-1.0.0-runner
```

Más información: <https://quarkus.io/guides/maven-tooling>. Hay Dockerfiles en `src/main/docker/`.

---

## 12. Pruebas

```bash
./mvnw test                                   # todo
./mvnw test -Dtest='ProductGraphQL*Test'      # un subconjunto
```

Tres estilos de prueba conviven, de menor a mayor alcance:

| Estilo | Ejemplo | Cómo funciona |
|---|---|---|
| **Unitaria pura** (Mockito, sin Quarkus) | `ProductGraphQLServiceV1Test`, `ProductGraphQLServiceV2Test`, `GraphQLClientServiceTest` | Se construye la clase con `new` y dependencias `mock(...)`. Muy rápida. |
| **Controller con `@QuarkusTest`** | `ProductGraphQLControllerV1Test`, `ProductGraphQLControllerV2Test`, `CustomerControllerTest` | Levanta Quarkus, reemplaza el servicio con `@InjectMock` y llama por HTTP con REST Assured. |
| **Integración** | `CustomerResourceTest`, `CustomerResourceIT` | Recorre la pila completa contra la base de datos. |

Patrón para probar un servicio que usa el cliente GraphQL (ver `ProductGraphQLServiceV2Test`):

```java
client = mock(DynamicGraphQLClient.class);
response = mock(Response.class);
when(client.executeAsync(anyString())).thenReturn(Uni.createFrom().item(response));
when(response.getList(Product.class, "allProducts")).thenReturn(List.of(product));

List<Product> result = service.list().await().indefinitely();
```

`.await().indefinitely()` convierte un `Uni` en valor síncrono; es habitual en tests, **no** en código de
producción.

---

## 13. Estructura del repositorio

```
src/main/java/org/digitalthinking/
├── config/          MyObjectMapperCustomizer      Jackson: omitir nulls
├── constants/       Constants                     Documentos GraphQL del servicio de productos
├── controllers/     CustomerController            REST de clientes
│                    ProductGraphQLControllerV1/V2 REST de productos (vía GraphQL remoto)
├── entities/        Customer, Product             Entidades JPA
├── exceptions/      NotFoundException (+Mapper)   404 uniforme
├── graphql/         CustomerResources             API GraphQL de clientes
├── repositories/    CustomerRepository            Panache
│                    CustomerSpringRepository     Spring Data JPA
│                    CustomerViewRepository       Blaze Entity Views
├── services/        CustomerService / SpringService / ViewService
│                    GraphQLClientService          Cliente GraphQL genérico
│                    ProductGraphQLServiceV1/V2    Consumo GraphQL de productos
├── usecases/        CustomerProductsUseCase       Cliente + catálogo en paralelo
└── views/           CustomerView, ProductView     Proyecciones Blaze

src/main/resources/  application.properties, import.sql
src/test/java/...    Pruebas (misma estructura de paquetes)
customer-schema.graphql   Copia del schema GraphQL
```

---

## 14. Ejercicios sugeridos

1. **Compara V1 y V2:** apaga el servicio de productos y llama a `/v1/product` y `/v2/product`. ¿Qué
   código HTTP obtienes en cada uno y por qué?
2. **Unifica errores:** haz que V2 convierta los errores GraphQL en `502` como V1.
3. **Nueva consulta Spring Data:** añade `findByPhone` y expónlo en `/customer/search`.
4. **Nueva proyección:** crea una `CustomerSummaryView` solo con `id` y `names`, y observa el SQL
   generado (`quarkus.hibernate-orm.log.sql=true`).
5. **Mutation de producto en GraphQL:** expón también la API de productos como `@GraphQLApi` local.
6. **Paginación:** añade `page`/`size` a `GET /customer` usando `PanacheQuery.page(...)`.
7. **Resiliencia:** añade reintentos al `Uni` de `getAllProducts()` con
   `.onFailure().retry().atMost(3)`.
8. **Producción real:** cambia `drop-and-create` por `none` y migra el esquema con Flyway o Liquibase.

---

## 15. Historia del proyecto

El historial git sigue la evolución como ejercicio de aprendizaje:

| Etapa | Qué se añadió |
|---|---|
| Base | Proyecto Quarkus con CRUD de clientes y relación con productos |
| `fix`/`refactor` | Secuencias de ids, `NotFoundException`, relación `Customer`–`Product` |
| WebClient | Endpoint `GET /customer/{id}/product` con productos vía WebClient reactivo |
| Postgres | Migración a PostgreSQL (rama `01-migrate-to-postgres`) |
| Blaze / Spring | Entity Views, repositorio Spring Data y servicios separados por repositorio |
| GraphQL | API GraphQL de clientes, caso de uso de productos, mutation `updateCustomer` |
| `02-graphql` | Clientes GraphQL de productos (V1 y V2) y sus controllers y tests |

---

## Recursos

- Quarkus: <https://quarkus.io/>
- Hibernate ORM con Panache: <https://quarkus.io/guides/hibernate-orm-panache>
- Spring Data JPA en Quarkus: <https://quarkus.io/guides/spring-data-jpa>
- SmallRye GraphQL y cliente: <https://quarkus.io/guides/smallrye-graphql>,
  <https://quarkus.io/guides/smallrye-graphql-client>
- Mutiny: <https://smallrye.io/smallrye-mutiny/>
- Blaze Persistence: <https://persistence.blazebit.com/>

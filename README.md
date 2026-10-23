# Proyecto Broadway

## Descripción del Problema

### Contexto
El Casino Broadway es una empresa de entretenimiento presencial y juegos de azar. Cuenta con una nómina operativa de más de 400 empleados distribuidos en turnos rotativos y una infraestructura física compuesta por 30 mesas de juego en vivo (Ruleta, Blackjack y Póker) y más de 120 terminales electrónicas. A pesar del flujo constante de dinero y fichas, la gestión de apuestas, el control de inventario de mesa y la administración de usuarios se realiza mediante métodos manuales y planillas descentralizadas, lo que genera inconsistencias contables y riesgos operativos diarios.

### Actores
* **Cajero:** Empleado responsable de la venta y canje de fichas por dinero en efectivo, registro del balance inicial y ejecución del arqueo/cierre de caja por turno.
* **Crúpier:** Operador directo de la mesa de juego asignada. Recibe las apuestas de los jugadores, valida que cumplan con los montos mínimos y máximos de la mesa y ejecuta el pago de las rondas ganadoras.
* **Jefe de Sala / Administrador:** Supervisor general del casino. Abre y cierra mesas, asigna crúpieres, consulta reportes consolidados y gestiona la lista de autoexclusión y vetos.
* **Cliente / Apostador:** Usuario registrado que adquiere fichas, realiza apuestas en las mesas, acumula beneficios según su volumen de juego y puede fijar topes de pérdida o solicitar su autoexclusión.

### Proceso Actual (Paso a Paso)
1. **Ingreso a sala:** El cliente entra sin un registro sistemático de identidad ni validación contra listas de autoexclusión o mayoría de edad.
2. **Compra de fichas:** El cliente entrega efectivo en caja. El cajero anota la transacción a mano en una planilla física y entrega las fichas.
3. **Apertura de mesa:** El crúpier recibe un cupo inicial de fichas físicas firmando un recibo en papel.
4. **Colocación de apuestas:** En cada ronda, los jugadores colocan fichas. El crúpier verifica visualmente que la apuesta respete los topes de la mesa.
5. **Cálculo y pago:** Tras el resultado de la jugada, el crúpier calcula mentalmente el valor a pagar según la regla del juego (ej. 35:1 en Ruleta o 3:2 en Blackjack).
6. **Canje de fichas:** El cliente regresa a caja con sus fichas. El cajero las cuenta manualmente, calcula el dinero equivalente y entrega el efectivo.
7. **Cierre de turno:** El cajero y el crúpier comparan el dinero y las fichas sobrantes contra las planillas de papel para intentar cuadrar los saldos.

### Dolor
Dentro de este esta la desconexión e inconsistencia de sus flujos de información debido a la dependencia exclusiva de registros físicos y cálculos manuales. Al no existir un sistema centralizado, el control del inventario de fichas y el flujo de efectivo sufre discrepancias constantes al cierre de cada turno; los cajeros y supervisores se ven forzados a confrontar planillas manuscritas que frecuentemente contienen tachaduras, omisiones o datos ilegibles, lo que vuelve prácticamente imposible auditar a tiempo los descuadres de caja o identificar fugas de capital. A este problema administrativo se suma la vulnerabilidad en las mesas de juego, donde los crúpieres deben calcular mentalmente las liquidaciones de cada ronda. Por otra parte, la inexistencia de una base de datos de clientes crea un vacío crítico tanto a nivel normativo como comercial: el establecimiento no tiene una forma confiable de verificar la mayoría de edad en cada punto de apuesta ni de aplicar los vetos o las solicitudes de autoexclusión exigidas por las regulaciones de juego responsable, lo que expone a la empresa a sanciones legales.

### Impacto
Analizamos el impacto desde tres distintas perspectivas:

* En lo técnico, toda la información de clientes, empleados, mesas, fichas y apuestas queda en un solo sistema y se guarda en archivos, por lo que no se pierde entre turnos ni al cerrar la aplicación. Los pagos se calculan automáticamente según la tabla de cada juego (35:1 en el pleno de ruleta, 3:2 en el blackjack natural), las apuestas se validan contra los límites de la mesa y el saldo del cliente, y cada empleado solo puede usar las funciones de su cargo. Esto elimina los errores de cálculo y de digitación, y permite rastrear quién hizo cada operación.
* En lo económico, el cierre de caja se genera a partir de los movimientos registrados y el sistema señala de inmediato cualquier descuadre entre el balance esperado y el efectivo contado. Así se reducen las pérdidas por pagos mal calculados y por fugas de dinero o fichas que hoy no se detectan, y el cierre de turno toma menos tiempo. Además, el programa de fidelidad automático (niveles Bronce, Plata, Oro y VIP con cashback) le da al casino una herramienta para retener a sus clientes frecuentes.
* En lo social, el sistema rechaza el registro de menores de 18 años y guarda un historial por cliente de lo que apuesta y pierde. Esto le permite al casino cumplir con las normas de juego responsable, evitar sanciones legales y proteger a los apostadores. Para los empleados, trabajar sin planillas manuscritas reduce la presión y los conflictos al cuadrar la caja al final de cada turno.

## Roles del Equipo

| Responsable de Producto | Responsable de Calidad | Responsable de Diseño | Responsable de Integración |
|---|---|---|---|
| [Camilo Andrés Ramírez Chinchilla](https://github.com/camiloopop) | [Santiago Caicedo Ramírez](https://github.com/Santiwr) y [Daniel Felipe Avila Mendez](https://github.com/danielavilajjc-commits)| [Johan Sebastian Buitrago Roncancio](https://github.com/johansebastianbuitrago7-netizen) y [Juan Camilo Avellaneda Urrego](https://github.com/Juanavur) | [Juan Andrés Lasso Arias](https://github.com/Weirdtions) |

## Tabla de requerimientos
Para comenzar con la entrega 2, se empezarán a abordar los RF-01, RF-02 Y RF-03, puesto que estos 3 brindan las bases para el sistema de clientes y de empleados, lo cual permitirá avanzar en el proyecto y crear las bases de los demás sistemas que son necesitados.

| ID | Requerimiento | Descripción | Actor | Prioridad | Criterio de aceptación |
|---|---|---|---|---|---|
| RF-01 | Registro de clientes | El sistema debe permitir al cajero registrar clientes con documento, nombre y fecha de nacimiento. | Cajero | Alta | No se permiten dos clientes con el mismo documento. Se rechaza a los menores de 18 años. |
| RF-02 | Registro de empleados | El sistema debe permitir al administrador registrar empleados con documento, nombre, usuario, contraseña y cargo. | Administrador | Alta | El usuario es único. La contraseña tiene mínimo 6 caracteres. No se aceptan campos vacíos. |
| RF-03 | División de empleados | El sistema debe permitir al administrador definir que cada empleado tiene un cargo (Administrador, Cajero, Crupier o Técnico de máquinas) lo que define qué funciones puede usar. | Administrador | Alta | Un cajero no puede crear mesas. Un crupier no puede vender fichas. Solo el técnico puede cambiar el estado de una máquina. |
| RF-04 | Registro de mesas | El sistema debe permitir al administrador crear mesas con código, juego, apuesta mínima, apuesta máxima, capacidad y crupier asignado. | Administrador | Alta | La mínima debe ser menor que la máxima. El código es único. La capacidad va de 1 a 7. Solo se asigna un empleado con cargo Crupier. |
| RF-05 | Registro de juegos | El sistema debe permitir al administrador registrar los juegos de mesa disponibles (Ruleta, Blackjack) con su nombre y su tabla de pagos. | Administrador | Media | No se permiten dos juegos con el mismo nombre. Una mesa solo puede usar un juego registrado. |
| RF-06 | Sistema de fichas | El sistema debe permitir al cajero vender fichas a los clientes y cambiarlas por dinero, llevando el saldo de fichas de cada cliente. | Cajero | Alta | Un cliente con saldo 0 que compra 100.000 queda con 100.000. No se puede cambiar más de lo que tiene. Un monto menor o igual a 0 se rechaza. |
| RF-07 | Sistema de finanzas | El sistema debe permitir al administrador registrar cada movimiento de dinero y generar el cierre de caja de cada turno. | Administrador | Alta | Con ventas por 1.000.000 y cambios por 700.000, el balance es 300.000. Si el efectivo contado no coincide, se muestra "DESCUADRE". |
| RF-08 | Sistema de apuestas | El sistema debe permitir al crupier registrar la apuesta de un cliente en una mesa y calcular su pago según el juego. | Crupier | Alta | Se rechaza la apuesta fuera del mínimo y el máximo de la mesa, o mayor al saldo. Con una apuesta de 10.000: pleno en ruleta gana 350.000 y blackjack natural gana 15.000. |
| RF-09 | Sistema de máquinas | El sistema debe permitir al tecnico de maquinas registrar las máquinas tragamonedas (código, valor por giro, estado) y sus jugadas. | Técnico de máquinas | Media | Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales se gana 10 veces la apuesta. Cada giro descuenta su valor del saldo. |
| RF-10 | Sistema de fidelidad | El sistema debe permitir al cajero asignar un nivel al cliente (Bronce, Plata, Oro, VIP) según el total apostado, con cashback sobre sus pérdidas. | Cajero | Media | Un cliente Bronce con 1.990.000 apostados que apuesta 20.000 pasa a Plata. Un cliente Oro que pierde 100.000 recibe 5.000 (5%). El nivel nunca baja. |
| RF-11 | Persistencia de datos | El sistema debe permitir al administrador persistir en archivos locales el estado de clientes, empleados, mesas y transacciones, y restaurarlo automáticamente al arrancar. | Administrador | Alta | Al registrar un cliente y asignarle fichas, cerrar la aplicación y reiniciar el sistema, el saldo del cliente y el catálogo de mesas deben coincidir exactamente con el estado previo al apagado. |

### Requerimientos No Funcionales (RNF)

| ID | Requerimiento | Descripción | Categoría | Prioridad | Criterio de aceptación |
|---|---|---|---|---|---|
| RNF-01 | Seguridad de credenciales | El sistema debe almacenar las contraseñas de los empleados de forma cifrada/hasheada. | Seguridad | Alta | Al inspeccionar el archivo de persistencia, ninguna contraseña debe ser legible en texto plano. |
| RNF-02 | Integridad y persistencia | El sistema debe guardar el estado de las mesas, saldos y transacciones en archivos locales de texto/JSON. | Persistencia | Alta | Ante un cierre abrupto de la aplicación, al reiniciar no se deben perder las transacciones confirmadas antes del cierre. |
| RNF-03 | Validación y robustez | El sistema debe manejar excepciones en entradas de usuario (números negativos, cadenas en campos numéricos). | Robustez | Media | Si un usuario ingresa texto en el monto de una apuesta, el sistema muestra un mensaje de error sin abortar la ejecución. |

# Plan hacia la Entrega 2

Para la Entrega 2 se abordarán prioritariamente los requerimientos **RF-01, RF-02 y RF-03**, ya que constituyen la base del modelo de dominio (gestión de personas, autenticación y jerarquía de permisos por rol), habilitando el desarrollo posterior de las mesas y transacciones.

## Tabla de Trazabilidad Inicial

| Requerimiento | Clases Involucradas | Temas de POO Aplicados | Estado |
|---|---|---|---|
| **RF-01** | `Cliente`, `Persona`, `Casino` | Herencia, Encapsulamiento, Validación de entradas y manejo de excepciones | En diseño |
| **RF-02** | `Trabajador`, `Persona`, `Administrador`, `Casino` | Herencia, Encapsulamiento, Constructores, Atributos privados con getters/setters | En diseño |
| **RF-03** | `Trabajador`, `Administrador`, `Cajero`, `Crupier`, `TecnicoMaquina` | Herencia, Clases abstractas, Polimorfismo, Control de acceso | En diseño |
| **RF-04** | `Mesa`, `Juego`, `Crupier`, `Casino` | Asociación entre objetos, Agregación, Colecciones (`List` / `ArrayList`) | En diseño |
| **RF-05** | `Juego`, `Ruleta`, `Blackjack`, `Casino` | Herencia, Clases abstractas / Métodos polimórficos, Colecciones de catálogo | En diseño |
| **RF-06** | `Cajero`, `Cliente`, `Transaccion`, `Casino` | Encapsulamiento, Asociación entre objetos, Invariantes de saldo | En diseño |
| **RF-07** | `Caja`, `MovimientoFinanciero`, `Administrador`, `Casino` | Colecciones de transacciones, Métodos de cálculo/agregación, Encapsulamiento | En diseño |
| **RF-08** | `Crupier`, `Mesa`, `Juego`, `Apuesta`, `Cliente` | Polimorfismo (`calcularPago()`), Sobrecarga de métodos, Asociación | En diseño |
| **RF-09** | `TecnicoMaquina`, `MaquinaTragamonedas`, `Cliente` | Encapsulamiento, Enumeraciones para estados operativos, Generación pseudoaleatoria | En diseño |
| **RF-10** | `Cliente`, `NivelFidelidad`, `Cajero` | Lógica de acumulación, Encapsulamiento, Reglas de negocio | En diseño |
| **RF-11** | `Casino`, `GestorPersistencia`, `LectorArchivos` | Flujos de entrada/salida (I/O), Serialización / Parsing de archivos, Manejo robusto de excepciones | En diseño |

---

## Registro de Decisiones de Diseño

### Decisión 1: Jerarquía polimórfica para el catálogo y reglas de los juegos
* **Decisión:** Implementar una clase base abstracta `Juego` de la cual hereden clases especializadas (`Ruleta`, `Blackjack`), obligando a implementar el método abstracto `calcularPago(apuesta, resultado)`.
* **Alternativas consideradas:**
  1. Utilizar una única clase `Mesa` con un atributo `String` o `Enum` para el tipo de juego, resolviendo el cálculo de ganancias con condicionales `switch`/`if-else`.
  2. Crear clases independientes para cada juego sin relación de herencia entre sí.
* **Por qué:** Cada modalidad de juego en un casino tiene reglas matemáticas y multiplicadores incompatibles entre sí (35:1 vs 3:2). El polimorfismo desacopla la mesa de la lógica particular de cálculo y permite incorporar nuevos juegos en futuras entregas sin alterar el código de la mesa (Principio Open/Closed).
* **Consecuencia:** Mayor modularidad y mantenibilidad del diseño, requiriendo el diseño de una interfaz/método abstracto común que admita los parámetros de las apuestas.

### Decisión 2: Mecanismo de persistencia local mediante archivos estructurados
* **Decisión:** Persistir los datos del casino (clientes, empleados, mesas, saldos y movimientos) en archivos locales de texto estructurado (formato JSON o CSV) mediante una clase utilitaria de persistencia.
* **Alternativas consideradas:**
  1. Integrar un motor de bases de datos relacional externo (MySQL/PostgreSQL) o embebido (SQLite).
  2. Mantener la información únicamente en memoria volátil (RAM) durante la ejecución.
* **Por qué:** Cumple con la exigencia de que los datos sobrevivan al cierre de la aplicación sin añadir dependencias de red o instalación de drivers que exceden el alcance de este corte, permitiendo aplicar directamente los temas del curso referentes a flujos de archivos (I/O) y manejo de excepciones en Java.
* **Consecuencia:** El equipo deberá programar manualmente los algoritmos de serialización, deserialización y validación de tipos e integridad de datos al cargar el archivo al inicio del sistema.


## Diagrama UML

En esta sección se explica cómo el diagrama de clases UML aplicando distintos conceptos: **clases**, **atributos y métodos estáticos**, **clases anidadas**, **interfaces** y **polimorfismo**. Para cada uno se describe dónde aparece en el diseño, para qué sirve y con qué decisión del proyecto se relaciona.

### Clases

El sistema se organiza alrededor de la clase "casino", que es el punto central: guarda las listas de clientes, empleados, mesas, máquinas y juegos, y la caja. A partir de ella se agrupan las demás clases según su responsabilidad:

| Grupo | Clases | Para qué sirven | RF |
|---|---|---|---|
| Gestión | "casino" | Registra clientes y empleados, los busca por documento y conecta todas las partes del sistema. | Todos |
| Personas | `Persona` *(abstracta)*, `Cliente`, `Trabajador` *(abstracta)*, `Administrador`, `Cajero`, `Crupier`, `TecnicoMaquina` | Datos comunes de cualquier persona; saldo, nivel y apuestas del cliente; usuario, contraseña y funciones de cada cargo. | RF-01, RF-02, RF-03, RF-10 |
| Juegos y apuestas | `Juego` *(abstracta)*, `Ruleta`, `Blackjack`, `Mesa`, `RondaMesa`, `Apuesta` | Catálogo de juegos con su forma de pagar, mesas con sus límites y crupier, rondas y apuestas de cada jugador. | RF-04, RF-05, RF-08 |
| Máquinas | `MaquinaTragaMoneda`, `MaquinaTragaMoneda.RondaMaquina` | Máquinas con su costo por giro y estado, y el registro de cada giro. | RF-09 |
| Caja y finanzas | `Caja`, `TurnoCaja`, `MovimientoCaja`, `Caja.CierreTurno` | Ventas y canjes de fichas, movimientos de dinero por turno y cierre de caja con detección de descuadre. | RF-06, RF-07 |

Además de los conceptos de esta entrega, el diseño usa **herencia** (`Persona → Cliente / Trabajador → cargos`, `Juego → Ruleta / Blackjack`) y **encapsulamiento**: los atributos son privados (`-`) y solo cambian a través de métodos que validan, por ejemplo el saldo del cliente solo cambia con `comprarFichas` y `apostar`.

### Atributos y métodos estáticos

Un miembro **estático** pertenece a la **clase** y no a cada objeto: existe una sola copia compartida por todos y se puede usar sin crear objetos. En el diagrama se marcan con `<static>` y aparecen subrayados.

| Miembro estático | Cómo se aplica | Utilidad en el diseño | RF |
|---|---|---|---|
| `Cliente.edadMinima = 18` | La edad mínima para registrarse es la misma para todos los clientes. `esMayorDeEdad(fechaNacimiento)` la usa para aceptar o rechazar el registro. | La regla queda escrita en un solo lugar; si cambia la ley, se cambia una línea. Se puede consultar antes de crear el cliente, por ejemplo para mostrar un error en el formulario. | RF-01 |
| `Casino.totalMesas = 50` | Límite de mesas que puede tener el casino. | Es un dato del casino y no de una mesa en particular, así que no tiene sentido repetirlo en cada objeto. | RF-04 |
| `NivelFidelidad.cashbackBronce = 0.0`, `cashbackPlata = 0.02`, `cashbackOro = 0.05`, `cashbackVip = 0.08` | Porcentaje de cashback de cada nivel. | Son las reglas del programa de fidelidad: valores fijos que comparten todos los clientes. | RF-10 |
| `NivelFidelidad.calcularCashback(nivel, perdida)` | Calcula el cashback a partir del nivel y lo que perdió el cliente, por ejemplo Oro con 100.000 de pérdida devuelve 5.000. | Es un cálculo que no depende de ningún cliente en particular, por eso es estático: se llama directamente sobre la clase. | RF-10 |

**Cómo podría ampliarse:** el mismo criterio sirve para otras reglas fijas del negocio, como la capacidad máxima de una mesa (7 jugadores), el multiplicador de las máquinas (10 veces la apuesta) o la longitud mínima de la contraseña (6 caracteres). También para contadores compartidos, como un `siguienteId` estático en `MovimientoCaja` que le dé a cada movimiento un número único.

### Clases anidadas

Una **clase anidada** se declara dentro de otra porque solo tiene sentido junto a ella. En el diagrama se conecta con una línea que termina en **⊕** del lado de la clase que la contiene.

| Clase anidada | Dentro de | Por qué está anidada | RF |
|---|---|---|---|
| `Caja.CierreTurno` | `Caja` | Es el resultado del cierre de caja: ventas, canjes y efectivo contado, con `getBalance()` y `hayDescuadre()`. Solo existe como parte de una caja y nadie más la usa. `TurnoCaja.cerrar(efectivo)` la crea al terminar el turno. | RF-07 |
| `NivelFidelidad` | `Cliente` | El nivel (Bronce, Plata, Oro, VIP) y su cashback solo tienen sentido para un cliente. | RF-10 |
| `RondaMaquina` | `MaquinaTragaMoneda` | Cada giro (hora, costo, premio y jugador) pertenece a una máquina concreta; `jugarGiro(cliente)` la crea. | RF-09 |

**Utilidad:** el código queda agrupado donde se usa (alta cohesión), el paquete no se llena de clases pequeñas que nadie más usa y el nombre deja clara la relación: `Caja.CierreTurno` se lee como "el cierre de una caja".

**Cómo podría aplicarse mejor:** en Java hay dos tipos de clase anidada. Una **estática** no necesita un objeto de la clase externa, mientras que una **interna** sí puede usar sus datos.
- `Caja.CierreTurno` debería ser **estática**, porque solo guarda números del resumen.
- `RondaMaquina` debería ser **interna**, porque necesita el costo por giro de *su* máquina.
- `RondaMesa` podría pasar a ser una clase interna de `Mesa` (`Mesa.RondaMesa`): una ronda siempre ocurre en una mesa y necesita sus límites, su capacidad y su juego.
- `NivelFidelidad` podría escribirse como un `enum` anidado.

### Interfaces

Una **interfaz** es un contrato: dice **qué** debe saber hacer una clase, sin decir **cómo**. En el diagrama se ve como una línea punteada con un triángulo vacío que apunta a la interfaz.

**`Operable`**: métodos `estaDisponible()`, `abrir()` y `cerrar()`.
- La implementan `Mesa` (abierta o cerrada) y `MaquinaTragaMoneda` (operativa o en mantenimiento).
- Las dos se ponen en servicio y se sacan de servicio, y ninguna permite jugar


---

## Documentación

- [Bitácora de uso de IA](docs/Bitacora-IA.md)
- [Registro de decisiones](docs/Registro-Decisiones.md)
- [Plan y trazabilidad](docs/Trazabilidad.md)

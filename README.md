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

-En lo técnico, toda la información de clientes, empleados, mesas, fichas y apuestas queda en un solo sistema y se guarda en archivos, por lo que no se pierde entre turnos ni al cerrar la aplicación. Los pagos se calculan automáticamente según la tabla de cada juego (35:1 en el pleno de ruleta, 3:2 en el blackjack natural), las apuestas se validan contra los límites de la mesa y el saldo del cliente, y cada empleado solo puede usar las funciones de su cargo. Esto elimina los errores de cálculo y de digitación, y permite rastrear quién hizo cada operación.

-En lo económico, el cierre de caja se genera a partir de los movimientos registrados y el sistema señala de inmediato cualquier descuadre entre el balance esperado y el efectivo contado. Así se reducen las pérdidas por pagos mal calculados y por fugas de dinero o fichas que hoy no se detectan, y el cierre de turno toma menos tiempo. Además, el programa de fidelidad automático (niveles Bronce, Plata, Oro y VIP con cashback) le da al casino una herramienta para retener a sus clientes frecuentes.

-En lo social, el sistema rechaza el registro de menores de 18 años y guarda un historial por cliente de lo que apuesta y pierde. Esto le permite al casino cumplir con las normas de juego responsable, evitar sanciones legales y proteger a los apostadores. Para los empleados, trabajar sin planillas manuscritas reduce la presión y los conflictos al cuadrar la caja al final de cada turno.


## Roles del Equipo

| Responsable de Producto | Responsable de Calidad | Responsable de Diseño | Responsable de Integración |
|---|---|---|---|
| [Camilo Andrés Ramírez Chinchilla](https://github.com/) | [Santiago Caicedo Ramírez](https://github.com/Santiwr) y [Daniel Felipe Avila Mendez](https://github.com/danielavilajjc-commits)| [Johan Sebastian Buitrago Roncancio](https://github.com/johansebastianbuitrago7-netizen) y [Juan Camilo Avellaneda Urrego](https://github.com/Juanavur) | [Juan Andrés Lasso Arias](https://github.com/Weirdtions) |

## Tabla de requerimientos
| ID | Requerimiento | Descripción | Actor | Prioridad | Criterio de aceptación |
|---|---|---|---|---|---|
| RF-01 | Registro de clientes | El sistema debe permitir al cajero registrar clientes con documento, nombre y fecha de nacimiento. | Cajero | Alta | No se permiten dos clientes con el mismo documento. Se rechaza a los menores de 18 años. |
| RF-02 | Registro de empleados | El sistema debe permitir al administrador registrar empleados con documento, nombre, usuario, contraseña y cargo. | Administrador | Alta | El usuario es único. La contraseña tiene mínimo 6 caracteres. No se aceptan campos vacíos. |
| RF-03 | División de empleados | El sistema debe permitir al administrador definir que cada empleado tiene un cargo (Administrador, Cajero, Crupier o Técnico de máquinas) lo que define qué funciones puede usar. | Administrador | Alta | Un cajero no puede crear mesas. Un crupier no puede vender fichas. Solo el técnico puede cambiar el estado de una máquina. |
| RF-04 | Registro de mesas | El sistema debe permitir al adminstrador crear mesas con código, juego, apuesta mínima, apuesta máxima, capacidad y crupier asignado. | Administrador | Alta | La mínima debe ser menor que la máxima. El código es único. La capacidad va de 1 a 7. Solo se asigna un empleado con cargo Crupier. |
| RF-05 | Registro de juegos | El sistema debe permitir al administrador registrar los juegos de mesa disponibles (Ruleta, Blackjack) con su nombre y su tabla de pagos. | Administrador | Media | No se permiten dos juegos con el mismo nombre. Una mesa solo puede usar un juego registrado. |
| RF-06 | Sistema de fichas | El sistema debe permitir al cajero vender fichas a los clientes y cambiarlas por dinero, llevando el saldo de fichas de cada cliente. | Cajero | Alta | Un cliente con saldo 0 que compra 100.000 queda con 100.000. No se puede cambiar más de lo que tiene. Un monto menor o igual a 0 se rechaza. |
| RF-07 | Sistema de finanzas | El sistema debe permitir al administrador registrar cada movimiento de dinero y generar el cierre de caja de cada turno. | Administrador | Alta | Con ventas por 1.000.000 y cambios por 700.000, el balance es 300.000. Si el efectivo contado no coincide, se muestra "DESCUADRE". |
| RF-08 | Sistema de apuestas | El sistema debe permitir al crupier registrar la apuesta de un cliente en una mesa y calcular su pago según el juego. | Crupier | Alta | Se rechaza la apuesta fuera del mínimo y el máximo de la mesa, o mayor al saldo. Con una apuesta de 10.000: pleno en ruleta gana 350.000 y blackjack natural gana 15.000. |
| RF-09 | Sistema de máquinas | El sistema debe permitir al tecnico de maquinas registrar las máquinas tragamonedas (código, valor por giro, estado) y sus jugadas. | Técnico de máquinas | Media | Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales se gana 10 veces la apuesta. Cada giro descuenta su valor del saldo. |
| RF-10 | Sistema de fidelidad | El sistema debe ser capaz de Asignar un nivel al cliente (Bronce, Plata, Oro, VIP) según el total apostado, con cashback sobre sus pérdidas. | Cajero | Media | Un cliente Bronce con 1.990.000 apostados que apuesta 20.000 pasa a Plata. Un cliente Oro que pierde 100.000 recibe 5.000 (5%). El nivel nunca baja. |


# Plan hacia la entrega 2

## Tabla de trazabilidad incial

|Requerimiento | Clases involucradas | Temas del curso | Estado|
|---|---|---|---|
|RF-01|Cliente, Casino|Encapsulamiento, herencia, validaciones basicas| En diseño|
|RF-02|Trabajador, Administrador, Casino|Encapsulamiento, herencia| En diseño |
|RF-03|Trabajador, Administrador, Cajero, Cruppier, TecnicoMaquina Casino|Encapsulamiento, herencia|En diseño|
|RF-04|Mesa, Juego, Cruppier, Administrador, Casino|Encapsulamiento, herencia, asociación entre objetos |En diseño|
|RF-05|Trabajador, Juego, Administador, Casino|Encapsulamiento, herencia|En diseño|
|RF-06|Trabajador,Cajero, Cliente, Casino |Encapsulamiento, herencia, asociación entre objetos, retorno metodos|En diseño|
|RF-07|Trabajador ,Cajero, Administrador, Casino |Encapsulamiento, herencia, retorno de metodos|En diseño|
|RF-08|Trabajador, Crupier, Juego, Mesa, Cliente, Casino|Encapsulamiento, herencia, asociación entre objetos, retorno metodos|En diseño|
|RF-09|Trabajador, TecnicoMaquina, Juego, MaquinaTragaMoneda, Cliente, Casino|Encapsulamiento, herencia|En diseño|
|RF-10|Trabajador, Cajero, Administrador, Cliente, Casino|Encapsulamiento, herencia, asociación entre objetos, retorno metodos|En diseño|



## Documentación

- [Bitácora de uso de IA](docs/Bitacora-IA.md)
- [Registro de decisiones](docs/Registro-Decisiones.md)
- [Plan y trazabilidad](docs/Trazabilidad.md)

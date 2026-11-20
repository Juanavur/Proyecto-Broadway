## Tabla de requerimientos
Para comenzar con la entrega 2, se empezarán a abordar los RF-01, RF-02 y RF-03, puesto que estos tres brindan las bases para el modelo de personas, autenticación y jerarquía de permisos por rol, habilitando el desarrollo posterior de las mesas y transacciones.

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
| RF-09 | Sistema de máquinas | El sistema debe permitir al técnico de máquinas registrar las máquinas tragamonedas (código, valor por giro, estado) y sus jugadas. | Técnico de máquinas | Media | Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales se gana 10 veces la apuesta. Cada giro descuenta su valor del saldo. |
| RF-10 | Sistema de fidelidad | El sistema debe asignar automáticamente un nivel al cliente (Bronce, Plata, Oro, VIP) según el total apostado, con cashback sobre sus pérdidas. | Sistema | Media | Un cliente Bronce con 1.990.000 apostados que apuesta 20.000 pasa a Plata. Un cliente Oro que pierde 100.000 recibe 5.000 (5%). El nivel nunca baja. |
| RF-11 | Persistencia de datos | El sistema debe permitir al administrador persistir en archivos locales el estado de clientes, empleados, mesas y transacciones, y restaurarlo automáticamente al arrancar. | Administrador | Alta | Al registrar un cliente y asignarle fichas, cerrar la aplicación y reiniciar el sistema, el saldo del cliente y el catálogo de mesas deben coincidir exactamente con el estado previo al apagado. |

# Plan hacia la Entrega 2

## Tabla de trazabilidad inicial

| Requerimiento | Clases involucradas | Temas de POO aplicados | Estado |
|---|---|---|---|
| **RF-01** | `Cliente`, `Persona`, `Casino` | Herencia, Encapsulamiento, Validación de entradas y manejo de excepciones | En diseño |
| **RF-02** | `Trabajador`, `Persona`, `Administrador`, `Casino` | Herencia, Encapsulamiento, Constructores, Atributos privados con getters/setters | En diseño |
| **RF-03** | `Trabajador`, `Administrador`, `Cajero`, `Crupier`, `TecnicoMaquina` | Herencia, Clases abstractas, Polimorfismo, Control de acceso | En diseño |
| **RF-04** | `Mesa`, `Juego`, `Crupier`, `Casino` | Asociación entre objetos, Agregación, Colecciones (`List` / `ArrayList`) | En diseño |
| **RF-05** | `Juego`, `Ruleta`, `Blackjack`, `Casino` | Herencia, Clases abstractas, Métodos polimórficos, Colecciones de catálogo | En diseño |
| **RF-06** | `Cajero`, `Cliente`, `Transaccion`, `Casino` | Encapsulamiento, Asociación entre objetos, Invariantes de saldo | En diseño |
| **RF-07** | `Caja`, `MovimientoFinanciero`, `Administrador`, `Casino` | Colecciones de transacciones, Métodos de cálculo/agregación, Encapsulamiento | En diseño |
| **RF-08** | `Crupier`, `Mesa`, `Juego`, `Apuesta`, `Cliente` | Polimorfismo (`calcularPago()`), Sobrecarga de métodos, Asociación | En diseño |
| **RF-09** | `TecnicoMaquina`, `MaquinaTragamonedas`, `Cliente` | Encapsulamiento, Enumeraciones para estados operativos, Generación pseudoaleatoria | En diseño |
| **RF-10** | `Cliente`, `NivelFidelidad`, `Cajero` | Lógica de acumulación, Encapsulamiento, Reglas de negocio | En diseño |
| **RF-11** | `Casino`, `GestorPersistencia`, `LectorArchivos` | Flujos de entrada/salida (I/O), Serialización / Parsing de archivos, Manejo robusto de excepciones | En diseño |

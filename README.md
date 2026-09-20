# Proyecto Broadway<br>

### Índice
- [Descripción](#descripci%C3%B3n-del-problema)
- [Requerimientos](#requerimientos)
- [Roles del Equipo](#roles-del-equipo)
#

### Descripción del Problema
El Casino Broadway es un casino grande de Bogotá que controla la venta de fichas, las apuestas y los pagos de forma manual, con planillas y cálculos que hacen los crupieres. Esto provoca descuadres de caja al cierre de cada turno, errores en el cálculo de pagos (teniendo en cuenta que cada juego tiene su propia tabla), apuestas fuera de los límites de la mesa y ningún tipo de control sobre la edad mínima permitida legal. Nuestro proyecto propone un sistema que pueda automatizar el registro de los apostadores, venta y el cambio de fichas, la gestión de mesas, la validación de apuestas, el cálculo automático de pagos según el juego, los niveles de fidelización, los límites de pérdida y la autoexclusión, y el cierre de caja, guardando la información en archivos para que no se pierda entre los cierres de caja.

### Requerimientos
| ID | Requerimiento | Descripción | Actor | Prioridad | Criterio de aceptación |
|---|---|---|---|---|---|
| RF-01 | Registro de clientes | Registrar clientes con documento, nombre y fecha de nacimiento. | Cajero | Alta | No se permiten dos clientes con el mismo documento. Se rechaza a los menores de 18 años. |
| RF-02 | Registro de empleados | Registrar empleados con documento, nombre, usuario, contraseña y cargo. | Administrador | Alta | El usuario es único. La contraseña tiene mínimo 6 caracteres. No se aceptan campos vacíos. |
| RF-03 | División de empleados | Cada empleado tiene un cargo (Administrador, Cajero, Crupier o Técnico de máquinas) que define qué funciones puede usar. | Administrador | Alta | Un cajero no puede crear mesas. Un crupier no puede vender fichas. Solo el técnico puede cambiar el estado de una máquina. |
| RF-04 | Registro de mesas | Crear mesas con código, juego, apuesta mínima, apuesta máxima, capacidad y crupier asignado. | Administrador | Alta | La mínima debe ser menor que la máxima. El código es único. La capacidad va de 1 a 7. Solo se asigna un empleado con cargo Crupier. |
| RF-05 | Registro de juegos | Registrar los juegos de mesa disponibles (Ruleta, Blackjack) con su nombre y su tabla de pagos. | Administrador | Media | No se permiten dos juegos con el mismo nombre. Una mesa solo puede usar un juego registrado. |
| RF-06 | Sistema de fichas | Vender fichas a los clientes y cambiarlas por dinero, llevando el saldo de fichas de cada cliente. | Cajero | Alta | Un cliente con saldo 0 que compra 100.000 queda con 100.000. No se puede cambiar más de lo que tiene. Un monto menor o igual a 0 se rechaza. |
| RF-07 | Sistema de finanzas | Registrar cada movimiento de dinero y generar el cierre de caja de cada turno. | Administrador | Media | Con ventas por 1.000.000 y cambios por 700.000, el balance es 300.000. Si el efectivo contado no coincide, se muestra "DESCUADRE". |
| RF-08 | Sistema de apuestas | Registrar la apuesta de un cliente en una mesa y calcular su pago según el juego. | Crupier | Alta | Se rechaza la apuesta fuera del mínimo y el máximo de la mesa, o mayor al saldo. Con una apuesta de 10.000: pleno en ruleta gana 350.000 y blackjack natural gana 15.000. |
| RF-09 | Sistema de máquinas | Registrar las máquinas tragamonedas (código, valor por giro, estado) y sus jugadas. | Técnico de máquinas | Media | Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales se gana 10 veces la apuesta. Cada giro descuenta su valor del saldo. |
| RF-10 | Sistema de fidelidad | Asignar un nivel al cliente (Bronce, Plata, Oro, VIP) según el total apostado, con cashback sobre sus pérdidas. | Sistema | Media | Un cliente Bronce con 1.990.000 apostados que apuesta 20.000 pasa a Plata. Un cliente Oro que pierde 100.000 recibe 5.000 (5%). El nivel nunca baja. |

### Roles del Equipo
| Responsable de Producto | Responsable de Calidad | Responsable de Diseño | Responsable de Integración |
|---|---|---|---|
| [Camilo Ramírez](https://github.com/) | [Santiago Caicedo](https://github.com/Santiwr) | [Johan Buitrago](https://github.com/johansebastianbuitrago7-netizen) y [Juan Avellaneda](https://github.com/Juanavur) | [Juan Lasso](https://github.com/Weirdtions) |

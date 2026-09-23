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
**Ingreso a sala:** El cliente entra sin un registro sistemático de identidad ni validación contra listas de autoexclusión o mayoría de edad.
**Compra de fichas:** El cliente entrega efectivo en caja. El cajero anota la transacción a mano en una planilla física y entrega las fichas.
**Apertura de mesa:** El crúpier recibe un cupo inicial de fichas físicas firmando un recibo en papel.
**Colocación de apuestas:** En cada ronda, los jugadores colocan fichas. El crúpier verifica visualmente que la apuesta respete los topes de la mesa.
**Cálculo y pago:** Tras el resultado de la jugada, el crúpier calcula mentalmente el valor a pagar según la regla del juego (ej. 35:1 en Ruleta o 3:2 en Blackjack).
**Canje de fichas:** El cliente regresa a caja con sus fichas. El cajero las cuenta manualmente, calcula el dinero equivalente y entrega el efectivo.
**Cierre de turno:** El cajero y el crúpier comparan el dinero y las fichas sobrantes contra las planillas de papel para intentar cuadrar los saldos.

### Dolor
Radica en la desconexión e inconsistencia de sus flujos de información debido a la dependencia exclusiva de registros físicos y cálculos manuales. Al no existir un sistema centralizado, el control del inventario de fichas y el flujo de efectivo sufre discrepancias constantes al cierre de cada turno; los cajeros y supervisores se ven forzados a confrontar planillas manuscritas que frecuentemente contienen tachaduras, omisiones o datos ilegibles, lo que vuelve prácticamente imposible auditar a tiempo los descuadres de caja o identificar fugas de capital. A este problema administrativo se suma la vulnerabilidad en las mesas de juego, donde los crúpieres deben calcular mentalmente las liquidaciones de cada ronda. Por otra parte, la inexistencia de una base de datos de clientes crea un vacío crítico tanto a nivel normativo como comercial: el establecimiento no tiene una forma confiable de verificar la mayoría de edad en cada punto de apuesta ni de aplicar los vetos o las solicitudes de autoexclusión exigidas por las regulaciones de juego responsable, lo que expone a la empresa a sanciones legales.

### Impacto

El Casino Broadway es un casino grande de Bogotá que controla la venta de fichas, las apuestas y los pagos de forma manual, con planillas y cálculos que hacen los crupieres. Esto provoca descuadres de caja al cierre de cada turno, errores en el cálculo de pagos (teniendo en cuenta que cada juego tiene su propia tabla), apuestas fuera de los límites de la mesa y ningún tipo de control sobre la edad mínima permitida legal. Nuestro proyecto propone un sistema que pueda automatizar el registro de los apostadores, venta y el cambio de fichas, la gestión de mesas, la validación de apuestas, el cálculo automático de pagos según el juego, los niveles de fidelización, los límites de pérdida y la autoexclusión, y el cierre de caja, guardando la información en archivos para que no se pierda entre los cierres de caja.

## Roles del Equipo

| Responsable de Producto | Responsable de Calidad | Responsable de Diseño | Responsable de Integración |
|---|---|---|---|
| [Camilo Ramírez](https://github.com/) | [Santiago Caicedo](https://github.com/Santiwr) y [Daniel Avila](https://github.com/danielavilajjc-commits)| [Johan Buitrago](https://github.com/johansebastianbuitrago7-netizen) y [Juan Avellaneda](https://github.com/Juanavur) | [Juan Lasso](https://github.com/Weirdtions) |

## Documentación

- [Bitácora de uso de IA](docs/Bitacora-IA.md)
- [Registro de decisiones](docs/Registro-Decisiones.md)
- [Plan y trazabilidad](docs/Trazabilidad.md)

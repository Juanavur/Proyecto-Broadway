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
El manejo manual de la información en el Casino Broadway puede generar varios problemas en el día a día, ya que al depender de planillas físicas y cálculos hechos por los empleados es más fácil que aparezcan errores en los pagos, descuadres de caja o inconsistencias entre las fichas y el dinero disponible. Además, al no tener toda la información centralizada, se vuelve más difícil saber de dónde salió un error, llevar un buen control de las mesas y de los clientes o verificar correctamente aspectos como la mayoría de edad, los vetos y la autoexclusión. Esto no solo complica el trabajo de los empleados, sino que también puede generar pérdidas de dinero, problemas de control y riesgos frente al cumplimiento de las normas del casino.


## Roles del Equipo

| Responsable de Producto | Responsable de Calidad | Responsable de Diseño | Responsable de Integración |
|---|---|---|---|
| [Camilo Ramírez](https://github.com/) | [Santiago Caicedo](https://github.com/Santiwr) y [Daniel Avila](https://github.com/danielavilajjc-commits)| [Johan Buitrago](https://github.com/johansebastianbuitrago7-netizen) y [Juan Avellaneda](https://github.com/Juanavur) | [Juan Lasso](https://github.com/Weirdtions) |

## Documentación

- [Bitácora de uso de IA](docs/Bitacora-IA.md)
- [Registro de decisiones](docs/Registro-Decisiones.md)
- [Plan y trazabilidad](docs/Trazabilidad.md)

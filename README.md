# Proyecto Broadway

## Descripción del Problema

### Contexto
El Casino Broadway es una empresa de entretenimiento presencial y juegos de azar ubicada en Bogotá, Colombia. Cuenta con una nómina operativa de más de 400 empleados distribuidos en turnos rotativos y una infraestructura física compuesta por 30 mesas de juego en vivo (Ruleta, Blackjack y Póker) y más de 120 terminales electrónicas. A pesar del flujo constante de dinero y fichas, la gestión de apuestas, el control de inventario de mesa y la administración de usuarios se realiza mediante métodos manuales y planillas descentralizadas, lo que genera inconsistencias contables y riesgos operativos diarios.

### Actores
* **Cajero:** Empleado responsable de la venta y canje de fichas por dinero en efectivo, registro del balance inicial y ejecución del arqueo/cierre de caja por turno.
* **Crúpier:** Operador directo de la mesa de juego asignada. Recibe las apuestas de los jugadores, valida que cumplan con los montos mínimos y máximos de la mesa y ejecuta el pago de las rondas ganadoras.
* **Jefe de Sala / Administrador:** Supervisor general del casino. Abre y cierra mesas, asigna crúpieres, consulta reportes consolidados y gestiona la lista de autoexclusión y vetos.
* **Cliente / Apostador:** Usuario registrado que adquiere fichas, realiza apuestas en las mesas, acumula beneficios según su volumen de juego y puede fijar topes de pérdida o solicitar su autoexclusión.

### Proceso actual
Actualmente el casino Broadway presenta formas muy anticuadas y arcaicas a la hora de efectuar sus procesos. Por ejemplo: Varias máquinas comparten la misma fórmula para calcular los pagos, cuando varias no comparten la misma probabilidad de ganar; no se tiene distinción entre varias máquinas del mismo tipo, esto produce errores porque muchas veces se estiman ganancias de acuerdo a la cantidad de máquinas de cierto tipo que hay, cuando en realidad hay varias dañadas; algo parecido ocurre con las mesas, pues no se hace la distinción del tipo de juego que son, que no todas dejan la misma ganancias y que en todas las mesas se tiene el mismo límite para apostar sin importa el tipo de mesa que es; y por último, está el sistema de clientes, el cual es inexistente, por lo que no se tiene registro si cumplen con la mayoría de edad, si pueden apostar o si están vetados.

### Dolor
El principal problema que presenta el casino Broadway, es que mucha información se pierde y que cosas como las mesas, máquinas y demás, se aplican normas generales, cuando no debería ser así. Es en este sentido que varios problemas aparecen, como lo pueden ser: El no tener un registro de clientes, por lo que resulta difícil tener su historial

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

# Guía de Casino Broadway

<p align="center"><img src="capturas-version-uml/01-portada.png" alt="Portada de Casino Broadway" width="100%"></p>

## Ejecutar el casino

En Linux x86-64 ejecuta `java -jar CasinoBroadway.jar`. El [JAR](../CasinoBroadway.jar) incluye JavaFX para esta plataforma. Pulsa **Espacio** en la portada para acceder como cliente o usar una cuenta de empleado de prueba.

| Cargo | Usuario | Contraseña | Funciones |
|---|---|---|---|
| Administrador | `admin` | `admin123` | Registra empleados, gestiona las mesas y consulta los clientes. |
| Cajero | `cajero` | `cajero123` | Registra clientes, vende y canjea fichas, entrega cashback y maneja los turnos de caja. |
| Crupier | `crupier` | `crupier123` | Atiende sus mesas de ruleta y blackjack y opera la sala de tragamonedas. |
| Técnico de máquinas | `tecnico` | `tecnico123` | Registra máquinas, las pone en mantenimiento y las vuelve a poner en servicio. |

## Juegos

Pulsa **Entrar como cliente** para crear un cliente nuevo y escribir cuánto efectivo trae. Desde el lobby se accede a las mesas, las máquinas y la caja. La barra superior muestra el efectivo, las fichas y el cashback, y permite silenciar los sonidos.

| Juego | Operación |
|---|---|
| Ruleta | Selecciona una ficha y pulsa una casilla del tapete. Puedes retirar o repetir las apuestas. **GIRAR** registra las apuestas con el crupier asignado y liquida la ronda al terminar. |
| Blackjack | Arma la apuesta con fichas y pulsa **Repartir**. Puedes pedir carta o plantarte. El blackjack natural paga 3 a 2. |
| Tragamonedas | Elige una máquina en servicio. El giro descuenta el costo de esa máquina. Tres símbolos iguales en la línea central pagan diez veces el costo. |
| Caja | Compra fichas con tu efectivo o cámbialas de vuelta con un turno de caja abierto. El historial muestra los movimientos de esa visita y el panel de cashback permite reclamar lo acumulado. |

La aplicación inicia con datos de demostración. Los registros y las transacciones de la sesión se mantienen en memoria.

## Requisitos

- **JDK 17 o superior**. El proyecto se verificó con JDK 21 y JavaFX 21.0.6.
- Para compilar desde el código, usa la carpeta `lib` de un SDK **JavaFX 21** compatible con tu sistema y arquitectura.

## Abrirlo en Visual Studio Code

1. Instala la extensión **Extension Pack for Java**.
2. `Archivo → Abrir carpeta…` y escoge la carpeta `CasinoBroadway`.
3. Agrega los JAR de JavaFX al proyecto mediante **Java: Configure Classpath**.
4. Ejecuta `Main` con las opciones de VM `--module-path "ruta/al/sdk/lib" --add-modules javafx.controls`.

## Abrirlo en IntelliJ IDEA

1. `File → Open…` y escoge la carpeta `CasinoBroadway`.
2. Si aparece el aviso *Project JDK is not defined*, pulsa **Setup SDK** y escoge tu JDK 17 o superior
   (o descárgalo desde ahí mismo).
3. En **Project Structure → Libraries**, agrega la carpeta `lib` del SDK JavaFX al módulo `CasinoBroadway`.
4. En la configuración **Casino Broadway**, agrega `--module-path "ruta/al/sdk/lib" --add-modules javafx.controls` en **VM options** y ejecuta `Main`.

La configuración ya viene en `.idea/` y `CasinoBroadway.iml`.

## Sin IDE

- Windows (CMD): `set "JAVAFX_LIB=C:\ruta\javafx-sdk\lib"` y luego `ejecutar.bat`.
- macOS / Linux: `JAVAFX_LIB="/ruta/javafx-sdk/lib" ./ejecutar.sh`. En Linux, el script también revisa el SDK del sistema y la caché local de Maven.

---

## Estructura

```text
src/
├── enums/        Efectos de sonido
├── files/        Logo y estilos JavaFX
├── interfaces/   Operable y ServicioSonido
├── modelos/      Dominio del diagrama UML
├── persistencia/ Reservada para RF-11
├── repositorios/ Reservada para repositorios
├── servicios/    Datos iniciales, formato y sonido
├── ui/fx/        Portada, acceso, sala de clientes y paneles de empleados
└── Main.java     Punto de entrada
```

## Correspondencia con el UML

- `Persona` es la base abstracta de `Cliente` y `Trabajador`. Los cuatro cargos definen sus permisos mediante `puedeRealizar(String)`.
- `Casino` reúne los clientes, trabajadores, mesas, máquinas y juegos, y posee una `Caja`.
- `Mesa` y `MaquinaTragaMoneda` implementan `Operable`. Cada mesa tiene un juego y un crupier asignado, y cada ronda aplica el cupo de la mesa.
- `Juego.calcularPremio(double)` es abstracto. `Ruleta` y `Blackjack` lo implementan con pagos distintos y `Apuesta` delega el cálculo al juego.
- `Caja.CierreTurno`, `Cliente.NivelFidelidad` y `MaquinaTragaMoneda.RondaMaquina` se declaran dentro de la clase correspondiente.

[Diagrama UML](Diagrama%20UML.png) · [Correspondencia de clases y requerimientos](../README.md#correspondencia-uml--código)

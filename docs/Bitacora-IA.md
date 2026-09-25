<table>
  <thead>
    <tr>
      <th width="10%">Fecha</th>
      <th width="10%">Proveedor</th>
      <th width="10%">Modelo</th>
      <th width="30%">Prompt</th>
      <th width="20%">Resultado</th>
      <th width="20%">Corregido</th>
    </tr>
  </thead>

  <tbody>
    <tr>
      <td>18/09/2026</td>
      <td>Gemini</td>
      <td>3.6 Flash</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Basandote en la informacion que te comparto: El Casino Broadway es un casino grande de Bogotá que controla la venta de fichas, las apuestas y los pagos de forma manual, con planillas y cálculos que hacen los crupieres. Esto provoca descuadres de caja al cierre de cada turno, errores en el cálculo de pagos (teniendo en cuenta que cada juego tiene su propia tabla), apuestas fuera de los límites de la mesa y ningún tipo de control sobre la edad mínima permitida legal. Nuestro proyecto propone un sistema que pueda automatizar el registro de los apostadores, venta y el cambio de fichas, la gestión de mesas, la validación de apuestas, el cálculo automático de pagos según el juego, los niveles de fidelización, los límites de pérdida y la autoexclusión, y el cierre de caja, guardando la información en archivos para que no se pierda entre los cierres de caja. Requerimientos funcionales: RF-01 Registro de clientes Registrar clientes con documento, nombre y fecha de nacimiento. Cajero Alta No se permiten dos clientes con el mismo documento. Se rechaza a los menores de 18 años. RF-02 Registro de empleados Registrar empleados con documento, nombre, usuario, contraseña y cargo. Administrador Alta El usuario es único. La contraseña tiene mínimo 6 caracteres. No se aceptan campos vacíos. RF-03 División de empleados Cada empleado tiene un cargo (Administrador, Cajero, Crupier o Técnico de máquinas) que define qué funciones puede usar. Administrador Alta Un cajero no puede crear mesas. Un crupier no puede vender fichas. Solo el técnico puede cambiar el estado de una máquina. RF-04 Registro de mesas Crear mesas con código, juego, apuesta mínima, apuesta máxima, capacidad y crupier asignado. Administrador Alta La mínima debe ser menor que la máxima. El código es único. La capacidad va de 1 a 7. Solo se asigna un empleado con cargo Crupier. RF-05 Registro de juegos Registrar los juegos de mesa disponibles (Ruleta, Blackjack) con su nombre y su tabla de pagos. Administrador Media No se permiten dos juegos con el mismo nombre. Una mesa solo puede usar un juego registrado. RF-06 Sistema de fichas Vender fichas a los clientes y cambiarlas por dinero, llevando el saldo de fichas de cada cliente. Cajero Alta Un cliente con saldo 0 que compra 100.000 queda con 100.000. No se puede cambiar más de lo que tiene. Un monto menor o igual a 0 se rechaza. RF-07 Sistema de finanzas Registrar cada movimiento de dinero y generar el cierre de caja de cada turno. Administrador Media Con ventas por 1.000.000 y cambios por 700.000, el balance es 300.000. Si el efectivo contado no coincide, se muestra "DESCUADRE". RF-08 Sistema de apuestas Registrar la apuesta de un cliente en una mesa y calcular su pago según el juego. Crupier Alta Se rechaza la apuesta fuera del mínimo y el máximo de la mesa, o mayor al saldo. Con una apuesta de 10.000: pleno en ruleta gana 350.000 y blackjack natural gana 15.000. RF-09 Sistema de máquinas Registrar las máquinas tragamonedas (código, valor por giro, estado) y sus jugadas. Técnico de máquinas Media Una máquina en mantenimiento no permite jugar. Con 3 símbolos iguales se gana 10 veces la apuesta. Cada giro descuenta su valor del saldo. RF-10 Sistema de fidelidad Asignar un nivel al cliente (Bronce, Plata, Oro, VIP) según el total apostado, con cashback sobre sus pérdidas. Sistema Media Un cliente Bronce con 1.990.000 apostados que apuesta 20.000 pasa a Plata. Un cliente Oro que pierde 100.000 recibe 5.000 (5%). El nivel nunca baja........ debes crear un diagrama de clase, que se conecte coherentemte entre si con sentido, que se vea para un proyecto serio y medio profesional
      </details></td>
      <td align="center"><img src="https://github.com/user-attachments/assets/0516d35f-ed2a-4701-8e38-0fb5b2ee5064" width="400"></td>
      <td>Imagen usada para apoyarse en la creación de la clase Casino con sus respectivos hijos, pero sin incluir todos los atributos y clases que nos sugería la IA</td>
    </tr>
    <tr>
      <td>24/09/2026</td>
      <td>Gemini</td>
      <td>1.5 Pro</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Basándote en las instrucciones qué vacíos o fallas tenemos frente a la rúbrica, qué requerimientos no funcionales (RNF) debemos agregar, cómo corregir el formato de los requerimientos funcionales (especialmente RF-11), cómo formular al menos dos decisiones de diseño bajo el esquema oficial (decisión · alternativas · por qué · consecuencia) y cómo enriquecer la tabla de trazabilidad hacia la Entrega 2 con temas de POO (polimorfismo, colecciones, excepciones y archivos).
      </details></td>
      <td>Diagnóstico detallado corrección de la fila descuadrada del RF-11, formulación de 3 Requerimientos No Funcionales (RNF-01 a RNF-03), redacción estructurada de 2 decisiones de arquitectura (jerarquía polimórfica en juegos y persistencia en archivos locales JSON/CSV), desglose de temas de POO en la tabla de trazabilidad y entrega del documento final.</td>
      <td>Se incorporaron los 3 RNF y la corrección de RF-11, se embebieron las dos decisiones de diseño y la bitácora directamente en el cuerpo principal del documento en lugar de dejarlas solo como enlaces externos, se refinó la tabla de trazabilidad especificando colecciones y polimorfismo en vez de solo herencia, y se corrigieron erratas tipográficas en los roles y clases antes del commit final.</td>
    </tr>
  </tbody>
</table>

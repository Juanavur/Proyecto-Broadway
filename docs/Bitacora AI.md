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
      <td>20/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Solicitud de esta bitácora: recopilar todos los prompts importantes usados con Claude durante el proyecto, con su fecha, y agregarlos a este archivo.
      </details></td>
      <td>Listado cronológico de los prompts (septiembre a noviembre) incorporado a la Bitácora AI.</td>
      <td>Se revisó que las fechas aproximadas quedaran marcadas como tales.</td>
    </tr>
    <tr>
      <td>20/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Implementar la persistencia (RF-11), que aún no estaba en el repositorio, y actualizar las diapositivas con esa parte.
      </details></td>
      <td>Guardado y carga del casino en JSON (datos/casino.json) con escritura segura, respaldo si el archivo está dañado y contraseñas cifradas; pruebas de persistencia; README, Trazabilidad y diapositivas actualizados. Se subió en la rama persistencia.</td>
      <td>Se mantuvo JSON (y no serialización de Java) para respetar la Decisión 2 y el RNF-02 ya documentados. Se trabajó en una rama aparte y luego se integró a main por pull request.</td>
    </tr>
    <tr>
      <td>19/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Combinar la versión de administrador con el juego del cliente como una simulación que genere al azar nombre, documento, nivel de fidelidad y dinero. Hacer el commit directo al repositorio y crear diapositivas de la presentación.
      </details></td>
      <td>Versión combinada (admin + simulación de cliente), commit al repositorio desde el navegador con la sesión del equipo y presentación en PowerPoint.</td>
      <td>Se probó la versión combinada localmente antes de subirla.</td>
    </tr>
    <tr>
      <td>12/11/2026 – 13/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Evaluar la fidelidad y coherencia entre el código y el diagrama UML con una nota aproximada; crear una versión más simplificada y fiel al UML sin sacrificar lo visual, en una rama aparte (sin alterar main). Comandos para descomprimir y correr cada versión en Warp.
      </details></td>
      <td>Evaluación con nota estimada y lista de diferencias UML↔código; rama version-uml con las 22 clases del diagrama; comandos de ejecución.</td>
      <td>main no se tocó; la versión fiel al UML se trabajó en su propia rama y luego se integró por pull request.</td>
    </tr>
    <tr>
      <td>06/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Abrir el código del juego en IntelliJ, enviar el .jar/.zip, comparar el repositorio con el repositorio guía del profesor (SistemaGestionBiblioteca) y ayudar a organizar el GitHub.
      </details></td>
      <td>Comparación con la guía (estructura por paquetes, README con requerimientos, conceptos de POO, capas y reglas del negocio) y archivos listos para entregar.</td>
      <td>Se adoptaron las secciones que faltaban en el README siguiendo el formato de la guía.</td>
    </tr>
    <tr>
      <td>06/11/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Subir el juego al GitHub y preparar una versión jugable con una portada estilizada en la que se presione ESPACIO para empezar.
      </details></td>
      <td>Portada del juego, README con capturas y archivos CasinoBroadway-GitHub.zip y CasinoBroadway.jar.</td>
      <td>El equipo subió la versión jugable al repositorio.</td>
    </tr>
    <tr>
      <td>23/10/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Actualizar las diapositivas al último diagrama UML, explicar Cliente, Persona y Nivel de fidelidad, y hacer las diapositivas más atractivas visualmente (cambiando algunas imágenes).
      </details></td>
      <td>Presentación actualizada con el UML final y explicación de cada clase.</td>
      <td>Se ajustaron imágenes y textos a pedido del equipo.</td>
    </tr>
    <tr>
      <td>23/10/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Exportar en PNG el diagrama final de Lucidchart y, basándose en ese UML y el README, explicar cómo se aplican los conceptos (estáticos, clases anidadas, interfaces, polimorfismo), su utilidad y su relación con las decisiones de diseño; entregarlo en Markdown.
      </details></td>
      <td>PNG del diagrama y texto en Markdown para el documento de la Entrega 2.</td>
      <td>El equipo revisó el texto y lo integró al documento.</td>
    </tr>
    <tr>
      <td>22/10/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Pasar el diagrama de clases a Lucidchart, exportarlo en PNG con el logo del Casino Broadway, crear diapositivas en PowerPoint con un presentador explicando el UML, quitar la persistencia del diagrama y de las diapositivas, y preparar las preguntas que podría hacer el profesor.
      </details></td>
      <td>Diagrama en Lucidchart, PNG con logo, presentación de 8 diapositivas y lista de preguntas con respuestas.</td>
      <td>Se quitó la persistencia y el cuadro de "Cómo leer el diagrama"; las diapositivas se hicieron en PowerPoint y no en Lucid.</td>
    </tr>
    <tr>
      <td>22/10/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Entrega 2: actualizar el diagrama de clases UML con clases, atributos y métodos estáticos, clases anidadas, interfaces y polimorfismo, y explicar en el documento cómo se aplican. Mejorar la legibilidad del diagrama y agregar el logo del casino.
      </details></td>
      <td>Diagrama UML actualizado (PNG), documento de la Entrega 2 en PDF y logo del Casino Broadway.</td>
      <td>Se pidió agrandar la letra y simplificar el diagrama para que se leyera completo.</td>
    </tr>
    <tr>
      <td>Octubre 2026 (aprox.)</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          El profesor pidió incluir clases anidadas, polimorfismo e interfaces: ¿cómo aplicarlos? Editar el juego agregándolos, manteniéndolo amigable y sencillo visual y jugablemente. Agregar sonidos de casino.
      </details></td>
      <td>Propuesta de dónde aplicar cada concepto y versión del juego actualizada con ellos y con sonidos.</td>
      <td>Se pidió que el juego siguiera siendo sencillo de usar.</td>
    </tr>
    <tr>
      <td>05/10/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Desarrollar un casino virtual que represente al Casino Broadway: tragamonedas, Blackjack, Póker, carreras de caballos, ruleta con distintos premios y caja para comprar y vender fichas, con reglas reales, nivel de fidelidad elegido al inicio (bronce, plata, oro, diamante con cashback de 2, 4, 8 y 10) y proyecto para Visual Studio Code e IntelliJ. Indicaciones para ejecutarlo en VS Code y en Warp.
      </details></td>
      <td>Juego en Java Swing con todos los módulos y proyectos para VS Code e IntelliJ, más comandos de ejecución.</td>
      <td>El equipo lo probó y pidió mejoras (sonidos, conceptos de POO).</td>
    </tr>
    <tr>
      <td>24/09/2026</td>
      <td>Anthropic</td>
      <td>Claude Opus 5.5</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Entrega 1: revisar la toma de decisiones (issue #7), el registro de decisiones (issue #5) y el README del repositorio, dar correcciones y una calificación estimada, ayudar con el impacto de las decisiones y armar un PDF con todo.
      </details></td>
      <td>Correcciones por sección, nota estimada, redacción del impacto y PDF de la entrega.</td>
      <td>Se descartaron los RNF, la bitácora y el apartado del equipo en esa revisión porque el profesor indicó que no eran necesarios; se quitó "Bogotá" del título del PDF.</td>
    </tr>
    <tr>
      <td>22/10/2026</td>
      <td>Gemini</td>
      <td>3.8 Flash</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Validación y contraste de un borrador preliminar de la tabla de requerimientos y la tabla de trazabilidad inicial frente al README principal: revisión de coherencia en criterios de aceptación (especialmente RF-11), clases involucradas, rigor en conceptos de POO y corrección de erratas en actores y nombres de clases.
      </details></td>
      <td>Diagnóstico de inconsistencias evidenciando un retroceso en el criterio de aceptación del RF-11 (redacción informal), simplificación excesiva de los temas de POO en la trazabilidad (omisión de polimorfismo, colecciones y manejo de I/O), exclusión de la clase base Persona y erratas tipográficas en clases y roles (Cruppier, adminstrador, tecnico de maquinas). Entrega de las dos tablas completamente sincronizadas y corregidas.</td>
      <td>El equipo descartó la redacción informal de RF-11 y reincorporó el criterio de prueba verificable, sincronizó la tabla de trazabilidad mapeando polimorfismo, clases abstractas, colecciones y manejo de excepciones/archivos a sus clases respectivas, normalizó los nombres de las clases (reincorporando Persona) y corrigió las inconsistencias en actores y ortografía.</td>
    </tr>
    <tr>
      <td>09/10/2026</td>
      <td>Anthropic</td>
      <td>Claude 5.5 Sonnet</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Apoyo para formular los requerimientos funcionales bajo el estándar "El sistema debe permitir al [actor]...", estructurar los criterios de aceptación medibles y verificar que el diseño permitiera aplicar herencia, polimorfismo y colecciones.
      </details></td>
      <td>Un borrador inicial con requerimientos transaccionales genéricos (similares a una tienda electrónica) y criterios de aceptación cualitativos (por ejemplo, "el sistema debe ser rápido y no fallar").</td>
      <td>El equipo descartó los requerimientos genéricos y los adaptó a la operativa real del Casino Broadway, introduciendo los actores operativos reales (Cajero, Crúpier, Técnico, Administrador), reglas exactas de pago (35:1, 3:2, 10x), validaciones de juego responsable (rechazo a menores de 18 años y topes de pérdida) y formuló la tabla de requerimientos no funcionales (RNF).</td>
    </tr>
    <tr>
      <td>24/09/2026</td>
      <td>Gemini</td>
      <td>3.8 Flash Avanzado</td>
      <td><details>
          <summary>Ver prompt completo</summary>
          Basándote en las instrucciones qué vacíos o fallas tenemos frente a la rúbrica, qué requerimientos no funcionales (RNF) debemos agregar, cómo corregir el formato de los requerimientos funcionales (especialmente RF-11), cómo formular al menos dos decisiones de diseño bajo el esquema oficial (decisión · alternativas · por qué · consecuencia) y cómo enriquecer la tabla de trazabilidad hacia la Entrega 2 con temas de POO (polimorfismo, colecciones, excepciones y archivos).
      </details></td>
      <td>Diagnóstico detallado corrección de la fila descuadrada del RF-11, formulación de 3 Requerimientos No Funcionales (RNF-01 a RNF-03), redacción estructurada de 2 decisiones de arquitectura (jerarquía polimórfica en juegos y persistencia en archivos locales JSON/CSV), desglose de temas de POO en la tabla de trazabilidad y entrega del documento final.</td>
      <td>Se incorporaron los 3 RNF y la corrección de RF-11, se embebieron las dos decisiones de diseño y la bitácora directamente en el cuerpo principal del documento en lugar de dejarlas solo como enlaces externos, se refinó la tabla de trazabilidad especificando colecciones y polimorfismo en vez de solo herencia, y se corrigieron erratas tipográficas en los roles y clases antes del commit final.</td>
    </tr>
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
  </tbody>
</table>

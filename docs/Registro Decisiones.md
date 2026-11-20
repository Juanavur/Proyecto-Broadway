Este documento registra las principales decisiones tomadas durante el diseño y desarrollo del sistema del Casino Broadway, junto a su respectivas justificación y requerimientos.

| Fecha | Decisión | Justificación | Requerimientos |
|---|---|---|---|
| 24/09/2026 | Creación del diagrama UML | Necesario para la siguiente entrega y para una clara organización de desarrollo del proyecto: clases, atributos y métodos definidos | <img alt="Diagrama UML" src="https://github.com/user-attachments/assets/890f7acb-b1e2-4f30-b4d7-4f3800c6ab57"/> |
| 23/09/2026 | Priorizar RF-01, RF-02 y RF-03 para comenzar el desarrollo | El registro de clientes, empleados y la división de estos últimos según su rol son la base para el desarrollo de los sistemas posteriores | RF-01, RF-02, RF-03 |
| 17/09/2026 | Dar a los empleados clases específicas según su rol, todos partiendo de una clase padre `Trabajador` | La herencia nos permite usar atributos comunes de cualquier empleado y asignar acciones para cada rol específico | RF-02, RF-03 |
| 17/09/2026 | Definir la clase `Casino` como punto central del sistema | Se requiere de una clase centralizada para facilitar tareas que requieran conectar distintos aspectos del casino | Todos |

Todo lo declarado está sujeto a cambios en el transcurso del proyecto.

Este documento registra las principales decisiones tomadas durante el diseño y desarrollo del sistema del Casino Broadway, junto a su respectivas justificación y requerimientos.

| Fecha | Decisión | Justificación | Requerimientos |
|---|---|---|---|
| 17/09/2026 | Definir la clase `Casino` como punto central del sistema | Se requiere de una clase centralizada para facilitar tareas que requieran conectar distintos aspectos del casino | Todos |
| 17/09/2026 | Dar a los empleados clases específicas según su rol, todos partiendo de una clase padre `Trabajador` | La herencia nos permite usar atributos comunes de cualquier empleado y asignar acciones para cada rol específico | RF-02, RF-03 |
| 24/09/2026 | Creación del diagrama UML | Necesario para la siguiente entrega y para una clara organización de desarrollo del proyecto: clases, atributos y métodos definidos | <img alt="Diagrama UML" src="https://github.com/user-attachments/assets/890f7acb-b1e2-4f30-b4d7-4f3800c6ab57"/> |

Todo lo declarado está sujeto a cambios en el transcurso del proyecto.

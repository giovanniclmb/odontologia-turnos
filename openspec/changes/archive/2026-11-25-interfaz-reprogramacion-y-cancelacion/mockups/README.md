# Mockups de C-12

Sistema visual aprobado en revisión 1. Mockup de C-12 aprobado por el usuario el 25/11/2026.

Abra [index.html](index.html) para recorrer una agenda de recepción con datos ficticios. Desde el turno de Ana Ejemplo puede abrir el formulario, volver a la agenda, reprogramar y cancelar. Para reproducir un conflicto, elija las 10:00 del 26 de noviembre con la profesional y el sillón originales. Para observar el aviso de destino, cambie de fecha o sillón. La simulación se restablece al recargar.

| Vista | Qué representa |
| --- | --- |
| [Reprogramar en recepción](index.html?vista=reprogramar) | Fecha, hora, profesional y sillón editables. Paciente, prestación y duración se conservan. |
| [Reprogramar en odontología](index.html?vista=reprogramar&rol=odontologia) | Turno propio de paciente asignado con profesional fijo. |
| [Conflicto de disponibilidad](index.html?vista=conflicto) | Mensaje genérico y valores de destino conservados para corregir el intento. |
| [Turno desactualizado](index.html?vista=version) | Aviso y acción para volver a consultar la agenda. |
| [Confirmar cancelación](index.html?vista=confirmar) | Identificación del turno, efecto de cancelar y acción inicial para mantenerlo. |
| [Turno cancelado](index.html?vista=cancelado) | Turno visible como cancelado y aviso de liberación de los recursos que ocupaba. |

El mockup reutiliza la agenda y los tokens del repositorio. Los estados se simulan localmente y el formulario no realiza solicitudes HTTP.

Las [capturas](previews/) muestran reprogramación, conflicto, confirmación y cancelación en 1440 × 900 y 390 × 844. Se verificaron ambos anchos sin desbordamiento horizontal y se recorrieron el rechazo, el cambio de sillón, la navegación al destino, la confirmación de cancelación y el profesional fijo en odontología.

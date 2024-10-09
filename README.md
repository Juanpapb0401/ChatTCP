# Chat con Llamadas de Voz y Grupos

Realizado por:

[Stick Martinez Valdes ](https://github.com/Stixkl)

[Juan Pablo Parra ](https://github.com/Juanpapb0401)

[Pablo Guzman ](https://github.com/Pableis05)

[Juan Esteban Eraso ](https://github.com/JuanEstebanEraso)

[Thomas Brueck ](https://github.com/Brueckk)

---

## Descripción del Proyecto

Este proyecto es un chat desarrollado en **Ice - ZeroC** y **Gradle** que ofrece funcionalidades avanzadas como llamadas de voz, envío de audios y la creación de grupos. Todas las operaciones se realizan a través de la terminal o command prompt, proporcionando una experiencia eficiente y centrada en la línea de comandos.

## Características

- **Mensajería Instantánea:** Envío y recepción de mensajes en tiempo real entre usuarios.
- **Llamadas de Voz:** Realiza llamadas de voz entre los participantes de forma rápida y sencilla.
- **Envío de Audios:** Graba y envía mensajes de audio para una comunicación más versátil.
- **Creación de Grupos:** Los usuarios pueden crear y administrar grupos de chat desde la terminal.
- **Interfaz en la Terminal:** Toda la interacción se realiza desde la línea de comandos, haciendo el chat ligero y rápido.

## Tecnologías Utilizadas

- **Lenguaje de Programación:** Java
- **Framework:** Ice - ZeroC para la comunicación y Gradle para la gestión de dependencias.
- **Protocolo de Comunicación:** ICE (Internet Communications Engine) para facilitar las llamadas de voz y el envío de mensajes.
- **Herramientas de Desarrollo:** Gradle como herramienta de construcción y administración de dependencias.

## Instalación

Requisitos
Java: Versión 11 o superior
Gradle: Versión 7.0 o superior
ZeroC - Ice: Framework para comunicación distribuida (preinstalado en el proyecto)
Sistema Operativo: Windows, macOS o Linux con acceso a la terminal o command prompt

Sigue los pasos a continuación para configurar el proyecto en tu entorno local.

1. **Clona el Repositorio:**
   ```bash
   git clone https://github.com/Juanpapb0401/ChatTCP.git


## Comandos para usar en el chat
/history:

Muestra el historial completo de mensajes enviados en el chat desde que el servidor se inició. Este comando es útil para ver las conversaciones anteriores.

/creategroup nombreGrupo:

Crea un nuevo grupo de chat con el nombre especificado. Si el grupo ya existe, se notifica al usuario que el grupo ya está creado.

/joingroup nombreGrupo:

Permite que un usuario se una a un grupo existente. Si el grupo no existe, se muestra un mensaje indicando que no se encontró el grupo.

/groups:

Muestra una lista de todos los grupos disponibles en el servidor. Si no hay grupos, indica que no hay grupos disponibles.

/msggroup nombreGrupo mensaje:

Envía un mensaje de texto al grupo especificado. El mensaje será visible para todos los miembros del grupo. Si el grupo no existe, se notifica al usuario.

/listclients:

Muestra una lista de todos los usuarios actualmente conectados al servidor. Si no hay otros usuarios conectados, indica que no hay clientes conectados.

/sendvoicenote destinatario rutaArchivo:

Envía una nota de voz a un usuario específico. El archivo especificado en rutaArchivo será enviado al destinatario indicado. La recepción del archivo se realiza en un hilo separado para no bloquear la ejecución del programa.

@usuario: mensajePrivado:

Envía un mensaje privado a otro usuario. El formato del comando es @usuario: mensaje, donde usuario es el nombre del destinatario y mensaje es el texto a enviar.

/listvoicenotes:

Muestra una lista de todas las notas de voz almacenadas en el sistema. Este comando muestra tanto las notas enviadas como recibidas por todos los usuarios.

/listuservoicenotes:

Muestra una lista de las notas de voz enviadas por el usuario que ejecuta el comando. Es útil para ver solo los audios que el usuario ha enviado.

/sendaudiogroup nombreGrupo rutaArchivo:

Envía un archivo de audio al grupo especificado. Primero, se notifica a los miembros del grupo que se ha enviado una nota de voz, y luego se procede a enviar el archivo. Si el grupo no existe o el archivo no es accesible, se muestra un mensaje de error.

Mensajes generales:

Si el mensaje no coincide con ninguno de los comandos anteriores, se envía como un mensaje de texto público para todos los usuarios conectados. El mensaje será mostrado en el formato nombreUsuario: mensaje. 
   

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

Hay que tener en cuenta que se puede correr el proyecto en un mismo terminal por eso la implementación de ICE. Pero tambien se puede correr en dos o mas terminales diferentes, siendo una un servidor y varias clientes. En el codigo esta la IP implementada como localhosto para poder correrlo de la primera manera escrita anteriormente. Si se desea correr de la segunda manera se debe de cambiar esta IP. Ya sea si se conectan por datos a computadores o una sala donde la señal por cable sea la misma.

1. **Clona el Repositorio:**
   ```bash
   git clone https://github.com/Juanpapb0401/ChatTCP.git
   
2. **Se debe descargar ICE**
   
3. **Tener mucho cuidado, se debe de asegurar que la carpeta que este abierta o donde este ubicado sea myapp, puesto que se
   encuentra ahi el codigo fuente. Se llega haciendo cd hasta que diga myapp**
   
4. **Abrir terminal Server**

   .\gradlew build
   
   .\gradlew :server:build (opcional porque ya se corrio el build para todo el proyecto)
   
   java -jar server/build/libs/server.jar

   Si lo hacen correctamente les debería salir esto en la terminal

  ![Imagen de WhatsApp 2024-10-09 a las 19 46 26_6d1b119f](https://github.com/user-attachments/assets/1c1160dd-4767-4350-b73d-9148fa8446be)

5. **Abrir terminar Client**
   
   .\gradlew build
   
   .\gradlew :client:build (opcional porque ya se corrio el build para todo el proyecto)
   
   java -jar client/build/libs/client.jar

![Imagen de WhatsApp 2024-10-09 a las 19 47 32_9755dee2](https://github.com/user-attachments/assets/0cb36040-640d-4ece-b3d3-a3a9695c6ed1)

   




   


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

Envía un mensaje privado a otro usuario. El formato del comando es @usuario: mensaje, donde usuario es el nombre del destinatario y mensaje es el texto a enviar. Tener en cuenta que se debe de colocar los ":" al final del nombre de usuario

/listvoicenotes:

Muestra una lista de todas las notas de voz almacenadas en el sistema. Este comando muestra tanto las notas enviadas como recibidas por todos los usuarios.

/listuservoicenotes:

Muestra una lista de las notas de voz enviadas por el usuario que ejecuta el comando. Es útil para ver solo los audios que el usuario ha enviado.

/sendaudiogroup nombreGrupo rutaArchivo:

Envía un archivo de audio al grupo especificado. Primero, se notifica a los miembros del grupo que se ha enviado una nota de voz, y luego se procede a enviar el archivo. Si el grupo no existe o el archivo no es accesible, se muestra un mensaje de error.

Mensajes generales:

Si el mensaje no coincide con ninguno de los comandos anteriores, se envía como un mensaje de texto público para todos los usuarios conectados. El mensaje será mostrado en el formato nombreUsuario: mensaje. 
   

package com.ucab.trivia.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.EstadoPregunta;
import com.ucab.trivia.domain.PreguntaDetallada;
import com.ucab.trivia.domain.PreguntaOriginal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


public class ServicioPreguntasConfig {
    // Ruta relativa al directorio padre
    private static final String RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO = "../banco_preguntas_gestion_compartido.json";
    // Ruta al archivo JSON original dentro de los recursos del proyecto (classpath)
    private static final String RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON = "/preguntasJuegoTrivia.json";

    private final ObjectMapper objectMapper;
    private List<PreguntaDetallada> preguntasGestionadas;

    
    //Constructor del servicio. Inicializa Jackson y carga las preguntas existentes.
    //Si el archivo de gestión de preguntas está vacío, intenta una importación inicial.
    
    public ServicioPreguntasConfig() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.findAndRegisterModules(); // Para manejar Enums con @JsonCreator/@JsonValue
        this.preguntasGestionadas = cargarPreguntasGestionDesdeArchivo();

        if (this.preguntasGestionadas.isEmpty()) {
            System.out.println("INFO: El archivo '" + RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO + "' está vacío o no existe. Se intentará una importación inicial de preguntas.");
            importarPreguntasDesdeJsonOriginal();
            // Es importante recargar después de la importación, ya que la importación guarda y luego esta instancia debe leer el archivo.
            this.preguntasGestionadas = cargarPreguntasGestionDesdeArchivo();
        }
    }

    //Carga la lista de preguntas detalladas desde el archivo JSON de gestión.
    
    private List<PreguntaDetallada> cargarPreguntasGestionDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO);
        if (archivo.exists() && archivo.length() > 0) {
            try {
                return objectMapper.readValue(archivo, new TypeReference<List<PreguntaDetallada>>() {});
            } catch (IOException e) {
                System.err.println(">> Error al cargar preguntas desde " + archivo.getAbsolutePath() + ": " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    //Guarda la lista actual de preguntas gestionadas en el archivo JSON.
    //Intenta una escritura atómica.
    
    private void guardarPreguntasGestionEnArchivo() {
        File tempFile = new File(RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO + ".tmp");
        File realFile = new File(RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO);
        try {
            File parentDir = realFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(tempFile, preguntasGestionadas);
            Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println(">> Error al guardar preguntas en " + realFile.getAbsolutePath() + ": " + e.getMessage());
            if(tempFile.exists()) tempFile.delete();
        } catch (UnsupportedOperationException uoe){
            try {
                Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                System.err.println(">> Error al guardar preguntas (fallback move) en " + realFile.getAbsolutePath() + ": " + ioe.getMessage());
            }
        }
    }

    /*Importa preguntas desde el archivo JSON original (formato Map<String, List<PreguntaOriginal>>)
      y las convierte al formato {@link PreguntaDetallada}.
      Solo se ejecuta si el archivo de gestión de preguntas está vacío.
      Las preguntas importadas se marcan como APROBADAS por defecto.*/
    
    public void importarPreguntasDesdeJsonOriginal() {
        // Verifica de nuevo si el archivo de gestión ya tiene contenido, para no duplicar en ejecuciones múltiples.
        if (!cargarPreguntasGestionDesdeArchivo().isEmpty()) {
            System.out.println("INFO: El archivo de gestión '" + RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO + "' ya tiene datos. No se realizará la importación automática.");
            return;
        }

        try (InputStream is = ServicioPreguntasConfig.class.getResourceAsStream(RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON)) {
            if (is == null) {
                System.err.println("CRÍTICO: No se pudo encontrar el archivo de recurso: " + RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON +
                        ". Asegúrese que esté en 'src/main/resources' del módulo 'trivia-config-app'.");
                return;
            }
            Map<String, List<PreguntaOriginal>> mapaPreguntasOriginales = objectMapper.readValue(is, new TypeReference<>() {});
            List<PreguntaDetallada> preguntasAImportar = new ArrayList<>();
            final int[] contadorImportadasConExito = {0};

            mapaPreguntasOriginales.forEach((nombreCategoriaJson, listaPreguntasSimples) -> {
                try {
                    CategoriaTrivia categoria = CategoriaTrivia.fromString(nombreCategoriaJson); // El enum maneja la conversión
                    listaPreguntasSimples.forEach(po -> {
                        preguntasAImportar.add(new PreguntaDetallada(
                                UUID.randomUUID().toString(),
                                po.getPregunta(),
                                po.getRespuesta(),
                                categoria,
                                EstadoPregunta.APROBADA, // Las preguntas originales se asumen APROBADAS
                                "sistema_importador",   // Usuario creador genérico para importadas
                                30                      // Tiempo por defecto en segundos
                        ));
                        contadorImportadasConExito[0]++;
                    });
                } catch (IllegalArgumentException e) {
                    System.err.println("ADVERTENCIA: La categoría '" + nombreCategoriaJson + "' del JSON original no es reconocida o es inválida. " +
                            "Se omitirán sus preguntas. Detalles: " + e.getMessage());
                }
            });

            if (!preguntasAImportar.isEmpty()) {
                // Añade las nuevas preguntas importadas a la lista en memoria (que debería estar vacía aquí)
                this.preguntasGestionadas.addAll(preguntasAImportar);
                guardarPreguntasGestionEnArchivo(); // Guarda la lista ahora poblada
                System.out.println(contadorImportadasConExito[0] + " preguntas fueron importadas desde '" + RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON +
                        "' y guardadas en '" + RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO + "'.");
            } else {
                System.out.println("INFO: No se importaron preguntas válidas desde '" + RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON +
                        "'. El archivo podría estar vacío o todas sus categorías son desconocidas.");
            }

        } catch (IOException e) {
            System.err.println("Error crítico al leer o procesar el archivo de preguntas originales '" + RUTA_RECURSO_PREGUNTAS_ORIGINALES_JSON + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Agrega una nueva pregunta al sistema.
     
    public String agregarPregunta(String textoPregunta, String respuestaCorrecta, CategoriaTrivia categoria,
                                  int tiempoMaximoSegundos, String emailUsuarioCreador) {
        if (textoPregunta == null || textoPregunta.trim().isEmpty() ||
                respuestaCorrecta == null || respuestaCorrecta.trim().isEmpty() ||
                categoria == null || tiempoMaximoSegundos <= 0) {
            return ">> Error: Datos de pregunta inválidos. Todos los campos son obligatorios y el tiempo debe ser positivo.";
        }
        PreguntaDetallada nuevaPregunta = new PreguntaDetallada(
                UUID.randomUUID().toString(),
                textoPregunta.trim(),
                respuestaCorrecta.trim(),
                categoria,
                EstadoPregunta.ESPERANDO_APROBACION,
                emailUsuarioCreador,
                tiempoMaximoSegundos);
        preguntasGestionadas.add(nuevaPregunta);
        guardarPreguntasGestionEnArchivo();
        return "Pregunta agregada con ID: " + nuevaPregunta.getId() + ". Queda en estado 'ESPERANDO_APROBACION'.";
    }

    /*Modifica una pregunta existente (excepto su ID).
     Solo se pueden modificar preguntas que no estén APROBADAS.*/
    
    public String modificarPregunta(String idPregunta, String nuevoTexto, String nuevaRespuesta,
                                    CategoriaTrivia nuevaCategoria, int nuevoTiempo) {
        Optional<PreguntaDetallada> optPregunta = preguntasGestionadas.stream().filter(p -> p.getId().equals(idPregunta)).findFirst();
        if (optPregunta.isEmpty()) return ">> Error: Pregunta con ID '" + idPregunta + "' no encontrada.";

        PreguntaDetallada preguntaAModificar = optPregunta.get();
        if (preguntaAModificar.getEstado() == EstadoPregunta.APROBADA) {
            return ">> Error: Las preguntas APROBADAS no pueden ser modificadas (solo eliminadas).";
        }

        if (nuevoTexto != null && !nuevoTexto.trim().isEmpty()) preguntaAModificar.setPregunta(nuevoTexto.trim());
        if (nuevaRespuesta != null && !nuevaRespuesta.trim().isEmpty()) preguntaAModificar.setRespuesta(nuevaRespuesta.trim());
        if (nuevaCategoria != null) preguntaAModificar.setCategoria(nuevaCategoria);
        if (nuevoTiempo > 0) preguntaAModificar.setTiempoMaximoRespuestaSegundos(nuevoTiempo);

        guardarPreguntasGestionEnArchivo();
        return "Pregunta con ID '" + idPregunta + "' modificada exitosamente.";
    }

    /*Modifica únicamente el tiempo máximo de respuesta de una pregunta.
    Solo para preguntas no APROBADAS.*/
    
    public String modificarTiempoPregunta(String idPregunta, int nuevoTiempoSegundos) {
        Optional<PreguntaDetallada> optPregunta = buscarPreguntaPorId(idPregunta);
        if (optPregunta.isEmpty()) return ">> Error: Pregunta con ID '" + idPregunta + "' no encontrada.";
        PreguntaDetallada pregunta = optPregunta.get();
        if (pregunta.getEstado() == EstadoPregunta.APROBADA) {
            return ">> Error: No se puede modificar el tiempo de una pregunta APROBADA.";
        }
        if (nuevoTiempoSegundos <= 0) return ">> Error: El tiempo máximo de respuesta debe ser un número positivo.";

        pregunta.setTiempoMaximoRespuestaSegundos(nuevoTiempoSegundos);
        guardarPreguntasGestionEnArchivo();
        return "Tiempo máximo de respuesta para pregunta ID '" + idPregunta + "' actualizado a " + nuevoTiempoSegundos + "s.";
    }

    
    //Elimina una pregunta del sistema.
    
    public String eliminarPregunta(String idPregunta) {
        boolean fueEliminada = preguntasGestionadas.removeIf(p -> p.getId().equals(idPregunta));
        if (fueEliminada) {
            guardarPreguntasGestionEnArchivo();
            return "Pregunta con ID '" + idPregunta + "' eliminada exitosamente.";
        }
        return ">> Error: Pregunta con ID '" + idPregunta + "' no encontrada para eliminar.";
    }

    //return Una copia de la lista de todas las preguntas gestionadas.
    public List<PreguntaDetallada> consultarTodasLasPreguntas() {
        return new ArrayList<>(preguntasGestionadas);
    }

    //Busca una pregunta por su ID.
     
    public Optional<PreguntaDetallada> buscarPreguntaPorId(String id) {
        return preguntasGestionadas.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    //Consulta preguntas filtradas por un estado específico.
    
    public List<PreguntaDetallada> consultarPreguntasPorEstado(EstadoPregunta estado) {
        return preguntasGestionadas.stream().filter(p -> p.getEstado() == estado).collect(Collectors.toList());
    }

    //Consulta preguntas que están esperando aprobación y no fueron creadas por el usuario actual.
    
    public List<PreguntaDetallada> consultarPreguntasParaAprobar(String emailUsuarioActual) {
        return preguntasGestionadas.stream()
                .filter(p -> p.getEstado() == EstadoPregunta.ESPERANDO_APROBACION &&
                        p.getUsuarioCreadorEmail() != null &&
                        !p.getUsuarioCreadorEmail().equals(emailUsuarioActual))
                .collect(Collectors.toList());
    }

    //Cambia el estado de una pregunta (ej. para aprobar o rechazar).

    public String cambiarEstadoPregunta(String idPregunta, EstadoPregunta nuevoEstado, String emailUsuarioGestor) {
        Optional<PreguntaDetallada> optPregunta = buscarPreguntaPorId(idPregunta);
        if (optPregunta.isEmpty()) return ">> Error: Pregunta con ID '" + idPregunta + "' no encontrada.";
        PreguntaDetallada pregunta = optPregunta.get();

        // Regla: Un usuario no puede aprobar/rechazar sus propias preguntas
        if ((nuevoEstado == EstadoPregunta.APROBADA || nuevoEstado == EstadoPregunta.RECHAZADA) &&
                pregunta.getUsuarioCreadorEmail() != null &&
                pregunta.getUsuarioCreadorEmail().equals(emailUsuarioGestor)) {
            return ">> Error: Un usuario no puede aprobar o rechazar sus propias preguntas.";
        }

        // Solo se pueden aprobar/rechazar las que están en ESPERANDO_APROBACION
        if (pregunta.getEstado() != EstadoPregunta.ESPERANDO_APROBACION &&
                (nuevoEstado == EstadoPregunta.APROBADA || nuevoEstado == EstadoPregunta.RECHAZADA)) {
            return ">> Error: Solo se pueden aprobar o rechazar preguntas que estén actualmente en 'ESPERANDO_APROBACION'.";
        }

        pregunta.setEstado(nuevoEstado);
        guardarPreguntasGestionEnArchivo();
        return "Pregunta ID '" + idPregunta + "' ha sido actualizada al estado: " + nuevoEstado;
    }
}

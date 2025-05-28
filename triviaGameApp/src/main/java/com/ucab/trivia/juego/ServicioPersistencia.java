package com.ucab.trivia.juego;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ucab.trivia.juego.utils.ConsolaUtilJuego;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ServicioPersistencia {
    // Ruta relativa al directorio padre para el archivo de guardado compartido
    private static final String RUTA_ARCHIVO_PARTIDA_GUARDADA_COMPARTIDO = "../partida_guardada_compartida.json";
    private final ObjectMapper objectMapper;

    //Constructor del servicio. Inicializa el ObjectMapper de Jackson.

    public ServicioPersistencia() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON legible
        this.objectMapper.findAndRegisterModules(); // Para Enums, Posicion, y otras clases anidadas
    }

    /**
     * Guarda el estado actual del juego en un archivo JSON.
     * Intenta una escritura atomica renombrando un archivo temporal.
     */
    public void guardarEstadoJuego(EstadoJuegoGuardado estadoDelJuego) {
        File tempFile = new File(RUTA_ARCHIVO_PARTIDA_GUARDADA_COMPARTIDO + ".tmp");
        File realFile = new File(RUTA_ARCHIVO_PARTIDA_GUARDADA_COMPARTIDO);
        try {
            File parentDir = realFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            objectMapper.writeValue(tempFile, estadoDelJuego);

            Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        } catch (IOException e) {
            System.err.println(">> Error critico al guardar la partida en " + realFile.getAbsolutePath() + ": " + e.getMessage());

            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (UnsupportedOperationException uoe){
            try {
                Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                System.err.println(">> Error critico al guardar la partida (usando fallback move) en " + realFile.getAbsolutePath() + ": " + ioe.getMessage());
            }
        }
    }

    /**
     * Carga el estado del juego desde un archivo JSON.
     * hace return Un objeto se enlaza a EstadoJuegoGuardado con el estado cargado,
     * o null si el archivo no existe, esta corrupto o hay un error.
     */
    public EstadoJuegoGuardado cargarEstadoJuego() {
        File archivoGuardado = new File(RUTA_ARCHIVO_PARTIDA_GUARDADA_COMPARTIDO);
        if (archivoGuardado.exists() && archivoGuardado.length() > 0) {
            try {
                EstadoJuegoGuardado estadoCargado = objectMapper.readValue(archivoGuardado, EstadoJuegoGuardado.class);
                ConsolaUtilJuego.mostrarMensaje("INFO: Partida anterior cargada exitosamente desde " + archivoGuardado.getAbsolutePath());
                return estadoCargado;
            } catch (IOException e) {
                System.err.println(">> Error al cargar la partida guardada desde " + archivoGuardado.getAbsolutePath() + ": " + e.getMessage());
                System.err.println("   El archivo podria estar corrupto o tener un formato incompatible. Se iniciara una nueva partida si se elige esa opcion.");
                return null;
            }
        }
        //ConsolaUtilJuego.mostrarMensaje("INFO: No se encontro una partida guardada en: " + archivoGuardado.getAbsolutePath());
        return null; // No existe partida guardada
    }

    /**
     * Verifica si existe un archivo de partida guardada.
     * retorna true si el archivo existe y no esta vacio, false en caso contrario.
     */
    public boolean existePartidaGuardada() {
        File archivo = new File(RUTA_ARCHIVO_PARTIDA_GUARDADA_COMPARTIDO);
        return archivo.exists() && archivo.length() > 0;
    }
}

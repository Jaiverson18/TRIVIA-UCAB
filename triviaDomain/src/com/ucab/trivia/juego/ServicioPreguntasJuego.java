package com.ucab.trivia.juego;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.EstadoPregunta;
import com.ucab.trivia.domain.PreguntaDetallada;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Servicio encargado de cargar y proveer preguntas para la aplicacion del juego.
 * Carga preguntas desde un archivo JSON gestionado por la aplicacion de configuracion,
 * filtrando solo aquellas que estan APROBADAS.
 */
public class ServicioPreguntasJuego {
    // Ruta relativa al directorio padre donde AppConfig guarda el archivo de gestion.
    private static final String RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO = "banco_preguntas_gestion_compartido.json";
    private final ObjectMapper objectMapper;
    private Map<CategoriaTrivia, List<PreguntaDetallada>> preguntasAprobadasAgrupadasPorCategoria;
    private final Random generadorAleatorio;

    //Constructor del servicio. Inicializa Jackson, el generador aleatorio y carga las preguntas.

    public ServicioPreguntasJuego() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.generadorAleatorio = new SecureRandom();
        cargarYFiltrarPreguntasAprobadas();
    }

    /**
     * Carga todas las preguntas desde el archivo JSON de gestion y luego las filtra
     * para quedarse solo con las APROBADAS, agrupandolas por categoria.
     */
    private void cargarYFiltrarPreguntasAprobadas() {
        this.preguntasAprobadasAgrupadasPorCategoria = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia categoria : CategoriaTrivia.values()) {
            preguntasAprobadasAgrupadasPorCategoria.put(categoria, new ArrayList<>());
        }

        File archivoDePreguntas = new File(RUTA_ARCHIVO_PREGUNTAS_GESTION_COMPARTIDO);
        if (!archivoDePreguntas.exists() || archivoDePreguntas.length() == 0) {
            System.err.println("ADVERTENCIA CRITICA: El archivo de preguntas '" + archivoDePreguntas.getAbsolutePath() +
                    "' no existe o esta vacio. El juego no tendra preguntas disponibles.");
            System.err.println("Por favor, ejecute primero la Aplicacion de Configuracion para generar o importar preguntas.");
            return;
        }

        try {
            List<PreguntaDetallada> todasLasPreguntasGestionadas = objectMapper.readValue(archivoDePreguntas, new TypeReference<List<PreguntaDetallada>>() {});

            for (PreguntaDetallada pregunta : todasLasPreguntasGestionadas) {
                if (pregunta.getEstado() == EstadoPregunta.APROBADA && pregunta.getCategoria() != null) {
                    preguntasAprobadasAgrupadasPorCategoria.get(pregunta.getCategoria()).add(pregunta);
                }
            }

            final boolean[] advertenciaMostrada = {false};
            preguntasAprobadasAgrupadasPorCategoria.forEach((categoria, listaDePreguntas) -> {
                if (listaDePreguntas.isEmpty()) {
                    System.err.println("ADVERTENCIA: No se encontraron preguntas APROBADAS para la categoria: " + categoria.getNombreMostrado());
                    advertenciaMostrada[0] = true;
                }
            });
            if (!advertenciaMostrada[0]) {
                System.out.println("INFO: Todas las categorias tienen preguntas aprobadas cargadas para el juego.");
            }

        } catch (IOException e) {
            System.err.println("Error fatal al cargar o procesar las preguntas para el juego desde '" + archivoDePreguntas.getAbsolutePath() + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Selecciona una pregunta aleatoria de la categoría especificada.
     * "categoria" la categoria de la cual seleccionar la pregunta.
     * Retorna una lista <PreguntaDetallada> aleatoria, o null si no hay preguntas aprobadas
     * disponibles para esa categoria o si la categoria es null.
     */

    public PreguntaDetallada seleccionarPreguntaAleatoria(CategoriaTrivia categoria) {
        if (categoria == null) {
            System.err.println("Error: Intento de seleccionar pregunta para categoria nula.");
            return null;
        }
        List<PreguntaDetallada> preguntasDeLaCategoria = preguntasAprobadasAgrupadasPorCategoria.get(categoria);
        if (preguntasDeLaCategoria == null || preguntasDeLaCategoria.isEmpty()) {
            return null;
        }
        return preguntasDeLaCategoria.get(generadorAleatorio.nextInt(preguntasDeLaCategoria.size()));
    }
}
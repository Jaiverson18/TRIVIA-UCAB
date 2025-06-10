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

public class ServicioPreguntasJuego {
    private static final String NOMBRE_ARCHIVO_PREGUNTAS = "banco_preguntas_gestion_compartido.json";
    private final ObjectMapper objectMapper;
    private Map<CategoriaTrivia, List<PreguntaDetallada>> preguntasAprobadasPorCategoria;
    private final Random generadorAleatorio;
    private final File archivoDePreguntas;

    public ServicioPreguntasJuego() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        this.generadorAleatorio = new SecureRandom();
        this.archivoDePreguntas = localizarArchivoDeDatos(NOMBRE_ARCHIVO_PREGUNTAS);
        cargarYFiltrarPreguntasAprobadas();
    }

    private File localizarArchivoDeDatos(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (archivo.exists()) {
            return archivo;
        }
        archivo = new File("../" + nombreArchivo);
        return archivo;
    }

    private void cargarYFiltrarPreguntasAprobadas() {
        this.preguntasAprobadasPorCategoria = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            preguntasAprobadasPorCategoria.put(cat, new ArrayList<>());
        }

        if (!archivoDePreguntas.exists() || archivoDePreguntas.length() == 0) {
            System.err.println("ADVERTENCIA CRÍTICA: El archivo de preguntas '" + archivoDePreguntas.getAbsolutePath() + "' no existe o está vacío.");
            System.err.println("El juego no tendrá preguntas. Ejecute AppConfig para generar/importar preguntas.");
            return;
        }

        try {
            List<PreguntaDetallada> todasLasPreguntas = objectMapper.readValue(archivoDePreguntas, new TypeReference<>() {});

            for (PreguntaDetallada p : todasLasPreguntas) {
                if (p.getEstado() == EstadoPregunta.APROBADA && p.getCategoria() != null) {
                    preguntasAprobadasPorCategoria.computeIfAbsent(p.getCategoria(), k -> new ArrayList<>()).add(p);
                }
            }

            preguntasAprobadasPorCategoria.forEach((cat, lista) -> {
                if (lista.isEmpty()) {
                    System.err.println("ADVERTENCIA: No hay preguntas APROBADAS para la categoría: " + cat.getNombreMostrado());
                }
            });

        } catch (IOException e) {
            System.err.println("Error fatal al cargar o procesar preguntas desde '" + archivoDePreguntas.getAbsolutePath() + "': " + e.getMessage());
        }
    }

    public PreguntaDetallada seleccionarPreguntaAleatoria(CategoriaTrivia categoria) {
        if (categoria == null) return null;
        List<PreguntaDetallada> listaCategoria = preguntasAprobadasPorCategoria.get(categoria);
        if (listaCategoria == null || listaCategoria.isEmpty()) return null;
        return listaCategoria.get(generadorAleatorio.nextInt(listaCategoria.size()));
    }
}
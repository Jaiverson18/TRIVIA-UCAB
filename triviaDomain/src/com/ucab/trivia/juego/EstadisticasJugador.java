package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import java.util.EnumMap;
import java.util.Map;

/**
 * Almacena y gestiona las estadísticas de un jugador durante una partida de TRIVIA-UCAB.
 * Incluye tiempo total de respuestas, preguntas correctas por categoría y juegos ganados.
 */
public class EstadisticasJugador {
    private long tiempoTotalRespuestasMs; // Solo se acumula si el juego es por tiempo
    private Map<CategoriaTrivia, Integer> correctasPorCategoria;
    private int juegosGanados; // Para la sesión/partida actual

    /**
     * Constructor que inicializa las estadísticas a cero.
     */
    public EstadisticasJugador() {
        this.tiempoTotalRespuestasMs = 0;
        this.correctasPorCategoria = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            this.correctasPorCategoria.put(cat, 0);
        }
        this.juegosGanados = 0;
    }

    /**
     * Agrega el tiempo transcurrido para una respuesta al total.
     * Solo debe llamarse si el juego está en modo por tiempo y el jugador respondió.
     * @param tiempoMs El tiempo en milisegundos que tomó la respuesta.
     */
    public void agregarTiempoRespuesta(long tiempoMs) {
        if (tiempoMs > 0) { // Solo agregar tiempos positivos
            this.tiempoTotalRespuestasMs += tiempoMs;
        }
    }

    /**
     * Registra una respuesta correcta para una categoría específica.
     * @param categoria La categoría de la pregunta respondida correctamente.
     */
    public void registrarRespuestaCorrecta(CategoriaTrivia categoria) {
        if(categoria != null) {
            this.correctasPorCategoria.merge(categoria, 1, Integer::sum);
        }
    }

    /**
     * Incrementa el contador de juegos ganados por el jugador.
     */
    public void registrarJuegoGanado() {
        this.juegosGanados++;
    }

    // Getters y Setters para la serialización/deserialización con Jackson

    /** @return El tiempo total acumulado de respuestas en milisegundos. */
    public long getTiempoTotalRespuestasMs() { return tiempoTotalRespuestasMs; }
    /** @param tiempoTotalRespuestasMs El nuevo tiempo total. */
    public void setTiempoTotalRespuestasMs(long tiempoTotalRespuestasMs) { this.tiempoTotalRespuestasMs = tiempoTotalRespuestasMs; }

    /** @return Un mapa de las preguntas correctas por categoría. */
    public Map<CategoriaTrivia, Integer> getCorrectasPorCategoria() { return correctasPorCategoria; }
    /** @param correctasPorCategoria El nuevo mapa de correctas por categoría. */
    public void setCorrectasPorCategoria(Map<CategoriaTrivia, Integer> correctasPorCategoria) { this.correctasPorCategoria = correctasPorCategoria; }

    /** @return El número de juegos ganados en esta sesión/partida. */
    public int getJuegosGanados() { return juegosGanados; }
    /** @param juegosGanados El nuevo número de juegos ganados. */
    public void setJuegosGanados(int juegosGanados) { this.juegosGanados = juegosGanados; }

    /**
     * Devuelve una representación en String de las estadísticas del jugador.
     * @return Un String formateado con las estadísticas.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        double segundosTranscurridos = tiempoTotalRespuestasMs / 1000.0;
        sb.append("  > Tiempo total acumulado en respuestas (en juegos por tiempo): ").append(String.format("%.2f", segundosTranscurridos)).append("s\n");
        sb.append("  > Preguntas respondidas correctamente por categoría:\n");
        if (correctasPorCategoria.values().stream().allMatch(v -> v == 0)) {
            sb.append("    - Ninguna\n");
        } else {
            correctasPorCategoria.forEach((categoria, contador) -> {
                if (contador > 0) { // Mostrar solo si hay alguna correcta
                    sb.append("    - ").append(categoria.getNombreMostrado()).append(": ").append(contador).append("\n");
                }
            });
        }
        sb.append("  > Juegos ganados en esta partida/sesión: ").append(juegosGanados);
        return sb.toString();
    }
}

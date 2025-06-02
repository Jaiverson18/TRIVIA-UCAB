package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import java.util.EnumMap;
import java.util.Map;

/**
 * Almacena y gestiona las estadisticas de un jugador durante una partida de TRIVIA-UCAB.
 * Incluye tiempo total de respuestas, preguntas correctas por categoria y juegos ganados.
 */
public class EstadisticasJugador {
    private long tiempoTotalRespuestasMs; // Solo se acumula si el juego es por tiempo
    private Map<CategoriaTrivia, Integer> correctasPorCategoria;
    private int juegosGanados; // Para la sesión/partida actual

    //Constructor que inicializa las estadisticas en cero.

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
     * Solo debe llamarse si el juego esta en modo por tiempo y el jugador respondio.
     * "tiempoMs" el tiempo en milisegundos que tomo la respuesta.
     */

    public void agregarTiempoRespuesta(long tiempoMs) {
        if (tiempoMs > 0) { // Solo agregar tiempos positivos
            this.tiempoTotalRespuestasMs += tiempoMs;
        }
    }

    /**
     * Registra una respuesta correcta para una categoria especifica.
     * "categoria" la categoria de la pregunta respondida correctamente.
     */

    public void registrarRespuestaCorrecta(CategoriaTrivia categoria) {
        if(categoria != null) {
            this.correctasPorCategoria.merge(categoria, 1, Integer::sum);
        }
    }

    //Incrementa el contador de juegos ganados por el jugador.

    public void registrarJuegoGanado() {
        this.juegosGanados++;
    }

    // Getters y Setters para la serializacion/deserializacion con Jackson

    public long getTiempoTotalRespuestasMs() {
        return tiempoTotalRespuestasMs; }

    public void setTiempoTotalRespuestasMs(long tiempoTotalRespuestasMs) {
        this.tiempoTotalRespuestasMs = tiempoTotalRespuestasMs; }

    public Map<CategoriaTrivia, Integer> getCorrectasPorCategoria() {
        return correctasPorCategoria; }

    public void setCorrectasPorCategoria(Map<CategoriaTrivia, Integer> correctasPorCategoria) {
        this.correctasPorCategoria = correctasPorCategoria; }

    public int getJuegosGanados() {
        return juegosGanados; }

    public void setJuegosGanados(int juegosGanados) {
        this.juegosGanados = juegosGanados; }

    //Devuelve una representacion en String de las estadisticas del jugador.

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        double segundosTranscurridos = tiempoTotalRespuestasMs / 1000.0;
        sb.append("  > Tiempo total acumulado en respuestas (en juegos por tiempo): ").append(String.format("%.2f", segundosTranscurridos)).append("s\n");
        sb.append("  > Preguntas respondidas correctamente por categoria:\n");
        if (correctasPorCategoria.values().stream().allMatch(v -> v == 0)) {
            sb.append("    - Ninguna\n");
        } else {
            correctasPorCategoria.forEach((categoria, contador) -> {
                if (contador > 0) { // Mostrar solo si hay alguna correcta
                    sb.append("    - ").append(categoria.getNombreMostrado()).append(": ").append(contador).append("\n");
                }
            });
        }
        sb.append("  > Juegos ganados en esta partida/sesion: ").append(juegosGanados);
        return sb.toString();
    }
}

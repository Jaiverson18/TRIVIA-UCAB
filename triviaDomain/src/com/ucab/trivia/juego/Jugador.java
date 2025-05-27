package com.ucab.trivia.juego;

import com.ucab.trivia.domain.Posicion;

/**
 * Representa a un jugador en el juego TRIVIA-UCAB.
 * Contiene su identificador (correo electrónico), su ficha, su posición actual en el tablero
 * y sus estadísticas de juego.
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.0
 * @since 2025-05-20
 */
public class Jugador {
    private String correoElectronico;
    private Ficha ficha;
    private Posicion posicionActual;
    private EstadisticasJugador estadisticas;

    /**
     * Constructor por defecto para Jackson. Inicializa la ficha y las estadísticas.
     * La posición actual se establecerá al inicio del juego.
     */
    public Jugador() {
        this.ficha = new Ficha();
        this.estadisticas = new EstadisticasJugador();
    }

    /**
     * Constructor para crear un nuevo jugador con su correo electrónico.
     * Inicializa automáticamente una nueva ficha y nuevas estadísticas.
     * @param correoElectronico El correo electrónico que identifica al jugador.
     */
    public Jugador(String correoElectronico) {
        this(); // Llama al constructor por defecto para inicializar ficha y estadísticas
        this.correoElectronico = correoElectronico;
    }

    // Getters y Setters

    /** @return El correo electrónico del jugador. */
    public String getCorreoElectronico() { return correoElectronico; }
    /** @param correoElectronico El nuevo correo electrónico del jugador. */
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    /** @return La ficha del jugador. */
    public Ficha getFicha() { return ficha; }
    /** @param ficha La nueva ficha para el jugador. */
    public void setFicha(Ficha ficha) { this.ficha = ficha; }

    /** @return La posición actual del jugador en el tablero. */
    public Posicion getPosicionActual() { return posicionActual; }
    /** @param posicionActual La nueva posición para el jugador. */
    public void setPosicionActual(Posicion posicionActual) { this.posicionActual = posicionActual; }

    /** @return Las estadísticas del jugador para la partida actual. */
    public EstadisticasJugador getEstadisticas() { return estadisticas; }
    /** @param estadisticas Las nuevas estadísticas para el jugador. */
    public void setEstadisticas(EstadisticasJugador estadisticas) { this.estadisticas = estadisticas; }

    /**
     * Devuelve una representación en String del jugador, incluyendo su correo, posición y ficha.
     * @return Un String que representa al jugador.
     */
    @Override
    public String toString() {
        return "Jugador: " + correoElectronico + " | " +
                (posicionActual != null ? posicionActual.toString() : "Posición Indefinida") + " | " +
                (ficha != null ? ficha.toString() : "Ficha Indefinida");
    }
}
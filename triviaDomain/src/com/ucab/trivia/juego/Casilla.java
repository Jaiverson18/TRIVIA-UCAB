package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;

/**
 * Representa una celda individual en el tablero de TRIVIA-UCAB.
 * Para el tablero hexagonal, esta clase almacena la categoría de la pregunta
 * y si es una casilla especial de "volver a lanzar" o la casilla central.
 * La posición (fila, columna) la maneja el tablero.
 */
public class Casilla {
    private CategoriaTrivia categoria;
    private boolean esReRoll;    // Si la casilla otorga volver a lanzar
    private boolean esCentro;    // Si es la casilla central para ganar
    private String jugadorEnCasilla; // Símbolo del jugador (ej "J1", "J2") o null/vacío si está libre

    /**
     * Constructor por defecto para Jackson o inicialización.
     */
    public Casilla() {
        this.jugadorEnCasilla = " "; // Espacio para casilla vacía en el render
    }

    /**
     * Crea una nueva instancia de Casilla (Celda Hexagonal).
     * @param categoria La categoría de la pregunta asociada. Puede ser null para el centro.
     * @param esReRoll true si la casilla permite volver a lanzar.
     * @param esCentro true si es la casilla central.
     */
    public Casilla(CategoriaTrivia categoria, boolean esReRoll, boolean esCentro) {
        this(); // Llama al constructor por defecto
        this.categoria = categoria;
        this.esReRoll = esReRoll;
        this.esCentro = esCentro;
    }

    // Getters y Setters
    /** @return La categoría de esta casilla. Null si es el centro. */
    public CategoriaTrivia getCategoria() { return categoria; }
    /** @param categoria La nueva categoría para esta casilla. */
    public void setCategoria(CategoriaTrivia categoria) { this.categoria = categoria; }

    /** @return true si la casilla permite volver a lanzar. */
    public boolean isEsReRoll() { return esReRoll; }
    /** @param esReRoll El nuevo estado de si es especial para re-roll. */
    public void setEsReRoll(boolean esReRoll) { this.esReRoll = esReRoll; }

    /** @return true si es la casilla central. */
    public boolean isEsCentro() { return esCentro; }
    /** @param esCentro El nuevo estado de si es la casilla central. */
    public void setEsCentro(boolean esCentro) { this.esCentro = esCentro; }

    /** @return El símbolo del jugador en la casilla, o un espacio si está vacía. */
    public String getJugadorEnCasilla() { return jugadorEnCasilla; }
    /** @param jugadorEnCasilla El nuevo símbolo del jugador (ej "J1") o " " para vaciar. */
    public void setJugadorEnCasilla(String jugadorEnCasilla) { this.jugadorEnCasilla = (jugadorEnCasilla == null || jugadorEnCasilla.trim().isEmpty()) ? " " : jugadorEnCasilla; }

    /**
     * Devuelve un carácter o símbolo corto representando la categoría para el tablero en consola.
     * @return Un String (usualmente una letra) para la categoría.
     */
    public String getSimboloCategoriaConsola() {
        if (esCentro) return "C"; // Centro
        if (categoria == null) return "?"; // Desconocido o especial sin categoría
        switch (categoria) {
            case GEOGRAFIA: return "G";
            case HISTORIA: return "H";
            case DEPORTES_PASATIEMPOS: return "D";
            case CIENCIAS_NATURALEZA: return "N";
            case ARTE_LITERATURA: return "A";
            case ENTRETENIMIENTO: return "E";
            default: return "X";
        }
    }

    @Override
    public String toString() {
        String info = "Cat: " + (categoria != null ? categoria.getNombreMostrado() : (esCentro ? "CENTRO" : "N/A"));
        if (esReRoll) info += " (Re-Roll)";
        return info;
    }
}
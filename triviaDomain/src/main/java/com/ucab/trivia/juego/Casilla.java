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
    private String jugadorEnCasilla; // Símbolo del jugador (ej "J1", "J2") o un espacio si está libre

    /**
     * Constructor por defecto. Inicializa la casilla como vacía.
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
        this(); // Llama al constructor por defecto para inicializar jugadorEnCasilla
        this.categoria = categoria;
        this.esReRoll = esReRoll;
        this.esCentro = esCentro;
    }

    // --- Getters y Setters ---
    public CategoriaTrivia getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTrivia categoria) {
        this.categoria = categoria;
    }

    public boolean isEsReRoll() {
        return esReRoll;
    }

    public void setEsReRoll(boolean esReRoll) {
        this.esReRoll = esReRoll;
    }

    public boolean isEsCentro() {
        return esCentro;
    }

    public void setEsCentro(boolean esCentro) {
        this.esCentro = esCentro;
    }

    public String getJugadorEnCasilla() {
        return jugadorEnCasilla;
    }

    public void setJugadorEnCasilla(String jugadorEnCasilla) {
        this.jugadorEnCasilla = (jugadorEnCasilla == null || jugadorEnCasilla.trim().isEmpty()) ? " " : jugadorEnCasilla;
    }

    /**
     * Devuelve un carácter o símbolo corto representando la categoría para el tablero en consola.
     * @return Un String (usualmente una letra) para la categoría.
     */
    public String getSimboloCategoriaConsola() {
        if (esCentro) return "C"; // Centro
        if (categoria == null) return "?"; // Desconocido o especial sin categoría

        // **INICIO DE LA CORRECCIÓN**
        // Se usa el nombre no calificado del enum en cada case.
        switch (categoria) {
            case GEOGRAFIA: return "G";
            case HISTORIA: return "H";
            case DEPORTES: return "D"; // Usando el nombre corregido del enum
            case CIENCIA: return "N";  // Usando el nombre corregido del enum
            case ARTE_LITERATURA: return "A";
            case ENTRETENIMIENTO: return "E";
            default: return "X";
        }
        // **FIN DE LA CORRECCIÓN**
    }

    @Override
    public String toString() {
        String info = "Cat: " + (categoria != null ? categoria.getNombreMostrado() : (esCentro ? "CENTRO" : "N/A"));
        if (esReRoll) info += " (Re-Roll)";
        return info;
    }
}

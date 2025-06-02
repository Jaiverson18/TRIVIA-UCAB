package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;

/**
 * Representa una celda individual en el tablero de TRIVIA-UCAB.
 * Para el tablero hexagonal, esta clase almacena la categoria de la pregunta
 * y si es una casilla especial de "volver a lanzar" o la casilla central.
 * La posición (fila, columna) la maneja el tablero.
 */
public class Casilla {
    private CategoriaTrivia categoria;
    private boolean esReRoll;    // Si la casilla otorga volver a lanzar
    private boolean esCentro;    // Si es la casilla central para ganar
    private String jugadorEnCasilla; // Simbolo del jugador (ej "J1", "J2") o null si esta libre

    //Constructor por defecto para Jackson o inicializacion.
    public Casilla() {
        this.jugadorEnCasilla = " "; // Espacio para casilla vacia en el render
    }

    /**
     * Crea una nueva instancia de Casilla (Celda Hexagonal).
     * "categoria" La categoria de la pregunta asociada. Puede ser null para el centro.
     * "esReRoll" true si la casilla permite volver a lanzar.
     * "esCentro" true si es la casilla central.
     */

    public Casilla(CategoriaTrivia categoria, boolean esReRoll, boolean esCentro) {
        this(); // Llama al constructor por defecto
        this.categoria = categoria;
        this.esReRoll = esReRoll;
        this.esCentro = esCentro;
    }

    // Getters y Setters
    public CategoriaTrivia getCategoria() {
        return categoria; }

    public void setCategoria(CategoriaTrivia categoria) {
        this.categoria = categoria; }

    public boolean isEsReRoll() {
        return esReRoll; }

    public void setEsReRoll(boolean esReRoll) {
        this.esReRoll = esReRoll; }

    public boolean isEsCentro() {
        return esCentro; }

    public void setEsCentro(boolean esCentro) {
        this.esCentro = esCentro; }

    public String getJugadorEnCasilla() {
        return jugadorEnCasilla; }

    public void setJugadorEnCasilla(String jugadorEnCasilla) {
        this.jugadorEnCasilla = (jugadorEnCasilla == null || jugadorEnCasilla.trim().isEmpty()) ? " " : jugadorEnCasilla; }

    //Devuelve un caracter o simbolo corto representando la categoria para el tablero en consola.

    public String getSimboloCategoriaConsola() {
        if (esCentro) return "C"; // Centro
        if (categoria == null) return "?"; // Desconocido o especial sin categoria
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
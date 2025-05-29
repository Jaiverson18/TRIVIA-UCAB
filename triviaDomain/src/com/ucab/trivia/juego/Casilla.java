package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;

public class Casilla {
    private CategoriaTrivia categoria;
    private boolean esReRoll;    // Si la casilla otorga volver a lanzar
    private boolean esCentro;    // Si es la casilla central para ganar
    private String jugadorEnCasilla; // Símbolo del jugador (ej "J1", "J2") o null/vacío si está libre

    public Casilla() {
        this.jugadorEnCasilla = " "; // Espacio para casilla vacía en el render
    }

    public Casilla(CategoriaTrivia categoria, boolean esReRoll, boolean esCentro) {
        this(); // Llama al constructor por defecto
        this.categoria = categoria;
        this.esReRoll = esReRoll;
        this.esCentro = esCentro;
    }

    // Getters y Setters

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
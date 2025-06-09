package com.ucab.trivia.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Enumeración que representa las categorías de preguntas.
 * Los nombres han sido ajustados para coincidir exactamente con las claves del archivo JSON de preguntas.
 *
 * @author (Tu Nombre/Equipo - Jaiverson)
 * @version 1.1
 * @since 2025-06-02
 */
public enum CategoriaTrivia {
    // Nombres ajustados para coincidir con preguntasJuegoTrivia.json
    GEOGRAFIA("Geografía", "Azul"),
    HISTORIA("Historia", "Amarillo"),
    DEPORTES("Deportes", "Naranja"),
    CIENCIA("Ciencia", "Verde"),
    ARTE_LITERATURA("Arte y Literatura", "Rojo"),
    ENTRETENIMIENTO("Entretenimiento", "Rosado");

    private final String nombreMostrado;
    private final String color;

    CategoriaTrivia(String nombreMostrado, String color) {
        this.nombreMostrado = nombreMostrado;
        this.color = color;
    }

    @JsonValue
    public String getNombreMostrado() {
        return nombreMostrado;
    }

    public String getColor() {
        return color;
    }

    @JsonCreator
    public static CategoriaTrivia fromString(String nombre) {
        return Arrays.stream(values())
                .filter(categoria -> categoria.nombreMostrado.equalsIgnoreCase(nombre) || categoria.name().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoría desconocida: " + nombre));
    }

    @Override
    public String toString() {
        return nombreMostrado;
    }
}
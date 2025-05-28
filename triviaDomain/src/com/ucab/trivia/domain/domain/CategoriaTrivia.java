package com.ucab.trivia.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;


    //Enumeracion que representa las categorias de preguntas en el juego.


public enum CategoriaTrivia {
    GEOGRAFIA("Geografia", "Azul"),
    HISTORIA("Historia", "Amarillo"),
    DEPORTES_PASATIEMPOS("Deportes y pasatiempos", "Naranja"),
    CIENCIAS_NATURALEZA("Ciencias y naturaleza", "Verde"),
    ARTE_LITERATURA("Arte y Literatura", "Rojo"),
    ENTRETENIMIENTO("Entretenimiento", "Rosado");

    private final String nombreMostrado;
    private final String color;

    /**
     * Constructor para la enumeracion CategoriaTrivia.
     * "nombreMostrado" El nombre de la categoria tal como se muestra al usuario.
     * "color" El color asociado a la categoria.
     */

    CategoriaTrivia(String nombreMostrado, String color) {
        this.nombreMostrado = nombreMostrado;
        this.color = color;
    }

    //Retorna el nombre la categoria.

    @JsonValue
    public String getNombreMostrado() {
        return nombreMostrado;
    }

    //Retorna el color de la categoria.

    public String getColor() {
        return color;
    }

    /**
     * Crea una instancia de CategoriaTrivia a partir de un String.
     * Intenta coincidir con el nombreMostrado o el nombre del enum.
     * "nombre" El String que representa la categoria.
     * Retorna la instancia de CategoriaTrivia correspondiente.
     */

    @JsonCreator
    public static CategoriaTrivia fromString(String nombre) {
        return Arrays.stream(values())
                .filter(categoria -> categoria.nombreMostrado.equalsIgnoreCase(nombre) || categoria.name().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Categoria desconocida: " + nombre +
                        ". Valores esperados (ignorando mayusculas/minusculas): " +
                        Arrays.toString(Arrays.stream(values()).map(CategoriaTrivia::getNombreMostrado).toArray())));
    }

    //Devuelve el nombre de la categoria.

    @Override
    public String toString() {
        return nombreMostrado;
    }
}
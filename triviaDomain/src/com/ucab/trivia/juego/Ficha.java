package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa la ficha hexagonal de un jugador, que tiene espacios
 * para cada categoria del juego. El objetivo es rellenar todos los espacios.
 */

public class Ficha {
    private Map<CategoriaTrivia, Boolean> categoriasObtenidas;

    //Constructor de Ficha. Inicializa todos los espacios de categoria como no obtenidos (false).

    public Ficha() {
        this.categoriasObtenidas = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            this.categoriasObtenidas.put(cat, false);
        }
    }

    /**
     * Verifica si una categoria especifica ya ha sido obtenida (marcada como true).
     * "categoria" la categoria a verificar.
     */

    public boolean haObtenidoCategoria(CategoriaTrivia categoria) {
        return categoria != null && this.categoriasObtenidas.getOrDefault(categoria, false);
    }

    /**
     * Marca una categoria como obtenida (true).
     * "categoria" la categoria a marcar. No hace nada si la categoria es null.
     */

    public void marcarCategoriaObtenida(CategoriaTrivia categoria) {
        if (categoria != null) {
            this.categoriasObtenidas.put(categoria, true);
        }
    }

    //Verifica si todos los espacios de la ficha (todas las categorias) han sido obtenidos.

    public boolean estaCompleta() {
        if (categoriasObtenidas.isEmpty()) return false;
        return this.categoriasObtenidas.values().stream().allMatch(obtenida -> obtenida);
    }

    // Getters y Setters para la serializacion/deserializacion con Jackson

    public Map<CategoriaTrivia, Boolean> getCategoriasObtenidas() {
        return categoriasObtenidas; }

    public void setCategoriasObtenidas(Map<CategoriaTrivia, Boolean> categoriasObtenidas) {
        this.categoriasObtenidas = categoriasObtenidas; }

    //Devuelve una representacion en String de la ficha, mostrando las iniciales de las categorias obtenidas.

    @Override
    public String toString() {
        String obtenidasComoString = categoriasObtenidas.entrySet().stream()
                .filter(Map.Entry::getValue) // Solo las categorias obtenidas (true)
                .map(entry -> entry.getKey().getNombreMostrado().substring(0, Math.min(entry.getKey().getNombreMostrado().length(), 3)).toUpperCase())
                .collect(Collectors.joining(" ")); // Une las iniciales con un espacio
        return "Ficha: [" + (obtenidasComoString.isEmpty() ? "VACIA" : obtenidasComoString) + "]";
    }
}
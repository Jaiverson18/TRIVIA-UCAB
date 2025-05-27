package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa la ficha hexagonal de un jugador, que tiene espacios
 * para cada categoría del juego. El objetivo es rellenar todos los espacios.
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.0
 * @since 2025-05-20
 */
public class Ficha {
    private Map<CategoriaTrivia, Boolean> categoriasObtenidas;

    /**
     * Constructor de Ficha. Inicializa todos los espacios de categoría como no obtenidos (false).
     */
    public Ficha() {
        this.categoriasObtenidas = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            this.categoriasObtenidas.put(cat, false);
        }
    }

    /**
     * Verifica si una categoría específica ya ha sido obtenida (marcada como true).
     * @param categoria La categoría a verificar.
     * @return true si la categoría ha sido obtenida, false en caso contrario o si la categoría es null.
     */
    public boolean haObtenidoCategoria(CategoriaTrivia categoria) {
        return categoria != null && this.categoriasObtenidas.getOrDefault(categoria, false);
    }

    /**
     * Marca una categoría como obtenida (true).
     * @param categoria La categoría a marcar. No hace nada si la categoría es null.
     */
    public void marcarCategoriaObtenida(CategoriaTrivia categoria) {
        if (categoria != null) {
            this.categoriasObtenidas.put(categoria, true);
        }
    }

    /**
     * Verifica si todos los espacios de la ficha (todas las categorías) han sido obtenidos.
     * @return true si la ficha está completa, false en caso contrario.
     */
    public boolean estaCompleta() {
        if (categoriasObtenidas.isEmpty()) return false;
        return this.categoriasObtenidas.values().stream().allMatch(obtenida -> obtenida);
    }

    // Getters y Setters para la serialización/deserialización con Jackson

    /** @return El mapa que indica qué categorías han sido obtenidas. */
    public Map<CategoriaTrivia, Boolean> getCategoriasObtenidas() { return categoriasObtenidas; }
    /** @param categoriasObtenidas El nuevo mapa de categorías obtenidas. */
    public void setCategoriasObtenidas(Map<CategoriaTrivia, Boolean> categoriasObtenidas) { this.categoriasObtenidas = categoriasObtenidas; }

    /**
     * Devuelve una representación en String de la ficha, mostrando las iniciales de las categorías obtenidas.
     * @return Un String que representa el estado de la ficha.
     */
    @Override
    public String toString() {
        String obtenidasComoString = categoriasObtenidas.entrySet().stream()
                .filter(Map.Entry::getValue) // Solo las categorías obtenidas (true)
                .map(entry -> entry.getKey().getNombreMostrado().substring(0, Math.min(entry.getKey().getNombreMostrado().length(), 3)).toUpperCase())
                .collect(Collectors.joining(" ")); // Une las iniciales con un espacio
        return "Ficha: [" + (obtenidasComoString.isEmpty() ? "VACÍA" : obtenidasComoString) + "]";
    }
}
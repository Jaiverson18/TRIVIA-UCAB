package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Ficha {
    private Map<CategoriaTrivia, Boolean> categoriasObtenidas;

    public Ficha() {
        this.categoriasObtenidas = new EnumMap<>(CategoriaTrivia.class);
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            this.categoriasObtenidas.put(cat, false);
        }
    }

    public boolean haObtenidoCategoria(CategoriaTrivia categoria) {
        return categoria != null && this.categoriasObtenidas.getOrDefault(categoria, false);
    }
    public void marcarCategoriaObtenida(CategoriaTrivia categoria) {
        if (categoria != null) {
            this.categoriasObtenidas.put(categoria, true);
        }
    }

    /**
     * **NUEVO MÉTODO**
     * Rellena todos los espacios de la ficha, marcando todas las categorías como obtenidas.
     */
    public void rellenarTodasLasCategorias() {
        for (CategoriaTrivia cat : CategoriaTrivia.values()) {
            this.categoriasObtenidas.put(cat, true);
        }
    }

    public boolean estaCompleta() {
        if (categoriasObtenidas.isEmpty()) return false;
        return this.categoriasObtenidas.values().stream().allMatch(obtenida -> obtenida);
    }

    public Map<CategoriaTrivia, Boolean> getCategoriasObtenidas() { return categoriasObtenidas; }
    public void setCategoriasObtenidas(Map<CategoriaTrivia, Boolean> c) { this.categoriasObtenidas = c; }

    @Override
    public String toString() {
        String obtenidasStr = categoriasObtenidas.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> entry.getKey().getNombreMostrado().substring(0, Math.min(entry.getKey().getNombreMostrado().length(), 3)).toUpperCase())
                .collect(Collectors.joining(" "));
        return "Ficha: [" + (obtenidasStr.isEmpty() ? "VACÍA" : obtenidasStr) + "]";
    }
}
package com.ucab.trivia.juego;

import java.security.SecureRandom; // Preferible sobre java.util.Random para aleatoriedad más fuerte

/**
 * Representa un dado de 6 caras utilizado en el juego TRIVIA-UCAB.
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.0
 * @since 2025-05-20
 */
public class Dado {
    private final SecureRandom random;

    /**
     * Constructor que inicializa el generador de números aleatorios.
     */
    public Dado() {
        this.random = new SecureRandom();
    }

    /**
     * Simula el lanzamiento del dado.
     * @return Un número entero aleatorio entre 1 y 6 (ambos inclusive).
     */
    public int lanzar() {
        return random.nextInt(6) + 1;
    }
}
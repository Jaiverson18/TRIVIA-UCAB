package com.ucab.trivia.juego;

import java.security.SecureRandom;

public class Dado {
    private final SecureRandom random;

    public Dado() {
        this.random = new SecureRandom();
    }

    public int lanzar() {
        return random.nextInt(6) + 1;
    }
}

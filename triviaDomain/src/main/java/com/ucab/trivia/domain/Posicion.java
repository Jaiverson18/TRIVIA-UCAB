package com.ucab.trivia.domain;

import java.util.Objects;

/**
 * Representa la posicion de un jugador en el tablero de TRIVIA-UCAB.
 * Una posicion puede ser en el CENTRO, en el CiRCULO (con un indice),
 * o en un RAYO (con un indice de rayo y un indice de profundidad en el rayo).
 */

public class Posicion {

    //Tipo de lugar en el tablero donde puede estar una ficha.

    public enum TipoLugar {
        // El hexagono central del tablero.
        CENTRO,
        // Una de las 42 casillas del perimetro circular.
        CIRCULO,
        // Una de las casillas en los 6 rayos que van hacia el centro.
        RAYO
    }

    private TipoLugar tipo;
    private int indiceCirculo;  // Valido si tipo == CIRCULO (0-41)
    private int indiceRayo;     // Valido si tipo == RAYO (0-5, cual de los 6 rayos)
    private int indiceEnRayo;   // Valido si tipo == RAYO (0-4, profundidad en el rayo, 0 es mas cercano al circulo)

    /**
     * Constructor privado para forzar el uso de los metodos factoria estaticos
     * y asegurar la validez de los indices.
     * "tipo" el tipo de lugar de la posicion.
     */

    private Posicion(TipoLugar tipo) {
        this.tipo = tipo;
    }

    /**
     * Crea una posicion que representa el centro del tablero.
     * Retorna una nueva instancia de Posicion en el CENTRO.
     */

    public static Posicion enCentro() {
        return new Posicion(TipoLugar.CENTRO);
    }

    /**
     * Crea una posición en el circulo del tablero.
     * "indiceCirculo" el indice de la casilla en el circulo (0 a 41).
     */

    public static Posicion enCirculo(int indiceCirculo) {
        if (indiceCirculo < 0 || indiceCirculo >= 42) { // 42 casillas según el PDF
            throw new IllegalArgumentException("Índice de círculo inválido: " + indiceCirculo + ". Debe estar entre 0 y 41.");
        }
        Posicion p = new Posicion(TipoLugar.CIRCULO);
        p.indiceCirculo = indiceCirculo;
        return p;
    }

    /**
     * Crea una posición en un rayo del tablero.
     * "indiceRayo" el indice del rayo (0 a 5).
     * "indiceEnRayo" el indice de la casilla dentro del rayo (0 a 4, donde 0 es la mas cercana al circulo y 4 la mas cercana al centro).
     */

    public static Posicion enRayo(int indiceRayo, int indiceEnRayo) {
        if (indiceRayo < 0 || indiceRayo >= 6) { // 6 rayos
            throw new IllegalArgumentException("Indice de rayo invalido: " + indiceRayo + ". Debe estar entre 0 y 5.");
        }
        // 5 casillas internas del rayo (sin contar la del círculo)
        if (indiceEnRayo < 0 || indiceEnRayo >= 5) {
            throw new IllegalArgumentException("Indice en rayo invalido: " + indiceEnRayo + ". Debe estar entre 0 y 4 (profundidad).");
        }
        Posicion p = new Posicion(TipoLugar.RAYO);
        p.indiceRayo = indiceRayo;
        p.indiceEnRayo = indiceEnRayo;
        return p;
    }

    //Constructor por defecto para la deserializacion con Jackson.

    public Posicion() {
    }

    public TipoLugar getTipo() {
        return tipo; }

    public int getIndiceCirculo() {
        if (tipo != TipoLugar.CIRCULO) throw new IllegalStateException("No es una posicion de circulo para obtener indiceCirculo.");
        return indiceCirculo;
    }

    public int getIndiceRayo() {
        if (tipo != TipoLugar.RAYO) throw new IllegalStateException("No es una posicion de rayo para obtener indiceRayo.");
        return indiceRayo;
    }

    public int getIndiceEnRayo() {
        if (tipo != TipoLugar.RAYO) throw new IllegalStateException("No es una posicion de rayo para obtener indiceEnRayo.");
        return indiceEnRayo;
    }

    // Setters para Jackson
    public void setTipo(TipoLugar tipo) {
        this.tipo = tipo; }

    public void setIndiceCirculo(int indiceCirculo) {
        this.indiceCirculo = indiceCirculo; }

    public void setIndiceRayo(int indiceRayo) {
        this.indiceRayo = indiceRayo; }

    public void setIndiceEnRayo(int indiceEnRayo) {
        this.indiceEnRayo = indiceEnRayo; }


    /**
     * Compara esta Posicion con otro objeto para determinar igualdad.
     * Dos posiciones son iguales si son del mismo tipo y sus indices correspondientes coinciden.
     * "o" El objeto a comparar.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posicion posicion = (Posicion) o;
        if (tipo != posicion.tipo) return false;
        switch (tipo) {
            case CENTRO: return true; // Todas las posiciones de CENTRO son iguales
            case CIRCULO: return indiceCirculo == posicion.indiceCirculo;
            case RAYO: return indiceRayo == posicion.indiceRayo && indiceEnRayo == posicion.indiceEnRayo;
            default: return false; // Tipo desconocido
        }
    }

    //Calcula el código hash para esta posicion.

    @Override
    public int hashCode() {
        switch (tipo) {
            case CENTRO: return Objects.hash(tipo);
            case CIRCULO: return Objects.hash(tipo, indiceCirculo);
            case RAYO: return Objects.hash(tipo, indiceRayo, indiceEnRayo);
            default: return super.hashCode();
        }
    }

    //Devuelve una representacion en String de la posicion.

    @Override
    public String toString() {
        switch (tipo) {
            case CENTRO:
                return "Centro del Tablero";

            case CIRCULO:
                return "Circulo, Casilla " + indiceCirculo;

            case RAYO:
                return "Rayo " + indiceRayo + ", Casilla Interna " + indiceEnRayo + " (profundidad)";

            default:
                return "Posicion Desconocida (Tipo: " + tipo + ")";
        }
    }
}
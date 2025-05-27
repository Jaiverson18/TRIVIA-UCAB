package main.java.com.ucab.trivia.domain;

import java.util.Objects;

/**
 * Representa la posición de un jugador en el tablero de TRIVIA-UCAB.
 * Una posición puede ser en el CENTRO, en el CÍRCULO (con un índice),
 * o en un RAYO (con un índice de rayo y un índice de profundidad en el rayo).
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.0
 * @since 2025-05-20
 */
public class Posicion {

    /**
     * Tipo de lugar en el tablero donde puede estar una ficha.
     */
    public enum TipoLugar {
        /** El hexágono central del tablero. */
        CENTRO,
        /** Una de las 42 casillas del perímetro circular. */
        CIRCULO,
        /** Una de las casillas en los 6 rayos que van hacia el centro. */
        RAYO
    }

    private TipoLugar tipo;
    private int indiceCirculo;  // Válido si tipo == CIRCULO (0-41)
    private int indiceRayo;     // Válido si tipo == RAYO (0-5, cuál de los 6 rayos)
    private int indiceEnRayo;   // Válido si tipo == RAYO (0-4, profundidad en el rayo, 0 es más cercano al círculo)

    /**
     * Constructor privado para forzar el uso de los métodos factoría estáticos
     * y asegurar la validez de los índices.
     * @param tipo El tipo de lugar de la posición.
     */
    private Posicion(TipoLugar tipo) {
        this.tipo = tipo;
    }

    /**
     * Crea una posición que representa el centro del tablero.
     * @return Una nueva instancia de Posicion en el CENTRO.
     */
    public static Posicion enCentro() {
        return new Posicion(TipoLugar.CENTRO);
    }

    /**
     * Crea una posición en el círculo del tablero.
     * @param indiceCirculo El índice de la casilla en el círculo (0 a 41).
     * @return Una nueva instancia de Posicion en el CIRCULO.
     * @throws IllegalArgumentException si el índice del círculo está fuera de rango.
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
     * @param indiceRayo El índice del rayo (0 a 5).
     * @param indiceEnRayo El índice de la casilla dentro del rayo (0 a 4, donde 0 es la más cercana al círculo y 4 la más cercana al centro).
     * @return Una nueva instancia de Posicion en un RAYO.
     * @throws IllegalArgumentException si alguno de los índices está fuera de rango.
     */
    public static Posicion enRayo(int indiceRayo, int indiceEnRayo) {
        if (indiceRayo < 0 || indiceRayo >= 6) { // 6 rayos
            throw new IllegalArgumentException("Índice de rayo inválido: " + indiceRayo + ". Debe estar entre 0 y 5.");
        }
        // 5 casillas internas del rayo (sin contar la del círculo)
        if (indiceEnRayo < 0 || indiceEnRayo >= 5) {
            throw new IllegalArgumentException("Índice en rayo inválido: " + indiceEnRayo + ". Debe estar entre 0 y 4 (profundidad).");
        }
        Posicion p = new Posicion(TipoLugar.RAYO);
        p.indiceRayo = indiceRayo;
        p.indiceEnRayo = indiceEnRayo;
        return p;
    }

    /**
     * Constructor por defecto para la deserialización con Jackson.
     * No se recomienda su uso directo para la creación de instancias.
     */
    public Posicion() {}

    /** @return El tipo de lugar de esta posición (CENTRO, CIRCULO, RAYO). */
    public TipoLugar getTipo() { return tipo; }

    /** @return El índice de la casilla si la posición es de tipo CIRCULO.
     * @throws IllegalStateException si la posición no es de tipo CIRCULO. */
    public int getIndiceCirculo() {
        if (tipo != TipoLugar.CIRCULO) throw new IllegalStateException("No es una posición de círculo para obtener indiceCirculo.");
        return indiceCirculo;
    }
    /** @return El índice del rayo si la posición es de tipo RAYO.
     * @throws IllegalStateException si la posición no es de tipo RAYO. */
    public int getIndiceRayo() {
        if (tipo != TipoLugar.RAYO) throw new IllegalStateException("No es una posición de rayo para obtener indiceRayo.");
        return indiceRayo;
    }
    /** @return El índice de profundidad en el rayo si la posición es de tipo RAYO.
     * @throws IllegalStateException si la posición no es de tipo RAYO. */
    public int getIndiceEnRayo() {
        if (tipo != TipoLugar.RAYO) throw new IllegalStateException("No es una posición de rayo para obtener indiceEnRayo.");
        return indiceEnRayo;
    }

    // Setters para Jackson
    /** @param tipo El nuevo tipo de lugar. */
    public void setTipo(TipoLugar tipo) { this.tipo = tipo; }
    /** @param indiceCirculo El nuevo índice del círculo. */
    public void setIndiceCirculo(int indiceCirculo) { this.indiceCirculo = indiceCirculo; }
    /** @param indiceRayo El nuevo índice del rayo. */
    public void setIndiceRayo(int indiceRayo) { this.indiceRayo = indiceRayo; }
    /** @param indiceEnRayo El nuevo índice de profundidad en el rayo. */
    public void setIndiceEnRayo(int indiceEnRayo) { this.indiceEnRayo = indiceEnRayo; }


    /**
     * Compara esta Posicion con otro objeto para determinar igualdad.
     * Dos posiciones son iguales si son del mismo tipo y sus índices correspondientes coinciden.
     * @param o El objeto a comparar.
     * @return true si las posiciones son iguales, false en caso contrario.
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

    /**
     * Calcula el código hash para esta Posicion.
     * @return El código hash.
     */
    @Override
    public int hashCode() {
        switch (tipo) {
            case CENTRO: return Objects.hash(tipo);
            case CIRCULO: return Objects.hash(tipo, indiceCirculo);
            case RAYO: return Objects.hash(tipo, indiceRayo, indiceEnRayo);
            default: return super.hashCode();
        }
    }

    /**
     * Devuelve una representación en String de la Posicion, útil para debugging.
     * @return Un String que representa la posición.
     */
    @Override
    public String toString() {
        switch (tipo) {
            case CENTRO: return "Centro del Tablero";
            case CIRCULO: return "Círculo, Casilla " + indiceCirculo;
            case RAYO: return "Rayo " + indiceRayo + ", Casilla Interna " + indiceEnRayo + " (profundidad)";
            default: return "Posición Desconocida (Tipo: " + tipo + ")";
        }
    }
}
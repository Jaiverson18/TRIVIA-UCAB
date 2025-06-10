package com.ucab.trivia.juego;

// NO se debe importar com.ucab.trivia.domain.Posicion
// Se importa la clase interna estática CoordenadaHex desde TableroHexagonal
import com.ucab.trivia.juego.TableroHexagonal.CoordenadaHex;

/**
 * Representa a un jugador en el juego TRIVIA-UCAB.
 * Contiene su identificador (correo electrónico), su ficha, su posición actual en el tablero
 * (usando CoordenadaHex) y sus estadísticas de juego.
 */
public class Jugador {
    private String correoElectronico;
    private Ficha ficha;
    private CoordenadaHex posicionActual; // <<--- CAMBIO CLAVE: El tipo ahora es CoordenadaHex
    private EstadisticasJugador estadisticas;

    /**
     * Constructor por defecto para Jackson. Inicializa la ficha y las estadísticas.
     */
    public Jugador() {
        this.ficha = new Ficha();
        this.estadisticas = new EstadisticasJugador();
    }

    /**
     * Constructor para crear un nuevo jugador con su correo electrónico.
     * @param correoElectronico El correo electrónico que identifica al jugador.
     */
    public Jugador(String correoElectronico) {
        this(); // Llama al constructor por defecto para inicializar los objetos internos
        this.correoElectronico = correoElectronico;
    }

    // --- Getters y Setters ---

    public String getCorreoElectronico() {
        return correoElectronico;
    }
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public Ficha getFicha() {
        return ficha;
    }
    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }

    /**
     * Obtiene la posición actual del jugador en el tablero.
     * @return La posición actual del jugador como un objeto CoordenadaHex.
     */
    public CoordenadaHex getPosicionActual() {
        return posicionActual; // <<--- CAMBIO CLAVE: El tipo de retorno ahora es CoordenadaHex
    }

    /**
     * Establece la nueva posición del jugador en el tablero.
     * @param posicionActual La nueva posición del jugador, que debe ser un objeto CoordenadaHex.
     */
    public void setPosicionActual(CoordenadaHex posicionActual) {
        this.posicionActual = posicionActual; // <<--- CAMBIO CLAVE: El tipo del parámetro ahora es CoordenadaHex
    }

    public EstadisticasJugador getEstadisticas() {
        return estadisticas;
    }
    public void setEstadisticas(EstadisticasJugador estadisticas) {
        this.estadisticas = estadisticas;
    }

    @Override
    public String toString() {
        return "Jugador: " + correoElectronico + " | " +
                (posicionActual != null ? posicionActual.toString() : "Posición Indefinida") + " | " +
                (ficha != null ? ficha.toString() : "Ficha Indefinida");
    }
}

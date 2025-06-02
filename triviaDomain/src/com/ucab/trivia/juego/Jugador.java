package com.ucab.trivia.juego;

import com.ucab.trivia.domain.Posicion;

/**
 * Representa a un jugador en el juego TRIVIA-UCAB.
 * Contiene su identificador (correo electronico), su ficha, su posicion actual en el tablero
 * y sus estadisticas de juego.
 */

public class Jugador {
    private String correoElectronico;
    private Ficha ficha;
    private Posicion posicionActual;
    private EstadisticasJugador estadisticas;

    /**
     * Constructor por defecto para Jackson. Inicializa la ficha y las estadisticas.
     * La posicion actual se establecera al inicio del juego.
     */

    public Jugador() {
        this.ficha = new Ficha();
        this.estadisticas = new EstadisticasJugador();
    }

    /**
     * Constructor para crear un nuevo jugador con su correo electronico.
     * Inicializa automaticamente una nueva ficha y nuevas estadisticas.
     * "correoElectronico" El correo electronico que identifica al jugador.
     */

    public Jugador(String correoElectronico) {
        this(); // Llama al constructor por defecto para inicializar ficha y estadisticas
        this.correoElectronico = correoElectronico;
    }

    // Getters y Setters

    public String getCorreoElectronico() {
        return correoElectronico; }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico; }

    public Ficha getFicha() {
        return ficha; }

    public void setFicha(Ficha ficha) {
        this.ficha = ficha; }

    public Posicion getPosicionActual() {
        return posicionActual; }

    public void setPosicionActual(Posicion posicionActual) {
        this.posicionActual = posicionActual; }

    public EstadisticasJugador getEstadisticas() {
        return estadisticas; }

    public void setEstadisticas(EstadisticasJugador estadisticas) {
        this.estadisticas = estadisticas; }

    //Devuelve una representacion en String del jugador, incluyendo su correo, posicion y ficha.

    @Override
    public String toString() {
        return "Jugador: " + correoElectronico + " | " +
                (posicionActual != null ? posicionActual.toString() : "Posicion Indefinida") + " | " +
                (ficha != null ? ficha.toString() : "Ficha Indefinida");
    }
}
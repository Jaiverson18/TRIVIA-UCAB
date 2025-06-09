package com.ucab.trivia.juego;

import java.util.List;

public class EstadoJuegoGuardado {
    private List<Jugador> jugadores;
    private int indiceJugadorActual;
    private boolean modoPorTiempo;
    private int tiempoMaximoGlobalSegundos; // <-- AÑADIDO para guardar el tiempo global
    private int version = 2; // Incrementar versión por cambio de formato

    public EstadoJuegoGuardado() {}

    public EstadoJuegoGuardado(List<Jugador> jugadores, int indiceJugadorActual, boolean modoPorTiempo, int tiempoMaximoGlobalSegundos) {
        this.jugadores = jugadores;
        this.indiceJugadorActual = indiceJugadorActual;
        this.modoPorTiempo = modoPorTiempo;
        this.tiempoMaximoGlobalSegundos = tiempoMaximoGlobalSegundos;
    }

    public List<Jugador> getJugadores() { return jugadores; }
    public void setJugadores(List<Jugador> jugadores) { this.jugadores = jugadores; }

    public int getIndiceJugadorActual() { return indiceJugadorActual; }
    public void setIndiceJugadorActual(int indiceJugadorActual) { this.indiceJugadorActual = indiceJugadorActual; }

    public boolean isModoPorTiempo() { return modoPorTiempo; }
    public void setModoPorTiempo(boolean modoPorTiempo) { this.modoPorTiempo = modoPorTiempo; }

    public int getTiempoMaximoGlobalSegundos() { return tiempoMaximoGlobalSegundos; }
    public void setTiempoMaximoGlobalSegundos(int tiempoMaximoGlobalSegundos) { this.tiempoMaximoGlobalSegundos = tiempoMaximoGlobalSegundos; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
package com.ucab.trivia.juego;

import java.util.List;

public class EstadoJuegoGuardado {
    private List<Jugador> jugadores;
    private int indiceJugadorActual;
    private boolean modoPorTiempo;
    private int versionFormatoGuardado = 1; // Para posible versionado futuro del formato

    //Constructor por defecto necesario para la deserializacion con Jackson.

    public EstadoJuegoGuardado() {}


    //Crea una instancia del estado del juego para ser guardado.
    //jugadores La lista de jugadores con su estado actual.
    //indiceJugadorActual El indice del jugador cuyo turno es el siguiente.
    //modoPorTiempo true si la partida se estaba jugando por tiempo, false en caso contrario.

    public EstadoJuegoGuardado(List<Jugador> jugadores, int indiceJugadorActual, boolean modoPorTiempo) {
        this.jugadores = jugadores;
        this.indiceJugadorActual = indiceJugadorActual;
        this.modoPorTiempo = modoPorTiempo;
    }

    // Getters y Setters necesarios para que Jackson pueda serializar y deserializar los campos.

    //La lista de jugadores de la partida guardada.
    public List<Jugador> getJugadores() { return jugadores; }
    //jugadores La nueva lista de jugadores
    public void setJugadores(List<Jugador> jugadores) { this.jugadores = jugadores; }

    //El indice del jugador que tiene el turno
    public int getIndiceJugadorActual() { return indiceJugadorActual; }
    //indiceJugadorActual El nuevo indice del jugador actual
    public void setIndiceJugadorActual(int indiceJugadorActual) { this.indiceJugadorActual = indiceJugadorActual; }

    //true si la partida guardada era en modo por tiempo
    public boolean isModoPorTiempo() { return modoPorTiempo; } // Jackson usa 'is' para getters booleanos
    //modoPorTiempo El nuevo estado del modo por tiempo.
    public void setModoPorTiempo(boolean modoPorTiempo) { this.modoPorTiempo = modoPorTiempo; }

    //La version del formato de guardado
    public int getVersionFormatoGuardado() { return versionFormatoGuardado; }
    //versionFormatoGuardado La nueva version del formato
    public void setVersionFormatoGuardado(int versionFormatoGuardado) { this.versionFormatoGuardado = versionFormatoGuardado; }
}
package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.juego.TableroHexagonal.CoordenadaHex;
import com.ucab.trivia.domain.PreguntaDetallada;
import com.ucab.trivia.juego.utils.ConsolaUtilJuego;

import java.util.ArrayList;
import java.util.List;


public class Juego {
    private List<Jugador> jugadores;
    private int indiceJugadorActual;
    private TableroHexagonal tablero;
    private Dado dado;
    private ServicioPreguntasJuego servicioPreguntas;
    private ServicioPersistencia servicioPersistencia;
    private boolean modoPorTiempo;
    private Jugador ganador;
    private boolean juegoTerminadoGlobalmente;

    private static final int RADIO_HEXAGONO_TABLERO = 3;


    //Constructor de Juego.

    public Juego() {
        // Los componentes se inicializarán completamente en configurarEIniciar
    }

    //Inicializa los componentes base necesarios para una partida.

    private void inicializarComponentesBase() {
        this.tablero = new TableroHexagonal(RADIO_HEXAGONO_TABLERO);
        this.dado = new Dado();
        this.servicioPreguntas = new ServicioPreguntasJuego();
        this.servicioPersistencia = new ServicioPersistencia();
        this.ganador = null;
        this.juegoTerminadoGlobalmente = false;
        this.jugadores = new ArrayList<>();
    }

    /**
      Configura e inicia una partida de TRIVIA-UCAB.
      correosJugadores Lista de correos para los jugadores de un nuevo juego.
      esModoPorTiempo true si el nuevo juego es por tiempo.
      intentarCargarPartida true para intentar cargar una partida guardada.
     */
    public void configurarEIniciar(List<String> correosJugadores, boolean esModoPorTiempo, boolean intentarCargarPartida) {
        inicializarComponentesBase();
        this.modoPorTiempo = esModoPorTiempo;

        if (intentarCargarPartida && servicioPersistencia.existePartidaGuardada()) {
            EstadoJuegoGuardado estadoCargado = servicioPersistencia.cargarEstadoJuego();
            if (estadoCargado != null && estadoCargado.getJugadores() != null) { // Verificar que jugadores no sea null
                this.jugadores = estadoCargado.getJugadores();
                // Validar y asignar posiciones si es necesario (Jackson podría no reconstruir objetos complejos perfectamente sin config)
                for(Jugador j : this.jugadores){
                    if(j.getPosicionActual() == null && this.tablero != null){ // Si la posición no se cargó bien
                        // Esto es un parche. Idealmente, la serialización de CoordenadaHex es robusta.
                        // Si falla la carga de posición, se reasigna al centro como emergencia.
                        System.err.println("Advertencia: Posición nula para jugador " + j.getCorreoElectronico() + " al cargar. Reasignando al centro.");
                        j.setPosicionActual(this.tablero.getCoordenadaCentro());
                        // Actualizar el tablero para reflejar esta posición si la casilla lo requiere
                        this.tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), j.getPosicionActual());
                    } else if (j.getPosicionActual() != null && this.tablero != null) {
                        this.tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), j.getPosicionActual());
                    }
                    if(j.getFicha() == null) j.setFicha(new Ficha()); // Asegurar que la ficha exista
                    if(j.getEstadisticas() == null) j.setEstadisticas(new EstadisticasJugador()); // Asegurar estadísticas
                }

                this.indiceJugadorActual = estadoCargado.getIndiceJugadorActual();
                this.modoPorTiempo = estadoCargado.isModoPorTiempo();
                ConsolaUtilJuego.mostrarMensaje("\nContinuando partida. Es el turno de: " + this.jugadores.get(this.indiceJugadorActual).getCorreoElectronico());
                ConsolaUtilJuego.presionaEnterParaContinuar();
                iniciarBucleDeJuego();
                return;
            } else {
                ConsolaUtilJuego.mostrarMensaje(">> No se pudo cargar la partida guardada o estaba corrupta. Se iniciará un nuevo juego.");
            }
        }

        if (correosJugadores == null || correosJugadores.isEmpty()) {
            ConsolaUtilJuego.mostrarMensaje(">> No se proporcionaron jugadores para iniciar un nuevo juego. La aplicación terminará.");
            return;
        }
        for (String correo : correosJugadores) {
            this.jugadores.add(new Jugador(correo));
        }
        establecerPosicionesInicialesDeJugadoresYEnTablero();
        determinarPrimerJugadorEnLanzar();
        ConsolaUtilJuego.mostrarMensaje("\nNuevo juego configurado. Comienza el jugador: " + this.jugadores.get(this.indiceJugadorActual).getCorreoElectronico());
        ConsolaUtilJuego.presionaEnterParaContinuar();
        guardarEstadoActualDelJuego();
        iniciarBucleDeJuego();
    }

    //Establece la posición inicial de todos los jugadores en el centro y actualiza el tablero.
    private void establecerPosicionesInicialesDeJugadoresYEnTablero() {
        CoordenadaHex centro = tablero.getCoordenadaCentro();
        for (Jugador j : jugadores) {
            j.setPosicionActual(centro);
            tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), centro);
        }
    }

    //Determina qué jugador comienza la partida lanzando el dado.
    private void determinarPrimerJugadorEnLanzar() {
        if (jugadores.isEmpty()) return;
        if (jugadores.size() == 1) { indiceJugadorActual = 0; return; }
        ConsolaUtilJuego.limpiarConsola();
        ConsolaUtilJuego.mostrarMensaje("--- Determinando el Primer Jugador ---");
        ConsolaUtilJuego.mostrarMensaje("Cada jugador lanzará el dado. El que obtenga el número más alto comenzará.");

        List<Integer> resultadosLanzamientos = new ArrayList<>();
        int lanzamientoMaximo = 0;
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugadorActual = jugadores.get(i);
            ConsolaUtilJuego.mostrarMensaje("\nTurno de lanzar para " + jugadorActual.getCorreoElectronico() + ".");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            int lanzamiento = dado.lanzar();
            ConsolaUtilJuego.mostrarMensaje(jugadorActual.getCorreoElectronico() + " lanzó un: " + lanzamiento + "!");
            resultadosLanzamientos.add(lanzamiento);
            if (lanzamiento > lanzamientoMaximo) {
                lanzamientoMaximo = lanzamiento;
            }
        }
        List<Integer> indicesDeJugadoresConLanzamientoMaximo = new ArrayList<>();
        for (int i = 0; i < resultadosLanzamientos.size(); i++) {
            if (resultadosLanzamientos.get(i) == lanzamientoMaximo) {
                indicesDeJugadoresConLanzamientoMaximo.add(i);
            }
        }
        if (indicesDeJugadoresConLanzamientoMaximo.size() > 1) {
            ConsolaUtilJuego.mostrarMensaje("\nHubo un empate con " + lanzamientoMaximo + "! El primero de los jugadores empatados en la lista de entrada comenzará.");
        }
        indiceJugadorActual = indicesDeJugadoresConLanzamientoMaximo.get(0);
    }

    //Inicia y gestiona el bucle principal del juego.
    private void iniciarBucleDeJuego() {
        ConsolaUtilJuego.mostrarMensaje("DEBUG: Bucle de juego iniciado. Lógica principal de turnos pendiente de implementar en este commit.");

    }

    //Guarda el estado actual del juego si la partida no ha terminado.
    private void guardarEstadoActualDelJuego() {
        if (servicioPersistencia != null && jugadores != null && !jugadores.isEmpty() && !juegoTerminadoGlobalmente) {
            List<Jugador> copiaJugadores = new ArrayList<>();
            for(Jugador j : jugadores){
                Jugador copiaJ = new Jugador(j.getCorreoElectronico());
                copiaJ.setFicha(j.getFicha());
                copiaJ.setEstadisticas(j.getEstadisticas());
                copiaJ.setPosicionActual(j.getPosicionActual());
                copiaJugadores.add(copiaJ);
            }
            EstadoJuegoGuardado estadoActual = new EstadoJuegoGuardado(copiaJugadores, indiceJugadorActual, modoPorTiempo);
            servicioPersistencia.guardarEstadoJuego(estadoActual);
        }
    }


}

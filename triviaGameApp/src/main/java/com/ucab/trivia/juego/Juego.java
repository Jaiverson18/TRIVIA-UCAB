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
    private int tiempoMaximoGlobalSegundos; // <-- AÑADIDO
    private Jugador ganador;
    private boolean juegoTerminadoGlobalmente;

    public Juego() {}

    private void inicializarComponentesBase() {
        this.tablero = new TableroHexagonal(TableroHexagonal.RADIO_HEXAGONO_PREDETERMINADO);
        this.dado = new Dado();
        this.servicioPreguntas = new ServicioPreguntasJuego();
        this.servicioPersistencia = new ServicioPersistencia();
        this.ganador = null;
        this.juegoTerminadoGlobalmente = false;
        this.jugadores = new ArrayList<>();
        this.tiempoMaximoGlobalSegundos = 0; // Se establecerá si es juego por tiempo
    }

    public void configurarEIniciar(List<String> correosJugadores, boolean esModoPorTiempo, int tiempoGlobal) {
        inicializarComponentesBase();
        this.modoPorTiempo = esModoPorTiempo;
        this.tiempoMaximoGlobalSegundos = esModoPorTiempo ? tiempoGlobal : 0;

        // Lógica de carga de partida (simplificada, ya que ahora el tiempo global es parte del estado)
        if (servicioPersistencia.existePartidaGuardada()) {
            String respCarga = ConsolaUtilJuego.leerString("Hay una partida guardada. ¿Cargarla? (S/N)").toUpperCase();
            if (respCarga.equals("S")) {
                EstadoJuegoGuardado estadoCargado = servicioPersistencia.cargarEstadoJuego();
                if (estadoCargado != null && estadoCargado.getJugadores() != null && !estadoCargado.getJugadores().isEmpty()) {
                    this.jugadores = estadoCargado.getJugadores();
                    for(Jugador j : this.jugadores) {
                        if(j.getPosicionActual() != null) this.tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), j.getPosicionActual());
                    }
                    this.indiceJugadorActual = estadoCargado.getIndiceJugadorActual();
                    this.modoPorTiempo = estadoCargado.isModoPorTiempo();
                    this.tiempoMaximoGlobalSegundos = estadoCargado.getTiempoMaximoGlobalSegundos();
                    ConsolaUtilJuego.mostrarMensaje("\nPartida cargada. Turno de: " + this.jugadores.get(this.indiceJugadorActual).getCorreoElectronico());
                    iniciarBucleDeJuego();
                    return;
                } else {
                    ConsolaUtilJuego.mostrarMensaje("No se pudo cargar. Iniciando nuevo juego.");
                }
            }
        }

        for (String correo : correosJugadores) {
            this.jugadores.add(new Jugador(correo));
        }
        establecerPosicionesInicialesDeJugadoresYEnTablero();
        determinarPrimerJugadorEnLanzar();
        ConsolaUtilJuego.mostrarMensaje("\nNuevo juego. Comienza: " + this.jugadores.get(this.indiceJugadorActual).getCorreoElectronico());
        guardarEstadoActualDelJuego();
        iniciarBucleDeJuego();
    }

    // El resto de la lógica de Juego (movimiento, preguntas, etc.) se mantiene, pero usará this.tiempoMaximoGlobalSegundos
    // en lugar de un tiempo por pregunta.

    private void establecerPosicionesInicialesDeJugadoresYEnTablero() {
        // ... (sin cambios)
        CoordenadaHex coordCentro = tablero.getCoordenadaCentro();
        for (Jugador j : jugadores) {
            j.setPosicionActual(coordCentro);
            tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), coordCentro);
        }
    }

    private void determinarPrimerJugadorEnLanzar() {
        // ... (sin cambios)
        if (jugadores.isEmpty() || jugadores.size() == 1) { indiceJugadorActual = 0; return; }
        ConsolaUtilJuego.limpiarConsola();
        ConsolaUtilJuego.mostrarMensaje("--- Determinando el Primer Jugador ---");
        List<Integer> resultados = new ArrayList<>();
        int maxLanzamiento = 0;
        for (Jugador j : jugadores) {
            ConsolaUtilJuego.mostrarMensaje("\nTurno de lanzar para " + j.getCorreoElectronico() + ".");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            int lanzamiento = dado.lanzar();
            ConsolaUtilJuego.mostrarMensaje(j.getCorreoElectronico() + " lanzó un: " + lanzamiento + "!");
            resultados.add(lanzamiento);
            if (lanzamiento > maxLanzamiento) maxLanzamiento = lanzamiento;
        }
        List<Integer> indicesConMax = new ArrayList<>();
        for (int i = 0; i < resultados.size(); i++) {
            if (resultados.get(i) == maxLanzamiento) indicesConMax.add(i);
        }
        if (indicesConMax.size() > 1) {
            ConsolaUtilJuego.mostrarMensaje("\nHubo un empate. El primero de los empatados en la lista comenzará.");
        }
        indiceJugadorActual = indicesConMax.get(0);
    }

    private void iniciarBucleDeJuego() {
        // ... (sin cambios)
        while (!juegoTerminadoGlobalmente) {
            Jugador jugadorConTurno = jugadores.get(indiceJugadorActual);
            ConsolaUtilJuego.limpiarConsola();
            tablero.dibujarTableroConsola();
            ConsolaUtilJuego.mostrarMensaje("==================================================");
            ConsolaUtilJuego.mostrarMensaje("TURNO DE: " + jugadorConTurno.getCorreoElectronico());
            CoordenadaHex posJugadorHex = jugadorConTurno.getPosicionActual();
            Casilla casillaJugadorActual = tablero.getCasilla(posJugadorHex);
            ConsolaUtilJuego.mostrarMensaje("POSICIÓN: " + posJugadorHex + " (Casilla: " + (casillaJugadorActual != null ? casillaJugadorActual.toString() : "N/A") + ")");
            ConsolaUtilJuego.mostrarMensaje(jugadorConTurno.getFicha().toString());
            ConsolaUtilJuego.mostrarMensaje("--------------------------------------------------");

            boolean elTurnoDeEsteJugadorSigue = true;
            while(elTurnoDeEsteJugadorSigue && !juegoTerminadoGlobalmente){
                elTurnoDeEsteJugadorSigue = procesarUnaAccionCompletaDelJugador(jugadorConTurno);
                guardarEstadoActualDelJuego();
            }

            if (!juegoTerminadoGlobalmente) {
                pasarAlSiguienteJugador();
            }
        }
        finalizarPartida();
    }

    private boolean procesarUnaAccionCompletaDelJugador(Jugador jugador) {
        // ... (sin cambios, usa el nuevo método de movimiento de TableroHexagonal)
        CoordenadaHex posActualHex = jugador.getPosicionActual();
        Casilla casillaActualTablero = tablero.getCasilla(posActualHex);

        if (casillaActualTablero != null && casillaActualTablero.isEsCentro()) {
            return manejarLogicaDeAccionEnCentro(jugador);
        }

        ConsolaUtilJuego.mostrarMensaje("Presiona Enter para lanzar el dado.");
        ConsolaUtilJuego.presionaEnterParaContinuar();
        int pasosObtenidos = dado.lanzar();
        ConsolaUtilJuego.mostrarMensaje(jugador.getCorreoElectronico() + " lanzó el dado y obtuvo: " + pasosObtenidos + "!");

        CoordenadaHex posicionPreviaHex = jugador.getPosicionActual();
        tablero.quitarJugadorDeCasilla(posicionPreviaHex);
        CoordenadaHex nuevaPosicionHex;

        if (jugador.getFicha().estaCompleta() && (casillaActualTablero != null && !casillaActualTablero.isEsCentro())) {
            int distanciaNecesariaAlCentro = tablero.distanciaHexagonal(posicionPreviaHex, tablero.getCoordenadaCentro());
            if (pasosObtenidos == distanciaNecesariaAlCentro) {
                ConsolaUtilJuego.mostrarMensaje("¡Tiro exacto ("+pasosObtenidos+") para llegar al centro!");
                nuevaPosicionHex = tablero.getCoordenadaCentro();
            } else {
                ConsolaUtilJuego.mostrarMensaje("Tiro no exacto ("+pasosObtenidos+"). Necesitabas "+distanciaNecesariaAlCentro+" para el centro. No te mueves.");
                tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), posicionPreviaHex);
                return false;
            }
        } else {
            nuevaPosicionHex = tablero.calcularNuevaPosicionEnLinea(posicionPreviaHex, pasosObtenidos);
        }

        jugador.setPosicionActual(nuevaPosicionHex);
        tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), nuevaPosicionHex);
        ConsolaUtilJuego.mostrarMensaje("Te mueves a la posición: " + nuevaPosicionHex);
        tablero.dibujarTableroConsola();

        Casilla casillaDeLlegada = tablero.getCasilla(nuevaPosicionHex);
        if (casillaDeLlegada != null && casillaDeLlegada.isEsCentro()) {
            return manejarLogicaDeAccionEnCentro(jugador);
        }

        if (casillaDeLlegada == null) {
            ConsolaUtilJuego.mostrarMensaje(">> Error interno: La nueva posición es inválida. Turno perdido.");
            return false;
        }

        ConsolaUtilJuego.mostrarMensaje("Has caído en una casilla de " + casillaDeLlegada);
        if (casillaDeLlegada.isEsReRoll()) {
            ConsolaUtilJuego.mostrarMensaje("¡Es una Casilla Especial! Vuelves a lanzar el dado.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            return true;
        } else {
            return manejarLogicaDePreguntaEnCasilla(jugador, casillaDeLlegada);
        }
    }

    private boolean manejarLogicaDeAccionEnCentro(Jugador jugador) {
        // ... (sin cambios)
        ConsolaUtilJuego.mostrarMensaje("Estás en el CENTRO.");
        if (jugador.getFicha().estaCompleta()) {
            ConsolaUtilJuego.mostrarMensaje("¡Tienes la ficha completa! Elige una categoría para la pregunta final y ganar:");
            CategoriaTrivia categoriaFinal = uiSeleccionarCategoriaParaGanar();
            PreguntaDetallada preguntaParaGanar = servicioPreguntas.seleccionarPreguntaAleatoria(categoriaFinal);

            if (preguntaParaGanar == null) {
                ConsolaUtilJuego.mostrarMensaje("¡Fortuna! No hay preguntas para " + categoriaFinal + ". ¡Ganas por defecto!");
                ganador = jugador; juegoTerminadoGlobalmente = true; return false;
            }
            if (uiRealizarPreguntaAlJugador(jugador, preguntaParaGanar)) {
                ConsolaUtilJuego.mostrarMensaje("¡RESPUESTA CORRECTA! ¡FELICIDADES, " + jugador.getCorreoElectronico() + ", HAS GANADO!");
                ganador = jugador; juegoTerminadoGlobalmente = true; return false;
            } else {
                ConsolaUtilJuego.mostrarMensaje("Respuesta incorrecta. Permaneces en el centro. Turno del siguiente.");
                return false;
            }
        } else {
            ConsolaUtilJuego.mostrarMensaje("Necesitas completar tu ficha. Debes salir del centro.");
            int pasosAlSalir = dado.lanzar();
            ConsolaUtilJuego.mostrarMensaje(jugador.getCorreoElectronico() + " lanzó " + pasosAlSalir + " para salir.");

            tablero.quitarJugadorDeCasilla(jugador.getPosicionActual());
            CoordenadaHex nuevaPosicionTrasSalir = tablero.calcularNuevaPosicionEnLinea(jugador.getPosicionActual(), pasosAlSalir);
            jugador.setPosicionActual(nuevaPosicionTrasSalir);
            tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), nuevaPosicionTrasSalir);
            ConsolaUtilJuego.mostrarMensaje("Te mueves a la posición: " + nuevaPosicionTrasSalir);
            tablero.dibujarTableroConsola();
            return true;
        }
    }

    private boolean manejarLogicaDePreguntaEnCasilla(Jugador jugador, Casilla casillaActual){
        // ... (sin cambios)
        if(casillaActual.getCategoria() == null){
            return false;
        }
        PreguntaDetallada preguntaDelTurno = servicioPreguntas.seleccionarPreguntaAleatoria(casillaActual.getCategoria());
        if (preguntaDelTurno == null) {
            ConsolaUtilJuego.mostrarMensaje("No hay preguntas para " + casillaActual.getCategoria() + ". ¡Qué suerte! Sigues jugando.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            return true;
        }
        if (uiRealizarPreguntaAlJugador(jugador, preguntaDelTurno)) {
            ConsolaUtilJuego.mostrarMensaje("¡Respuesta Correcta!");
            if (!jugador.getFicha().haObtenidoCategoria(casillaActual.getCategoria())) {
                jugador.getFicha().marcarCategoriaObtenida(casillaActual.getCategoria());
                ConsolaUtilJuego.mostrarMensaje("¡Has obtenido el 'quesito' de " + casillaActual.getCategoria() + "!");
                ConsolaUtilJuego.mostrarMensaje(jugador.getFicha().toString());
                if (jugador.getFicha().estaCompleta()) {
                    ConsolaUtilJuego.mostrarMensaje("¡FICHA COMPLETA! Ahora debes dirigirte al centro para ganar.");
                }
            }
            ConsolaUtilJuego.mostrarMensaje("Vuelves a lanzar el dado.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            return true;
        } else {
            ConsolaUtilJuego.mostrarMensaje("Respuesta Incorrecta. Turno del siguiente jugador.");
            return false;
        }
    }

    private boolean uiRealizarPreguntaAlJugador(Jugador jugador, PreguntaDetallada pregunta) {
        ConsolaUtilJuego.mostrarMensaje("\nCATEGORÍA: " + pregunta.getCategoria().getNombreMostrado());
        ConsolaUtilJuego.mostrarMensaje("PREGUNTA: " + pregunta.getPregunta());
        String respuestaDelUsuario;
        long tiempoInicio = System.currentTimeMillis();
        if (modoPorTiempo) {
            // **AQUÍ SE USA EL TIEMPO GLOBAL**
            respuestaDelUsuario = ConsolaUtilJuego.leerStringConCronometro("Tu respuesta", this.tiempoMaximoGlobalSegundos);
        } else {
            respuestaDelUsuario = ConsolaUtilJuego.leerString("Tu respuesta");
        }
        long tiempoFin = System.currentTimeMillis();
        if (modoPorTiempo && respuestaDelUsuario != null) {
            jugador.getEstadisticas().agregarTiempoRespuesta(tiempoFin - tiempoInicio);
        }
        if (respuestaDelUsuario == null) return false;
        boolean esCorrecta = respuestaDelUsuario.trim().equalsIgnoreCase(pregunta.getRespuesta().trim());
        if (esCorrecta) jugador.getEstadisticas().registrarRespuestaCorrecta(pregunta.getCategoria());
        return esCorrecta;
    }

    private CategoriaTrivia uiSeleccionarCategoriaParaGanar() {
        // ... (sin cambios)
        ConsolaUtilJuego.mostrarMensaje("Elige la categoría para tu pregunta final:");
        CategoriaTrivia[] todasLasCategorias = CategoriaTrivia.values();
        for (int i = 0; i < todasLasCategorias.length; i++) {
            ConsolaUtilJuego.mostrarMensaje((i + 1) + ". " + todasLasCategorias[i].getNombreMostrado());
        }
        int opcionElegida = ConsolaUtilJuego.leerInt("Opción de Categoría", 1, todasLasCategorias.length);
        return todasLasCategorias[opcionElegida - 1];
    }

    private void pasarAlSiguienteJugador() {
        // ... (sin cambios)
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
    }

    private void guardarEstadoActualDelJuego() {
        if (servicioPersistencia != null && jugadores != null && !jugadores.isEmpty() && !juegoTerminadoGlobalmente) {
            // **AQUÍ SE GUARDA EL TIEMPO GLOBAL**
            EstadoJuegoGuardado estadoActual = new EstadoJuegoGuardado(new ArrayList<>(jugadores), indiceJugadorActual, modoPorTiempo, this.tiempoMaximoGlobalSegundos);
            servicioPersistencia.guardarEstadoJuego(estadoActual);
        }
    }

    private void finalizarPartida() {
        // ... (sin cambios)
        ConsolaUtilJuego.mostrarMensaje("\n--- ESTADÍSTICAS FINALES ---");
        if (ganador != null) {
            ganador.getEstadisticas().registrarJuegoGanado();
        }
        if (jugadores == null || jugadores.isEmpty()) {
            ConsolaUtilJuego.mostrarMensaje("No hay información de jugadores para mostrar.");
            return;
        }
        for (Jugador j : jugadores) {
            ConsolaUtilJuego.mostrarMensaje("\nJugador: " + j.getCorreoElectronico());
            ConsolaUtilJuego.mostrarMensaje(j.getEstadisticas().toString());
        }
        ConsolaUtilJuego.mostrarMensaje("--------------------------------------");
    }
}
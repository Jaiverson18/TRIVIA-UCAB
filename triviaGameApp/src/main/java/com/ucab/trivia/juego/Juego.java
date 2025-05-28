package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.juego.TableroHexagonal.CoordenadaHex; // Usar CoordenadaHex
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

    private static final int RADIO_HEXAGONO_TABLERO = 3; // Radio 3 da 37 casillas (1 centro + 6 + 12 + 18)

    public Juego() {}

    private void inicializarComponentesBase() {
        this.tablero = new TableroHexagonal(RADIO_HEXAGONO_TABLERO);
        this.dado = new Dado();
        this.servicioPreguntas = new ServicioPreguntasJuego();
        this.servicioPersistencia = new ServicioPersistencia();
        this.ganador = null;
        this.juegoTerminadoGlobalmente = false;
        this.jugadores = new ArrayList<>();
    }

    public void configurarEIniciar(List<String> correosJugadores, boolean esModoPorTiempo, boolean intentarCargarPartida) {
        inicializarComponentesBase();
        this.modoPorTiempo = esModoPorTiempo;

        if (intentarCargarPartida && servicioPersistencia.existePartidaGuardada()) {
            EstadoJuegoGuardado estadoCargado = servicioPersistencia.cargarEstadoJuego();
            if (estadoCargado != null && estadoCargado.getJugadores() != null && !estadoCargado.getJugadores().isEmpty()) {
                this.jugadores = estadoCargado.getJugadores();
                for(Jugador j : this.jugadores){ // Restaurar estado transitorio/referencias si es necesario
                    if(j.getPosicionActual() != null) this.tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), j.getPosicionActual());
                    if(j.getFicha() == null) j.setFicha(new Ficha());
                    if(j.getEstadisticas() == null) j.setEstadisticas(new EstadisticasJugador());
                }
                this.indiceJugadorActual = estadoCargado.getIndiceJugadorActual();
                if(this.indiceJugadorActual >= this.jugadores.size() || this.indiceJugadorActual < 0) this.indiceJugadorActual = 0; // Fallback
                this.modoPorTiempo = estadoCargado.isModoPorTiempo();
                ConsolaUtilJuego.mostrarMensaje("\nContinuando partida. Es el turno de: " + this.jugadores.get(this.indiceJugadorActual).getCorreoElectronico());
                ConsolaUtilJuego.presionaEnterParaContinuar();
                iniciarBucleDeJuego();
                return;
            } else {
                ConsolaUtilJuego.mostrarMensaje(">> No se pudo cargar la partida guardada o estaba corrupta/vacia. Se iniciara un nuevo juego.");
            }
        }

        if (correosJugadores == null || correosJugadores.isEmpty()) {
            ConsolaUtilJuego.mostrarMensaje(">> No se proporcionaron jugadores para iniciar un nuevo juego. La aplicacion terminara.");
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

    private void establecerPosicionesInicialesDeJugadoresYEnTablero() {
        CoordenadaHex coordCentro = tablero.getCoordenadaCentro();
        for (Jugador j : jugadores) {
            j.setPosicionActual(coordCentro);
            tablero.colocarJugadorEnCasilla(j.getCorreoElectronico(), coordCentro);
        }
    }

    private void determinarPrimerJugadorEnLanzar() {
        if (jugadores.isEmpty()) return;
        if (jugadores.size() == 1) { indiceJugadorActual = 0; return; }
        ConsolaUtilJuego.limpiarConsola();
        ConsolaUtilJuego.mostrarMensaje("--- Determinando el Primer Jugador ---");
        ConsolaUtilJuego.mostrarMensaje("Cada jugador lanzara el dado. El que obtenga el numero mas alto comenzara.");

        List<Integer> resultadosLanzamientos = new ArrayList<>();
        int lanzamientoMaximo = 0;
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador jugadorActual = jugadores.get(i);
            ConsolaUtilJuego.mostrarMensaje("\nTurno de lanzar para " + jugadorActual.getCorreoElectronico() + ".");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            int lanzamiento = dado.lanzar();
            ConsolaUtilJuego.mostrarMensaje(jugadorActual.getCorreoElectronico() + " lanzo un: " + lanzamiento + "!");
            resultadosLanzamientos.add(lanzamiento);
            if (lanzamiento > lanzamientoMaximo) lanzamientoMaximo = lanzamiento;
        }
        List<Integer> indicesConMax = new ArrayList<>();
        for (int i = 0; i < resultadosLanzamientos.size(); i++) {
            if (resultadosLanzamientos.get(i) == lanzamientoMaximo) indicesConMax.add(i);
        }
        if (indicesConMax.size() > 1) {
            ConsolaUtilJuego.mostrarMensaje("\nHubo un empate con " + lanzamientoMaximo + "! El primero de los jugadores empatados en la lista de entrada comenzara.");
        }
        indiceJugadorActual = indicesConMax.get(0);
    }

    private void iniciarBucleDeJuego() {
        while (!juegoTerminadoGlobalmente) {
            Jugador jugadorConTurno = jugadores.get(indiceJugadorActual);
            ConsolaUtilJuego.limpiarConsola();
            tablero.dibujarTableroConsola(); // Dibujar tablero al inicio de cada turno
            ConsolaUtilJuego.mostrarMensaje("==================================================");
            ConsolaUtilJuego.mostrarMensaje("TURNO DE: " + jugadorConTurno.getCorreoElectronico());
            ConsolaUtilJuego.mostrarMensaje("POSICION: " + jugadorConTurno.getPosicionActual() +
                    " (Casilla: " + (tablero.getCasilla(jugadorConTurno.getPosicionActual()) != null ?
                    tablero.getCasilla(jugadorConTurno.getPosicionActual()).toString() : "CENTRO") + ")");
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
        CoordenadaHex posActualJugador = jugador.getPosicionActual();
        Casilla casillaActualJugador = tablero.getCasilla(posActualJugador);

        if (casillaActualJugador != null && casillaActualJugador.isEsCentro()) {
            return manejarLogicaDeAccionEnCentro(jugador);
        }

        ConsolaUtilJuego.mostrarMensaje("Presiona Enter para lanzar el dado.");
        ConsolaUtilJuego.presionaEnterParaContinuar();
        int pasosObtenidos = dado.lanzar();
        ConsolaUtilJuego.mostrarMensaje(jugador.getCorreoElectronico() + " lanzo el dado y obtuvo: " + pasosObtenidos + "!");

        CoordenadaHex posicionPreviaAlMovimiento = jugador.getPosicionActual();
        // Limpiar la casilla anterior del jugador en el tablero (visual)
        tablero.quitarJugadorDeCasilla(posicionPreviaAlMovimiento);

        CoordenadaHex nuevaPosicionCalculada;

        if (jugador.getFicha().estaCompleta() && (casillaActualJugador != null && !casillaActualJugador.isEsCentro())) { // Intentar llegar al centro
            int distanciaNecesariaAlCentro = tablero.distanciaHexagonal(posicionPreviaAlMovimiento, tablero.getCoordenadaCentro());
            if (pasosObtenidos == distanciaNecesariaAlCentro) {
                ConsolaUtilJuego.mostrarMensaje("¡Tiro exacto ("+pasosObtenidos+") para llegar al centro del tablero!");
                nuevaPosicionCalculada = tablero.getCoordenadaCentro();
            } else {
                ConsolaUtilJuego.mostrarMensaje("Tiro no exacto ("+pasosObtenidos+"). Necesitabas "+distanciaNecesariaAlCentro+" para el centro. No te mueves. Turno del siguiente.");
                tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), posicionPreviaAlMovimiento); // Volver a colocar al jugador
                return false;
            }
        } else { // Movimiento normal
            nuevaPosicionCalculada = tablero.calcularNuevaPosicionPasoAPaso(posicionPreviaAlMovimiento, pasosObtenidos, jugador);
        }

        jugador.setPosicionActual(nuevaPosicionCalculada);
        tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), nuevaPosicionCalculada); // Actualizar tablero visual
        ConsolaUtilJuego.mostrarMensaje("Te mueves a la posicion: " + nuevaPosicionCalculada);
        tablero.dibujarTableroConsola(); // Mostrar tablero actualizado despues del movimiento

        Casilla casillaDeLlegada = tablero.getCasilla(nuevaPosicionCalculada);
        if (casillaDeLlegada != null && casillaDeLlegada.isEsCentro()) {
            return manejarLogicaDeAccionEnCentro(jugador); // Re-evaluar accion en centro
        }

        if (casillaDeLlegada == null) {
            ConsolaUtilJuego.mostrarMensaje(">> Error interno: La nueva posicion no corresponde a una casilla valida. Turno perdido.");
            return false;
        }

        ConsolaUtilJuego.mostrarMensaje("Has caido en una casilla de " + casillaDeLlegada);
        if (casillaDeLlegada.isEsReRoll()) {
            ConsolaUtilJuego.mostrarMensaje("¡Es una Casilla Especial! Vuelves a lanzar el dado.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            return true;
        } else {
            return manejarLogicaDePreguntaEnCasilla(jugador, casillaDeLlegada);
        }
    }

    private boolean manejarLogicaDeAccionEnCentro(Jugador jugador) {
        ConsolaUtilJuego.mostrarMensaje("Estas en el CENTRO.");
        if (jugador.getFicha().estaCompleta()) {
            ConsolaUtilJuego.mostrarMensaje("¡Tienes la ficha completa! Elige una categoria para la pregunta final y ganar:");
            CategoriaTrivia categoriaFinal = uiSeleccionarCategoriaParaGanar();
            PreguntaDetallada preguntaParaGanar = servicioPreguntas.seleccionarPreguntaAleatoria(categoriaFinal);

            if (preguntaParaGanar == null) {
                ConsolaUtilJuego.mostrarMensaje("¡Fortuna! No hay preguntas para " + categoriaFinal + ". ¡Ganas por defecto!");
                ganador = jugador; juegoTerminadoGlobalmente = true; return false;
            }
            if (uiRealizarPreguntaAlJugador(jugador, preguntaParaGanar)) {
                ConsolaUtilJuego.mostrarMensaje("¡RESPUESTA CORRECTA! ¡FELICIDADES, " + jugador.getCorreoElectronico() + ", HAS GANADO TRIVIA-UCAB!");
                ganador = jugador; juegoTerminadoGlobalmente = true; return false;
            } else {
                ConsolaUtilJuego.mostrarMensaje("Respuesta incorrecta. Permaneces en el centro. Turno del siguiente.");
                return false;
            }
        } else {
            ConsolaUtilJuego.mostrarMensaje("Necesitas completar tu ficha antes de poder ganar. Debes salir del centro.");
            ConsolaUtilJuego.mostrarMensaje("Presiona Enter para lanzar el dado y salir del centro.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            int pasosAlSalir = dado.lanzar();
            ConsolaUtilJuego.mostrarMensaje(jugador.getCorreoElectronico() + " lanzo " + pasosAlSalir + ".");

            // Para salir del centro, el jugador elige una de las 6 direcciones "vecinas"
            List<CoordenadaHex> vecinosDelCentro = tablero.getVecinosValidos(tablero.getCoordenadaCentro());
            if (vecinosDelCentro.isEmpty()) {
                ConsolaUtilJuego.mostrarMensaje("Error: El centro no tiene salidas. Fin del turno."); return false;
            }
            ConsolaUtilJuego.mostrarMensaje("Elige una direccion para salir del centro:");
            for(int i=0; i<vecinosDelCentro.size(); i++){
                ConsolaUtilJuego.mostrarMensaje((i+1) + ". Hacia " + vecinosDelCentro.get(i));
            }
            int dirElegidaIdx = ConsolaUtilJuego.leerInt("Direccion (1-" + vecinosDelCentro.size() + ")", 1, vecinosDelCentro.size()) -1;
            CoordenadaHex casillaIntermediaSalida = vecinosDelCentro.get(dirElegidaIdx);


            // Limpiar la posicion anterior (centro)
            tablero.quitarJugadorDeCasilla(jugador.getPosicionActual());
            CoordenadaHex nuevaPosicionTrasSalir = tablero.calcularNuevaPosicionPasoAPaso(casillaIntermediaSalida, pasosAlSalir - 1, jugador);
            jugador.setPosicionActual(nuevaPosicionTrasSalir);
            tablero.colocarJugadorEnCasilla(jugador.getCorreoElectronico(), nuevaPosicionTrasSalir);
            ConsolaUtilJuego.mostrarMensaje("Te mueves a la posicion: " + nuevaPosicionTrasSalir);
            tablero.dibujarTableroConsola();
            return true; // Debe actuar en la nueva casilla
        }
    }

    private boolean manejarLogicaDePreguntaEnCasilla(Jugador jugador, Casilla casillaActual){
        if(casillaActual.getCategoria() == null){ // Podria ser el centro si la logica de arriba falla en redirigir
            ConsolaUtilJuego.mostrarMensaje("Casilla sin categoria (posiblemente el centro, manejado incorrectamente). Turno perdido.");
            return false;
        }
        PreguntaDetallada preguntaDelTurno = servicioPreguntas.seleccionarPreguntaAleatoria(casillaActual.getCategoria());
        if (preguntaDelTurno == null) {
            ConsolaUtilJuego.mostrarMensaje("No hay preguntas disponibles para la categoria " + casillaActual.getCategoria() +
                    ". ¡Que suerte! Se considera como si hubieras acertado para seguir jugando.");
            ConsolaUtilJuego.mostrarMensaje("Vuelves a lanzar el dado.");
            ConsolaUtilJuego.presionaEnterParaContinuar();
            return true;
        }
        if (uiRealizarPreguntaAlJugador(jugador, preguntaDelTurno)) {
            ConsolaUtilJuego.mostrarMensaje("¡Respuesta Correcta!");
            if (!jugador.getFicha().haObtenidoCategoria(casillaActual.getCategoria())) {
                jugador.getFicha().marcarCategoriaObtenida(casillaActual.getCategoria());
                ConsolaUtilJuego.mostrarMensaje("¡Has obtenido el 'quesito' de la categoria " + casillaActual.getCategoria() + "!");
                ConsolaUtilJuego.mostrarMensaje(jugador.getFicha().toString());
                if (jugador.getFicha().estaCompleta()) {
                    ConsolaUtilJuego.mostrarMensaje("¡HAS COMPLETADO TU FICHA! Ahora tu objetivo es llegar al centro para ganar.");
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
        ConsolaUtilJuego.mostrarMensaje("\nCATEGORIA: " + pregunta.getCategoria().getNombreMostrado());
        ConsolaUtilJuego.mostrarMensaje("PREGUNTA: " + pregunta.getPregunta());
        String respuestaDelUsuario;
        long tiempoInicio = System.currentTimeMillis();

        if (modoPorTiempo) {
            respuestaDelUsuario = ConsolaUtilJuego.leerStringConCronometro("Tu respuesta", pregunta.getTiempoMaximoRespuestaSegundos());
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
        ConsolaUtilJuego.mostrarMensaje("Elige la categoria para tu pregunta final:");
        CategoriaTrivia[] todasLasCategorias = CategoriaTrivia.values();
        for (int i = 0; i < todasLasCategorias.length; i++) {
            ConsolaUtilJuego.mostrarMensaje((i + 1) + ". " + todasLasCategorias[i].getNombreMostrado());
        }
        int opcionElegida = ConsolaUtilJuego.leerInt("Opcion de Categoria", 1, todasLasCategorias.length);
        return todasLasCategorias[opcionElegida - 1];
    }

    private void pasarAlSiguienteJugador() {
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
    }

    private void guardarEstadoActualDelJuego() {
        if (servicioPersistencia != null && jugadores != null && !jugadores.isEmpty() && !juegoTerminadoGlobalmente) {
            List<Jugador> copiaJugadores = new ArrayList<>();
            for(Jugador jOriginal : jugadores) { // Crear copias para evitar problemas con referencias de Jackson
                Jugador jCopia = new Jugador(jOriginal.getCorreoElectronico());
                // Copiar estado de Ficha
                Ficha fichaCopia = new Ficha();
                if (jOriginal.getFicha() != null && jOriginal.getFicha().getCategoriasObtenidas() != null) {
                    fichaCopia.setCategoriasObtenidas(new EnumMap<>(jOriginal.getFicha().getCategoriasObtenidas()));
                }
                jCopia.setFicha(fichaCopia);
                // Copiar estado de Estadisticas
                EstadisticasJugador estCopia = new EstadisticasJugador();
                if(jOriginal.getEstadisticas() != null) {
                    estCopia.setTiempoTotalRespuestasMs(jOriginal.getEstadisticas().getTiempoTotalRespuestasMs());
                    if (jOriginal.getEstadisticas().getCorrectasPorCategoria() != null) {
                        estCopia.setCorrectasPorCategoria(new EnumMap<>(jOriginal.getEstadisticas().getCorrectasPorCategoria()));
                    }
                    estCopia.setJuegosGanados(jOriginal.getEstadisticas().getJuegosGanados());
                }
                jCopia.setEstadisticas(estCopia);
                // Copiar Posicion (CoordenadaHex)
                if (jOriginal.getPosicionActual() != null) {
                    jCopia.setPosicionActual(new CoordenadaHex(jOriginal.getPosicionActual().fila, jOriginal.getPosicionActual().col));
                }
                copiaJugadores.add(jCopia);
            }
            EstadoJuegoGuardado estadoActual = new EstadoJuegoGuardado(copiaJugadores, indiceJugadorActual, modoPorTiempo);
            servicioPersistencia.guardarEstadoJuego(estadoActual);
        }
    }

    private void finalizarPartida() {
        ConsolaUtilJuego.mostrarMensaje("\n--- ESTADISTICAS FINALES DE LA PARTIDA ---");
        if (jugadores == null || jugadores.isEmpty()) {
            ConsolaUtilJuego.mostrarMensaje("No hay informacion de jugadores para mostrar estadisticas.");
            return;
        }
        for (Jugador j : jugadores) {
            ConsolaUtilJuego.mostrarMensaje("\nEstadisticas para: " + j.getCorreoElectronico());
            ConsolaUtilJuego.mostrarMensaje(j.getEstadisticas().toString());
        }
        ConsolaUtilJuego.mostrarMensaje("---------------------------------------------");
    }
}

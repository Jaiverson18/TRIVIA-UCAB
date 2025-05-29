package com.ucab.trivia.juego;

import com.ucab.trivia.juego.utils.ConsolaUtilJuego;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AppJuego {

    // Patron simple para validar el formato de un correo electronico.
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    //Valida si un string tiene un formato de correo electronico simple.
    private static boolean esFormatoCorreoValido(String correo) {
        return correo != null && PATRON_EMAIL.matcher(correo).matches();
    }

    //Punto de entrada principal de la aplicacion del juego.

    public static void main(String[] args) {
        ConsolaUtilJuego.limpiarConsola();
        ConsolaUtilJuego.mostrarMensaje("===================================");
        ConsolaUtilJuego.mostrarMensaje("¡BIENVENIDO A TRIVIA-UCAB EL JUEGO!");
        ConsolaUtilJuego.mostrarMensaje("===================================");

        boolean intentarCargarPartidaGuardada = false;
        ServicioPersistencia servicioDePersistencia = new ServicioPersistencia(); // Instancia para verificar si existe partida
        if (servicioDePersistencia.existePartidaGuardada()) {
            String respuestaCarga = ConsolaUtilJuego.leerString("Se encontro una partida guardada previamente. ¿Desea cargarla? (S/N)").toUpperCase();
            if (respuestaCarga.equals("S")) {
                intentarCargarPartidaGuardada = true;
            }
        }

        List<String> correosDeLosJugadores = new ArrayList<>();
        boolean elJuegoSeraModoPorTiempo = false;

        // Solo pedir configuracion para un nuevo juego si no se va a cargar una partida
        if (!intentarCargarPartidaGuardada) {
            int numeroDeJugadores = ConsolaUtilJuego.leerInt("Ingrese el numero de jugadores para esta partida (1-6)", 1, 6);
            Set<String> correosIngresados = new HashSet<>(); // Para evitar correos duplicados

            for (int i = 0; i < numeroDeJugadores; i++) {
                String correoJugador;
                boolean correoValidoYUnico;
                do {
                    correoJugador = ConsolaUtilJuego.leerString("Correo electronico del Jugador " + (i + 1) + " (ej: usuario@dominio.com)");
                    if (!esFormatoCorreoValido(correoJugador)) {
                        ConsolaUtilJuego.mostrarMensaje(">> Formato de correo electronico invalido. Por favor, intente de nuevo.");
                        correoValidoYUnico = false;
                    } else if (correosIngresados.contains(correoJugador.toLowerCase())) {
                        ConsolaUtilJuego.mostrarMensaje(">> Este correo electronico ya ha sido ingresado para otro jugador. Use uno diferente.");
                        correoValidoYUnico = false;
                    }
                    else {
                        correoValidoYUnico = true;
                    }
                } while (!correoValidoYUnico);
                correosDeLosJugadores.add(correoJugador);
                correosIngresados.add(correoJugador.toLowerCase()); // Guardar en minusculas para comparacion sin case-sensitive
            }

            String respuestaModoPorTiempo = ConsolaUtilJuego.leerString("¿Desea jugar esta partida en modo por tiempo (con cronometro para preguntas)? (S/N)").toUpperCase();
            if (respuestaModoPorTiempo.equals("S")) {
                elJuegoSeraModoPorTiempo = true;
            }
        }

        // Crear e iniciar el juego
        Juego triviaJuego = new Juego();
        // Si se intenta cargar, la lista `correosDeLosJugadores` estara vacia,
        // y `elJuegoSeraModoPorTiempo` no se usara si la carga es exitosa, ya que se tomaran los de la partida guardada.
        triviaJuego.configurarEIniciar(correosDeLosJugadores, elJuegoSeraModoPorTiempo, intentarCargarPartidaGuardada);

        ConsolaUtilJuego.mostrarMensaje("\n===================================");
        ConsolaUtilJuego.mostrarMensaje("Gracias por jugar TRIVIA-UCAB. ¡Hasta la proxima!");
        ConsolaUtilJuego.mostrarMensaje("===================================");
        ConsolaUtilJuego.presionaEnterParaContinuar(); // Para que la ventana no se cierre inmediatamente si se ejecuta desde un JAR simple.
    }
}
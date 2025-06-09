package com.ucab.trivia.juego;

import com.ucab.trivia.juego.utils.ConsolaUtilJuego;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AppJuego {
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static boolean esFormatoCorreoValido(String correo) {
        return correo != null && PATRON_EMAIL.matcher(correo).matches();
    }

    public static void main(String[] args) {
        ConsolaUtilJuego.limpiarConsola();
        ConsolaUtilJuego.mostrarMensaje("===================================");
        ConsolaUtilJuego.mostrarMensaje("¡BIENVENIDO A TRIVIA-UCAB EL JUEGO!");
        ConsolaUtilJuego.mostrarMensaje("===================================");

        Juego triviaJuego = new Juego();
        List<String> correos = new ArrayList<>();
        boolean esPorTiempo = false;
        int tiempoGlobal = 0;

        // La lógica para cargar partida ahora está dentro de configurarEIniciar,
        // pero podemos preguntar antes para decidir si pedimos configuración de nuevo juego.
        ServicioPersistencia sp = new ServicioPersistencia();
        boolean cargar = false;
        if (sp.existePartidaGuardada()) {
            String respCarga = ConsolaUtilJuego.leerString("Hay una partida guardada. ¿Cargarla? (S/N)").toUpperCase();
            if (respCarga.equals("S")) {
                cargar = true;
            }
        }

        if (!cargar) {
            int numJug = ConsolaUtilJuego.leerInt("Ingrese el número de jugadores (1-6)", 1, 6);
            Set<String> correosIngresados = new HashSet<>();
            for (int i = 0; i < numJug; i++) {
                String correo;
                do {
                    correo = ConsolaUtilJuego.leerString("Correo del Jugador " + (i + 1));
                    if (!esFormatoCorreoValido(correo)) {
                        ConsolaUtilJuego.mostrarMensaje(">> Formato de correo inválido. Intente de nuevo.");
                    } else if (correosIngresados.contains(correo.toLowerCase())) {
                        ConsolaUtilJuego.mostrarMensaje(">> Este correo ya ha sido ingresado. Use uno diferente.");
                    }
                } while (!esFormatoCorreoValido(correo) || correosIngresados.contains(correo.toLowerCase()));
                correos.add(correo);
                correosIngresados.add(correo.toLowerCase());
            }

            String respTiempo = ConsolaUtilJuego.leerString("¿Desea jugar por tiempo? (S/N)").toUpperCase();
            if (respTiempo.equals("S")) {
                esPorTiempo = true;
                tiempoGlobal = ConsolaUtilJuego.leerInt("Ingrese el tiempo máximo global para responder cada pregunta (en segundos)", 10, 60);
            }
        }

        triviaJuego.configurarEIniciar(correos, esPorTiempo, tiempoGlobal);

        ConsolaUtilJuego.mostrarMensaje("\nGracias por jugar TRIVIA-UCAB. ¡Hasta la próxima!");
    }
}
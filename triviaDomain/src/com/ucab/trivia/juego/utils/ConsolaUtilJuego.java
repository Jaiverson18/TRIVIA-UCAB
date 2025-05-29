package com.ucab.trivia.juego.utils;

import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Utilidades para la interacción por consola en la aplicación del juego.
 * Incluye métodos para leer entradas y un intento de cronómetro en consola.
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.0
 * @since 2025-05-20
 */
public class ConsolaUtilJuego {
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");

    /**
     * Lee una línea de texto ingresada por el usuario.
     * @param mensaje El mensaje a mostrar antes de la entrada.
     * @return El String ingresado.
     */
    public static String leerString(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine();
    }

    /**
     * Lee un entero del usuario, validando que esté en un rango.
     * @param mensaje El mensaje a mostrar.
     * @param min El valor mínimo permitido.
     * @param max El valor máximo permitido.
     * @return El entero válido ingresado.
     */
    public static int leerInt(String mensaje, int min, int max) {
        int numero;
        while (true) {
            System.out.print(mensaje + " (" + min + "-" + max + "): ");
            String linea = scanner.nextLine();
            try {
                numero = Integer.parseInt(linea);
                if (numero >= min && numero <= max) {
                    break;
                } else {
                    System.out.println(">> Error: El número debe estar entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Entrada inválida. Por favor, ingrese un número entero.");
            }
        }
        return numero;
    }

    /**
     * Muestra un mensaje en la consola.
     * @param mensaje El mensaje a mostrar.
     */
    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * Pausa la ejecución y espera que el usuario presione Enter.
     */
    public static void presionaEnterParaContinuar() {
        System.out.print("\n>> Presiona Enter para continuar...");
        scanner.nextLine();
    }

    /**
     * Lee una cadena de texto del usuario con un cronómetro visible en la consola.
     * El cronómetro actualiza la misma línea.
     * @param mensaje El mensaje a mostrar al usuario antes de la entrada.
     * @param segundosTimeout El tiempo máximo en segundos para esperar la entrada.
     * @return El String ingresado por el usuario, o null si se agota el tiempo o hay interrupción.
     */
    public static String leerStringConCronometro(String mensaje, int segundosTimeout) {
        ConsolaUtilJuego.mostrarMensaje(mensaje); // Muestra el mensaje principal de la pregunta una vez.

        BlockingQueue<String> entradaDelUsuario = new ArrayBlockingQueue<>(1);
        // Hilo para leer la entrada del usuario de forma no bloqueante para el cronómetro
        Thread hiloLectorEntrada = new Thread(() -> {
            try {
                if (System.in.available() > 0) { // Limpiar buffer residual si es necesario (puede no ser efectivo en todas las consolas)
                    while(System.in.available() > 0) System.in.read();
                }
                String input = scanner.nextLine();
                entradaDelUsuario.offer(input); // Usar offer para no bloquear si el hilo principal ya terminó
            } catch (Exception e) {
                // Puede ser una NoSuchElementException si el scanner se cierra o IllegalStateException
                // No es crítico si el hilo principal ya ha terminado por timeout
            }
        });
        hiloLectorEntrada.setDaemon(true); // Permite que el programa termine aunque este hilo esté activo
        hiloLectorEntrada.start();

        String resultadoFinal = null;
        boolean tiempoExpirado = false;

        try {
            for (int i = segundosTimeout; i >= 0; i--) {
                System.out.print("\rTiempo restante: " + String.format("%02d", i) + "s  "); // Espacios al final para limpiar caracteres previos

                if (i == 0) { // Último segundo, ya no esperamos más por la entrada
                    tiempoExpirado = true;
                    break;
                }

                resultadoFinal = entradaDelUsuario.poll(1, TimeUnit.SECONDS); // Espera 1 segundo por la entrada
                if (resultadoFinal != null) { // El usuario ingresó algo
                    break; // Salir del bucle del cronómetro
                }
                // Si resultadoFinal es null, el poll timeouteó después de 1s, continuar cronómetro
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablecer estado de interrupción
            System.out.print("\r>> Cronómetro interrumpido.         ");
            // resultadoFinal seguirá siendo null o lo que se haya obtenido.
        } finally {
            System.out.print("\r                                \r"); // Limpiar completamente la línea del contador
            if (hiloLectorEntrada.isAlive()) {
                hiloLectorEntrada.interrupt(); // Interrumpir el hilo lector si todavía está activo
            }
        }

        if (tiempoExpirado && resultadoFinal == null) { // Se cumplió el tiempo y no hubo entrada
            ConsolaUtilJuego.mostrarMensaje("¡TIEMPO AGOTADO!");
        }
        return resultadoFinal; // Puede ser null si hubo timeout o interrupción sin entrada válida
    }

    /**
     * "Limpia" la consola imprimiendo múltiples líneas nuevas.
     */
    public static void limpiarConsola() {
        // Solución simple y portable
        for (int i = 0; i < 40; ++i) System.out.println();
    }
}

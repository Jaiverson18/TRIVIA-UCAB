package com.ucab.trivia.juego.utils;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Utilidades para la interacción por consola en la aplicación del juego.
 * Incluye métodos para leer entradas y un cronómetro funcional en consola.
 *
 * @author (Tu Nombre/Equipo - Luis)
 * @version 1.1 // Versión actualizada con corrección de cronómetro
 * @since 2025-06-02
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
     * Utiliza un hilo separado para la entrada y un bucle en el hilo principal para el conteo.
     * @param mensaje El mensaje a mostrar al usuario antes de la entrada.
     * @param segundosTimeout El tiempo máximo en segundos para esperar la entrada.
     * @return El String ingresado por el usuario, o null si se agota el tiempo o hay una interrupción.
     */
    public static String leerStringConCronometro(String mensaje, int segundosTimeout) {
        System.out.println(mensaje);
        BlockingQueue<String> entradaCola = new ArrayBlockingQueue<>(1);

        // Hilo para leer la entrada del usuario sin bloquear el cronómetro
        Thread hiloLector = new Thread(() -> {
            try {
                String input = scanner.nextLine();
                entradaCola.put(input); // Espera si es necesario, pero se interrumpirá si el tiempo se acaba
            } catch (InterruptedException e) {
                // Esto es esperado si el tiempo se acaba y el hilo principal interrumpe a este
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Ignorar otras excepciones como NoSuchElementException si el scanner se cierra
            }
        });
        hiloLector.setDaemon(true); // Permite que el programa termine aunque este hilo esté bloqueado
        hiloLector.start();

        String resultado = null;
        try {
            for (int i = segundosTimeout; i >= 0; i--) {
                System.out.print("\rTiempo restante: " + String.format("%02d", i) + "s  "); // Espacios para limpiar la línea

                // Intenta obtener la entrada de la cola, esperando hasta 1 segundo
                resultado = entradaCola.poll(1, TimeUnit.SECONDS);

                if (resultado != null) {
                    // El usuario ingresó algo, salir del bucle
                    break;
                }

                if (i == 0) {
                    // Se acabó el tiempo y el poll del último segundo no devolvió nada
                    System.out.print("\r¡Tiempo agotado!                   ");
                    hiloLector.interrupt(); // Interrumpir el hilo que está esperando en scanner.nextLine()
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.print("\rCronómetro interrumpido.         ");
        }

        System.out.println(); // Moverse a la siguiente línea después del cronómetro
        return resultado; // Devolverá el input del usuario o null si hubo timeout/interrupción
    }

    /**
     * "Limpia" la consola imprimiendo múltiples líneas nuevas.
     */
    public static void limpiarConsola() {
        for (int i = 0; i < 40; ++i) System.out.println();
    }
}
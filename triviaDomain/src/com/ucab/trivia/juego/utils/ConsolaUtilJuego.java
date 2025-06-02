package com.ucab.trivia.juego.utils;

import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Utilidades para la interaccion por consola en la aplicacion del juego.
 * Incluye metodos para leer entradas y un intento de cronometro en consola.
 */
public class ConsolaUtilJuego {
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");

    /**
     * Lee una linea de texto ingresada por el usuario.
     * "mensaje" el mensaje a mostrar antes de la entrada.
     */
    public static String leerString(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine();
    }

    /**
     * Lee un entero del usuario, validando que este en un rango.
     * "mensaje" el mensaje a mostrar.
     * "min" el valor minimo permitido.
     * "max" el valor maximo permitido.
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
                    System.out.println(">> Error: El numero debe estar entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Entrada invalida. Por favor, ingrese un numero entero.");
            }
        }
        return numero;
    }

    /**
     * Muestra un mensaje en la consola.
     * "mensaje" El mensaje a mostrar.
     */
    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    //Pausa la ejecucion y espera que el usuario presione Enter.
    public static void presionaEnterParaContinuar() {
        System.out.print("\n>> Presiona Enter para continuar...");
        scanner.nextLine();
    }

    /**
     * Lee una cadena de texto del usuario con un cronometro visible en la consola.
     * El cronometro actualiza la misma linea.
     * "mensaje" el mensaje a mostrar al usuario antes de la entrada.
     * "segundosTimeout" el tiempo maximo en segundos para esperar la entrada.
     * */
    public static String leerStringConCronometro(String mensaje, int segundosTimeout) {
        ConsolaUtilJuego.mostrarMensaje(mensaje); // Muestra el mensaje principal de la pregunta una vez.

        BlockingQueue<String> entradaDelUsuario = new ArrayBlockingQueue<>(1);
        // Hilo para leer la entrada del usuario de forma no bloqueante para el cronometro
        Thread hiloLectorEntrada = new Thread(() -> {
            try {
                if (System.in.available() > 0) { // Limpiar buffer residual si es necesario (puede no ser efectivo en todas las consolas)
                    while(System.in.available() > 0) System.in.read();
                }
                String input = scanner.nextLine();
                entradaDelUsuario.offer(input); // Usar offer para no bloquear si el hilo principal ya termino
            } catch (Exception e) {
                // Puede ser una NoSuchElementException si el scanner se cierra o IllegalStateException
                // No es critico si el hilo principal ya ha terminado por timeout
            }
        });
        hiloLectorEntrada.setDaemon(true); // Permite que el programa termine aunque este hilo este activo
        hiloLectorEntrada.start();

        String resultadoFinal = null;
        boolean tiempoExpirado = false;

        try {
            for (int i = segundosTimeout; i >= 0; i--) {
                System.out.print("\rTiempo restante: " + String.format("%02d", i) + "s  "); // Espacios al final para limpiar caracteres previos

                if (i == 0) { // Ultimo segundo, ya no esperamos más por la entrada
                    tiempoExpirado = true;
                    break;
                }

                resultadoFinal = entradaDelUsuario.poll(1, TimeUnit.SECONDS); // Espera 1 segundo por la entrada
                if (resultadoFinal != null) { // El usuario ingreso algo
                    break; // Salir del bucle del cronometro
                }
                // Si resultadoFinal es null, el poll timeouteo despues de 1s, continuar cronometro
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablecer estado de interrupcion
            System.out.print("\r>> Cronómetro interrumpido.         ");
            // resultadoFinal seguira siendo null o lo que se haya obtenido.
        } finally {
            System.out.print("\r                                \r"); // Limpiar completamente la linea del contador
            if (hiloLectorEntrada.isAlive()) {
                hiloLectorEntrada.interrupt(); // Interrumpir el hilo lector si todavía está activo
            }
        }

        if (tiempoExpirado && resultadoFinal == null) { // Se cumplio el tiempo y no hubo entrada
            ConsolaUtilJuego.mostrarMensaje("¡TIEMPO AGOTADO!");
        }
        return resultadoFinal; // Puede ser null si hubo timeout o interrupcion sin entrada valida
    }

    //"Limpia" la consola imprimiendo multiples lineas nuevas.
    public static void limpiarConsola() {
        // Solucion simple y portable
        for (int i = 0; i < 40; ++i) System.out.println();
    }
}

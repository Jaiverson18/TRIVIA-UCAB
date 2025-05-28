package com.ucab.trivia.config.utils;

import java.util.Scanner;

    //Utilidades para la interaccion por consola en la aplicacion de configuracion.

public class ConsolaUtilConfig {
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");

    /**
     * Lee una linea de texto ingresada por el usuario.
     * "mensaje" El mensaje a mostrar al usuario antes de la entrada.
     */

    public static String leerString(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine();
    }

    /**
     * Lee un numero entero ingresado por el usuario, validando que este en un rango.
     * Vuelve a solicitar la entrada hasta que sea valida.
     * "min" El valor minimo permitido.
     * "max" El valor maximo permitido.
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

    public static void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    //Intenta limpiar la pantalla de la consola imprimiendo multiples lineas nuevas.

    public static void limpiarConsola() {
        for (int i = 0; i < 30; ++i) System.out.println();
    }

    //Pausa la ejecucion hasta que el usuario presione Enter.

    public static void presionaEnterParaContinuar() {
        System.out.print("\n>> Presiona Enter para continuar...");
        scanner.nextLine();
    }
}
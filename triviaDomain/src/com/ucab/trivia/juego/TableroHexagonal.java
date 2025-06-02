package main.java.com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.Posicion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableroHexagonal {

    // Para un tablero hexagonal en una matriz, se usan coordenadas axiales, cubicas o de offset.
    // Offset ("odd-r" o "pointy top") es a menudo mas facil de mapear a una matriz 2D.
    // Asumiremos un hexagono con un tamaño/radio. Por ejemplo, un radio de 3 (del centro a un vertice)
    // podria generar un hexagono de 7 celdas de diametro.
    // Celda central (0,0). Luego capas alrededor.

    private final Casilla[][] tableroMatriz; // Representacion del tablero
    private final int radio; // Radio del hexagono (numero de "anillos" sin contar el centro)
    private final int dimensionMatriz; // Tamaño de la matriz cuadrada que contendra el hexagono

    // Coordenada de la casilla central en la matriz
    private final CoordenadaHex centroTableroCoord;

    // Para mapear jugadores a sus simbolos en el tablero (J1, J2, etc.)
    private Map<String, String> emailASimboloJugador;
    private int proximoSimboloIdx;

    public static class CoordenadaHex {
        public final int fila;
        public final int col;

        public CoordenadaHex(int fila, int col) {
            this.fila = fila;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoordenadaHex that = (CoordenadaHex) o;
            return fila == that.fila && col == that.col;
        }

        @Override
        public int hashCode() {
            return 31 * fila + col;
        }

        @Override
        public String toString() {
            return "(" + fila + "," + col + ")";
        }
    }



    public TableroHexagonal(int radio) {
        if (radio < 1) throw new IllegalArgumentException("El radio del hexagono debe ser al menos 1.");
        this.radio = radio;
        // La dimension de la matriz cuadrada necesaria para un hexagono de radio N es (2*N + 1)
        this.dimensionMatriz = 2 * radio + 1;
        this.tableroMatriz = new Casilla[dimensionMatriz][dimensionMatriz];
        this.centroTableroCoord = new CoordenadaHex(radio, radio); // Centro de la matriz
        this.emailASimboloJugador = new HashMap<>();
        this.proximoSimboloIdx = 1;
        inicializarTablero();
    }
    private void inicializarTablero() {
        CategoriaTrivia[] categoriasCiclicas = CategoriaTrivia.values();
        int catIndex = 0;

        // Iterar sobre la matriz y decidir que celdas son parte del hexagono
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            for (int col = 0; col < dimensionMatriz; col++) {
                // Convertir coordenadas de matriz (fila, col) a coordenadascubicas (x,y,z) o axiales (q,r)
                // para chequear distancia al centro y determinar si es parte del hexagono.
                // Usando "pointy top" hexagonos, con offset "odd-r" (filas impares desplazadas)
                // Coordenadas Cúbicas desde offset:
                // x = col - (fila - (fila&1)) / 2
                // z = fila
                // y = -x - z
                // Si |x| + |y| + |z| <= 2 * radio, es parte del hexagono (o |q| + |r| + |-q-r| <= radio en axial)
                // Simplificacion: usar distancia Manhattan de la matriz al centro como proxy para un hexagono "aproximado"
                // o una formula de distancia hexagonal real.

                // Distancia hexagonal desde el centro (radio, radio) a (fila, col)
                // Convertir (fila, col) de matriz a coordenadas axiales relativas al centro de la matriz:
                // Asumimos que el centro de la matriz (radio, radio) es el hex (0,0) en axial.
                // int q = col - radio;
                // int r = fila - radio;

                // Formula para hexagonos "pointy top" en una matriz de offset "odd-r"
                // q (columna axial) = col_matriz - (fila_matriz - (fila_matriz & 1)) / 2
                // r (fila axial)    = fila_matriz
                // Coordenadas del centro de la matriz (this.radio, this.radio) deben mapear a (q=0, r=0) axial *conceptual*.
                // La celda (q_axial_centro, r_axial_centro) es ( (this.radio) - (this.radio - (this.radio&1))/2 , this.radio)

                int q_centro_mat = this.radio; // Columna del centro en la matriz
                int r_centro_mat = this.radio; // Fila del centro en la matriz

                // Convertir (col, fila) de la matriz a (x,y,z) cubicas centradas en (0,0,0)
                // Columna de matriz a x_cubica (con offset para centrar)
                int x_cub = col - q_centro_mat - (fila - r_centro_mat - ((fila - r_centro_mat) & 1)) / 2;
                // Fila de matriz a z_cubica (con offset para centrar)
                int z_cub = fila - r_centro_mat;
                int y_cub = -x_cub - z_cub;

                // Una celda (x,y,z) esta dentro del hexagono de radio R si:
                // max(|x|, |y|, |z|) <= R
                if (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) <= this.radio) {
                    boolean esCentroReal = (fila == centroTableroCoord.fila && col == centroTableroCoord.col);
                    CategoriaTrivia catAsignada = esCentroReal ? null : categoriasCiclicas[catIndex % categoriasCiclicas.length];

                    // Logica simple para casillas de Re-Roll (ej. las del primer anillo exterior)
                    boolean esReRoll = !esCentroReal && (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) == 1);

                    if (this.radio >=2 && !esCentroReal && (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) == 2) ) {
                        esReRoll = true;
                    } else {
                        esReRoll = false;
                    }

                    tableroMatriz[fila][col] = new Casilla(catAsignada, esReRoll, esCentroReal);
                    if (!esCentroReal) {
                        catIndex++;
                    }
                } else {
                    tableroMatriz[fila][col] = null; // No es parte del hexágono
                }
            }
        }
        // Asegurar que la casilla central sea marcada como tal.
        if (tableroMatriz[centroTableroCoord.fila][centroTableroCoord.col] != null){
            tableroMatriz[centroTableroCoord.fila][centroTableroCoord.col].setEsCentro(true);
            tableroMatriz[centroTableroCoord.fila][centroTableroCoord.col].setCategoria(null); // El centro no tiene categoria de pregunta
        } else {
            System.err.println("Error Critico: La celda central no fue inicializada como parte del hexagono.");
        }
    }
    /**
     * Obtiene la Casilla en una coordenada especifica de la matriz.
     * "coord" la coordenada (fila, columna) de la matriz.
     */

    public Casilla getCasilla(CoordenadaHex coord) {
        if (coord == null || coord.fila < 0 || coord.fila >= dimensionMatriz || 
            coord.col < 0 || coord.col >= dimensionMatriz) {
            return null; // Fuera de los limites de la matriz
        }
        return tableroMatriz[coord.fila][coord.col];
    }

    /**
     * Obtiene la Casilla en una coordenada específica de la matriz.
     * "fila"; la fila en la matriz.
     * "col" ; la columna en la matriz.
     * Retorna la Casilla en esa coordenada, o null.
     */
    public Casilla getCasilla(int fila, int col) {
        return getCasilla(new CoordenadaHex(fila, col));
    }

    /**
     * Coloca el símbolo de un jugador en una casilla específica.
     * "emailJugador" el email del jugador para obtener su simbolo.
     * "coord" la coordenada donde colocar al jugador.
     */
    public void colocarJugadorEnCasilla(String emailJugador, CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) {
            String simbolo = emailASimboloJugador.computeIfAbsent(emailJugador, k -> "J" + (proximoSimboloIdx++));
            c.setJugadorEnCasilla(simbolo);
        }
    }

    /**
     * Quita el simbolo de cualquier jugador de una casilla especifica (la deja vacia).
     * "coord" la coordenada de donde quitar al jugador.
     */
    public void quitarJugadorDeCasilla(CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) {
            c.setJugadorEnCasilla(" "); // " " representa vacia
        }
    }

    public CoordenadaHex getCoordenadaCentro() {
        return centroTableroCoord;
    }

    public int getRadio() {
        return radio;
    }

    public int getDimensionMatriz() {
        return dimensionMatriz;
    }

    /**
     * Dibuja una representacion simple del tablero hexagonal en la consola.
     * Muestra el smbolo de la categoria o 'C' para el centro, y el simbolo del jugador si hay uno.
     * Las celdas que no son parte del hexagono se muestran como ".".
     */
    public void dibujarTableroConsola() {
        System.out.println("\n--- TABLERO TRIVIA-UCAB (Hexagonal) ---");
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            // Aplicar offset para filas impares para la visualizacion "pointy top"
            if ((fila - radio) % 2 != 0 && radio % 2 == 0 || (fila - radio) % 2 == 0 && radio % 2 != 0 ) { // Heuristica simple para indentar
                 System.out.print("  "); // Indentacion para filas "impares" relativas al centro conceptual
            } else if (dimensionMatriz > 5 && (fila - radio) %2 !=0){ //Ajuste para radios mayores
                 System.out.print("  ");
            }


            for (int col = 0; col < dimensionMatriz; col++) {
                Casilla c = tableroMatriz[fila][col];
                if (c == null) {
                    System.out.print(" .  "); // Fuera del hexagono
                } else {
                    String jugador = c.getJugadorEnCasilla();
                    if (!jugador.trim().isEmpty()) {
                        System.out.print("[" + jugador + "]"); // Jugador en la casilla
                    } else {
                        System.out.print("[" + c.getSimboloCategoriaConsola() + "]"); // Categoria o Centro
                    }
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        System.out.println("--- Leyenda: C=Centro, G=Geografia, H=Historia, D=Deportes, N=Naturaleza, A=Arte, E=Entretenimiento ---");
        System.out.println("--- Jugadores: J1, J2, etc. ---");
    }
    /**
     * Direcciones de movimiento en un hexagono "pointy top" usando offset "odd-r".
     * Estos son los cambios en (fila, col) de la matriz para cada una de las 6 direcciones.
     * La paridad de la fila actual ('r' en "odd-r") afecta a los vecinos diagonales.
     * El array de direcciones_offset[paridad_fila][direccion] almacena los delta (df, dc).
     */
    private static final int[][][] DIRECCIONES_OFFSET_POINTY_ODD_R = {
        { // Filas Pares (even r)
            {-1,  0}, // N (conceptual, en pointy sería NE y NW) - Usamos como NW para simplificar
            {-1, +1}, // NE
            { 0, +1}, // E (SE en pointy)
            {+1, +1}, // SE (S en pointy)
            {+1,  0}, // SW (S en pointy)
            { 0, -1}  // W (SW en pointy)
            // Estas 6 direcciones cubren los vecinos de un hexagono pointy-top.
            // La numeracion podria ser: 0:E, 1:NE, 2:NW, 3:W, 4:SW, 5:SE
        },
        { // Filas Impares (odd r)
            {-1, -1}, // NW
            {-1,  0}, // NE
            { 0, +1}, // E
            {+1,  0}, // SE
            {+1, -1}, // SW
            { 0, -1}  // W
        }
    };
    // Para la interfaz de usuario, es mejor numerar las direcciones 1-6
    // Direcciones para el usuario (1-6), y como mapean a los indices de arriba (0-5)
    // Podriamos definir un enum DireccionHex { E, NE, NW, W, SW, SE }


    /**
     * Obtiene las coordenadas de las casillas vecinas válidas de una coordenada dada.
     * "coordActual" la coordenada actual.
     */
    public List<CoordenadaHex> getVecinosValidos(CoordenadaHex coordActual) {
        List<CoordenadaHex> vecinos = new ArrayList<>();
        if (getCasilla(coordActual) == null) return vecinos; // No es una casilla valida

        int paridadFila = coordActual.fila % 2; // 0 para par, 1 para impar

        int[][] deltas = {
            // Para filas pares (r is even)
            { {0, +1}, {-1, 0}, {-1, -1}, {0, -1}, {+1, -1}, {+1, 0} }, 
            // Para filas impares (r is odd) - pointy top, odd-r (impares desplazadas a la derecha)
            { {0, +1}, {-1, +1}, {-1, 0}, {0, -1}, {+1, 0}, {+1, +1} }
        };

        for (int i = 0; i < 6; i++) {
            int df = deltas[paridadFila][i][0];
            int dc = deltas[paridadFila][i][1];
            CoordenadaHex vecinoCoord = new CoordenadaHex(coordActual.fila + df, coordActual.col + dc);
            if (getCasilla(vecinoCoord) != null) { // Es una casilla valida en el tablero
                vecinos.add(vecinoCoord);
            }
        }
        return vecinos;
    }

    /**
     * Calcula la nueva posicion despues de moverse una cierta cantidad de pasos desde una
     * coordenada actual, siguiendo una secuencia de elecciones de direccion.
     * Este es un movimiento paso a paso.
     * "coordInicio" la coordenada de inicio.
     * "pasos" el numero total de pasos a moverse.
     * "jugador" el jugador que se mueve (para la interfaz de eleccin de direccion).
     */
    public CoordenadaHex calcularNuevaPosicionPasoAPaso(CoordenadaHex coordInicio, int pasos, Jugador jugador) {
        CoordenadaHex posActual = coordInicio;
        System.out.println("Moviendo a " + jugador.getCorreoElectronico() + " " + pasos + " pasos desde " + posActual);

        for (int i = 0; i < pasos; i++) {
            List<CoordenadaHex> vecinos = getVecinosValidos(posActual);
            if (vecinos.isEmpty()) {
                System.out.println("No hay movimientos validos desde " + posActual + ". El jugador se queda.");
                break; // No se puede mover mas
            }

            System.out.println("Paso " + (i + 1) + "/" + pasos + ". Estás en " + posActual + ". Elige direccion para el siguiente paso:");
            for (int j = 0; j < vecinos.size(); j++) {
                System.out.println((j + 1) + ". Mover a " + vecinos.get(j) + " (Casilla: " + getCasilla(vecinos.get(j)).getSimboloCategoriaConsola() + ")");
            }

            int eleccion = ConsolaUtilJuego.leerInt("Elige direccion (1-" + vecinos.size() + ")", 1, vecinos.size());
            posActual = vecinos.get(eleccion - 1);
            System.out.println("Movido a: " + posActual);

            // Limpiar la posicion anterior del jugador y colocarlo en la nueva
        }
        System.out.println("Posicion final despues de " + pasos + " pasos: " + posActual);
        return posActual;
    }

    /**
     * Calcula la distancia (en numero de casillas hexagonales) entre dos coordenadas.
     * Util para verificar si un tiro es exacto para llegar al centro.
     * Se basa en coordenadas cubicas.
     * "c1" Coordenada 1.
     * "c2" Coordenada 2.
     */
    public int distanciaHexagonal(CoordenadaHex c1, CoordenadaHex c2) {
        // Convertir c1 y c2 de matriz a cubicas relativas al centro (0,0,0) del sistema cubico.
        // Matriz centro (radio, radio) es cubico (0,0,0).

        // Coords cubicas para c1
        int x1_cub = c1.col - radio - (c1.fila - radio - ((c1.fila - radio) & 1)) / 2;
        int z1_cub = c1.fila - radio;
        int y1_cub = -x1_cub - z1_cub;

        // Coords cubicas para c2
     /**
     * Direcciones de movimiento en un hexagono "pointy top" usando offset "odd-r".
     * Estos son los cambios en (fila, col) de la matriz para cada una de las 6 direcciones.
     * La paridad de la fila actual ('r' en "odd-r") afecta a los vecinos diagonales.
     * Usaremos la convencion "pointy top" con "odd-r" (filas impares desplazadas a la derecha).
     * El array de direcciones_offset[paridad_fila][direccion] almacena los delta (df, dc).
     */
    private static final int[][][] DIRECCIONES_OFFSET_POINTY_ODD_R = {
        { // Filas Pares (even r)
            {-1,  0}, // N (conceptual, en pointy seria NE y NW) - Usamos como NW para simplificar
            {-1, +1}, // NE
            { 0, +1}, // E (SE en pointy)
            {+1, +1}, // SE (S en pointy)
            {+1,  0}, // SW (S en pointy)
            { 0, -1}  // W (SW en pointy)
            // Estas 6 direcciones cubren los vecinos de un hexagono pointy-top.
            // La numeracion podria ser: 0:E, 1:NE, 2:NW, 3:W, 4:SW, 5:SE
        },
        { // Filas Impares (odd r)
            {-1, -1}, // NW
            {-1,  0}, // NE
            { 0, +1}, // E
            {+1,  0}, // SE
            {+1, -1}, // SW
            { 0, -1}  // W
        }
    };

    /**
     * Obtiene las coordenadas de las casillas vecinas validas de una coordenada dada.
     * "coordActual" La coordenada actual.
     */
    public List<CoordenadaHex> getVecinosValidos(CoordenadaHex coordActual) {
        List<CoordenadaHex> vecinos = new ArrayList<>();
        if (getCasilla(coordActual) == null) return vecinos; // No es una casilla valida

        int paridadFila = coordActual.fila % 2; // 0 para par, 1 para impar

        // Usaremos 6 direcciones estandar para hexagonos pointy-top
        // (df, dc) para E, NE, NW, W, SW, SE (el orden puede variar)
        int[][] deltas = {
            // Para filas pares (r is even)
            { {0, +1}, {-1, 0}, {-1, -1}, {0, -1}, {+1, -1}, {+1, 0} }, 
            // Para filas impares (r is odd) - pointy top, odd-r (impares desplazadas a la derecha)
            { {0, +1}, {-1, +1}, {-1, 0}, {0, -1}, {+1, 0}, {+1, +1} }
        };

        for (int i = 0; i < 6; i++) {
            int df = deltas[paridadFila][i][0];
            int dc = deltas[paridadFila][i][1];
            CoordenadaHex vecinoCoord = new CoordenadaHex(coordActual.fila + df, coordActual.col + dc);
            if (getCasilla(vecinoCoord) != null) { // Es una casilla valida en el tablero
                vecinos.add(vecinoCoord);
            }
        }
        return vecinos;
    }

    /**
     * Calcula la nueva posicion despues de moverse una cierta cantidad de pasos desde una
     * coordenada actual, siguiendo una secuencia de elecciones de direccion.
     * Este es un movimiento paso a paso.
     * "coordInicio" la coordenada de inicio.
     * "pasos" el numero total de pasos a moverse.
     * "jugador" el jugador que se mueve (para la interfaz de eleccion de direccion).
     */
    public CoordenadaHex calcularNuevaPosicionPasoAPaso(CoordenadaHex coordInicio, int pasos, Jugador jugador) {
        CoordenadaHex posActual = coordInicio;
        System.out.println("Moviendo a " + jugador.getCorreoElectronico() + " " + pasos + " pasos desde " + posActual);

        for (int i = 0; i < pasos; i++) {
            List<CoordenadaHex> vecinos = getVecinosValidos(posActual);
            if (vecinos.isEmpty()) {
                System.out.println("No hay movimientos validos desde " + posActual + ". El jugador se queda.");
                break; // No se puede mover mas
            }

            System.out.println("Paso " + (i + 1) + "/" + pasos + ". Estas en " + posActual + ". Elige direccion para el siguiente paso:");
            for (int j = 0; j < vecinos.size(); j++) {
                System.out.println((j + 1) + ". Mover a " + vecinos.get(j) + " (Casilla: " + getCasilla(vecinos.get(j)).getSimboloCategoriaConsola() + ")");
            }

            int eleccion = ConsolaUtilJuego.leerInt("Elige direccion (1-" + vecinos.size() + ")", 1, vecinos.size());
            posActual = vecinos.get(eleccion - 1);
            System.out.println("Movido a: " + posActual);

            // Limpiar la posicion anterior del jugador y colocarlo en la nueva

        }
        System.out.println("Posicion final despues de " + pasos + " pasos: " + posActual);
        return posActual;
    }

    /**
     * Calcula la distancia (en numero de casillas hexagonales) entre dos coordenadas.
     * Util para verificar si un tiro es exacto para llegar al centro.
     * Se basa en coordenadas cubicas.
     * "c1" coordenada 1.
     * "c2" coordenada 2.
     * Retorna distancia hexagonal.
     */
    public int distanciaHexagonal(CoordenadaHex c1, CoordenadaHex c2) {
        // Convertir c1 y c2 de matriz a cubicas relativas al centro (0,0,0) del sistema cubico.
        // Matriz centro (radio, radio) es cubico (0,0,0).

        // Coords cubicas para c1
        int x1_cub = c1.col - radio - (c1.fila - radio - ((c1.fila - radio) & 1)) / 2;
        int z1_cub = c1.fila - radio;
        int y1_cub = -x1_cub - z1_cub;

        // Coords cúbicas para c2
        int x2_cub = c2.col - radio - (c2.fila - radio - ((c2.fila - radio) & 1)) / 2;
        int z2_cub = c2.fila - radio;
        int y2_cub = -x2_cub - z2_cub;

        return (Math.abs(x1_cub - x2_cub) + Math.abs(y1_cub - y2_cub) + Math.abs(z1_cub - z2_cub)) / 2;
    }
        int x2_cub = c2.col - radio - (c2.fila - radio - ((c2.fila - radio) & 1)) / 2;
        int z2_cub = c2.fila - radio;
        int y2_cub = -x2_cub - z2_cub;

        return (Math.abs(x1_cub - x2_cub) + Math.abs(y1_cub - y2_cub) + Math.abs(z1_cub - z2_cub)) / 2;
    }
}

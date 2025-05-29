package main.java.com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.Posicion;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableroHexagonal {

    // Para un tablero hexagonal en una matriz, se usan coordenadas axiales, cúbicas o de offset.
    // Offset ("odd-r" o "pointy top") es a menudo más fácil de mapear a una matriz 2D.
    // Asumiremos un hexágono con un tamaño/radio. Por ejemplo, un radio de 3 (del centro a un vértice)
    // podría generar un hexágono de 7 celdas de diámetro.
    // Celda central (0,0). Luego capas alrededor.

    private final Casilla[][] tableroMatriz; // Representación del tablero
    private final int radio; // Radio del hexágono (número de "anillos" sin contar el centro)
    private final int dimensionMatriz; // Tamaño de la matriz cuadrada que contendrá el hexágono

    // Coordenada de la casilla central en la matriz
    private final CoordenadaHex centroTableroCoord;

    // Para mapear jugadores a sus símbolos en el tablero (J1, J2, etc.)
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
        if (radio < 1) throw new IllegalArgumentException("El radio del hexágono debe ser al menos 1.");
        this.radio = radio;
        // La dimensión de la matriz cuadrada necesaria para un hexágono de radio N es (2*N + 1)
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

        // Iterar sobre la matriz y decidir qué celdas son parte del hexágono
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            for (int col = 0; col < dimensionMatriz; col++) {
                // Convertir coordenadas de matriz (fila, col) a coordenadas cúbicas (x,y,z) o axiales (q,r)
                // para chequear distancia al centro y determinar si es parte del hexágono.
                // Usando "pointy top" hexágonos, con offset "odd-r" (filas impares desplazadas)
                // Coordenadas Cúbicas desde offset:
                // x = col - (fila - (fila&1)) / 2
                // z = fila
                // y = -x - z
                // Si |x| + |y| + |z| <= 2 * radio, es parte del hexágono (o |q| + |r| + |-q-r| <= radio en axial)
                // Simplificación: usar distancia Manhattan de la matriz al centro como proxy para un hexágono "aproximado"
                // o una fórmula de distancia hexagonal real.

                // Distancia hexagonal desde el centro (radio, radio) a (fila, col)
                // Convertir (fila, col) de matriz a coordenadas axiales relativas al centro de la matriz:
                // Asumimos que el centro de la matriz (radio, radio) es el hex (0,0) en axial.
                // int q = col - radio;
                // int r = fila - radio;
                // No, esto no es directo para offset.

                // Fórmula para hexágonos "pointy top" en una matriz de offset "odd-r"
                // q (columna axial) = col_matriz - (fila_matriz - (fila_matriz & 1)) / 2
                // r (fila axial)    = fila_matriz
                // Coordenadas del centro de la matriz (this.radio, this.radio) deben mapear a (q=0, r=0) axial *conceptual*.
                // La celda (q_axial_centro, r_axial_centro) es ( (this.radio) - (this.radio - (this.radio&1))/2 , this.radio)

                int q_centro_mat = this.radio; // Columna del centro en la matriz
                int r_centro_mat = this.radio; // Fila del centro en la matriz

                // Convertir (col, fila) de la matriz a (x,y,z) cúbicas centradas en (0,0,0)
                // Columna de matriz a x_cubica (con offset para centrar)
                int x_cub = col - q_centro_mat - (fila - r_centro_mat - ((fila - r_centro_mat) & 1)) / 2;
                // Fila de matriz a z_cubica (con offset para centrar)
                int z_cub = fila - r_centro_mat;
                int y_cub = -x_cub - z_cub;

                // Una celda (x,y,z) está dentro del hexágono de radio R si:
                // max(|x|, |y|, |z|) <= R
                if (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) <= this.radio) {
                    boolean esCentroReal = (fila == centroTableroCoord.fila && col == centroTableroCoord.col);
                    CategoriaTrivia catAsignada = esCentroReal ? null : categoriasCiclicas[catIndex % categoriasCiclicas.length];

                    // Lógica simple para casillas de Re-Roll (ej. las del primer anillo exterior)
                    boolean esReRoll = !esCentroReal && (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) == 1);
                    // Podría ser más sofisticado, ej. cada N casillas.
                    // El PDF original tenía 12 casillas especiales en 42.
                    // Un hexágono de radio 3 tiene 36 casillas + 1 centro.
                    // Radio 1: 6 casillas. Podríamos hacerlas todas re-roll.
                    // Radio 2: 12 casillas. Podríamos hacerlas 4 de ellas re-roll.
                    // Radio 3: 18 casillas. Podríamos hacer 6 de ellas re-roll.
                    // Para radio 3, el anillo más externo tiene 18 casillas. El segundo 12. El primero 6.
                    // Si radio=3, el anillo externo es max(|x,y,z|) == 3.
                    // Si queremos ~10-12 especiales, podrían ser las del anillo de radio=2.
                    if (this.radio >=2 && !esCentroReal && (Math.max(Math.abs(x_cub), Math.max(Math.abs(y_cub), Math.abs(z_cub))) == 2) ) {
                        esReRoll = true; // Ejemplo: Casillas del segundo anillo son re-roll
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
            tableroMatriz[centroTableroCoord.fila][centroTableroCoord.col].setCategoria(null); // El centro no tiene categoría de pregunta
        } else {
            // Esto sería un error en la lógica de generación del hexágono
            System.err.println("Error Crítico: La celda central no fue inicializada como parte del hexágono.");
        }
    }
/**
     * Obtiene la Casilla en una coordenada específica de la matriz.
     * @param coord La coordenada (fila, columna) de la matriz.
     * @return La Casilla en esa coordenada, o null si la coordenada está fuera de los límites
     * o no es parte del hexágono.
     */
    public Casilla getCasilla(CoordenadaHex coord) {
        if (coord == null || coord.fila < 0 || coord.fila >= dimensionMatriz || 
            coord.col < 0 || coord.col >= dimensionMatriz) {
            return null; // Fuera de los límites de la matriz
        }
        return tableroMatriz[coord.fila][coord.col];
    }

    /**
     * Obtiene la Casilla en una coordenada específica de la matriz.
     * @param fila La fila en la matriz.
     * @param col La columna en la matriz.
     * @return La Casilla en esa coordenada, o null.
     */
    public Casilla getCasilla(int fila, int col) {
        return getCasilla(new CoordenadaHex(fila, col));
    }

    /**
     * Coloca el símbolo de un jugador en una casilla específica.
     * @param emailJugador El email del jugador para obtener su símbolo.
     * @param coord La coordenada donde colocar al jugador.
     */
    public void colocarJugadorEnCasilla(String emailJugador, CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) {
            String simbolo = emailASimboloJugador.computeIfAbsent(emailJugador, k -> "J" + (proximoSimboloIdx++));
            c.setJugadorEnCasilla(simbolo);
        }
    }

    /**
     * Quita el símbolo de cualquier jugador de una casilla específica (la deja vacía).
     * @param coord La coordenada de donde quitar al jugador.
     */
    public void quitarJugadorDeCasilla(CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) {
            c.setJugadorEnCasilla(" "); // " " representa vacía
        }
    }

    /** @return La coordenada de la casilla central del tablero. */
    public CoordenadaHex getCoordenadaCentro() {
        return centroTableroCoord;
    }

    /** @return El radio del hexágono. */
    public int getRadio() {
        return radio;
    }

    /** @return La dimensión de la matriz (cuadrada) que contiene el hexágono. */
    public int getDimensionMatriz() {
        return dimensionMatriz;
    }

    /**
     * Dibuja una representación simple del tablero hexagonal en la consola.
     * Muestra el símbolo de la categoría o 'C' para el centro, y el símbolo del jugador si hay uno.
     * Las celdas que no son parte del hexágono se muestran como ".".
     */
    public void dibujarTableroConsola() {
        System.out.println("\n--- TABLERO TRIVIA-UCAB (Hexagonal) ---");
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            // Aplicar offset para filas impares para la visualización "pointy top"
            if ((fila - radio) % 2 != 0 && radio % 2 == 0 || (fila - radio) % 2 == 0 && radio % 2 != 0 ) { // Heurística simple para indentar
                 System.out.print("  "); // Indentación para filas "impares" relativas al centro conceptual
            } else if (dimensionMatriz > 5 && (fila - radio) %2 !=0){ //Ajuste para radios mayores
                 System.out.print("  ");
            }


            for (int col = 0; col < dimensionMatriz; col++) {
                Casilla c = tableroMatriz[fila][col];
                if (c == null) {
                    System.out.print(" .  "); // Fuera del hexágono
                } else {
                    String jugador = c.getJugadorEnCasilla();
                    if (!jugador.trim().isEmpty()) {
                        System.out.print("[" + jugador + "]"); // Jugador en la casilla
                    } else {
                        System.out.print("[" + c.getSimboloCategoriaConsola() + "]"); // Categoría o Centro
                    }
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        System.out.println("--- Leyenda: C=Centro, G=Geografía, H=Historia, D=Deportes, N=Naturaleza, A=Arte, E=Entretenimiento ---");
        System.out.println("--- Jugadores: J1, J2, etc. ---");
    }
// ... (código existente de TableroHexagonal.java, incluyendo dibujarTableroConsola) ...

    /**
     * Direcciones de movimiento en un hexágono "pointy top" usando offset "odd-r".
     * Estos son los cambios en (fila, col) de la matriz para cada una de las 6 direcciones.
     * La paridad de la fila actual ('r' en "odd-r") afecta a los vecinos diagonales.
     * N
     * NW   NE
     * \ /
     * SW---SE
     * / \
     * S (conceptual, no es una dirección directa en pointy-top con matriz)
     * Direcciones para pointy-top, odd-r (r impar desplazado a la derecha):
     * Par (even r): (q, r)
     * NE: (q+1, r-1) -> (c+1, f-1)
     * E:  (q+1, r  ) -> (c+1, f  )
     * SE: (q+1, r+1) -> (c+1, f+1)
     * SW: (q  , r+1) -> (c  , f+1)
     * W:  (q-1, r  ) -> (c-1, f  )
     * NW: (q  , r-1) -> (c  , f-1)
     * Impar (odd r): (q, r)
     * NE: (q  , r-1) -> (c  , f-1)
     * E:  (q+1, r  ) -> (c+1, f  )
     * SE: (q  , r+1) -> (c  , f+1)
     * SW: (q-1, r+1) -> (c-1, f+1)
     * W:  (q-1, r  ) -> (c-1, f  )
     * NW: (q-1, r-1) -> (c-1, f-1)
     *
     * Para nuestra matriz (fila, col):
     * Fila par (ej. fila 0, 2, 4...):
     * 1 (NE): (f-1, c+1) si pointy, (f-1,c) si flat / (f-1, c)
     * 2 (E):  (f,   c+1)
     * 3 (SE): (f+1, c+1) si pointy, (f+1,c) si flat / (f+1, c)
     * 4 (SW): (f+1, c-1) si pointy, (f+1,c-1) si flat / (f+1, c-1)
     * 5 (W):  (f,   c-1)
     * 6 (NW): (f-1, c-1) si pointy, (f-1,c-1) si flat / (f-1, c-1)
     * Fila impar (ej. fila 1, 3, 5...):
     * 1 (NE): (f-1, c) si pointy, (f-1,c+1) si flat / (f-1, c+1)
     * 2 (E):  (f,   c+1)
     * 3 (SE): (f+1, c) si pointy, (f+1,c+1) si flat / (f+1, c+1)
     * 4 (SW): (f+1, c-1) si pointy, (f+1,c) si flat / (f+1, c)
     * 5 (W):  (f,   c-1)
     * 6 (NW): (f-1, c-1) si pointy, (f-1,c) si flat / (f-1, c)
     * Usaremos la convención "pointy top" con "odd-r" (filas impares desplazadas a la derecha).
     * El array de direcciones_offset[paridad_fila][direccion] almacenará los delta (df, dc).
     */
    private static final int[][][] DIRECCIONES_OFFSET_POINTY_ODD_R = {
        { // Filas Pares (even r)
            {-1,  0}, // N (conceptual, en pointy sería NE y NW) - Usamos como NW para simplificar
            {-1, +1}, // NE
            { 0, +1}, // E (SE en pointy)
            {+1, +1}, // SE (S en pointy)
            {+1,  0}, // SW (S en pointy)
            { 0, -1}  // W (SW en pointy)
            // Estas 6 direcciones cubren los vecinos de un hexágono pointy-top.
            // La numeración podría ser: 0:E, 1:NE, 2:NW, 3:W, 4:SW, 5:SE
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
    // Direcciones para el usuario (1-6), y cómo mapean a los índices de arriba (0-5)
    // Podríamos definir un enum DireccionHex { E, NE, NW, W, SW, SE }


    /**
     * Obtiene las coordenadas de las casillas vecinas válidas de una coordenada dada.
     * @param coordActual La coordenada actual.
     * @return Una lista de CoordenadaHex de los vecinos válidos (dentro del hexágono).
     */
    public List<CoordenadaHex> getVecinosValidos(CoordenadaHex coordActual) {
        List<CoordenadaHex> vecinos = new ArrayList<>();
        if (getCasilla(coordActual) == null) return vecinos; // No es una casilla válida

        int paridadFila = coordActual.fila % 2; // 0 para par, 1 para impar

        // Usaremos 6 direcciones estándar para hexágonos pointy-top
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
            if (getCasilla(vecinoCoord) != null) { // Es una casilla válida en el tablero
                vecinos.add(vecinoCoord);
            }
        }
        return vecinos;
    }

    /**
     * Calcula la nueva posición después de moverse una cierta cantidad de pasos desde una
     * coordenada actual, siguiendo una secuencia de elecciones de dirección.
     * Este es un movimiento paso a paso.
     * @param coordInicio La coordenada de inicio.
     * @param pasos El número total de pasos a moverse.
     * @param jugador El jugador que se mueve (para la interfaz de elección de dirección).
     * @return La CoordenadaHex final después de los movimientos.
     */
    public CoordenadaHex calcularNuevaPosicionPasoAPaso(CoordenadaHex coordInicio, int pasos, Jugador jugador) {
        CoordenadaHex posActual = coordInicio;
        System.out.println("Moviendo a " + jugador.getCorreoElectronico() + " " + pasos + " pasos desde " + posActual);

        for (int i = 0; i < pasos; i++) {
            List<CoordenadaHex> vecinos = getVecinosValidos(posActual);
            if (vecinos.isEmpty()) {
                System.out.println("No hay movimientos válidos desde " + posActual + ". El jugador se queda.");
                break; // No se puede mover más
            }

            System.out.println("Paso " + (i + 1) + "/" + pasos + ". Estás en " + posActual + ". Elige dirección para el siguiente paso:");
            for (int j = 0; j < vecinos.size(); j++) {
                System.out.println((j + 1) + ". Mover a " + vecinos.get(j) + " (Casilla: " + getCasilla(vecinos.get(j)).getSimboloCategoriaConsola() + ")");
            }

            int eleccion = ConsolaUtilJuego.leerInt("Elige dirección (1-" + vecinos.size() + ")", 1, vecinos.size());
            posActual = vecinos.get(eleccion - 1);
            System.out.println("Movido a: " + posActual);

            // Limpiar la posición anterior del jugador y colocarlo en la nueva
            // Esto es más para el renderizado en el bucle principal del juego.
            // Aquí solo calculamos la posición final.
        }
        System.out.println("Posición final después de " + pasos + " pasos: " + posActual);
        return posActual;
    }

    /**
     * Calcula la distancia (en número de casillas hexagonales) entre dos coordenadas.
     * Útil para verificar si un tiro es exacto para llegar al centro.
     * Se basa en coordenadas cúbicas.
     * @param c1 Coordenada 1.
     * @param c2 Coordenada 2.
     * @return La distancia hexagonal.
     */
    public int distanciaHexagonal(CoordenadaHex c1, CoordenadaHex c2) {
        // Convertir c1 y c2 de matriz a cúbicas relativas al centro (0,0,0) del sistema cúbico.
        // Matriz centro (radio, radio) es cúbico (0,0,0).

        // Coords cúbicas para c1
        int x1_cub = c1.col - radio - (c1.fila - radio - ((c1.fila - radio) & 1)) / 2;
        int z1_cub = c1.fila - radio;
        int y1_cub = -x1_cub - z1_cub;

        // Coords cúbicas para c2    /**
     * Direcciones de movimiento en un hexágono "pointy top" usando offset "odd-r".
     * Estos son los cambios en (fila, col) de la matriz para cada una de las 6 direcciones.
     * La paridad de la fila actual ('r' en "odd-r") afecta a los vecinos diagonales.
     * N
     * NW   NE
     * \ /
     * SW---SE
     * / \
     * S (conceptual, no es una dirección directa en pointy-top con matriz)
     * Direcciones para pointy-top, odd-r (r impar desplazado a la derecha):
     * Par (even r): (q, r)
     * NE: (q+1, r-1) -> (c+1, f-1)
     * E:  (q+1, r  ) -> (c+1, f  )
     * SE: (q+1, r+1) -> (c+1, f+1)
     * SW: (q  , r+1) -> (c  , f+1)
     * W:  (q-1, r  ) -> (c-1, f  )
     * NW: (q  , r-1) -> (c  , f-1)
     * Impar (odd r): (q, r)
     * NE: (q  , r-1) -> (c  , f-1)
     * E:  (q+1, r  ) -> (c+1, f  )
     * SE: (q  , r+1) -> (c  , f+1)
     * SW: (q-1, r+1) -> (c-1, f+1)
     * W:  (q-1, r  ) -> (c-1, f  )
     * NW: (q-1, r-1) -> (c-1, f-1)
     *
     * Para nuestra matriz (fila, col):
     * Fila par (ej. fila 0, 2, 4...):
     * 1 (NE): (f-1, c+1) si pointy, (f-1,c) si flat / (f-1, c)
     * 2 (E):  (f,   c+1)
     * 3 (SE): (f+1, c+1) si pointy, (f+1,c) si flat / (f+1, c)
     * 4 (SW): (f+1, c-1) si pointy, (f+1,c-1) si flat / (f+1, c-1)
     * 5 (W):  (f,   c-1)
     * 6 (NW): (f-1, c-1) si pointy, (f-1,c-1) si flat / (f-1, c-1)
     * Fila impar (ej. fila 1, 3, 5...):
     * 1 (NE): (f-1, c) si pointy, (f-1,c+1) si flat / (f-1, c+1)
     * 2 (E):  (f,   c+1)
     * 3 (SE): (f+1, c) si pointy, (f+1,c+1) si flat / (f+1, c+1)
     * 4 (SW): (f+1, c-1) si pointy, (f+1,c) si flat / (f+1, c)
     * 5 (W):  (f,   c-1)
     * 6 (NW): (f-1, c-1) si pointy, (f-1,c) si flat / (f-1, c)
     * Usaremos la convención "pointy top" con "odd-r" (filas impares desplazadas a la derecha).
     * El array de direcciones_offset[paridad_fila][direccion] almacenará los delta (df, dc).
     */
    private static final int[][][] DIRECCIONES_OFFSET_POINTY_ODD_R = {
        { // Filas Pares (even r)
            {-1,  0}, // N (conceptual, en pointy sería NE y NW) - Usamos como NW para simplificar
            {-1, +1}, // NE
            { 0, +1}, // E (SE en pointy)
            {+1, +1}, // SE (S en pointy)
            {+1,  0}, // SW (S en pointy)
            { 0, -1}  // W (SW en pointy)
            // Estas 6 direcciones cubren los vecinos de un hexágono pointy-top.
            // La numeración podría ser: 0:E, 1:NE, 2:NW, 3:W, 4:SW, 5:SE
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
    // Direcciones para el usuario (1-6), y cómo mapean a los índices de arriba (0-5)
    // Podríamos definir un enum DireccionHex { E, NE, NW, W, SW, SE }


    /**
     * Obtiene las coordenadas de las casillas vecinas válidas de una coordenada dada.
     * @param coordActual La coordenada actual.
     * @return Una lista de CoordenadaHex de los vecinos válidos (dentro del hexágono).
     */
    public List<CoordenadaHex> getVecinosValidos(CoordenadaHex coordActual) {
        List<CoordenadaHex> vecinos = new ArrayList<>();
        if (getCasilla(coordActual) == null) return vecinos; // No es una casilla válida

        int paridadFila = coordActual.fila % 2; // 0 para par, 1 para impar

        // Usaremos 6 direcciones estándar para hexágonos pointy-top
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
            if (getCasilla(vecinoCoord) != null) { // Es una casilla válida en el tablero
                vecinos.add(vecinoCoord);
            }
        }
        return vecinos;
    }

    /**
     * Calcula la nueva posición después de moverse una cierta cantidad de pasos desde una
     * coordenada actual, siguiendo una secuencia de elecciones de dirección.
     * Este es un movimiento paso a paso.
     * @param coordInicio La coordenada de inicio.
     * @param pasos El número total de pasos a moverse.
     * @param jugador El jugador que se mueve (para la interfaz de elección de dirección).
     * @return La CoordenadaHex final después de los movimientos.
     */
    public CoordenadaHex calcularNuevaPosicionPasoAPaso(CoordenadaHex coordInicio, int pasos, Jugador jugador) {
        CoordenadaHex posActual = coordInicio;
        System.out.println("Moviendo a " + jugador.getCorreoElectronico() + " " + pasos + " pasos desde " + posActual);

        for (int i = 0; i < pasos; i++) {
            List<CoordenadaHex> vecinos = getVecinosValidos(posActual);
            if (vecinos.isEmpty()) {
                System.out.println("No hay movimientos válidos desde " + posActual + ". El jugador se queda.");
                break; // No se puede mover más
            }

            System.out.println("Paso " + (i + 1) + "/" + pasos + ". Estás en " + posActual + ". Elige dirección para el siguiente paso:");
            for (int j = 0; j < vecinos.size(); j++) {
                System.out.println((j + 1) + ". Mover a " + vecinos.get(j) + " (Casilla: " + getCasilla(vecinos.get(j)).getSimboloCategoriaConsola() + ")");
            }

            int eleccion = ConsolaUtilJuego.leerInt("Elige dirección (1-" + vecinos.size() + ")", 1, vecinos.size());
            posActual = vecinos.get(eleccion - 1);
            System.out.println("Movido a: " + posActual);

            // Limpiar la posición anterior del jugador y colocarlo en la nueva
            // Esto es más para el renderizado en el bucle principal del juego.
            // Aquí solo calculamos la posición final.
        }
        System.out.println("Posición final después de " + pasos + " pasos: " + posActual);
        return posActual;
    }

    /**
     * Calcula la distancia (en número de casillas hexagonales) entre dos coordenadas.
     * Útil para verificar si un tiro es exacto para llegar al centro.
     * Se basa en coordenadas cúbicas.
     * @param c1 Coordenada 1.
     * @param c2 Coordenada 2.
     * @return La distancia hexagonal.
     */
    public int distanciaHexagonal(CoordenadaHex c1, CoordenadaHex c2) {
        // Convertir c1 y c2 de matriz a cúbicas relativas al centro (0,0,0) del sistema cúbico.
        // Matriz centro (radio, radio) es cúbico (0,0,0).

        // Coords cúbicas para c1
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

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

}
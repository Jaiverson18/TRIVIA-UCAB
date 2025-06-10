package com.ucab.trivia.juego;

import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.juego.utils.ConsolaUtilJuego;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TableroHexagonal {

    public static final int RADIO_HEXAGONO_PREDETERMINADO = 3;
    private final Casilla[][] tableroMatriz;
    private final int radio;
    private final int dimensionMatriz;
    private final CoordenadaHex centroTableroCoord;
    private Map<String, String> emailASimboloJugador;
    private int proximoSimboloIdx;
    private final List<CoordenadaHex> verticesRadiales; // <-- AÑADIDO para guardar las esquinas

    public static class CoordenadaHex {
        // ... (el contenido de la clase CoordenadaHex se mantiene igual que en la última versión) ...
        private int fila;
        private int col;

        public CoordenadaHex() {}
        public CoordenadaHex(int fila, int col) {
            this.fila = fila;
            this.col = col;
        }
        public int getFila() { return fila; }
        public void setFila(int fila) { this.fila = fila; }
        public int getCol() { return col; }
        public void setCol(int col) { this.col = col; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoordenadaHex that = (CoordenadaHex) o;
            return fila == that.fila && col == that.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fila, col);
        }

        @Override
        public String toString() {
            return "Coord(" + fila + "," + col + ")";
        }
    }

    public TableroHexagonal(int radio) {
        if (radio < 1) throw new IllegalArgumentException("El radio del hexágono debe ser al menos 1.");
        this.radio = radio;
        this.dimensionMatriz = 2 * radio + 1;
        this.tableroMatriz = new Casilla[dimensionMatriz][dimensionMatriz];
        this.centroTableroCoord = new CoordenadaHex(radio, radio);
        this.emailASimboloJugador = new HashMap<>();
        this.proximoSimboloIdx = 1;
        this.verticesRadiales = new ArrayList<>(); // <-- AÑADIDO
        inicializarTablero();
    }

    private void inicializarTablero() {
        CategoriaTrivia[] categoriasCiclicas = CategoriaTrivia.values();
        int catIndex = 0;
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            for (int col = 0; col < dimensionMatriz; col++) {
                // Lógica de conversión a coordenadas cúbicas para determinar si la celda es parte del hexágono
                int q = col - (fila + (fila & 1)) / 2;
                int r = fila;
                int q_centro = this.radio - (this.radio + (this.radio & 1)) / 2;
                int r_centro = this.radio;
                int dq = q - q_centro;
                int dr = r - r_centro;
                int x_cub = dq;
                int z_cub = dr;
                int y_cub = -x_cub-z_cub;

                if (Math.abs(x_cub) + Math.abs(y_cub) + Math.abs(z_cub) <= this.radio * 2) {
                    boolean esCentroReal = (fila == centroTableroCoord.getFila() && col == centroTableroCoord.getCol());
                    CategoriaTrivia catAsignada = esCentroReal ? null : categoriasCiclicas[catIndex % categoriasCiclicas.length];

                    // Lógica para casillas de Re-Roll
                    boolean esReRoll = false;
                    int distManhattan = Math.abs(dq) + Math.abs(dr) + Math.abs(-dq-dr);
                    if (!esCentroReal && distManhattan == 2 * this.radio - 2) { // Penúltimo anillo
                        esReRoll = true;
                    }

                    // **NUEVA LÓGICA PARA IDENTIFICAR VÉRTICES**
                    // Los vértices de un hexágono de radio R en coordenadas cúbicas son las permutaciones de (R, -R, 0)
                    // (R,0,-R), (0,R,-R), (-R,R,0), (-R,0,R), (0,-R,R), (R,-R,0)
                    if (Math.abs(x_cub) + Math.abs(y_cub) + Math.abs(z_cub) == 2 * this.radio && (x_cub==0 || y_cub==0 || z_cub==0)) {
                        this.verticesRadiales.add(new CoordenadaHex(fila, col));
                    }

                    tableroMatriz[fila][col] = new Casilla(catAsignada, esReRoll, esCentroReal);
                    if (!esCentroReal) catIndex++;
                } else {
                    tableroMatriz[fila][col] = null;
                }
            }
        }
        Casilla casillaCentral = tableroMatriz[centroTableroCoord.getFila()][centroTableroCoord.getCol()];
        if (casillaCentral != null) {
            casillaCentral.setEsCentro(true);
            casillaCentral.setCategoria(null);
        } else {
            tableroMatriz[centroTableroCoord.getFila()][centroTableroCoord.getCol()] = new Casilla(null, false, true);
        }
    }

    /**
     * **NUEVO MÉTODO**
     * Verifica si una coordenada corresponde a uno de los 6 vértices "radiales" del hexágono.
     * @param coord La coordenada a verificar.
     * @return true si es una de las 6 esquinas, false en caso contrario.
     */
    public boolean esCasillaVerticeRadial(CoordenadaHex coord) {
        return this.verticesRadiales.contains(coord);
    }

    // ... El resto de los métodos de TableroHexagonal (getCasilla, colocarJugador, dibujarTablero, getVecinosValidos, etc.) se mantienen igual que en la última versión que te di ...
    // ... Pega aquí el resto de los métodos de la clase TableroHexagonal que ya tenías ...
    public Casilla getCasilla(CoordenadaHex coord) {
        if (coord == null || coord.getFila() < 0 || coord.getFila() >= dimensionMatriz || coord.getCol() < 0 || coord.getCol() >= dimensionMatriz) return null;
        return tableroMatriz[coord.getFila()][coord.getCol()];
    }
    public void colocarJugadorEnCasilla(String emailJugador, CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) {
            String simbolo = emailASimboloJugador.computeIfAbsent(emailJugador, k -> "J" + (proximoSimboloIdx++));
            c.setJugadorEnCasilla(simbolo);
        }
    }
    public void quitarJugadorDeCasilla(CoordenadaHex coord) {
        Casilla c = getCasilla(coord);
        if (c != null) c.setJugadorEnCasilla(" ");
    }
    public CoordenadaHex getCoordenadaCentro() {
        return new CoordenadaHex(centroTableroCoord.getFila(), centroTableroCoord.getCol());
    }
    public void dibujarTableroConsola() {
        System.out.println("\n--- TABLERO TRIVIA-UCAB ---");
        for (int fila = 0; fila < dimensionMatriz; fila++) {
            for (int i = 0; i < Math.abs(radio - fila); i++) System.out.print("  ");
            for (int col = 0; col < dimensionMatriz; col++) {
                Casilla c = tableroMatriz[fila][col];
                if (c == null) System.out.print("    ");
                else {
                    String jugador = c.getJugadorEnCasilla();
                    if (jugador != null && !jugador.trim().isEmpty()) System.out.print("[" + String.format("%-2s", jugador) + "]");
                    else System.out.print("[" + String.format("%-2s", c.getSimboloCategoriaConsola()) + "]");
                }
            }
            System.out.println();
        }
        System.out.println("----------------------------------------");
        System.out.println("Leyenda: C=Centro, G=Geografía, H=Historia, D=Deportes, N=Naturaleza, A=Arte, E=Entretenimiento");
    }
    public List<CoordenadaHex> getVecinosValidos(CoordenadaHex coordActual) {
        List<CoordenadaHex> vecinos = new ArrayList<>();
        if (getCasilla(coordActual) == null) return vecinos;
        final int[][][] deltas = { { {+1, 0}, {+1, -1}, {0, -1}, {-1, 0}, {-1, -1}, {0, +1} }, { {+1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, +1}, {+1, +1} } };
        int paridadCol = coordActual.getCol() % 2;
        for (int[] delta : deltas[paridadCol]) {
            CoordenadaHex vecino = new CoordenadaHex(coordActual.getFila() + delta[1], coordActual.getCol() + delta[0]);
            if (getCasilla(vecino) != null) vecinos.add(vecino);
        }
        return vecinos;
    }
    public CoordenadaHex calcularNuevaPosicionEnLinea(CoordenadaHex coordInicio, int pasos) {
        CoordenadaHex posActual = coordInicio;
        List<CoordenadaHex> vecinos = getVecinosValidos(posActual);
        if (vecinos.isEmpty()) return posActual;
        ConsolaUtilJuego.mostrarMensaje("Elige una dirección para moverte " + pasos + " pasos:");
        for (int i = 0; i < vecinos.size(); i++) System.out.println((i + 1) + ". Hacia " + vecinos.get(i));
        int eleccion = ConsolaUtilJuego.leerInt("Elige dirección", 1, vecinos.size());
        CoordenadaHex vecinoElegido = vecinos.get(eleccion - 1);
        int deltaFila = vecinoElegido.getFila() - posActual.getFila();
        int deltaCol = vecinoElegido.getCol() - posActual.getCol();
        CoordenadaHex posFinal = posActual;
        for (int i = 0; i < pasos; i++) {
            CoordenadaHex siguientePaso = new CoordenadaHex(posFinal.getFila() + deltaFila, posFinal.getCol() + deltaCol);
            if (getCasilla(siguientePaso) != null) posFinal = siguientePaso;
            else { ConsolaUtilJuego.mostrarMensaje("El camino se bloquea. Te detienes en: " + posFinal); break; }
        }
        return posFinal;
    }
    public int distanciaHexagonal(CoordenadaHex c1, CoordenadaHex c2) {
        if (c1 == null || c2 == null) return Integer.MAX_VALUE;
        int q1 = c1.getCol(); int r1 = c1.getFila() - (c1.getCol() + (c1.getCol()&1)) / 2;
        int x1 = q1; int z1 = r1; int y1 = -x1-z1;
        int q2 = c2.getCol(); int r2 = c2.getFila() - (c2.getCol() + (c2.getCol()&1)) / 2;
        int x2 = q2; int z2 = r2; int y2 = -x2-z2;
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
    }
}
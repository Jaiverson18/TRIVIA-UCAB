package com.ucab.trivia.domain;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Representa una pregunta con todos sus detalles, incluyendo su estado,
 * creador y tiempo maximo de respuesta. Esta clase es utilizada por
 * la aplicacion de configuracion y por la aplicacion del juego.
 */
public class PreguntaDetallada {
    private String id;
    private String pregunta;
    private String respuesta;
    private CategoriaTrivia categoria;
    private EstadoPregunta estado;
    private String usuarioCreadorEmail;
    private int tiempoMaximoRespuestaSegundos;

    //Constructor necesario para la deserialización con Jackson.

    public PreguntaDetallada() {
    }

    /**
     * Constructor para crear una nueva pregunta detallada.
     * "id" Identificador unico de la pregunta.
     * "pregunta" El texto de la pregunta.
     * "respuesta" La respuesta correcta a la pregunta.
     * "categoria" La categoria a la que pertenece la pregunta.
     * "estado" El estado actual de la pregunta (ej. APROBADA, ESPERANDO_APROBACION).
     * "usuarioCreadorEmail" El correo electronico del usuario que creo la pregunta.
     * "tiempoMaximoRespuestaSegundos" El tiempo maximo en segundos para responder la pregunta.
     */
    public PreguntaDetallada(String id, String pregunta, String respuesta, CategoriaTrivia categoria,
                             EstadoPregunta estado, String usuarioCreadorEmail, int tiempoMaximoRespuestaSegundos) {
        this.id = id;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.categoria = categoria;
        this.estado = estado;
        this.usuarioCreadorEmail = usuarioCreadorEmail;
        this.tiempoMaximoRespuestaSegundos = tiempoMaximoRespuestaSegundos;
    }

    // Getters y Setters

    public String getId() {
        return id; }

    public void setId(String id) {
        this.id = id; }

    public String getPregunta() {
        return pregunta; }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta; }

    public String getRespuesta() {
        return respuesta; }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta; }

    public CategoriaTrivia getCategoria() {
        return categoria; }

    public void setCategoria(CategoriaTrivia categoria) {
        this.categoria = categoria; }

    public EstadoPregunta getEstado() {
        return estado; }

    public void setEstado(EstadoPregunta estado) {
        this.estado = estado; }

    public String getUsuarioCreadorEmail() {
        return usuarioCreadorEmail; }

    public void setUsuarioCreadorEmail(String usuarioCreadorEmail) {
        this.usuarioCreadorEmail = usuarioCreadorEmail; }

    public int getTiempoMaximoRespuestaSegundos() {
        return tiempoMaximoRespuestaSegundos; }

    public void setTiempoMaximoRespuestaSegundos(int tiempoMaximoRespuestaSegundos) {
        this.tiempoMaximoRespuestaSegundos = tiempoMaximoRespuestaSegundos; }

    /**
     * Compara esta pregunta con otro objeto por igualdad, basandose en el ID.
     * "o" El objeto a comparar.
     * Retorna true si los objetos son iguales (mismo ID), false si no lo son.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntaDetallada that = (PreguntaDetallada) o;
        return Objects.equals(id, that.id);
    }

    //Calcula el codigo hash para la pregunta, basado en su ID.

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Devuelve una representacion en String de la pregunta.

    @Override
    public String toString() {
        return "ID: " + id + " | Cat: " + (categoria != null ? categoria.getNombreMostrado() : "N/A") +
                " | Estado: " + estado + " | Tiempo: " + tiempoMaximoRespuestaSegundos + "s | Pregunta: \"" + pregunta + "\"";
    }
}
package com.ucab.trivia.domain;

import java.util.Objects;

/**
 * Representa una pregunta con sus detalles. El tiempo de respuesta ya no se almacena aquí,
 * será una configuración global del juego.
 *
 * @author (Tu Nombre/Equipo - Jaiverson)
 * @version 1.1
 * @since 2025-06-02
 */
public class PreguntaDetallada {
    private String id;
    private String pregunta;
    private String respuesta;
    private CategoriaTrivia categoria;
    private EstadoPregunta estado;
    private String usuarioCreadorEmail;
    // Se elimina 'tiempoMaximoRespuestaSegundos'

    public PreguntaDetallada() {
    }

    /**
     * Constructor para crear una nueva pregunta detallada (sin tiempo individual).
     */
    public PreguntaDetallada(String id, String pregunta, String respuesta, CategoriaTrivia categoria,
                             EstadoPregunta estado, String usuarioCreadorEmail) {
        this.id = id;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.categoria = categoria;
        this.estado = estado;
        this.usuarioCreadorEmail = usuarioCreadorEmail;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public CategoriaTrivia getCategoria() { return categoria; }
    public void setCategoria(CategoriaTrivia categoria) { this.categoria = categoria; }

    public EstadoPregunta getEstado() { return estado; }
    public void setEstado(EstadoPregunta estado) { this.estado = estado; }

    public String getUsuarioCreadorEmail() { return usuarioCreadorEmail; }
    public void setUsuarioCreadorEmail(String usuarioCreadorEmail) { this.usuarioCreadorEmail = usuarioCreadorEmail; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreguntaDetallada that = (PreguntaDetallada) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Cat: " + (categoria != null ? categoria.getNombreMostrado() : "N/A") +
                " | Estado: " + estado + " | Pregunta: \"" + pregunta + "\"";
    }
}
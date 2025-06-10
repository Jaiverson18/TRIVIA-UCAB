package com.ucab.trivia.domain;

import java.lang.reflect.Constructor;

/**
 * Clase simple para deserializar las preguntas del JSON original proporcionado por el usuario,
 * el cual tiene un formato de mapa donde la clave es la categoria y el valor es una lista
 * de objetos con "pregunta" y "respuesta".
 */

public class PreguntaOriginal {
    private String pregunta;
    private String respuesta;

    //Constructor por defecto para Jackson.

    public PreguntaOriginal() {
    }

    /**
     * Constructor para crear una PreguntaOriginal.
     * "pregunta" El texto de la pregunta.
     * "respuesta" La respuesta a la pregunta.
     */

    public PreguntaOriginal(String pregunta, String respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public String getPregunta() {
        return pregunta; }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta; }

    public String getRespuesta() {
        return respuesta; }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta; }
}
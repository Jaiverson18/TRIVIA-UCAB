package com.ucab.trivia.domain;

/**
 *  Enumeracion que define los posibles estados de una pregunta
 *  dentro del sistema de configuracion.
 */

public enum EstadoPregunta {

    //Si la pregunta ha sido creada pero aun no ha sido revisada.

    ESPERANDO_APROBACION,

    //Si la pregunta ha sido revisada y aprobada para su uso en el juego.

    APROBADA,

    //Si la pregunta ha sido revisada y rechazada.

    RECHAZADA
}

package com.ucab.trivia.config;

import java.lang.reflect.Constructor;

/**
 * Representa a un usuario del sistema de configuracion.
 * Almacena el correo y la contraseña de forma cifrada.
 */

public class Usuario {
    private String correoElectronicoCifrado;
    private String contrasenaCifrada;

    //Constructor por defecto para Jackson.

    public Usuario() {
    }

    /**
     * Crea una nueva instancia de Usuario con datos ya cifrados.
     * "correoElectronicoCifrado" El correo electronico del usuario, ya cifrado.
     * "contrasenaCifrada" La contraseña del usuario, ya cifrada.
     */

    public Usuario(String correoElectronicoCifrado, String contrasenaCifrada) {
        this.correoElectronicoCifrado = correoElectronicoCifrado;
        this.contrasenaCifrada = contrasenaCifrada;
    }

    public String getCorreoElectronicoCifrado() {
        return correoElectronicoCifrado; }

    public void setCorreoElectronicoCifrado(String correoElectronicoCifrado) {
        this.correoElectronicoCifrado = correoElectronicoCifrado; }

    public String getContrasenaCifrada() {
        return contrasenaCifrada; }

    public void setContrasenaCifrada(String contrasenaCifrada) {
        this.contrasenaCifrada = contrasenaCifrada; }

}
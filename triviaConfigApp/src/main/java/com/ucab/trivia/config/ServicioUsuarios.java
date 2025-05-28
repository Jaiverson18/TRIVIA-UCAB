package com.ucab.trivia.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ucab.trivia.config.utils.EncripcionUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio para gestionar las operaciones de usuarios, como registro, autenticacion
 * y persistencia de datos de usuario en el archivo JSON.
 * Los datos sensibles se almacenan cifrados.
 */

public class ServicioUsuarios {

    private static final String RUTA_ARCHIVO_USUARIOS_COMPARTIDO = "../usuarios_compartido.json";
    private final ObjectMapper objectMapper;
    private List<Usuario> usuarios;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    //Constructor del servicio. Inicializa el ObjectMapper y carga los usuarios existentes.

    public ServicioUsuarios() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Para JSON legible
        this.usuarios = cargarUsuariosDesdeArchivo();
    }

    /**
     * Carga la lista de usuarios desde el archivo JSON.
     * Retorna una lista vacia si el archivo no existe o hay error.
     */

    private List<Usuario> cargarUsuariosDesdeArchivo() {
        File archivo = new File(RUTA_ARCHIVO_USUARIOS_COMPARTIDO);
        if (archivo.exists() && archivo.length() > 0) {
            try {
                return objectMapper.readValue(archivo, new TypeReference<List<Usuario>>() {});
            } catch (IOException e) {
                System.err.println(">> Error al cargar usuarios desde " + archivo.getAbsolutePath() + ": " + e.getMessage());
            }
        } else {
            System.out.println("INFO: Archivo de usuarios no encontrado en " + archivo.getAbsolutePath() + " o esta vacio. Se creara uno nuevo al registrar el primer usuario.");
        }
        return new ArrayList<>();
    }

    //Guarda la lista actual de usuarios en el archivo JSON.

    private void guardarUsuariosEnArchivo() {
        File tempFile = new File(RUTA_ARCHIVO_USUARIOS_COMPARTIDO + ".tmp");
        File realFile = new File(RUTA_ARCHIVO_USUARIOS_COMPARTIDO);
        try {
            File parentDir = realFile.getParentFile(); // Directorio "../"
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("INFO: Creando directorio para archivos compartidos: " + parentDir.getAbsolutePath());
                parentDir.mkdirs();
            }
            objectMapper.writeValue(tempFile, usuarios);
            Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println(">> Error al guardar usuarios en " + realFile.getAbsolutePath() + ": " + e.getMessage());
            if(tempFile.exists()) tempFile.delete(); // Limpiar temporal si falla el move
        } catch (UnsupportedOperationException uoe){ // ATOMIC_MOVE puede no ser soportado por todos los FS
            try {
                Files.move(tempFile.toPath(), realFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                System.err.println(">> Error al guardar usuarios (movimiento de respaldo) en " + realFile.getAbsolutePath() + ": " + ioe.getMessage());
            }
        }
    }

    /**
     * Valida el formato de una direccion de correo.
     * "correo" El correo a validar.
     * Retorna true si el formato es valido, false si no.
     */

    public boolean validarFormatoCorreo(String correo) {
        return correo != null && EMAIL_PATTERN.matcher(correo).matches();
    }

    /**
     * Valida la longitud de una contraseña segun los requisitos del proyecto (6 caracteres).
     * "contrasena" La contraseña a validar.
     * Retorna true si la contraseña es valida, false si no.
     */

    public boolean validarContrasena(String contrasena) {
        return contrasena != null && contrasena.length() == 6;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Verifica que el correo no este ya registrado, cifra los datos y los guarda.
     * "contrasena" La contraseña del nuevo usuario.
     * "repetirContrasena" La confirmacion de la contraseña.
     * Retorna un mensaje indicando el resultado (exitoso o error).
     */

    public String registrarUsuario(String correo, String contrasena, String repetirContrasena) {
        if (!validarFormatoCorreo(correo)) return ">> Error: El correo es invalido.";
        if (!validarContrasena(contrasena)) return ">> Error: La contraseña debe tener exactamente 6 caracteres.";
        if (!contrasena.equals(repetirContrasena)) return ">> Error: Las contraseñas no coinciden.";

        // Verificar si el correo ya existe (desencriptando los almacenados)

        for (Usuario usuarioExistente : usuarios) {
            try {
                if (EncripcionUtil.desencriptar(usuarioExistente.getCorreoElectronicoCifrado()).equals(correo)) {
                    return ">> Error: El correo '" + correo + "' ya esta registrado.";
                }
            } catch (Exception e) {
                System.err.println("Advertencia: Error al desencriptar un usuario existente durante la verificacion: " + e.getMessage());
            }
        }

        try {
            String correoCifrado = EncripcionUtil.encriptar(correo);
            String contrasenaCifrada = EncripcionUtil.encriptar(contrasena);
            usuarios.add(new Usuario(correoCifrado, contrasenaCifrada));
            guardarUsuariosEnArchivo();
            return "Usuario '" + correo + "' registrado exitosamente.";
        } catch (Exception e) {
            System.err.println(">> Error critico durante el cifrado o registro del usuario " + correo + ": " + e.getMessage());
            e.printStackTrace();
            return ">> Error interno al registrar el usuario. Consulte la consola del administrador para mas detalles.";
        }
    }

    /**
     * Autentica un usuario comparando el correo y contraseña proporcionados
     * con los datos almacenados (despues de desencriptarlos).
     * "correo" El correo del usuario que intenta iniciar sesión.
     * "contrasena" La contraseña proporcionada.
     * Retorna el correo del usuario en texto si la autenticacion es exitosa,
     * o null si falla o el usuario no se encuentra.
     */

    public String autenticarUsuario(String correo, String contrasena) {
        if (!validarFormatoCorreo(correo)) {
            System.out.println("Formato de correo invalido para autenticación.");
            return null;
        }
        if (contrasena == null || contrasena.isEmpty()) {
            System.out.println("La contraseña no puede estar vacia para la autenticacion.");
            return null;
        }

        for (Usuario usuarioAlmacenado : usuarios) {
            try {
                String correoAlmacenadoDescifrado = EncripcionUtil.desencriptar(usuarioAlmacenado.getCorreoElectronicoCifrado());
                if (correoAlmacenadoDescifrado.equals(correo)) {
                    String contrasenaAlmacenadaDescifrada = EncripcionUtil.desencriptar(usuarioAlmacenado.getContrasenaCifrada());
                    if (contrasenaAlmacenadaDescifrada.equals(contrasena)) {
                        return correo; // Autenticación exitosa
                    }
                    // Correo coincide, contraseña no. No continuar buscando.
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Advertencia: Error al procesar un registro de usuario durante la autenticacion: " + e.getMessage());
            }
        }
        return null; // Usuario no encontrado o la contraseña no coincidió con el correo encontrado.
    }
}
package com.ucab.trivia.config.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilidad para encriptar y desencriptar datos sensibles utilizando AES.
 * La clave de encriptacion se deriva de una clave maestra y un salt definidos en la clase.
 */

public class EncripcionUtil {

    private static final String LLAVE_MAESTRA_TEXTO_CONFIG = "TriviaUCABKeyMasterConfigApp#2025"; // Debe ser de 16, 24 o 32 bytes para AES
    private static final String SALT_MAESTRO_TEXTO_CONFIG = "TriviaUCABSaltDerivationConfigApp!";

    private static final String ALGORITMO_CIFRADO_AES = "AES/CBC/PKCS5Padding";
    private static final String ALGORITMO_DERIVACION_CLAVE_PBKDF2 = "PBKDF2WithHmacSHA256";
    private static final int TAMANO_CLAVE_AES_BITS = 256;
    private static final int ITERACIONES_PBKDF2_DERIVACION = 65536;

    /**
     * Deriva una clave secreta AES a partir de la clave maestra y el salt definidos.
     * El "Exception" es por si ocurre un error durante la derivacion de la clave.
     */

    private static SecretKey getClaveSecretaDerivada() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO_DERIVACION_CLAVE_PBKDF2);
        KeySpec spec = new PBEKeySpec(LLAVE_MAESTRA_TEXTO_CONFIG.toCharArray(),
                SALT_MAESTRO_TEXTO_CONFIG.getBytes(StandardCharsets.UTF_8),
                ITERACIONES_PBKDF2_DERIVACION,
                TAMANO_CLAVE_AES_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Encripta un valor de texto plano.
     * Utiliza un Vector de Inicializacion (IV) aleatorio para cada encriptacion,
     * el cual se antepone al texto cifrado resultante.
     * "valorPlano" El String a encriptar.
     * "Exception" Por si ocurre un error durante la encriptacion.
     */
    public static String encriptar(String valorPlano) throws Exception {
        if (valorPlano == null) return null;
        SecretKey claveSecreta = getClaveSecretaDerivada();
        Cipher cipher = Cipher.getInstance(ALGORITMO_CIFRADO_AES);

        byte[] iv = new byte[cipher.getBlockSize()]; // AES block size es 16 bytes
        SecureRandom random = SecureRandom.getInstanceStrong();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, claveSecreta, ivParameterSpec);
        byte[] textoCifradoBytes = cipher.doFinal(valorPlano.getBytes(StandardCharsets.UTF_8));

        byte[] ivMasTextoCifrado = new byte[iv.length + textoCifradoBytes.length];
        System.arraycopy(iv, 0, ivMasTextoCifrado, 0, iv.length);
        System.arraycopy(textoCifradoBytes, 0, ivMasTextoCifrado, iv.length, textoCifradoBytes.length);

        return Base64.getEncoder().encodeToString(ivMasTextoCifrado);
    }

    /**
     * Desencripta un valor que fue previamente encriptado con el metodo {@link #encriptar(String)}.
     * Espera que el IV este antepuesto al texto cifrado en el String codificado en Base64.
     * "Exception" Por si ocurre un error durante la desencriptacion o el formato es incorrecto.
     */
    public static String desencriptar(String valorCifradoConIv) throws Exception {
        if (valorCifradoConIv == null) return null;
        SecretKey claveSecreta = getClaveSecretaDerivada();
        byte[] ivMasCifradoDecodificado = Base64.getDecoder().decode(valorCifradoConIv);
        Cipher cipher = Cipher.getInstance(ALGORITMO_CIFRADO_AES);
        int blockSize = cipher.getBlockSize();

        if (ivMasCifradoDecodificado.length <= blockSize) {
            throw new IllegalArgumentException("El valor cifrado proporcionado es invalido o demasiado corto para contener un IV y datos cifrados.");
        }

        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivMasCifradoDecodificado, 0, blockSize);

        byte[] textoCifradoBytes = new byte[ivMasCifradoDecodificado.length - blockSize];
        System.arraycopy(ivMasCifradoDecodificado, blockSize, textoCifradoBytes, 0, textoCifradoBytes.length);

        cipher.init(Cipher.DECRYPT_MODE, claveSecreta, ivParameterSpec);
        byte[] textoPlanoBytes = cipher.doFinal(textoCifradoBytes);

        return new String(textoPlanoBytes, StandardCharsets.UTF_8);
    }
}

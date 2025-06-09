package com.ucab.trivia.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.EstadoPregunta;
import com.ucab.trivia.domain.PreguntaDetallada;
import com.ucab.trivia.domain.PreguntaOriginal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServicioPreguntasConfig {
    private static final String NOMBRE_ARCHIVO_PREGUNTAS = "banco_preguntas_gestion_compartido.json";
    private static final String RUTA_RECURSO_PREGUNTAS_ORIGINALES = "/preguntasJuegoTrivia.json";

    private final ObjectMapper objectMapper;
    private List<PreguntaDetallada> preguntasGestionadas;
    private final File archivoDePreguntas;

    public ServicioPreguntasConfig() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.findAndRegisterModules();
        this.archivoDePreguntas = localizarArchivoDeDatos(NOMBRE_ARCHIVO_PREGUNTAS);
        this.preguntasGestionadas = cargarPreguntasGestionDesdeArchivo();

        if (this.preguntasGestionadas.isEmpty()) {
            System.out.println("INFO: El banco de preguntas de gestión está vacío. Intentando importación inicial...");
            importarPreguntasDesdeJsonOriginal();
            this.preguntasGestionadas = cargarPreguntasGestionDesdeArchivo();
        }
    }

    private File localizarArchivoDeDatos(String nombreArchivo) {
        File archivo = new File(nombreArchivo);
        if (archivo.exists()) {
            return archivo;
        }
        return new File("../" + nombreArchivo);
    }

    private List<PreguntaDetallada> cargarPreguntasGestionDesdeArchivo() {
        if (archivoDePreguntas.exists() && archivoDePreguntas.length() > 0) {
            try {
                return objectMapper.readValue(archivoDePreguntas, new TypeReference<>() {});
            } catch (IOException e) {
                System.err.println(">> Error al cargar preguntas: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private void guardarPreguntasGestionEnArchivo() {
        File tempFile = new File(archivoDePreguntas.getAbsolutePath() + ".tmp");
        try {
            objectMapper.writeValue(tempFile, preguntasGestionadas);
            Files.move(tempFile.toPath(), archivoDePreguntas.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println(">> Error al guardar preguntas: " + e.getMessage());
            try {
                if (tempFile.exists()) Files.delete(tempFile.toPath());
                Files.copy(tempFile.toPath(), archivoDePreguntas.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.err.println(">> Error en el fallback de guardado: " + ex.getMessage());
            }
        }
    }

    public void importarPreguntasDesdeJsonOriginal() {
        if (!cargarPreguntasGestionDesdeArchivo().isEmpty()) {
            System.out.println("INFO: El archivo de gestión ya tiene datos. No se re-importará.");
            return;
        }

        try (InputStream is = ServicioPreguntasConfig.class.getResourceAsStream(RUTA_RECURSO_PREGUNTAS_ORIGINALES)) {
            if (is == null) {
                System.err.println("CRÍTICO: No se pudo encontrar: " + RUTA_RECURSO_PREGUNTAS_ORIGINALES);
                return;
            }
            Map<String, List<PreguntaOriginal>> mapaPreguntas = objectMapper.readValue(is, new TypeReference<>() {});
            List<PreguntaDetallada> importadas = new ArrayList<>();
            mapaPreguntas.forEach((nombreCat, lista) -> {
                try {
                    CategoriaTrivia categoria = CategoriaTrivia.fromString(nombreCat);
                    lista.forEach(po -> importadas.add(new PreguntaDetallada(
                            UUID.randomUUID().toString(), po.getPregunta(), po.getRespuesta(), categoria,
                            EstadoPregunta.APROBADA, "sistema_importador"
                    )));
                } catch (IllegalArgumentException e) {
                    System.err.println("ADVERTENCIA: Categoría '" + nombreCat + "' no reconocida. Se omitirán sus preguntas.");
                }
            });

            if (!importadas.isEmpty()) {
                this.preguntasGestionadas.addAll(importadas);
                guardarPreguntasGestionEnArchivo();
                System.out.println(importadas.size() + " preguntas importadas y guardadas.");
            }

        } catch (IOException e) {
            System.err.println("Error crítico al leer o procesar " + RUTA_RECURSO_PREGUNTAS_ORIGINALES + ": " + e.getMessage());
        }
    }

    public String agregarPregunta(String texto, String resp, CategoriaTrivia cat, String emailCreador) {
        if (texto.trim().isEmpty() || resp.trim().isEmpty() || cat == null) {
            return "Error: Datos inválidos.";
        }
        PreguntaDetallada p = new PreguntaDetallada(UUID.randomUUID().toString(), texto, resp, cat,
                EstadoPregunta.ESPERANDO_APROBACION, emailCreador);
        preguntasGestionadas.add(p);
        guardarPreguntasGestionEnArchivo();
        return "Pregunta agregada (ID: " + p.getId() + "). Esperando aprobación.";
    }

    public String modificarPregunta(String id, String nTexto, String nResp, CategoriaTrivia nCat) {
        Optional<PreguntaDetallada> optP = preguntasGestionadas.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (optP.isEmpty()) return "Error: Pregunta no encontrada.";
        PreguntaDetallada p = optP.get();
        if (p.getEstado() == EstadoPregunta.APROBADA) return "Error: Preguntas APROBADAS no se pueden modificar.";

        if (nTexto != null && !nTexto.trim().isEmpty()) p.setPregunta(nTexto.trim());
        if (nResp != null && !nResp.trim().isEmpty()) p.setRespuesta(nResp.trim());
        if (nCat != null) p.setCategoria(nCat);

        guardarPreguntasGestionEnArchivo();
        return "Pregunta ID " + id + " modificada.";
    }

    public String eliminarPregunta(String id) {
        boolean exito = preguntasGestionadas.removeIf(p -> p.getId().equals(id));
        if (exito) {
            guardarPreguntasGestionEnArchivo();
            return "Pregunta ID " + id + " eliminada.";
        }
        return "Error: Pregunta no encontrada para eliminar.";
    }

    public List<PreguntaDetallada> consultarTodasLasPreguntas() {
        return new ArrayList<>(preguntasGestionadas);
    }

    // ... El resto de los métodos (buscar, consultar por estado, cambiar estado) se mantienen igual que en la versión anterior ...
    public Optional<PreguntaDetallada> buscarPreguntaPorId(String id) {
        return preguntasGestionadas.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<PreguntaDetallada> consultarPreguntasPorEstado(EstadoPregunta estado) {
        return preguntasGestionadas.stream().filter(p -> p.getEstado() == estado).collect(Collectors.toList());
    }

    public List<PreguntaDetallada> consultarPreguntasParaAprobar(String emailUsuarioActual) {
        return preguntasGestionadas.stream()
                .filter(p -> p.getEstado() == EstadoPregunta.ESPERANDO_APROBACION &&
                        p.getUsuarioCreadorEmail() != null &&
                        !p.getUsuarioCreadorEmail().equals(emailUsuarioActual))
                .collect(Collectors.toList());
    }

    public String cambiarEstadoPregunta(String id, EstadoPregunta nuevoEstado, String emailUsuarioGestor) {
        Optional<PreguntaDetallada> optP = buscarPreguntaPorId(id);
        if (optP.isEmpty()) return "Error: Pregunta no encontrada.";
        PreguntaDetallada p = optP.get();

        if ((nuevoEstado == EstadoPregunta.APROBADA || nuevoEstado == EstadoPregunta.RECHAZADA) &&
                p.getUsuarioCreadorEmail() != null &&
                p.getUsuarioCreadorEmail().equals(emailUsuarioGestor)) {
            return "Error: No puede aprobar/rechazar sus propias preguntas.";
        }

        if (p.getEstado() != EstadoPregunta.ESPERANDO_APROBACION && (nuevoEstado == EstadoPregunta.APROBADA || nuevoEstado == EstadoPregunta.RECHAZADA)) {
            return "Error: Solo se pueden aprobar o rechazar preguntas que estén en 'ESPERANDO_APROBACION'.";
        }

        p.setEstado(nuevoEstado);
        guardarPreguntasGestionEnArchivo();
        return "Pregunta ID " + id + " actualizada al estado: " + nuevoEstado;
    }
}
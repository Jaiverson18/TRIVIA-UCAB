package com.ucab.trivia.config;

import com.ucab.trivia.config.utils.ConsolaUtilConfig; // Asegúrate que la importación es la correcta
import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.EstadoPregunta;
import com.ucab.trivia.domain.PreguntaDetallada;

import java.util.List;
import java.util.Optional;

/**
 * Clase principal para la Aplicación de Configuración de TRIVIA-UCAB.
 * Permite a los usuarios registrarse, iniciar sesión y gestionar preguntas.
 *
 * @author (Tu Nombre/Equipo - Ricardo)
 * @version 1.1 // Versión con corrección de llamadas a ConsolaUtil
 * @since 2025-06-02
 */
public class AppConfig {
    private final ServicioUsuarios servicioUsuarios;
    private final ServicioPreguntasConfig servicioPreguntasConfig;
    private String emailUsuarioLogueado;

    public AppConfig() {
        this.servicioUsuarios = new ServicioUsuarios();
        this.servicioPreguntasConfig = new ServicioPreguntasConfig();
    }

    public static void main(String[] args) {
        new AppConfig().iniciarAplicacion();
    }

    public void iniciarAplicacion() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("Bienvenido a la Aplicación de Configuración TRIVIA-UCAB");
        while (true) {
            if (emailUsuarioLogueado == null) {
                mostrarMenuAutenticacion();
            } else {
                mostrarMenuPrincipalLogueado();
            }
        }
    }

    private void mostrarMenuAutenticacion() {
        ConsolaUtilConfig.mostrarMensaje("\n--- Autenticación ---");
        ConsolaUtilConfig.mostrarMensaje("1. Iniciar Sesión");
        ConsolaUtilConfig.mostrarMensaje("2. Registrar Nuevo Usuario");
        ConsolaUtilConfig.mostrarMensaje("3. Salir");
        int opcion = ConsolaUtilConfig.leerInt("Seleccione una opción", 1, 3);

        switch (opcion) {
            case 1: manejarInicioSesion(); break;
            case 2: manejarRegistroUsuario(); break;
            case 3: System.exit(0); break;
        }
    }

    private void manejarInicioSesion() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Iniciar Sesión ---");
        String correo = ConsolaUtilConfig.leerString("Correo Electrónico");
        String contrasena = ConsolaUtilConfig.leerString("Contraseña");
        String correoAutenticado = servicioUsuarios.autenticarUsuario(correo, contrasena);
        if (correoAutenticado != null) {
            emailUsuarioLogueado = correoAutenticado;
            ConsolaUtilConfig.mostrarMensaje("\n¡Inicio de sesión exitoso! Bienvenido " + emailUsuarioLogueado + ".");
        } else {
            ConsolaUtilConfig.mostrarMensaje("\n>> Error: Correo o contraseña incorrectos.");
        }
        ConsolaUtilConfig.presionaEnterParaContinuar();
    }

    private void manejarRegistroUsuario() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Registrar Nuevo Usuario ---");
        String correo = ConsolaUtilConfig.leerString("Correo Electrónico (ej: usuario@dominio.com)");
        String contrasena = ConsolaUtilConfig.leerString("Contraseña (exactamente 6 caracteres)");
        String repetirContrasena = ConsolaUtilConfig.leerString("Repetir Contraseña");
        String resultado = servicioUsuarios.registrarUsuario(correo, contrasena, repetirContrasena);
        ConsolaUtilConfig.mostrarMensaje(resultado);
        ConsolaUtilConfig.presionaEnterParaContinuar();
    }

    private void mostrarMenuPrincipalLogueado() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("\n--- Menú Principal (Usuario: " + emailUsuarioLogueado + ") ---");
        ConsolaUtilConfig.mostrarMensaje("1. Gestión de Preguntas");
        ConsolaUtilConfig.mostrarMensaje("2. Importar Preguntas Originales");
        ConsolaUtilConfig.mostrarMensaje("3. Cerrar Sesión");
        int opcion = ConsolaUtilConfig.leerInt("Seleccione una opción", 1, 3);

        switch (opcion) {
            case 1: manejarMenuGestionPreguntas(); break;
            case 2:
                servicioPreguntasConfig.importarPreguntasDesdeJsonOriginal();
                ConsolaUtilConfig.presionaEnterParaContinuar();
                break;
            case 3:
                emailUsuarioLogueado = null;
                ConsolaUtilConfig.mostrarMensaje("Sesión cerrada.");
                ConsolaUtilConfig.presionaEnterParaContinuar();
                break;
        }
    }

    private void manejarMenuGestionPreguntas() {
        boolean continuar = true;
        while (continuar) {
            ConsolaUtilConfig.limpiarConsola();
            ConsolaUtilConfig.mostrarMensaje("\n--- Gestión de Preguntas ---");
            ConsolaUtilConfig.mostrarMensaje("1. Agregar Nueva Pregunta");
            ConsolaUtilConfig.mostrarMensaje("2. Modificar Pregunta Existente");
            ConsolaUtilConfig.mostrarMensaje("3. Eliminar Pregunta");
            ConsolaUtilConfig.mostrarMensaje("4. Consultar Preguntas (Todas)");
            ConsolaUtilConfig.mostrarMensaje("5. Consultar Preguntas por Estado");
            ConsolaUtilConfig.mostrarMensaje("6. Aprobar/Rechazar Preguntas Pendientes");
            ConsolaUtilConfig.mostrarMensaje("7. Volver al Menú Principal");
            int opcion = ConsolaUtilConfig.leerInt("Seleccione una opción", 1, 7);

            switch (opcion) {
                case 1: uiAgregarPregunta(); break;
                case 2: uiModificarPregunta(); break;
                case 3: uiEliminarPregunta(); break;
                case 4: uiConsultarTodas(); break;
                case 5: uiConsultarPorEstado(); break;
                case 6: uiAprobarRechazar(); break;
                case 7: continuar = false; break;
            }
            if(continuar) ConsolaUtilConfig.presionaEnterParaContinuar();
        }
    }

    private void uiAgregarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Agregar Nueva Pregunta ---");
        String texto = ConsolaUtilConfig.leerString("Texto de la pregunta");
        String respuesta = ConsolaUtilConfig.leerString("Respuesta correcta");
        CategoriaTrivia categoria = uiSeleccionarCategoria(null);
        if (categoria == null) return;
        String resultado = servicioPreguntasConfig.agregarPregunta(texto, respuesta, categoria, emailUsuarioLogueado);
        ConsolaUtilConfig.mostrarMensaje(resultado);
    }

    private CategoriaTrivia uiSeleccionarCategoria(CategoriaTrivia actual) {
        String prompt = "\nSeleccione la categoría";
        if(actual != null) prompt += " (actual: " + actual.getNombreMostrado() + ")";
        ConsolaUtilConfig.mostrarMensaje(prompt + ":");
        CategoriaTrivia[] categorias = CategoriaTrivia.values();
        for (int i = 0; i < categorias.length; i++) {
            ConsolaUtilConfig.mostrarMensaje((i + 1) + ". " + categorias[i].getNombreMostrado());
        }
        int maxOpcion = categorias.length;
        if (actual != null) {
            maxOpcion++;
            ConsolaUtilConfig.mostrarMensaje(maxOpcion + ". No cambiar");
        }
        int opcion = ConsolaUtilConfig.leerInt("Categoría", 1, maxOpcion);
        if (actual != null && opcion == maxOpcion) return actual;
        return categorias[opcion - 1];
    }

    private void uiModificarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Modificar Pregunta ---");
        String id = ConsolaUtilConfig.leerString("ID de la pregunta a modificar");
        Optional<PreguntaDetallada> optP = servicioPreguntasConfig.buscarPreguntaPorId(id);
        if (optP.isEmpty()) { ConsolaUtilConfig.mostrarMensaje("Pregunta no encontrada."); return; }
        PreguntaDetallada pActual = optP.get();
        if (pActual.getEstado() == EstadoPregunta.APROBADA) { ConsolaUtilConfig.mostrarMensaje("No se pueden modificar preguntas APROBADAS."); return; }

        ConsolaUtilConfig.mostrarMensaje("Modificando: " + pActual);
        String nTexto = ConsolaUtilConfig.leerString("Nuevo texto (Enter para no cambiar)");
        String nResp = ConsolaUtilConfig.leerString("Nueva respuesta (Enter para no cambiar)");
        CategoriaTrivia nCat = uiSeleccionarCategoria(pActual.getCategoria());

        String resultado = servicioPreguntasConfig.modificarPregunta(id, nTexto, nResp, nCat);
        ConsolaUtilConfig.mostrarMensaje(resultado);
    }

    private void uiEliminarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Eliminar Pregunta ---"); // CORREGIDO
        String id = ConsolaUtilConfig.leerString("ID de la pregunta a eliminar"); // CORREGIDO
        String confirmacion = ConsolaUtilConfig.leerString("¿Seguro que desea eliminar la pregunta ID '"+id+"'? (S/N)").toUpperCase(); // CORREGIDO
        if (confirmacion.equals("S")) {
            ConsolaUtilConfig.mostrarMensaje(servicioPreguntasConfig.eliminarPregunta(id)); // CORREGIDO
        } else {
            ConsolaUtilConfig.mostrarMensaje("Eliminación cancelada."); // CORREGIDO
        }
    }

    private void uiConsultarTodas() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Todas las Preguntas ---"); // CORREGIDO
        List<PreguntaDetallada> todas = servicioPreguntasConfig.consultarTodasLasPreguntas();
        if (todas.isEmpty()) { ConsolaUtilConfig.mostrarMensaje("No hay preguntas."); return; } // CORREGIDO
        todas.forEach(p -> ConsolaUtilConfig.mostrarMensaje(p.toString() + " [Creador: " + p.getUsuarioCreadorEmail() + "]")); // CORREGIDO
    }

    private void uiConsultarPorEstado() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Consultar por Estado ---"); // CORREGIDO
        EstadoPregunta[] estados = EstadoPregunta.values();
        for(int i=0; i<estados.length; i++) { ConsolaUtilConfig.mostrarMensaje((i+1) + ". " + estados[i]); } // CORREGIDO
        int opcion = ConsolaUtilConfig.leerInt("Seleccione estado", 1, estados.length); // CORREGIDO
        EstadoPregunta estSel = estados[opcion-1];
        List<PreguntaDetallada> filtradas = servicioPreguntasConfig.consultarPreguntasPorEstado(estSel);
        ConsolaUtilConfig.mostrarMensaje("\n--- Preguntas en estado: " + estSel + " ---"); // CORREGIDO
        if (filtradas.isEmpty()) { ConsolaUtilConfig.mostrarMensaje("Ninguna."); return; } // CORREGIDO
        filtradas.forEach(p -> ConsolaUtilConfig.mostrarMensaje(p.toString() + " [Creador: " + p.getUsuarioCreadorEmail() + "]")); // CORREGIDO
    }

    private void uiAprobarRechazar() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Aprobar/Rechazar Preguntas Pendientes ---"); // CORREGIDO
        List<PreguntaDetallada> pendientes = servicioPreguntasConfig.consultarPreguntasParaAprobar(emailUsuarioLogueado);
        if (pendientes.isEmpty()) { ConsolaUtilConfig.mostrarMensaje("No hay preguntas de otros usuarios pendientes de su gestión."); return; } // CORREGIDO

        for (int i = 0; i < pendientes.size(); i++) {
            ConsolaUtilConfig.mostrarMensaje((i + 1) + ". " + pendientes.get(i).toString()); // CORREGIDO
        }
        int numSel = ConsolaUtilConfig.leerInt("Número de pregunta a gestionar (0 para cancelar)", 0, pendientes.size()); // CORREGIDO
        if (numSel == 0) return;
        PreguntaDetallada pSel = pendientes.get(numSel - 1);

        ConsolaUtilConfig.mostrarMensaje("Gestionando ID: " + pSel.getId() + "\n1. Aprobar | 2. Rechazar | 3. Cancelar"); // CORREGIDO
        int accion = ConsolaUtilConfig.leerInt("Acción", 1, 3); // CORREGIDO
        String resultado = "Acción cancelada.";
        if (accion == 1) resultado = servicioPreguntasConfig.cambiarEstadoPregunta(pSel.getId(), EstadoPregunta.APROBADA, emailUsuarioLogueado);
        else if (accion == 2) resultado = servicioPreguntasConfig.cambiarEstadoPregunta(pSel.getId(), EstadoPregunta.RECHAZADA, emailUsuarioLogueado);
        ConsolaUtilConfig.mostrarMensaje(resultado); // CORREGIDO
    }
}
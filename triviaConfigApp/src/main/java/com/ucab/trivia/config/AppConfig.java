package com.ucab.trivia.config;

import com.ucab.trivia.config.utils.ConsolaUtilConfig;
import com.ucab.trivia.domain.CategoriaTrivia;
import com.ucab.trivia.domain.EstadoPregunta;
import com.ucab.trivia.domain.PreguntaDetallada;

import java.util.List;
import java.util.Optional;


//Clase principal para la Aplicacion de Configuracion de TRIVIA-UCAB.
//Permite a los usuarios registrarse, iniciar sesion y gestionar preguntas.

public class AppConfig {
    private final ServicioUsuarios servicioUsuarios;
    private final ServicioPreguntasConfig servicioPreguntasConfig;
    private String emailUsuarioLogueado; // Almacena el email en texto plano del usuario actual


    //Constructor de AppConfig. Inicializa los servicios necesarios.

    public AppConfig() {
        this.servicioUsuarios = new ServicioUsuarios();
        this.servicioPreguntasConfig = new ServicioPreguntasConfig();
        // La importacion de preguntas originales se intenta en el constructor de ServicioPreguntasConfig
    }


    //Punto de entrada principal de la aplicacion de configuracion.

    public static void main(String[] args) {
        AppConfig app = new AppConfig();
        app.iniciarAplicacion();
    }

    //Inicia el bucle principal de la aplicacion, mostrando menus segun el estado de autenticacion.

    public void iniciarAplicacion() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("=====================================================");
        ConsolaUtilConfig.mostrarMensaje("  Bienvenido a la Aplicacion de Configuracion        ");
        ConsolaUtilConfig.mostrarMensaje("                TRIVIA-UCAB                          ");
        ConsolaUtilConfig.mostrarMensaje("=====================================================");
        while (true) {
            if (emailUsuarioLogueado == null) {
                mostrarMenuAutenticacion();
            } else {
                mostrarMenuPrincipalLogueado();
            }
        }
    }

    //Muestra el menu de autenticacion (Iniciar Sesion, Registrarse, Salir).

    private void mostrarMenuAutenticacion() {
        ConsolaUtilConfig.mostrarMensaje("\n--- Menu de Autenticacion ---");
        ConsolaUtilConfig.mostrarMensaje("1. Iniciar Sesion");
        ConsolaUtilConfig.mostrarMensaje("2. Registrar Nuevo Usuario");
        ConsolaUtilConfig.mostrarMensaje("3. Salir de la Aplicacion");
        int opcion = ConsolaUtilConfig.leerInt("Seleccione una opcion", 1, 3);

        switch (opcion) {
            case 1: manejarInicioSesion(); break;
            case 2: manejarRegistroUsuario(); break;
            case 3: manejarSalirAplicacion(); break;
        }
    }

    //Maneja la logica para el inicio de sesion de un usuario.

    private void manejarInicioSesion() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Iniciar Sesion ---");
        String correo = ConsolaUtilConfig.leerString("Correo Electronico");
        String contrasena = ConsolaUtilConfig.leerString("Contrasena");
        String correoAutenticado = servicioUsuarios.autenticarUsuario(correo, contrasena);
        if (correoAutenticado != null) {
            emailUsuarioLogueado = correoAutenticado;
            ConsolaUtilConfig.mostrarMensaje("\n¡Inicio de sesion exitoso! Bienvenido de nuevo, " + emailUsuarioLogueado + ".");
        } else {
            ConsolaUtilConfig.mostrarMensaje("\n>> Error: Correo electronico o contrasena incorrectos.");
        }
        ConsolaUtilConfig.presionaEnterParaContinuar();
    }

    //Maneja la logica para el registro de un nuevo usuario.

    private void manejarRegistroUsuario() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Registrar Nuevo Usuario ---");
        String correo = ConsolaUtilConfig.leerString("Ingrese su Correo Electronico (ej: usuario@dominio.com)");
        String contrasena = ConsolaUtilConfig.leerString("Ingrese su Contrasena (debe tener exactamente 6 caracteres)");
        String repetirContrasena = ConsolaUtilConfig.leerString("Repita su Contrasena");
        String resultadoRegistro = servicioUsuarios.registrarUsuario(correo, contrasena, repetirContrasena);
        ConsolaUtilConfig.mostrarMensaje(resultadoRegistro);
        ConsolaUtilConfig.presionaEnterParaContinuar();
    }


    //Maneja la salida de la aplicacion.

    private void manejarSalirAplicacion() {
        ConsolaUtilConfig.mostrarMensaje("\nGracias por usar la aplicacion de configuracion. ¡Hasta pronto!");
        System.exit(0);
    }


    //Muestra el menu principal para un usuario que ha iniciado sesion.

    private void mostrarMenuPrincipalLogueado() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("\n--- Menu Principal (Usuario Logueado: " + emailUsuarioLogueado + ") ---");
        ConsolaUtilConfig.mostrarMensaje("1. Gestion de Preguntas");
        ConsolaUtilConfig.mostrarMensaje("2. Forzar Importacion de Preguntas Originales (PRECAUCION: Puede duplicar si ya existen)");
        ConsolaUtilConfig.mostrarMensaje("3. Cerrar Sesion");
        int opcion = ConsolaUtilConfig.leerInt("Seleccione una opcion", 1, 3);

        switch (opcion) {
            case 1: manejarMenuGestionPreguntas(); break;
            case 2:
                ConsolaUtilConfig.mostrarMensaje("\nIntentando importar preguntas desde el archivo JSON original...")
                servicioPreguntasConfig.importarPreguntasDesdeJsonOriginal();
                ConsolaUtilConfig.presionaEnterParaContinuar();
                break;
            case 3:
                emailUsuarioLogueado = null;
                ConsolaUtilConfig.mostrarMensaje("Sesion cerrada exitosamente.");
                ConsolaUtilConfig.presionaEnterParaContinuar();
                ConsolaUtilConfig.limpiarConsola(); // Limpiar para volver al menu de autenticacion
                break;
        }
    }


    //Muestra y maneja el submenu para la gestion de preguntas.

    private void manejarMenuGestionPreguntas() {
        boolean continuarEnEsteMenu = true;
        while (continuarEnEsteMenu) {
            ConsolaUtilConfig.limpiarConsola();
            ConsolaUtilConfig.mostrarMensaje("\n--- Submenu: Gestion de Preguntas (Usuario: " + emailUsuarioLogueado + ") ---");
            ConsolaUtilConfig.mostrarMensaje("1. Agregar Nueva Pregunta");
            ConsolaUtilConfig.mostrarMensaje("2. Modificar Pregunta Existente (Texto, Respuesta, Categoria, Tiempo)");
            ConsolaUtilConfig.mostrarMensaje("3. Modificar Unicamente el Tiempo Maximo de Respuesta de una Pregunta");
            ConsolaUtilConfig.mostrarMensaje("4. Eliminar Pregunta");
            ConsolaUtilConfig.mostrarMensaje("5. Consultar Todas las Preguntas Registradas");
            ConsolaUtilConfig.mostrarMensaje("6. Consultar Preguntas por Estado Especifico");
            ConsolaUtilConfig.mostrarMensaje("7. Aprobar o Rechazar Preguntas Pendientes (creadas por otros usuarios)");
            ConsolaUtilConfig.mostrarMensaje("8. Volver al Menu Principal");
            int opcion = ConsolaUtilConfig.leerInt("Seleccione una opcion del submenu", 1, 8);

            switch (opcion) {
                case 1: uiSubMenuAgregarPregunta(); break;
                case 2: uiSubMenuModificarPregunta(); break;
                case 3: uiSubMenuModificarTiempoPregunta(); break;
                case 4: uiSubMenuEliminarPregunta(); break;
                case 5: uiSubMenuConsultarTodasLasPreguntas(); break;
                case 6: uiSubMenuConsultarPreguntasPorEstado(); break;
                case 7: uiSubMenuAprobarRechazarPreguntas(); break;
                case 8: continuarEnEsteMenu = false; break; // Sale de este submenu
            }
            if(continuarEnEsteMenu) ConsolaUtilConfig.presionaEnterParaContinuar(); // Pausa antes de volver a mostrar el submenu
        }
    }

    // Flujo de UI para agregar una nueva pregunta.
    private void uiSubMenuAgregarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Agregar Nueva Pregunta ---");
        String texto = ConsolaUtilConfig.leerString("Ingrese el texto de la nueva pregunta");
        String respuesta = ConsolaUtilConfig.leerString("Ingrese la respuesta correcta");
        CategoriaTrivia categoriaSeleccionada = uiSubMenuSeleccionarCategoria(null); // null indica que no hay categoria actual
        if (categoriaSeleccionada == null) { ConsolaUtilConfig.mostrarMensaje(">> Operacion cancelada: No se selecciono categoria."); return; }
        int tiempoMax = ConsolaUtilConfig.leerInt("Ingrese el tiempo maximo de respuesta en segundos (ej. 30)", 5, 300);
        String resultado = servicioPreguntasConfig.agregarPregunta(texto, respuesta, categoriaSeleccionada, tiempoMax, emailUsuarioLogueado);
        ConsolaUtilConfig.mostrarMensaje(resultado);
    }

    //Flujo de UI para seleccionar una categoria, permitiendo no cambiar una actual si se provee.

    private CategoriaTrivia uiSubMenuSeleccionarCategoria(CategoriaTrivia categoriaActual) {
        String mensajePrompt = "\nSeleccione la categoria";
        if (categoriaActual != null) {
            mensajePrompt += " (actual: " + categoriaActual.getNombreMostrado() + ")";
        }
        ConsolaUtilConfig.mostrarMensaje(mensajePrompt + ":");

        CategoriaTrivia[] todasLasCategorias = CategoriaTrivia.values();
        for (int i = 0; i < todasLasCategorias.length; i++) {
            ConsolaUtilConfig.mostrarMensaje((i + 1) + ". " + todasLasCategorias[i].getNombreMostrado());
        }

        int opcionNoCambiar = todasLasCategorias.length + 1;
        if (categoriaActual != null) {
            ConsolaUtilConfig.mostrarMensaje(opcionNoCambiar + ". No cambiar categoria actual (" + categoriaActual.getNombreMostrado() + ")");
        }

        int minOpcionValida = 1;
        int maxOpcionValida = categoriaActual != null ? opcionNoCambiar : todasLasCategorias.length;

        int opcionElegida = ConsolaUtilConfig.leerInt("Opcion de Categoria", minOpcionValida, maxOpcionValida);

        if (categoriaActual != null && opcionElegida == opcionNoCambiar) return categoriaActual; // Elige no cambiar
        if (opcionElegida >= minOpcionValida && opcionElegida <= todasLasCategorias.length) return todasLasCategorias[opcionElegida - 1]; // Seleccion valida

        ConsolaUtilConfig.mostrarMensaje(">> Seleccion de categoria invalida o cancelada.");
        return categoriaActual; // Devuelve la actual si la nueva seleccion fallo, o null si no habia actual
    }

    //Flujo de UI para modificar una pregunta existente.
    private void uiSubMenuModificarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Modificar Pregunta Existente ---");
        String idPregunta = ConsolaUtilConfig.leerString("Ingrese el ID de la pregunta que desea modificar");
        Optional<PreguntaDetallada> optPregunta = servicioPreguntasConfig.buscarPreguntaPorId(idPregunta);
        if (optPregunta.isEmpty()) { ConsolaUtilConfig.mostrarMensaje(">> No se encontro una pregunta con el ID '" + idPregunta + "'."); return; }

        PreguntaDetallada preguntaActual = optPregunta.get();
        if (preguntaActual.getEstado() == EstadoPregunta.APROBADA) {
            ConsolaUtilConfig.mostrarMensaje(">> Las preguntas que ya han sido APROBADAS no pueden ser modificadas (solo eliminadas).");
            return;
        }

        ConsolaUtilConfig.mostrarMensaje("Modificando pregunta con ID: " + idPregunta);
        ConsolaUtilConfig.mostrarMensaje("Texto actual: " + preguntaActual.getPregunta());
        String nuevoTexto = ConsolaUtilConfig.leerString("Nuevo texto (deje vacio para no cambiar)");

        ConsolaUtilConfig.mostrarMensaje("Respuesta actual: " + preguntaActual.getRespuesta());
        String nuevaRespuesta = ConsolaUtilConfig.leerString("Nueva respuesta (deje vacio para no cambiar)");

        CategoriaTrivia nuevaCategoria = uiSubMenuSeleccionarCategoria(preguntaActual.getCategoria());

        ConsolaUtilConfig.mostrarMensaje("Tiempo maximo actual: " + preguntaActual.getTiempoMaximoRespuestaSegundos() + "s");
        int nuevoTiempo = ConsolaUtilConfig.leerInt("Nuevo tiempo maximo en segundos (ingrese 0 para no cambiar)", 0, 300);

        String resultado = servicioPreguntasConfig.modificarPregunta(idPregunta,
                nuevoTexto, // El servicio manejara si es vacio
                nuevaRespuesta,
                nuevaCategoria,
                nuevoTiempo);
        ConsolaUtilConfig.mostrarMensaje(resultado);
    }

    //Flujo de UI para modificar solo el tiempo de una pregunta.
    private void uiSubMenuModificarTiempoPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Modificar Tiempo Maximo de Respuesta de una Pregunta ---");
        String idPregunta = ConsolaUtilConfig.leerString("Ingrese el ID de la pregunta para modificar su tiempo");
        Optional<PreguntaDetallada> optPregunta = servicioPreguntasConfig.buscarPreguntaPorId(idPregunta);
        if (optPregunta.isEmpty()) { ConsolaUtilConfig.mostrarMensaje(">> Pregunta no encontrada."); return; }

        PreguntaDetallada pregunta = optPregunta.get();
        if (pregunta.getEstado() == EstadoPregunta.APROBADA) {
            ConsolaUtilConfig.mostrarMensaje(">> No se puede modificar el tiempo de una pregunta que ya esta APROBADA.");
            return;
        }

        ConsolaUtilConfig.mostrarMensaje("Pregunta: " + pregunta.getPregunta().substring(0, Math.min(30, pregunta.getPregunta().length())) + "...");
        ConsolaUtilConfig.mostrarMensaje("Tiempo maximo actual: " + pregunta.getTiempoMaximoRespuestaSegundos() + "s");
        int nuevoTiempoSegundos = ConsolaUtilConfig.leerInt("Ingrese el nuevo tiempo maximo en segundos", 5, 300);
        String resultado = servicioPreguntasConfig.modificarTiempoPregunta(idPregunta, nuevoTiempoSegundos);
        ConsolaUtilConfig.mostrarMensaje(resultado);
    }

    //Flujo de UI para eliminar una pregunta.
    private void uiSubMenuEliminarPregunta() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Eliminar Pregunta ---");
        String idPregunta = ConsolaUtilConfig.leerString("Ingrese el ID de la pregunta que desea eliminar");
        Optional<PreguntaDetallada> optPregunta = servicioPreguntasConfig.buscarPreguntaPorId(idPregunta);
        if (optPregunta.isEmpty()) { ConsolaUtilConfig.mostrarMensaje(">> Pregunta no encontrada."); return; }

        ConsolaUtilConfig.mostrarMensaje("Pregunta a eliminar: " + optPregunta.get().getPregunta());
        String confirmacion = ConsolaUtilConfig.leerString("¿Esta seguro que desea eliminar esta pregunta? (S/N)").toUpperCase();
        if (confirmacion.equals("S")) {
            ConsolaUtilConfig.mostrarMensaje(servicioPreguntasConfig.eliminarPregunta(idPregunta));
        } else {
            ConsolaUtilConfig.mostrarMensaje("Eliminacion cancelada.");
        }
    }

    //Flujo de UI para consultar todas las preguntas.
    private void uiSubMenuConsultarTodasLasPreguntas() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Listado de Todas las Preguntas Registradas ---");
        List<PreguntaDetallada> todasLasPreguntas = servicioPreguntasConfig.consultarTodasLasPreguntas();
        if (todasLasPreguntas.isEmpty()) {
            ConsolaUtilConfig.mostrarMensaje("No hay preguntas registradas en el sistema.");
            return;
        }
        for(PreguntaDetallada pregunta : todasLasPreguntas) {
            ConsolaUtilConfig.mostrarMensaje(pregunta.toString() + " [Creador: " + pregunta.getUsuarioCreadorEmail() + "]");
        }
    }

    //Flujo de UI para consultar preguntas por un estado especifico.
    private void uiSubMenuConsultarPreguntasPorEstado() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Consultar Preguntas por Estado ---");
        EstadoPregunta[] todosLosEstados = EstadoPregunta.values();
        for(int i=0; i < todosLosEstados.length; i++) {
            ConsolaUtilConfig.mostrarMensaje((i+1) + ". " + todosLosEstados[i]);
        }
        int opcionEstado = ConsolaUtilConfig.leerInt("Seleccione el estado por el cual desea filtrar", 1, todosLosEstados.length);
        EstadoPregunta estadoSeleccionado = todosLosEstados[opcionEstado-1];

        List<PreguntaDetallada> preguntasFiltradas = servicioPreguntasConfig.consultarPreguntasPorEstado(estadoSeleccionado);
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("\n--- Preguntas en estado: " + estadoSeleccionado + " ---");
        if (preguntasFiltradas.isEmpty()) {
            ConsolaUtilConfig.mostrarMensaje("No se encontraron preguntas en el estado '" + estadoSeleccionado + "'.");
            return;
        }
        for(PreguntaDetallada pregunta : preguntasFiltradas) {
            ConsolaUtilConfig.mostrarMensaje(pregunta.toString() + " [Creador: " + pregunta.getUsuarioCreadorEmail() + "]");
        }
    }

    //Flujp de UI para aprobar o rechazar preguntas pendientes.
    private void uiSubMenuAprobarRechazarPreguntas() {
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("--- Aprobar/Rechazar Preguntas Pendientes (creadas por otros usuarios) ---");
        List<PreguntaDetallada> preguntasParaGestionar = servicioPreguntasConfig.consultarPreguntasParaAprobar(emailUsuarioLogueado);

        if (preguntasParaGestionar.isEmpty()) {
            ConsolaUtilConfig.mostrarMensaje("No hay preguntas de otros usuarios que esten actualmente pendientes de su gestion.");
            return;
        }

        ConsolaUtilConfig.mostrarMensaje("Preguntas pendientes para su revision:");
        for (int i = 0; i < preguntasParaGestionar.size(); i++) {
            PreguntaDetallada p = preguntasParaGestionar.get(i);
            ConsolaUtilConfig.mostrarMensaje((i + 1) + ". " + p.toString() + " (Creada por: " + p.getUsuarioCreadorEmail() + ")");
        }

        int numeroSeleccionado = ConsolaUtilConfig.leerInt("Ingrese el numero de la pregunta que desea gestionar (0 para cancelar)", 0, preguntasParaGestionar.size());
        if (numeroSeleccionado == 0) {
            ConsolaUtilConfig.mostrarMensaje("Gestion cancelada por el usuario.");
            return;
        }

        PreguntaDetallada preguntaSeleccionada = preguntasParaGestionar.get(numeroSeleccionado - 1);
        ConsolaUtilConfig.limpiarConsola();
        ConsolaUtilConfig.mostrarMensaje("\nDetalles de la pregunta seleccionada (ID: " + preguntaSeleccionada.getId() + "):");
        ConsolaUtilConfig.mostrarMensaje("  Texto: " + preguntaSeleccionada.getPregunta());
        ConsolaUtilConfig.mostrarMensaje("  Respuesta: " + preguntaSeleccionada.getRespuesta());
        ConsolaUtilConfig.mostrarMensaje("  Categoria: " + preguntaSeleccionada.getCategoria().getNombreMostrado());
        ConsolaUtilConfig.mostrarMensaje("  Tiempo Max.: " + preguntaSeleccionada.getTiempoMaximoRespuestaSegundos() + "s");
        ConsolaUtilConfig.mostrarMensaje("  Creador: " + preguntaSeleccionada.getUsuarioCreadorEmail());

        ConsolaUtilConfig.mostrarMensaje("\nAcciones disponibles:");
        ConsolaUtilConfig.mostrarMensaje("1. Aprobar esta Pregunta");
        ConsolaUtilConfig.mostrarMensaje("2. Rechazar esta Pregunta");
        ConsolaUtilConfig.mostrarMensaje("3. Cancelar y volver");
        int accionElegida = ConsolaUtilConfig.leerInt("Seleccione una accion", 1, 3);

        String resultadoAccion = "Accion cancelada por el usuario.";
        if (accionElegida == 1) {
            resultadoAccion = servicioPreguntasConfig.cambiarEstadoPregunta(preguntaSeleccionada.getId(), EstadoPregunta.APROBADA, emailUsuarioLogueado);
        } else if (accionElegida == 2) {
            resultadoAccion = servicioPreguntasConfig.cambiarEstadoPregunta(preguntaSeleccionada.getId(), EstadoPregunta.RECHAZADA, emailUsuarioLogueado);
        }
        ConsolaUtilConfig.mostrarMensaje(resultadoAccion);
    }
}

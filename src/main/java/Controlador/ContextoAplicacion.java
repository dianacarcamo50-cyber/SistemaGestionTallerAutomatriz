package Controlador;

/** Controlador compartido por las ventanas de una misma ejecución. */
public final class ContextoAplicacion {
    private static final TallerControlador CONTROLADOR = new TallerControlador();

    private ContextoAplicacion() {
    }

    public static TallerControlador getControlador() {
        return CONTROLADOR;
    }
}
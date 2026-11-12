package Vista;

import Controlador.TallerControlador;
import Modelo.OrdenTrabajo;
import Modelo.Servicio;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class CanvasReporte2D extends JPanel {
    private final TallerControlador controlador;

    public CanvasReporte2D(TallerControlador controlador) {
        this.controlador = java.util.Objects.requireNonNull(controlador);
        setBackground(Color.WHITE);
    }

    @Override
    public Dimension getPreferredSize() {
        int servicios = controlador.getRepoServicios().obtenerTodos().size();
        return new Dimension(700, Math.max(510, 380 + servicios * 34));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            List<OrdenTrabajo> ordenes = controlador.getRepoOrdenes().obtenerTodos();
            List<Servicio> catalogo = controlador.getRepoServicios().obtenerTodos();
            dibujarEstados(g2, ordenes);
            dibujarIngresos(g2, ordenes, catalogo);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarEstados(Graphics2D g, List<OrdenTrabajo> ordenes) {
        Map<OrdenTrabajo.EstadoOrden, Integer> conteo = new EnumMap<>(OrdenTrabajo.EstadoOrden.class);
        for (OrdenTrabajo.EstadoOrden estado : OrdenTrabajo.EstadoOrden.values()) conteo.put(estado, 0);
        for (OrdenTrabajo orden : ordenes) conteo.merge(orden.getEstado(), 1, Integer::sum);
        g.setColor(Color.BLACK);
        g.drawString("Órdenes por estado", 30, 28);
        if (ordenes.isEmpty()) {
            g.drawString("No hay órdenes registradas.", 30, 58);
            return;
        }
        int base = 220, altura = 145, ancho = Math.max(1, getWidth() - 100);
        int maximo = java.util.Collections.max(conteo.values());
        int paso = ancho / conteo.size();
        String[] etiquetas = {"Pendiente", "En proceso", "Finalizada", "Cancelada"};
        Color[] colores = {Color.ORANGE, new Color(58, 112, 190), new Color(54, 153, 102), new Color(196, 77, 77)};
        g.drawLine(45, base, Math.max(46, getWidth() - 30), base);
        int i = 0;
        for (int cantidad : conteo.values()) {
            int x = 55 + i * paso;
            int alto = maximo == 0 ? 0 : (int) ((long) cantidad * altura / maximo);
            int barra = Math.max(1, Math.min(65, paso - 12));
            g.setColor(colores[i]); g.fillRect(x, base - alto, barra, alto);
            g.setColor(Color.BLACK); g.drawString(String.valueOf(cantidad), x, base - alto - 5);
            g.drawString(etiquetas[i], x, base + 18);
            i++;
        }
    }

    private void dibujarIngresos(Graphics2D g, List<OrdenTrabajo> ordenes, List<Servicio> catalogo) {
        Map<String, BigDecimal> ingresos = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Servicio servicio : catalogo) {
            ingresos.put(servicio.getCodigo(), BigDecimal.ZERO);
            nombres.put(servicio.getCodigo(), servicio.getDescripcion());
        }
        for (OrdenTrabajo orden : ordenes) {
            if (orden.getEstado() != OrdenTrabajo.EstadoOrden.FINALIZADA) continue;
            for (Servicio servicio : orden.getServicios()) {
                nombres.putIfAbsent(servicio.getCodigo(), servicio.getDescripcion());
                ingresos.merge(servicio.getCodigo(), BigDecimal.valueOf(servicio.getPrecioBase()), BigDecimal::add);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Ingresos por servicio: órdenes finalizadas, sin impuestos", 30, 285);
        if (ingresos.isEmpty()) {
            g.drawString("No hay servicios en el catálogo.", 30, 315);
            return;
        }
        BigDecimal maximo = ingresos.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        int y = 315, xBarra = 250, anchoBarra = Math.max(20, getWidth() - xBarra - 110);
        FontMetrics fm = g.getFontMetrics();
        for (Map.Entry<String, BigDecimal> entrada : ingresos.entrySet()) {
            String etiqueta = entrada.getKey() + " - " + nombres.get(entrada.getKey());
            while (etiqueta.length() > 1 && fm.stringWidth(etiqueta) > xBarra - 40) {
                etiqueta = etiqueta.substring(0, etiqueta.length() - 1);
            }
            BigDecimal valor = entrada.getValue();
            int largo = maximo.signum() == 0 ? 0 : valor.multiply(BigDecimal.valueOf(anchoBarra))
                    .divide(maximo, 0, RoundingMode.HALF_UP).intValue();
            g.setColor(Color.BLACK); g.drawString(etiqueta, 30, y + 14);
            g.setColor(new Color(58, 112, 190)); g.fillRect(xBarra, y, largo, 18);
            g.setColor(Color.BLACK);
            g.drawString("L. " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString(), xBarra + largo + 5, y + 14);
            y += 34;
        }
    }
}
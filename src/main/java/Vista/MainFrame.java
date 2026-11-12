/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;
import Controlador.TallerControlador;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Garme
 */

public class MainFrame extends JFrame {
    private final TallerControlador controlador = Controlador.ContextoAplicacion.getControlador();
    private final CanvasReporte2D canvasReporte = new CanvasReporte2D(controlador);
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame() {
        setTitle("AutoTech Solutions - Taller Automotriz");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel inicio = new JPanel(new GridBagLayout());
        JButton btnInventario = new JButton("Abrir inventario");
        btnInventario.addActionListener(e -> new Vista.Inventario(controlador).setVisible(true));
        inicio.add(btnInventario);
        JButton btnOrdenes = new JButton("Crear órdenes");
        btnOrdenes.addActionListener(e -> new CrearOrdenes(controlador, this::actualizarReporte).setVisible(true));
        inicio.add(btnOrdenes);
        JButton btnClientes = new JButton("Clientes");
        btnClientes.addActionListener(e -> new Vista.Cliente(controlador).setVisible(true));
        inicio.add(btnClientes);
        JButton btnVehiculos = new JButton("Vehículos");
        btnVehiculos.addActionListener(e -> new Vista.Vehiculo(controlador).setVisible(true));
        inicio.add(btnVehiculos);
        JButton btnServicios = new JButton("Servicios");
        btnServicios.addActionListener(e -> new ServiciosMecanicos(controlador).setVisible(true));
        inicio.add(btnServicios);
        JButton btnFacturacion = new JButton("Facturación");
        btnFacturacion.addActionListener(e -> new Facturacion(controlador).setVisible(true));
        inicio.add(btnFacturacion);
        tabs.addTab("Inicio", inicio);

        actualizarReporte();
        JScrollPane reporteScroll = new JScrollPane(canvasReporte);
        tabs.addTab("Reporte Java 2D", reporteScroll);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == reporteScroll) actualizarReporte();
        });

        add(tabs, BorderLayout.CENTER);
        
        controlador.verificarAlertasInventario();
        setVisible(true); 
    }


    private void actualizarReporte() {
        canvasReporte.revalidate();
        canvasReporte.repaint();
    }

}

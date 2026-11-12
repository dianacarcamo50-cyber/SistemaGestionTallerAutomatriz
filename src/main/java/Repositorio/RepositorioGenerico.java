package Repositorio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class RepositorioGenerico<K, T> {
    private final HashMap<K, T> mapa = new HashMap<>();
    private final ArrayList<K> claves = new ArrayList<>();
    private final ArrayList<T> lista = new ArrayList<>();
    private final Stack<Estado<K, T>> historial = new Stack<>();

    private static final class Estado<K, T> {
        private final Map<K, T> mapa;
        private final List<K> claves;
        private final List<T> lista;

        private Estado(Map<K, T> mapa, List<K> claves, List<T> lista) {
            this.mapa = new HashMap<>(mapa);
            this.claves = new ArrayList<>(claves);
            this.lista = new ArrayList<>(lista);
        }
    }

    public synchronized void guardar(K clave, T objeto) {
        Objects.requireNonNull(clave, "La clave no puede ser nula.");
        Objects.requireNonNull(objeto, "El objeto no puede ser nulo.");
        int indice = claves.indexOf(clave);
        historial.push(new Estado<>(mapa, claves, lista));
        if (indice >= 0) {
            lista.set(indice, objeto);
        } else {
            claves.add(clave);
            lista.add(objeto);
        }
        mapa.put(clave, objeto);
    }

    public synchronized T buscar(K clave) {
        return mapa.get(clave);
    }

    public synchronized void eliminar(K clave) {
        int indice = claves.indexOf(clave);
        if (indice < 0) return;
        historial.push(new Estado<>(mapa, claves, lista));
        mapa.remove(clave);
        claves.remove(indice);
        lista.remove(indice);
    }

    public synchronized boolean deshacer() {
        if (historial.empty()) return false;
        Estado<K, T> anterior = historial.pop();
        mapa.clear();
        mapa.putAll(anterior.mapa);
        claves.clear();
        claves.addAll(anterior.claves);
        lista.clear();
        lista.addAll(anterior.lista);
        return true;
    }

    public synchronized boolean deshacerInsercion(java.util.function.Predicate<T> puedeRetirar) {
        if (historial.empty()) return false;
        Estado<K, T> anterior = historial.peek();
        if (mapa.size() != anterior.mapa.size() + 1) return false;
        for (Map.Entry<K, T> entrada : anterior.mapa.entrySet()) {
            if (mapa.get(entrada.getKey()) != entrada.getValue()) return false;
        }
        for (Map.Entry<K, T> entrada : mapa.entrySet()) {
            if (!anterior.mapa.containsKey(entrada.getKey())) {
                if (!puedeRetirar.test(entrada.getValue())) return false;
                return deshacer();
            }
        }
        return false;
    }

    public synchronized List<T> obtenerTodos() {
        return new ArrayList<>(lista);
    }

    public synchronized boolean existe(K clave) {
        return mapa.containsKey(clave);
    }
}

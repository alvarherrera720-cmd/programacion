package org.example;
import dao.*;
import config.HikariCPConfig;
import dao.AutorDAO;
import dao.LibroDAO;
import objetos.Autor;
import objetos.Libro;

import java.sql.SQLException;
import java.util.ArrayList;

/*
 * Clase principal del programa de gestión de la biblioteca.
 *
 * Demuestra los 5 ejemplos pedidos en la tarea:
 *   1. Traer toda la información de la base de datos
 *   2. Meter un nuevo libro en la base de datos
 *   3. Modificar un autor en la base de datos
 *   4. Asignar un libro a un autor y guardarlo en la base de datos
 *   5. Eliminar un autor de un libro y guardarlo en la base de datos
 */
public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // Ponemos los DAOs que vamos a usar
        LibroDAO libroDAO = new LibroDAO();
        AutorDAO autorDAO = new AutorDAO();

        // DATOS DE EJEMPLO: insertamos datos iniciales en la BD
        System.out.println("\n======== Insertando Datos De Ejemplo ========");

        // Creamos algunos autores
        autorDAO.insertar(new Autor("11111111A", "Gabriel", "García Márquez", "nose"));
        autorDAO.insertar(new Autor("22222222B", "Isabel", "Allende", "nose"));
        autorDAO.insertar(new Autor("33333333C", "Mario", "Vargas Llosa", "nose"));

        // Creamos algunos libros
        libroDAO.insertar(new Libro("978-84-01-001", "Cien años de soledad", 1967, "Anaya", "Terror"));
        libroDAO.insertar(new Libro("978-84-01-002", "La casa de los espíritus", 1982, "nose", "nose"));
        libroDAO.insertar(new Libro("978-84-01-003", "La ciudad y los perros", 1963, "nose", "nose"));

        // Asignamos autores a libros usando libroDAO directamente (sin relacionDAO)
        libroDAO.asignarAutorALibro("978-84-01-001", "11111111A"); // García Márquez -> Cien años
        libroDAO.asignarAutorALibro("978-84-01-002", "22222222B"); // Allende -> La casa
        libroDAO.asignarAutorALibro("978-84-01-003", "33333333C"); // Vargas Llosa -> La ciudad

        // EJEMPLO 1: TRAER TODA LA INFORMACIÓN DE LA BASE DE DATOS
        System.out.println("\n======== EJEMPLO 1: TODA LA INFORMACIÓN ========");

        System.out.println("\n--- LIBROS con sus autores ---");
        ArrayList<Libro> todosLosLibros = libroDAO.obtenerTodos();
        for (Libro libro : todosLosLibros) {
            System.out.println(libro);
        }

        System.out.println("\n--- AUTORES ---");
        ArrayList<Autor> todosLosAutores = autorDAO.obtenerTodos();
        for (Autor autor : todosLosAutores) {
            System.out.println(autor);
        }

        // EJEMPLO 2: METER UN NUEVO LIBRO EN LA BASE DE DATOS
        System.out.println("\n======== EJEMPLO 2: INSERTAR NUEVO LIBRO ========");

        // Usamos el constructor completo igual que los demás libros
        Libro nuevoLibro = new Libro("978-84-01-004", "El amor en los tiempos del cólera", 1985, "nose", "nose");
        boolean insertado = libroDAO.insertar(nuevoLibro);

        if (insertado) {
            System.out.println("Libro insertado: " + nuevoLibro.getTitulo());
            // Verificamos que se ha guardado consultando la BD
            Libro libroGuardado = libroDAO.obtenerPorIsbn("978-84-01-004");
            System.out.println("Verificación desde BD: " + libroGuardado);
        }

        // EJEMPLO 3: MODIFICAR UN AUTOR EN LA BASE DE DATOS
        System.out.println("\n======== EJEMPLO 3: MODIFICAR AUTOR ========");

        Autor autorAntes = autorDAO.obtenerPorDni("22222222B");
        System.out.println("Autor antes:   " + autorAntes);

        // Usamos el constructor completo con todos los campos
        Autor autorModificado = new Autor("22222222B", "Isabel", "Allende Llona", "nose");
        boolean actualizado = autorDAO.actualizar(autorModificado);

        if (actualizado) {
            Autor autorDespues = autorDAO.obtenerPorDni("22222222B");
            System.out.println("Autor después: " + autorDespues);
        }

        // EJEMPLO 4: ASIGNAR UN LIBRO A UN AUTOR Y GUARDARLO EN LA BD
        System.out.println("\n======== EJEMPLO 4: ASIGNAR LIBRO A AUTOR ========");

        String isbnParaAsignar = "978-84-01-004";
        String dniParaAsignar  = "11111111A";

        System.out.println("Asignando libro '" + isbnParaAsignar
                + "' al autor '" + dniParaAsignar + "'...");

        // Llamamos directamente sobre libroDAO (ya no existe relacionDAO)
        boolean asignado = libroDAO.asignarAutorALibro(isbnParaAsignar, dniParaAsignar);

        if (asignado) {
            Libro libroConAutor = libroDAO.obtenerPorIsbn(isbnParaAsignar);
            System.out.println("Libro actualizado en BD: " + libroConAutor);
        }

        // EJEMPLO 5: ELIMINAR UN AUTOR DE UN LIBRO Y GUARDARLO EN LA BD
        System.out.println("\n======== EJEMPLO 5: ELIMINAR AUTOR DE LIBRO ========");

        Libro libroAntes = libroDAO.obtenerPorIsbn("978-84-01-001");
        System.out.println("Libro antes:   " + libroAntes);

        String isbnLibro = "978-84-01-001";
        String dniAutor  = "11111111A";

        System.out.println("Eliminando autor '" + dniAutor
                + "' del libro '" + isbnLibro + "'...");

        // Llamamos directamente sobre libroDAO
        boolean eliminado = libroDAO.eliminarAutorDeLibro(isbnLibro, dniAutor);

        if (eliminado) {
            Libro libroDespues = libroDAO.obtenerPorIsbn(isbnLibro);
            System.out.println("Libro después: " + libroDespues);

            // El autor sigue existiendo en la BD, solo se eliminó la relación
            Autor autorSigue = autorDAO.obtenerPorDni(dniAutor);
            System.out.println("El autor sigue en la BD: " + autorSigue);
        }

        // ESTADO FINAL
        System.out.println("\n======== ESTADO FINAL DE LA BASE DE DATOS ========");

        System.out.println("\n--- TODOS LOS LIBROS (estado final) ---");
        for (Libro l : libroDAO.obtenerTodos()) {
            System.out.println(l);
        }

        System.out.println("\n--- TODOS LOS AUTORES (estado final) ---");
        for (Autor a : autorDAO.obtenerTodos()) {
            System.out.println(a);
        }


        // CERRAMOS EL POOL DE HIKARICP al terminar el programa
        HikariCPConfig.getDataSource().close();
        System.out.println("\n[HikariCP] Pool de conexiones cerrado.");
    }
}
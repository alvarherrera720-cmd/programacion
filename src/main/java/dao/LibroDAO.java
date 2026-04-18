package dao;

import objetos.*;
import config.*;

import java.sql.*;
import java.util.*;

/*
 * Clase LibroDAO
 */
public class LibroDAO {

    // OBTENER TODOS LOS LIBROS
    public ArrayList<Libro> getAll() throws SQLException {

        // Lista donde guardamos los libros que sacamos de la BD
        ArrayList<Libro> bitacora = new ArrayList<>();

        String sql = "SELECT * FROM libro";

        // Abrimos conexión, ejecutamos la consulta y recorremos los resultados
        try (
                Connection con = HikariCPConfig.getDataSource().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            // Cada fila del ResultSet es un libro
            while (rs.next()) {

                // Convertimos cada fila en un objeto Libro
                bitacora.add(new Libro(
                        rs.getString("isbn"),
                        rs.getString("titulo"),
                        rs.getInt("anio"),
                        rs.getString("editorial"),
                        rs.getString("genero")
                ));
            }
        }

        // Devolvemos todos los libros encontrados
        return bitacora;
    }

    // INSERTAR LIBRO
    public boolean insertar (Libro libro) throws SQLException {

        // Query preparada para insertar un libro
        String sql = "INSERT INTO libro (isbn, titulo, anio, editorial, genero) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Sustituimos los ? por los datos del objeto libro
            pst.setString(1, libro.getIsbn());
            pst.setString(2, libro.getTitulo());

            // Ejecutamos el INSERT
            int filasAfectadas = pst.executeUpdate();

            // Si se insertó correctamente
            if (filasAfectadas > 0) {
                System.out.println("Libro insertado exitosamente: " + libro);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar el libro:" + e.getMessage());
        }

        return false;
    }



    // ACTUALIZAR LIBRO
    public void updateLibro(Libro l) throws SQLException {

        // Query para actualizar un libro usando el ISBN
        String sql = "UPDATE libros SET titulo=?, anio=?, editorial=?, genero=? WHERE isbn =?";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Asignamos valores en orden
            pst.setString(1, l.getTitulo());
            pst.setInt(2, l.getAnio());
            pst.setString(3, l.getGenero());
            pst.setString(4, l.getGenero());

        }
    }

    // ELIMINAR LIBRO
    public boolean eliminarAutorDeLibro (String isbn, String dniAutor) {

        Connection con = null;

        try {
            con = HikariCPConfig.getDataSource().getConnection();

            // Desactivamos autocommit para controlar la transacción manualmente
            con.setAutoCommit(false);

            // Borramos primero las relaciones en libro_autor
            String sqlRelaciones = "DELETE FROM libro_autor WHERE isbn_libro = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlRelaciones)) {
                ps.setString(1, isbn);
                int filasRelaciones = ps.executeUpdate();
                System.out.println("Relaciones eliminadas: " + filasRelaciones);
            }

            // Borramos el libro
            String sqlLibro = "DELETE FROM libro WHERE isbn = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlLibro)) {
                ps.setString(1, isbn);
                int filasLibro = ps.executeUpdate();

                // Si no existe el libro hace un rollback
                if (filasLibro == 0) {
                    con.rollback();
                    System.out.println("No se encontró libro con ISBN: " + isbn);
                    return false;
                }
            }

            // Si todo ha ido bien guardamos cambios
            con.commit();
            System.out.println("Libro " + isbn + " y sus relaciones eliminados correctamente.");
            return true;

        } catch (SQLException e) {

            // Si hay error  deshacemos todo
            if (con != null) {
                try {
                    con.rollback();
                    System.err.println("Rollback ejecutado: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;

        } finally {
            // Dejamos la conexión limpia antes de devolverla al pool
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    // ASIGNAR AUTOR A LIBRO
    public boolean asignarAutorALibro(String isbnLibro, String dniAutor) {

        // Insertamos en la tabla intermedia
        String sql = "INSERT INTO libro_autor (isbn_libro, dni_autor) VALUES (?, ?)";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Guardamos la relación
            ps.setString(1, isbnLibro);
            ps.setString(2, dniAutor);

            int filasAfectadas = ps.executeUpdate();

            // Si se insertó sera correcto
            if (filasAfectadas > 0) {
                System.out.println("[LibroDAO] Autor '" + dniAutor
                        + "' asignado al libro '" + isbnLibro + "' correctamente.");
                return true;
            }

        } catch (SQLException e) {

            // Error típico duplicado a la hora de la relación
            if (e.getErrorCode() == 1062) {
                System.out.println("[LibroDAO] El autor ya estaba asignado a este libro.");
            } else {
                System.err.println("[LibroDAO] Error al asignar autor: " + e.getMessage());
            }
        }

        return false;
    }

    // ELIMINAR RELACIÓN LIBRO-AUTOR
    public boolean eliminarAutorDeLibros(String isbnLibro, String dniAutor) throws SQLException {

        Connection con = null;

        try{
            con =  HikariCPConfig.getDataSource().getConnection();

            // Usamos transacción manual
            con.setAutoCommit(false);

            String sql = "DELETE FROM libro_autor WHERE isbn_libro dniAutor= ?";

            try (PreparedStatement pst = con.prepareStatement(sql)) {


                pst.setString(1, isbnLibro);
                pst.setString(2, dniAutor);

                int filasAfectadas = pst.executeUpdate();

                // Si no se borró nada es porque no existía la relación
                if (filasAfectadas == 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {

            if (con != null) {
                con.rollback();
            }
            return false;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }



    // OBTENER LIBRO POR ISBN
    public Libro obtenerPorIsbn(String isbn) {

        // Usamos JOIN para traer libro + autores en una sola consulta
        String sql = "SELECT l.isbn, l.titulo, l.anio, l.editorial, l.genero, " +
                "a.dni, a.nombre, a.apellido, a.nacionalidad " +
                "FROM libro l " +
                "LEFT JOIN libro_autor la ON l.isbn = la.isbn_libro " +
                "LEFT JOIN autor a ON la.dni_autor = a.dni " +
                "WHERE l.isbn = ?";

        Libro libro = null;

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, isbn);

            try (ResultSet rs = ps.executeQuery()) {

                // Puede haber varias filas ahora mismo una por autor
                while (rs.next()) {

                    // Creamos el libro solo una vez
                    if (libro == null) {
                        libro = new Libro(
                                rs.getString("isbn"),
                                rs.getString("titulo"),
                                rs.getInt("anio"),
                                rs.getString("editorial"),
                                rs.getString("genero")
                        );
                    }

                    // Añadimos autores si existen
                    String dniAutor = rs.getString("dni");
                    if (dniAutor != null) {
                        libro.addAutor(new Autor(
                                dniAutor,
                                rs.getString("nombre"),
                                rs.getString("apellido"),
                                rs.getString("nacionalidad")
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[LibroDAO] Error al obtener libro por ISBN: " + e.getMessage());
        }

        return libro;
    }



    // OBTENER TODOS LOS LIBROS
    public ArrayList<Libro> obtenerTodos() {

        ArrayList<Libro> lista = new ArrayList<>();

        // JOIN para traer libros y autores juntos
        String sql = "SELECT l.isbn, l.titulo, l.anio, l.editorial, l.genero, " +
                "a.dni, a.nombre, a.apellido, a.nacionalidad " +
                "FROM libro l " +
                "LEFT JOIN libro_autor la ON l.isbn = la.isbn_libro " +
                "LEFT JOIN autor a ON la.dni_autor = a.dni " +
                "ORDER BY l.titulo, a.apellido";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Libro libroActual = null;

            // Agrupamos filas por libro
            while (rs.next()) {
                String isbn = rs.getString("isbn");

                // Si cambia el ISBN es por un nuevo libro
                if (libroActual == null || !libroActual.getIsbn().equals(isbn)) {
                    libroActual = new Libro(
                            isbn,
                            rs.getString("titulo"),
                            rs.getInt("anio"),
                            rs.getString("editorial"),
                            rs.getString("genero")
                    );
                    lista.add(libroActual);
                }

                // Añadimos autores si existen
                String dniAutor = rs.getString("dni");
                if (dniAutor != null) {
                    libroActual.addAutor(new Autor(
                            dniAutor,
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("nacionalidad")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("[LibroDAO] Error al obtener todos los libros: " + e.getMessage());
        }

        return lista;
    }
}
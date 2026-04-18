package dao;

import objetos.*;
import config.*;
import org.example.*;
import java.sql.*;
import java.util.*;

/*
 * Clase AutorDAO
 */
public class AutorDAO {


    // SACAR TODOS LOS AUTORES
    public ArrayList<Autor> getAll() throws SQLException {

        // Lista donde vamos a guardar los autores que vienen de la BD
        ArrayList<Autor> lista = new ArrayList<>();

        String sql = "SELECT * FROM autor";

        // Abrimos conexión, preparamos consulta y la ejecutamos
        try (
                Connection con = HikariCPConfig.getDataSource().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            // Recorremos cada fila del resultado
            while (rs.next()) {

                // Por cada fila creamos un objeto Autor con sus datos
                lista.add(new Autor(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("nacionalidad")
                ));
            }
        }

        // Devolvemos la lista completa (aunque esté vacía)
        return lista;
    }

    // INSERTAR AUTOR
    public boolean insertar(Autor autor) {

        // Query para insertar un autor (solo dni y nombre en este caso)
        String sql = "INSERT INTO autor (dni, nombre ) VALUES (?, ?)";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Sustituimos los ? por los datos del objeto
            ps.setString(1, autor.getDni());
            ps.setString(2, autor.getNombre());

            // Ejecutamos la inserción
            int filasAfectadas = ps.executeUpdate();

            // Si se insertó al menos una fila es porque ha funcionado a la perfección
            if (filasAfectadas > 0) {
                System.out.println("[AutorDAO] Autor insertado correctamente: " + autor);
                return true;
            }

        } catch (SQLException e) {
            // Si hay error lo mostramos
            System.err.println("[AutorDAO] Error al insertar autor: " + e.getMessage());
        }

        return false;
    }



    // ACTUALIZAR AUTOR
    public void updateAutor(Autor a) throws SQLException {

        String sql = "UPDATE autor SET nombre=?, apellido=?, nacionalidad =? WHERE dni=?";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // el orden tiene que coincidir con la query
            pst.setString(1, a.getNombre());
            pst.setString(2, a.getApellido());
            pst.setString(3, a.getNacionalidad());
            pst.setString(4, a.getDni());

            // Ejecutamos el update
            pst.executeUpdate();
        }
    }



    // ELIMINAR AUTOR (CON TRANSACCIÓN)
    public boolean eliminar(String dni) throws SQLException {

        Connection con = null;

        try {
            con = HikariCPConfig.getDataSource().getConnection();

            // Quitamos autocommit para controlar la transacción manualmente
            con.setAutoCommit(false);

            // Primero borramos las relaciones con libros
            // Esto se hace para evitar errores de claves foráneas
            String sqlAL = "DELETE FROM libro_autor WHERE dni_autor =?";
            try (PreparedStatement pst = con.prepareStatement(sqlAL)) {
                pst.setString(1, dni);
                pst.executeUpdate();
            }

            // Luego borramos el autor
            String sqlA = "DELETE FROM autor WHERE dni =?";
            try (PreparedStatement pst = con.prepareStatement(sqlA)) {
                pst.setString(1, dni);

                int filasAutor = pst.executeUpdate();

                // Si no se borró nada → ese DNI no existía
                if (filasAutor == 0) {
                    con.rollback(); // deshacemos todo
                    return false;
                }
            }

            // Si todo ha ido bien lo que ba hacer es guardar los cambios
            con.commit();
            return true;

        } catch (Exception e) {

            // Si ocurre cualquier error va a tirar un rollback para dejar la BD como estaba
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
            // Cerramos la conexión sí o sí
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {e.printStackTrace();}
            }
        }
    }



    // OBTENER AUTOR POR DNI
    public Autor obtenerPorDni(String dni) {

        String sql = "SELECT dni, nombre, apellido, nacionalidad FROM autor WHERE dni = ?";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Metemos el DNI en la consulta
            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {

                // Si encuentra resultado, lo convierte en objeto Autor
                if (rs.next()) {
                    return new Autor(
                            rs.getString("dni"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("nacionalidad")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("[AutorDAO] Error al obtener autor: " + e.getMessage());
        }

        // Si no existe ese DNI
        return null;
    }


    // =========================
    // OBTENER TODOS (sin excepción)
    // =========================
    public ArrayList<Autor> obtenerTodos() {

        ArrayList<Autor> lista = new ArrayList<>();

        String sql = "SELECT dni, nombre, apellido, nacionalidad FROM autor ORDER BY apellido";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Igual que getAll pero ordenado por apellido
            while (rs.next()) {
                lista.add(new Autor(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("nacionalidad")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[AutorDAO] Error al obtener autores: " + e.getMessage());
        }

        return lista;
    }



    // ACTUALIZAR AUTOR
    public boolean actualizar(Autor autor) {

        String sql = "UPDATE autor SET nombre = ?, apellido = ?, nacionalidad = ? WHERE dni = ?";

        try (Connection con = HikariCPConfig.getDataSource().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignamos los valores del objeto a la query
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellido());
            ps.setString(3, autor.getNacionalidad());
            ps.setString(4, autor.getDni());

            int filasAfectadas = ps.executeUpdate();

            // Si se modificó algo → éxito
            if (filasAfectadas > 0) {
                System.out.println("[AutorDAO] Autor actualizado: " + autor);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[AutorDAO] Error al actualizar autor: " + e.getMessage());
        }

        return false;
    }
}
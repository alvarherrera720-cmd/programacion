package config;

// Importamos las clases de HikariCP (pool de conexiones)
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/*
 * Esta clase se encarga de configurar la conexión a la base de datos
 */
public class HikariCPConfig {

    private static HikariDataSource dataSource;

    // BLOQUE STATIC
    // Esto se ejecuta UNA SOLA VEZ cuando se carga la clase
    static {

        // Creamos la configuración del pool
        HikariConfig config = new HikariConfig();


        // jdbc:mysql:// dice el tipo de BD
        // localhost es el servidor
        // 3306 es el puerto
        // Bibliotecapr es el nombre de la BD
        config.setJdbcUrl("jdbc:mysql://localhost:3306/Bibliotecapr");

        // Usuario de la BD
        config.setUsername("root");

        // Contraseña
        config.setPassword("");

        // Número máximo de conexiones abiertas a la vez
        config.setMaximumPoolSize(10);

        // Creamos el DataSource
        dataSource = new HikariDataSource(config);
    }

    // MÉTODO PARA OBTENER EL DATASOURCE
    // Lo usan los DAO para conectarse a la BD
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
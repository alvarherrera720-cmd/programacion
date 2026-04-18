package objetos;

/*
 * Clase Autor
 */
public class Autor {
    protected String dni;
    protected String nombre;
    protected String apellido;
    protected String nacionalidad;

    public Autor (String dni, String nombre, String apellido, String nacionalidad){
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nacionalidad = nacionalidad;
    }
    //Getters y Setters

    public String getDni() {return dni;}
    public void setDni(String dni) {this.dni = dni;}

    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getApellido() {return apellido;}
    public void setApellido(String apellido) {this.apellido = apellido;}

    public String getNacionalidad() {return nacionalidad;}
    public void setNacionalidad(String nacionalidad) {this.nacionalidad = nacionalidad;}
    @Override
    public String toString() {
        return "Autor{dni='" + dni + "', nombre='" + nombre + "'}";
    }
}

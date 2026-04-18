package objetos;

import java.util.ArrayList;

/*
 *  Libro
 */
public class Libro {
    protected String isbn;
    protected String titulo;
    protected int anio;
    protected String editorial;
    protected String genero;
    protected ArrayList<Autor> autores;

    //Constructor vacio
    public Libro() {
        this.autores = new ArrayList<>(); // Inicializamos la lista vacia
    }

    public Libro (String isbn, String titulo, int anio, String editorial, String genero){
        this.isbn = isbn;
        this.titulo = titulo;
        this.anio = anio;
        this.editorial = editorial;
        this.genero = genero;
        this.autores = new ArrayList<>();

    }

    //Getters y setters

    public String getIsbn() {return isbn;}
    public void setIsbn(String isbn) {this.isbn = isbn;}

    public String getTitulo() {return titulo;}
    public void setTitulo(String titulo) {this.titulo = titulo;}

    public int getAnio() {return anio;}
    public void setAnio(int anio) {this.anio = anio;}

    public String getEditorial() {return editorial;}
    public void setEditorial(String editorial) {this.editorial = editorial;}

    public String getGenero() {return genero;}
    public void setGenero(String genero) {this.genero = genero;}



    // Metodo auxiliar para añadir un autor a la lista del libro

    public void addAutor(Autor autor) {
        this.autores.add(autor);
    }


    // toString: muestra el libro y todos sus autores

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Libro{isbn='").append(isbn)
                .append("', titulo='").append(titulo)
                .append("', anio=").append(anio)
                .append(", autores=[");

        // Recorremos la lista de autores para mostrarlos
        for (int i = 0; i < autores.size(); i++) {
            sb.append(autores.get(i).getNombre());
            if (i < autores.size() - 1) sb.append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }
}

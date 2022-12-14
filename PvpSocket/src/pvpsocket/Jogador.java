package pvpsocket;

import java.io.Serializable;

public class Jogador implements Serializable {
    
    private int id;
    private final String nome;
    private int vida = 100;
    
    
    public Jogador(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Jogador other = (Jogador) obj;
        return this.id == other.id;
    }
    
    

    @Override
    public String toString() {
        return "[" + nome + "]\n" + "vida: " + vida;
    }
    
    
}

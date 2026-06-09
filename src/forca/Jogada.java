package forca;

import java.io.Serializable;

public class Jogada implements Serializable {

    private char letra;

    public Jogada(char letra) {
        this.letra = Character.toLowerCase(letra);
    }

    public char getLetra() {
        return letra;
    }

    public void setLetra(char letra) {
        this.letra = Character.toLowerCase(letra);
    }

    public String toString() {
        return "Letra jogada: " + letra;
    }
}

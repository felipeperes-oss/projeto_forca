package forca;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Forca {

    private static final int MAX_ERROS = 6;

    private String palavra;
    private char[] palavraTentativa;
    private int contaAcerto;
    private int contaErro;
    private String letrasTentadas;

    public void consultarPalavra() throws IOException {
        ArrayList<String> palavras = new ArrayList<String>();
        Scanner leitor = new Scanner(new File("palavras.txt"));

        while (leitor.hasNextLine()) {
            String linha = leitor.nextLine().trim();

            if (linha.length() > 0) {
                palavras.add(linha);
            }
        }

        leitor.close();

        if (palavras.size() == 0) {
            throw new IOException("Arquivo de palavras está vazio!");
        }

        Random sorteador = new Random();
        int posicao = sorteador.nextInt(palavras.size());
        palavra = palavras.get(posicao).toLowerCase();
    }

    public void definirPalavra(String palavra) {
        this.palavra = palavra.toLowerCase();
    }

    public void iniciar() {
        contaAcerto = 0;
        contaErro = 0;
        letrasTentadas = "";
        palavraTentativa = new char[palavra.length()];

        for (int i = 0; i < palavra.length(); i++) {
            palavraTentativa[i] = '_';
        }

        System.out.println("=== Jogo de Forca iniciado! ===\n");
        exibirEstado();
    }

    public boolean tentativa(char letra) {
        letra = Character.toLowerCase(letra);

        if (!Character.isLetter(letra)) {
            System.out.println("Digite apenas letras.\n");
            return false;
        }

        if (letraJaTentada(letra)) {
            System.out.println("Você já tentou a letra '" + letra + "'! Tente outra.\n");
            return false;
        }

        letrasTentadas = letrasTentadas + letra + " ";
        boolean acertou = false;

        for (int i = 0; i < palavra.length(); i++) {
            if (palavra.charAt(i) == letra) {
                palavraTentativa[i] = letra;
                contaAcerto++;
                acertou = true;
            }
        }

        if (acertou == false) {
            contaErro++;
            System.out.println("Letra '" + letra + "' não está na palavra! Erros: "
                    + contaErro + "/" + MAX_ERROS + "\n");
        } else {
            System.out.println("Boa! A letra '" + letra + "' está na palavra!\n");
        }

        exibirEstado();
        return acertou;
    }

    public boolean letraJaTentada(char letra) {
        letra = Character.toLowerCase(letra);
        return letrasTentadas.indexOf(letra) >= 0;
    }

    public boolean isGanhador() {
        return contaAcerto == palavra.length();
    }

    public boolean isPerdedor() {
        return contaErro >= MAX_ERROS;
    }

    public void exibirEstado() {
        System.out.print("Palavra:  ");

        for (int i = 0; i < palavraTentativa.length; i++) {
            System.out.print(palavraTentativa[i] + "  ");
        }

        System.out.println();
        System.out.println("Letras tentadas: " + letrasTentadas);
        System.out.println("Erros: " + contaErro + "/" + MAX_ERROS);
        System.out.println();
    }

    public String getPalavra() {
        return palavra;
    }

    public int getContaErro() {
        return contaErro;
    }

    public String getLetrasTentadas() {
        return letrasTentadas;
    }

    public char[] getPalavraTentativa() {
        return palavraTentativa;
    }
}

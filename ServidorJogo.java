package forca;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorJogo {

    private static final int PORTA = 8080;
    private static final int MAX_CONEXOES = 2;

    private ServerSocket servidor;

    private Socket jogador1;
    private ObjectOutputStream outputJogador1;
    private ObjectInputStream inputJogador1;

    private Socket jogador2;
    private ObjectOutputStream outputJogador2;
    private ObjectInputStream inputJogador2;

    private Forca jogo;
    private String palavraSorteada;

    private boolean jogador1QuerNovoJogo;
    private boolean jogador2QuerNovoJogo;
    private boolean resultadoGravado;

    private String historicoTentativas;

    public void iniciar() throws IOException {
        servidor = new ServerSocket(PORTA, MAX_CONEXOES);

        sortearPalavra();

        jogador1QuerNovoJogo = false;
        jogador2QuerNovoJogo = false;

        System.out.println("Servidor Forca inicializado: " + servidor + "\n");
    }

    private void sortearPalavra() throws IOException {
        jogo = new Forca();
        jogo.consultarPalavra();
        palavraSorteada = jogo.getPalavra();
        jogo.iniciar();

        resultadoGravado = false;
        historicoTentativas = "";
    }

    public void conectar() throws IOException {
        System.out.println("Aguardando Jogador 1...");
        jogador1 = servidor.accept();
        System.out.println("Jogador 1 conectado: "
                + jogador1.getInetAddress() + ":" + jogador1.getPort() + "\n");

        outputJogador1 = new ObjectOutputStream(jogador1.getOutputStream());
        outputJogador1.flush();
        outputJogador1.writeObject("1;true;" + palavraSorteada);
        outputJogador1.flush();

        System.out.println("Aguardando Jogador 2...");
        jogador2 = servidor.accept();
        System.out.println("Jogador 2 conectado: "
                + jogador2.getInetAddress() + ":" + jogador2.getPort() + "\n");

        outputJogador2 = new ObjectOutputStream(jogador2.getOutputStream());
        outputJogador2.flush();
        outputJogador2.writeObject("2;false;" + palavraSorteada);
        outputJogador2.flush();
    }

    public void comunicar() throws IOException {
        inputJogador1 = new ObjectInputStream(jogador1.getInputStream());
        inputJogador2 = new ObjectInputStream(jogador2.getInputStream());

        Thread thread1 = new Thread(new GerenciadorDeJogadas(inputJogador1, outputJogador2, this, 1));
        Thread thread2 = new Thread(new GerenciadorDeJogadas(inputJogador2, outputJogador1, this, 2));

        thread1.start();
        thread2.start();

        System.out.println("Comunicação entre jogadores iniciada.\n");
    }

    public synchronized void registrarJogada(int jogadorId, Jogada jogada) throws IOException {
        if (resultadoGravado) {
            return;
        }

        historicoTentativas = historicoTentativas
                + "Jogador " + jogadorId + ": " + jogada.getLetra() + "\n";

        jogo.tentativa(jogada.getLetra());

        if (jogo.isGanhador()) {
            gravarResultado(jogadorId, pegarOutroJogador(jogadorId),
                    "O Jogador " + jogadorId + " acertou a letra final e descobriu a palavra.");

        } else if (jogo.isPerdedor()) {
            gravarResultado(pegarOutroJogador(jogadorId), jogadorId,
                    "O Jogador " + jogadorId + " errou a última tentativa de letra.");
        }
    }

    private void gravarResultado(int vencedor, int perdedor, String motivo) throws IOException {
        RegistroResultado.gravarResultado(
                palavraSorteada,
                vencedor,
                perdedor,
                motivo,
                historicoTentativas
        );

        resultadoGravado = true;
        System.out.println("Resultado gravado no arquivo resultados_forca.txt");
    }

    private int pegarOutroJogador(int jogadorId) {
        if (jogadorId == 1) {
            return 2;
        } else {
            return 1;
        }
    }

    public synchronized void solicitarNovoJogo(int jogadorId) throws IOException {
        if (jogadorId == 1) {
            jogador1QuerNovoJogo = true;
            System.out.println("Jogador 1 quer jogar novamente.");
        } else {
            jogador2QuerNovoJogo = true;
            System.out.println("Jogador 2 quer jogar novamente.");
        }

        if (jogador1QuerNovoJogo && jogador2QuerNovoJogo) {
            sortearPalavra();

            outputJogador1.writeObject("NOVO_JOGO;" + palavraSorteada + ";true");
            outputJogador1.flush();

            outputJogador2.writeObject("NOVO_JOGO;" + palavraSorteada + ";false");
            outputJogador2.flush();

            jogador1QuerNovoJogo = false;
            jogador2QuerNovoJogo = false;
        }
    }

    public void encerrar() {
        try {
            if (inputJogador1 != null) {
                inputJogador1.close();
            }

            if (outputJogador1 != null) {
                outputJogador1.close();
            }

            if (jogador1 != null) {
                jogador1.close();
            }

            if (inputJogador2 != null) {
                inputJogador2.close();
            }

            if (outputJogador2 != null) {
                outputJogador2.close();
            }

            if (jogador2 != null) {
                jogador2.close();
            }

            if (servidor != null) {
                servidor.close();
            }

            System.out.println("Servidor encerrado.");
        } catch (IOException ex) {
            System.out.println("Erro ao encerrar servidor: " + ex.getMessage());
        }
    }
}

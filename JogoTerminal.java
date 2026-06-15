package forca;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class JogoTerminal {

    private Scanner console;
    private Forca jogo;
    private String jogadorId;
    private String palavraRecebida;
    private boolean suaVez;
    private boolean fim;

    private Socket servidorConexao;
    private ObjectInputStream servidorEntrada;
    private ObjectOutputStream servidorSaida;

    public JogoTerminal() throws Exception {
        jogo = new Forca();
        console = new Scanner(System.in);

        conectar();
        iniciar();
        jogar();
    }

    public void conectar() throws Exception {
        servidorConexao = new Socket("10.105.68.127", 8080);

        System.out.println("Conexão realizada: "
                + servidorConexao.getLocalAddress().getHostName()
                + ":" + servidorConexao.getLocalPort() + "\n");

        servidorSaida = new ObjectOutputStream(servidorConexao.getOutputStream());
        servidorEntrada = new ObjectInputStream(servidorConexao.getInputStream());

        String mensagem = (String) servidorEntrada.readObject();
        String[] info = mensagem.split(";");

        jogadorId = info[0];
        suaVez = info[1].equals("true");
        palavraRecebida = info[2];

        System.out.println("Você é o Jogador " + jogadorId);
    }

    public void iniciar() throws Exception {
        jogo.definirPalavra(palavraRecebida);
        jogo.iniciar();
        fim = false;
    }

    public void jogar() throws Exception {
        while (fim == false) {

            if (suaVez == false) {
                receberJogadaDoAdversario();
            }

            if (fim) {
                break;
            }

            jogarMinhaLetra();
        }
    }

    private void receberJogadaDoAdversario() throws Exception {
        System.out.println("Aguarde a jogada do adversário...");

        Object mensagem = servidorEntrada.readObject();

        if (mensagem instanceof Jogada) {
            Jogada jogadaRecebida = (Jogada) mensagem;
            char letra = jogadaRecebida.getLetra();

            System.out.println("O adversário tentou a letra: " + letra);

            boolean acertou = jogo.tentativa(letra);

            if (acertou) {
                suaVez = false;
            } else {
                suaVez = true;
            }

            checarTermino();

        } else if (mensagem instanceof String) {
            String texto = (String) mensagem;

            if (texto.startsWith("NOVO_JOGO;")) {
                String[] info = texto.split(";");
                palavraRecebida = info[1];
                suaVez = info[2].equals("true");
                iniciar();
            }
        }
    }

    private void jogarMinhaLetra() throws Exception {
        boolean letraValida = false;

        while (fim == false && letraValida == false) {
            System.out.print("Sua vez! Informe uma letra: ");
            String entrada = console.next();
            entrada = entrada.trim();

            if (entrada.length() != 1) {
                System.out.println("Digite apenas uma letra.");
            } else {
                char letra = Character.toLowerCase(entrada.charAt(0));

                if (!Character.isLetter(letra)) {
                    System.out.println("Digite apenas letras.");
                } else if (jogo.letraJaTentada(letra)) {
                    System.out.println("Essa letra já foi tentada. Escolha outra.");
                } else {
                    Jogada minhaJogada = new Jogada(letra);
                    boolean acertou = jogo.tentativa(minhaJogada.getLetra());

                    servidorSaida.writeObject(minhaJogada);
                    servidorSaida.flush();

                    if (acertou) {
                        suaVez = true;
                    } else {
                        suaVez = false;
                    }

                    letraValida = true;
                    checarTermino();
                }
            }
        }
    }

    private void checarTermino() throws Exception {
        if (jogo.isGanhador() || jogo.isPerdedor()) {
            fim = true;
            jogo.exibirEstado();

            if (jogo.isGanhador()) {
                System.out.println("PARABÉNS! A palavra foi descoberta: " + jogo.getPalavra());
            } else {
                System.out.println("GAME OVER! Vocês excederam o limite de erros.");
                System.out.println("A palavra era: " + jogo.getPalavra());
            }

            checarReinicio();
        }
    }

    private void checarReinicio() throws Exception {
        char resposta = ' ';

        while (resposta != 'S' && resposta != 'N') {
            System.out.print("Deseja jogar novamente (S/N): ");
            resposta = console.next().toUpperCase().charAt(0);

            if (resposta != 'S' && resposta != 'N') {
                System.out.println("Resposta inválida!");
            }
        }

        if (resposta == 'S') {
            servidorSaida.writeObject("NOVO_JOGO_PEDIDO");
            servidorSaida.flush();
            System.out.println("Aguardando o outro jogador...");
        } else {
            fecharConexao();
            System.exit(0);
        }
    }

    private void fecharConexao() throws Exception {
        servidorEntrada.close();
        servidorSaida.close();
        servidorConexao.close();
    }
}

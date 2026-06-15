package forca;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GerenciadorDeJogadas implements Runnable {

    private ObjectInputStream inputJogador;
    private ObjectOutputStream outputDestino;
    private ServidorJogo servidor;
    private int jogadorId;
    private boolean rodando;

    public GerenciadorDeJogadas(ObjectInputStream inputJogador, ObjectOutputStream outputDestino,
            ServidorJogo servidor, int jogadorId) {

        this.inputJogador = inputJogador;
        this.outputDestino = outputDestino;
        this.servidor = servidor;
        this.jogadorId = jogadorId;
        this.rodando = true;
    }

    public void parar() {
        rodando = false;
    }

    public void run() {
        while (rodando) {
            try {
                Object mensagem = inputJogador.readObject();

                if (mensagem instanceof String) {
                    String texto = (String) mensagem;

                    if (texto.equals("NOVO_JOGO_PEDIDO")) {
                        servidor.solicitarNovoJogo(jogadorId);
                    } else {
                        enviarParaOutroJogador(mensagem);
                    }

                } else if (mensagem instanceof Jogada) {
                    Jogada jogada = (Jogada) mensagem;
                    servidor.registrarJogada(jogadorId, jogada);
                    enviarParaOutroJogador(jogada);
                } else {
                    enviarParaOutroJogador(mensagem);
                }

            } catch (Exception ex) {
                System.out.println("Jogador desconectado. Encerrando gerenciador.");
                rodando = false;
            }
        }
    }

    private void enviarParaOutroJogador(Object mensagem) throws Exception {
        outputDestino.writeObject(mensagem);
        outputDestino.flush();
    }
}

package forca;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ForcaGUI extends JFrame {

    private Forca jogo;
    private String jogadorId;
    private String palavraRecebida;
    private boolean suaVez;
    private boolean fim;

    private Socket servidorConexao;
    private ObjectInputStream servidorEntrada;
    private ObjectOutputStream servidorSaida;

    private JButton jButtonPalavra;
    private JButton jButtonStatus;
    private JButton jButtonErros;
    private JButton jButtonLetras;
    private JButton jButtonJogar;
    private JButton jButtonSair;

    public ForcaGUI() throws Exception {

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(525, 360);
        setResizable(false);
        setLayout(null);
        setLocationRelativeTo(null);

        jButtonPalavra = new JButton("");
        jButtonPalavra.setFont(new Font("Arial", Font.PLAIN, 34));
        jButtonPalavra.setBounds(25, 25, 475, 75);
        jButtonPalavra.setEnabled(false);
        add(jButtonPalavra);

        jButtonStatus = new JButton("Conectando...");
        jButtonStatus.setFont(new Font("Arial", Font.PLAIN, 18));
        jButtonStatus.setBounds(25, 115, 475, 45);
        jButtonStatus.setEnabled(false);
        add(jButtonStatus);

        jButtonErros = new JButton("");
        jButtonErros.setFont(new Font("Arial", Font.PLAIN, 18));
        jButtonErros.setBounds(25, 175, 230, 45);
        jButtonErros.setEnabled(false);
        add(jButtonErros);

        jButtonLetras = new JButton("");
        jButtonLetras.setFont(new Font("Arial", Font.PLAIN, 16));
        jButtonLetras.setBounds(270, 175, 230, 45);
        jButtonLetras.setEnabled(false);
        add(jButtonLetras);

        jButtonJogar = new JButton("Jogar Letra");
        jButtonJogar.setFont(new Font("Arial", Font.PLAIN, 22));
        jButtonJogar.setBounds(25, 240, 230, 55);
        jButtonJogar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                escolherLetra();
            }
        });
        add(jButtonJogar);

        jButtonSair = new JButton("Sair");
        jButtonSair.setFont(new Font("Arial", Font.PLAIN, 22));
        jButtonSair.setBounds(270, 240, 230, 55);
        jButtonSair.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sair();
            }
        });
        add(jButtonSair);

        setVisible(true);

        jogo = new Forca();
        desabilitarOpcoes();

        conectar();
        iniciar();

        if (suaVez) {
            habilitarOpcoes();
        }

        receberLetra();
    }

    private void habilitarOpcoes() {
        if (fim == false) {
            jButtonJogar.setEnabled(true);
        }
    }

    private void desabilitarOpcoes() {
        jButtonJogar.setEnabled(false);
    }

    private void escolherLetra() {
        try {
            if (suaVez == false) {
                JOptionPane.showMessageDialog(this, "Aguarde o adversário.");
                return;
            }

            String entrada = JOptionPane.showInputDialog(this, "Digite apenas uma letra:");

            if (entrada == null) {
                return;
            }

            entrada = entrada.trim();

            if (entrada.length() == 0) {
                JOptionPane.showMessageDialog(this, "Você precisa digitar uma letra.");
                return;
            }

            if (entrada.length() > 1) {
                JOptionPane.showMessageDialog(this, "Digite apenas uma letra por tentativa.");
                return;
            }

            char letra = Character.toLowerCase(entrada.charAt(0));

            if (!Character.isLetter(letra)) {
                JOptionPane.showMessageDialog(this, "Digite apenas letras.");
                return;
            }

            if (jogo.letraJaTentada(letra)) {
                JOptionPane.showMessageDialog(this, "Essa letra já foi tentada. Escolha outra.");
                return;
            }

            Jogada jogada = new Jogada(letra);
            boolean acertou = jogo.tentativa(jogada.getLetra());

            servidorSaida.writeObject(jogada);
            servidorSaida.flush();

            if (acertou) {
                suaVez = true;
            } else {
                suaVez = false;
            }

            atualizarTela();
            checarTermino(true);
            controlarBotaoJogar();

        } catch (Exception ex) {
            mostrarErro("Erro", ex);
        }
    }

    private void receberLetra() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Object mensagemRecebida = servidorEntrada.readObject();

                        if (mensagemRecebida instanceof Jogada) {
                            Jogada jogada = (Jogada) mensagemRecebida;
                            boolean acertou = jogo.tentativa(jogada.getLetra());

                            if (acertou) {
                                suaVez = false;
                            } else {
                                suaVez = true;
                            }

                            atualizarTela();
                            checarTermino(false);
                            controlarBotaoJogar();

                        } else if (mensagemRecebida instanceof String) {
                            String mensagem = (String) mensagemRecebida;

                            if (mensagem.startsWith("NOVO_JOGO;")) {
                                String[] info = mensagem.split(";");

                                palavraRecebida = info[1];
                                suaVez = info[2].equals("true");

                                iniciar();
                                controlarBotaoJogar();
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (fim == false) {
                        mostrarErro("Erro na conexão", ex);
                        dispose();
                    }
                }
            }
        });

        thread.start();
    }

    public void conectar() throws Exception {
        servidorConexao = new Socket("10.105.64.222", 8080);

        servidorSaida = new ObjectOutputStream(servidorConexao.getOutputStream());
        servidorEntrada = new ObjectInputStream(servidorConexao.getInputStream());

        String mensagem = (String) servidorEntrada.readObject();
        String[] info = mensagem.split(";");

        jogadorId = info[0];
        suaVez = info[1].equals("true");
        palavraRecebida = info[2];

        setTitle("FORCA - Jogador " + jogadorId);
    }

    private void iniciar() throws Exception {
        jogo.definirPalavra(palavraRecebida);
        jogo.iniciar();
        fim = false;
        atualizarTela();
    }

    private void atualizarTela() {
        jButtonPalavra.setText(formatarPalavra());

        if (suaVez) {
            jButtonStatus.setText("Jogador " + jogadorId + " - Sua vez");
        } else {
            jButtonStatus.setText("Jogador " + jogadorId + " - Aguarde");
        }

        jButtonErros.setText("Erros: " + jogo.getContaErro() + "/6");
        jButtonLetras.setText("Letras: " + jogo.getLetrasTentadas());
    }

    private String formatarPalavra() {
        String texto = "";
        char[] letras = jogo.getPalavraTentativa();

        for (int i = 0; i < letras.length; i++) {
            texto = texto + letras[i] + " ";
        }

        return texto;
    }

    private void controlarBotaoJogar() {
        if (fim == false) {
            if (suaVez) {
                habilitarOpcoes();
            } else {
                desabilitarOpcoes();
            }
        }
    }

    private void checarTermino(boolean fuiEuQueJoguei) throws Exception {
        if (jogo.isGanhador() || jogo.isPerdedor()) {
            fim = true;
            desabilitarOpcoes();

            if (jogo.isGanhador()) {
                if (fuiEuQueJoguei) {
                    JOptionPane.showMessageDialog(this, "Você venceu! Você descobriu a palavra: " + jogo.getPalavra());
                } else {
                    JOptionPane.showMessageDialog(this, "Você perdeu! O adversário descobriu a palavra: " + jogo.getPalavra());
                }
            } else {
                if (fuiEuQueJoguei) {
                    JOptionPane.showMessageDialog(this, "Você perdeu! Você errou a última tentativa. Palavra: " + jogo.getPalavra());
                } else {
                    JOptionPane.showMessageDialog(this, "Você venceu! O adversário errou a última tentativa. Palavra: " + jogo.getPalavra());
                }
            }

            checarReinicio();
        }
    }

    private void checarReinicio() throws Exception {
        int op = JOptionPane.showConfirmDialog(this, "Jogar novamente?", "Reiniciar", JOptionPane.YES_NO_OPTION);

        if (op == JOptionPane.YES_OPTION) {
            servidorSaida.writeObject("NOVO_JOGO_PEDIDO");
            servidorSaida.flush();

            desabilitarOpcoes();
            jButtonStatus.setText("Aguardando o outro jogador...");
        } else {
            sair();
        }
    }

    private void sair() {
        try {
            fim = true;

            if (servidorEntrada != null) {
                servidorEntrada.close();
            }

            if (servidorSaida != null) {
                servidorSaida.close();
            }

            if (servidorConexao != null) {
                servidorConexao.close();
            }
        } catch (Exception ex) {
            mostrarErro("Erro", ex);
        }

        dispose();
    }

    private void mostrarErro(String titulo, Exception ex) {
        String mensagem = ex.getMessage();

        if (mensagem == null || mensagem.length() == 0) {
            mensagem = ex.getClass().getSimpleName();
        }

        JOptionPane.showMessageDialog(this, titulo + ": " + mensagem);
    }
}

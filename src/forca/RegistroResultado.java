package forca;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class RegistroResultado {

    public static void gravarResultado(String palavra, int vencedor, int perdedor,
            String motivo, String historicoTentativas) throws IOException {

        FileWriter escritor = new FileWriter("resultados_forca.txt", true);

        escritor.write("===== RESULTADO DA PARTIDA =====\n");
        escritor.write("Data: " + new Date() + "\n");
        escritor.write("Palavra: " + palavra + "\n");
        escritor.write("Vencedor: Jogador " + vencedor + "\n");
        escritor.write("Perdedor: Jogador " + perdedor + "\n");
        escritor.write("Motivo: " + motivo + "\n\n");
        escritor.write("Histórico de tentativas:\n");
        escritor.write(historicoTentativas);
        escritor.write("================================\n\n");

        escritor.close();
    }
}

package forca;

public class MainServidor {

    public static void main(String[] args) throws Exception {
        ServidorJogo servidor = new ServidorJogo();

        servidor.iniciar();
        servidor.conectar();
        servidor.comunicar();
    }
}

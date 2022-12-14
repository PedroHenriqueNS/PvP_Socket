package pvpsocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author pedro
 */
public class Servidor {

    static ServerSocket serverSocket_main;
    static ServerSocket serverSocket_updater;
    static Socket clientSocket1;
    static Socket clientSocket2;

    static String clientID1;
    static String clientID2;

    static Jogador jogador1;
    static Jogador jogador2;
    static int winnerID;
    static String winner;

    static Conexao conexao;

    static boolean whileBreaker_GamingSession = false;
    static boolean whileBreaker_Updater = false;
    static boolean endGame = false;

    public static void createServer(String serverType) {
        if ("mainServer".equals(serverType)) {
            try {
                serverSocket_main = new ServerSocket(20000);
                serverSocket_main.setSoTimeout(999999999);

                System.out.println("Criando o Server Socket...");
            } catch (IOException e) {
                System.out.println("Não foi criado o Server Socket.\nCódigo do erro: " + e);
            }
        } else if ("updateServer".equals(serverType)) {
            try {
                serverSocket_updater = new ServerSocket(10000);
                serverSocket_updater.setSoTimeout(0);
                System.out.println("Criando o Update Server Socket...");
            } catch (IOException e) {
                System.out.println("Não foi criado o Updater Server Socket.\nCódigo do erro: " + e);
            }
        }
    }

    private static Runnable gamingSession = new Runnable() {
        @Override
        public void run() {

            try {
                Thread.sleep(1000);
                conexao.sendPlayer(clientSocket1, jogador2);
                Thread.sleep(100);
                conexao.sendPlayer(clientSocket2, jogador1);

                System.out.println("Preparando Jogo");

                Thread.sleep(1000);
                conexao.sendCommand(clientSocket1, "startGame");
                conexao.sendCommand(clientSocket2, "startGame");

                System.out.println("Iniciando Jogo");

            } catch (Exception e) {
                System.out.println("Erro de envio de informação, erro: " + e);
            }

            String gamingCommand1;
            String gamingCommand2;

            boolean erro;

            int defesa1 = 0, defesa2 = 0;
            boolean attack1 = false, attack2 = false;

            while (whileBreaker_GamingSession == false) {
                
                attack1 = false;
                attack2 = false;

                erro = false;

                try {

                    gamingCommand1 = conexao.receiveCommand(clientSocket1);
                    gamingCommand2 = conexao.receiveCommand(clientSocket2);

                    System.out.println(gamingCommand1.trim() + " | " + gamingCommand2.trim());

                    switch (gamingCommand1.trim()) {
                        case "atacar":
                            if (defesa2 >= 1) {
                                --defesa2;
                                conexao.sendCommand(clientSocket1, "player2 defended");
                                Thread.sleep(100);
                                conexao.sendCommand(clientSocket2, "player2 defended");
                            } else {
                                attack1 = true;
                            }
                            break;

                        case "defender":
                            ++defesa1;
                            conexao.sendCommand(clientSocket1, "defense increased");
                            break;

                        case "curar":
                            jogador1.setVida(jogador1.getVida() + 15);
                            conexao.sendCommand(clientSocket1, "cure");
                            break;

                        case "desistir":
                            jogador1.setVida(0);
                            conexao.sendCommand(clientSocket1, "endgame player2win");
                            conexao.sendCommand(clientSocket2, "endgame player2win");
                            break;
                        case "status":
                            conexao.sendCommand(clientSocket1, "sendingStatus");
                            break;
                        default:
                            conexao.sendCommand(clientSocket1, "wrongCommand");
                    }
                    switch (gamingCommand2.trim()) {
                        case "atacar":
                            if (defesa1 >= 1) {
                                --defesa1;
                                conexao.sendCommand(clientSocket1, "player1 defended");
                                conexao.sendCommand(clientSocket2, "player1 defended");
                            } else {
                                attack2 = true;
                            }
                            break;

                        case "defender":
                            ++defesa2;
                            conexao.sendCommand(clientSocket2, "defense increased");
                            break;

                        case "curar":
                            jogador2.setVida(jogador2.getVida() + 15);
                            conexao.sendCommand(clientSocket2, "cure");
                            break;

                        case "desistir":
                            jogador2.setVida(0);
                            conexao.sendCommand(clientSocket1, "endgame player1win");
                            conexao.sendCommand(clientSocket2, "endgame player1win");
                            break;
                        case "status":
                            conexao.sendCommand(clientSocket2, "sendingStatus");
                            break;
                        default:
                            conexao.sendCommand(clientSocket2, "wrongCommand");
                    }

                    if (attack1 == true && attack2 == true) {
                        jogador1.setVida(jogador1.getVida() - 10);
                        jogador2.setVida(jogador2.getVida() - 10);

                        conexao.sendCommand(clientSocket1, "both attacked");
                        Thread.sleep(100);
                        conexao.sendCommand(clientSocket2, "both attacked");
                    } else {
                        if (attack1 == true) {
                            jogador1.setVida(jogador1.getVida() - 10);
                            conexao.sendCommand(clientSocket1, "player1 attacked");
                            Thread.sleep(100);
                            conexao.sendCommand(clientSocket2, "player1 attacked");
                        }
                        if (attack2 == true) {
                            jogador2.setVida(jogador2.getVida() - 10);
                            conexao.sendCommand(clientSocket1, "player2 attacked");
                            conexao.sendCommand(clientSocket2, "player2 attacked");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Erro na jogatina, erro: " + e);
                }
            }

        }
    };

    private static Runnable updateClients = new Runnable() {
        @Override
        public void run() {
            Socket clientSocketUpdater1 = null, clientSocketUpdater2 = null;

            try {
                clientSocketUpdater1 = serverSocket_updater.accept();
                clientSocketUpdater2 = serverSocket_updater.accept();
            } catch (IOException e) {
                System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
            }
            whileBreaker_Updater = false;

            while (whileBreaker_Updater == false) {
                try {
                    conexao.sendPlayer(clientSocketUpdater1, jogador1);
                    conexao.sendPlayer(clientSocketUpdater1, jogador2);
                    conexao.sendPlayer(clientSocketUpdater2, jogador1);
                    conexao.sendPlayer(clientSocketUpdater2, jogador2);

                } catch (Exception e) {
                    System.out.println("Envio de atualização mal-sucedida, erro: " + e);
                }
            }
        }
    };

    public static void main(String[] args) throws Exception {
        createServer("mainServer");
        createServer("updateServer");

        conexao = new Conexao();

        try {
            clientSocket1 = serverSocket_main.accept();
            if (clientSocket1.isConnected()) {
                System.out.println("Jogador 1 entrou");
                clientSocket1.setReuseAddress(true);

                jogador1 = conexao.receivePlayer(clientSocket1);
                jogador1.setId(1);
                clientID1 = clientSocket1.getInetAddress().getHostAddress();
                conexao.sendPlayer(clientSocket1, jogador1);
            }
        } catch (Exception e) {
            System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
        }

        try {
            clientSocket2 = serverSocket_main.accept();
            if (clientSocket2.isConnected()) {
                System.out.println("Jogador 2 entrou");
                jogador2 = conexao.receivePlayer(clientSocket2);
                jogador2.setId(2);
                clientID2 = clientSocket2.getInetAddress().getHostAddress();
                conexao.sendPlayer(clientSocket2, jogador2);
            }
        } catch (IOException e) {
            System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
        }
        Thread.sleep(1000);
        new Thread(gamingSession).start();
        new Thread(updateClients).start();

        boolean whileBreakerEndGame = false;
        while (whileBreakerEndGame == false) {
            if (jogador1.getVida() <= 0) {
                endGame = true;
                winnerID = jogador2.getId();
                winner = jogador2.getNome();
            }
            if (jogador2.getVida() <= 0) {
                endGame = true;
                winnerID = jogador1.getId();
                winner = jogador1.getNome();
            }

            if (endGame == true) {
                whileBreakerEndGame = false;
            }
        }

        if (jogador1.getId() == winnerID) {
            conexao.sendCommand(clientSocket1, "endgame player1win");
            conexao.sendCommand(clientSocket2, "endgame player1win");
        } else {
            conexao.sendCommand(clientSocket1, "endgame player2win");
            conexao.sendCommand(clientSocket2, "endgame player2win");
        }

        whileBreaker_Updater = true;

        clientSocket1.close();
        clientSocket2.close();
        serverSocket_main.close();
        serverSocket_updater.close();
    }
}

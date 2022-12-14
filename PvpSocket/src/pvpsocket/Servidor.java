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
                System.out.println("Criando o Server Socket...");
            } catch (IOException e) {
                System.out.println("Não foi criado o Server Socket.\nCódigo do erro: " + e);
            }
        } else if ("updateServer".equals(serverType)) {
            try {
                serverSocket_updater = new ServerSocket(10000);
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
                conexao.sendCommand(clientSocket1, "prepareToReceive Enemy");
                conexao.sendCommand(clientSocket2, "prepareToReceive Enemy");
                conexao.sendPlayer(clientSocket1, jogador2);
                conexao.sendPlayer(clientSocket2, jogador1);

                conexao.sendCommand(clientSocket1, "startGame");
                conexao.sendCommand(clientSocket2, "startGame");
            } catch (Exception e) {
                System.out.println("Erro de envio de informação, erro: " + e);
            }

            String gamingCommand1;
            String gamingCommand2;

            boolean erro;

            boolean attack1, attack2;
            boolean defense1, defense2;

            while (whileBreaker_GamingSession) {
                defense1 = false;
                defense2 = false;
                attack1 = false;
                attack2 = false;
                erro = false;

                try {
                    gamingCommand1 = conexao.receiveCommand(clientSocket1);
                    gamingCommand2 = conexao.receiveCommand(clientSocket2);

                    switch (gamingCommand1) {
                        case "atacar":
                            jogador2.setVida(jogador2.getVida() - 10);
                            break;

                        case "defender":
                            defense2 = true;
                            break;

                        case "curar":
                            jogador1.setVida(jogador1.getVida() + 15);
                            break;

                        case "desistir":
                            jogador1.setVida(0);
                            break;
                        case "status":
                            conexao.sendCommand(clientSocket1, "sendingStatus");
                            break;
                        default:
                            erro = true;
                            conexao.sendCommand(clientSocket1, "wrongCommand");
                    }
                    switch (gamingCommand2) {
                        case "atacar":
                            attack2 = true;
                            break;

                        case "defender":
                            defense2 = true;
                            break;

                        case "curar":
                            jogador2.setVida(jogador2.getVida() + 15);
                            break;

                        case "desistir":
                            jogador2.setVida(0);
                            break;
                        case "status":
                            conexao.sendCommand(clientSocket2, "sendingStatus");
                            break;
                        default:
                            erro = true;
                            conexao.sendCommand(clientSocket2, "wrongCommand");
                    }
                    if (defense1 == true) {
                        if (attack2 == true) {
                            conexao.sendCommand(clientSocket1, "player1 defended");
                            conexao.sendCommand(clientSocket2, "player1 defended");
                        }
                        conexao.sendCommand(clientSocket1, "you defended nothing");
                    } else {
                        if (attack2 == true) {
                            conexao.sendCommand(clientSocket1, "player2 attacked");
                            jogador1.setVida(jogador1.getVida() - 10);
                        }
                    }
                    if (defense2 == true) {
                        if (attack1 == true) {
                            conexao.sendCommand(clientSocket1, "player2 defended");
                            conexao.sendCommand(clientSocket2, "player2 defended");
                        }
                        conexao.sendCommand(clientSocket2, "you defended nothing");
                    } else {
                        if (attack1 == true) {
                            conexao.sendCommand(clientSocket2, "player1 attacked");
                            jogador1.setVida(jogador1.getVida() - 10);
                        }
                    }

                    if (!erro == true) {
                        conexao.sendCommand(clientSocket1, "continue");
                    }
                    if (!erro == true) {
                        conexao.sendCommand(clientSocket2, "continue");
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
            Socket clientSocketUpdater1;
            Socket clientSocketUpdater2;

            while (whileBreaker_Updater == false) {
                try {
                    clientSocketUpdater1 = serverSocket_updater.accept();
                    if (clientSocketUpdater1.getInetAddress() == clientSocket1.getInetAddress()) {
                        whileBreaker_Updater = true;
                    } else {
                        System.out.println("Client errado tentando conexão com Updater");
                    }
                } catch (IOException e) {
                    System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
                }
            }
            whileBreaker_Updater = false;
            while (whileBreaker_Updater == false) {
                try {
                    clientSocketUpdater2 = serverSocket_updater.accept();
                    if (clientSocketUpdater2.getInetAddress() == clientSocket2.getInetAddress()) {
                        whileBreaker_Updater = true;
                    } else {
                        System.out.println("Client errado tentando conexão com Updater");
                    }
                } catch (IOException e) {
                    System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
                }
            }
            whileBreaker_Updater = false;

            while (whileBreaker_Updater == false) {
                try {
                    conexao.sendPlayer(clientSocket1, jogador1);
                    conexao.sendPlayer(clientSocket1, jogador2);
                    conexao.sendPlayer(clientSocket2, jogador1);
                    conexao.sendPlayer(clientSocket2, jogador2);
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Envio de atualização mal-sucedida, erro: " + e);
                }
            }
        }
    };

    public static void main(String[] args) throws Exception {
        createServer("mainServer");
        
        conexao = new Conexao();

        try {
            clientSocket1 = serverSocket_main.accept();
            if (clientSocket1.isConnected()) {
                System.out.println("Jogador 1 entrou");
                jogador1 = conexao.receivePlayer(clientSocket1);
                jogador1.setId(1);
                conexao.sendPlayer(clientSocket1, jogador1);
            }
        } catch (Exception e) {
            System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
        }

        createServer("updateServer");

        new Thread(updateClients).start();
        conexao.sendCommand(clientSocket1, "Connection Success");

        try {
            clientSocket2 = serverSocket_main.accept();
            if (clientSocket2.isConnected()) {
                System.out.println("Jogador 2 entrou");
                jogador2 = conexao.receivePlayer(clientSocket2);
                jogador2.setId(2);
                conexao.sendPlayer(clientSocket2, jogador2);
            }
        } catch (IOException e) {
            System.out.println("Conexão mal sucedida.\nCódigo do erro: " + e);
        }
        conexao.sendCommand(clientSocket2, "Connection Success");

        new Thread(gamingSession).start();

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

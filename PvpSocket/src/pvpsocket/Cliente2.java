/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pvpsocket;

import java.io.IOException;
import static java.lang.System.in;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author pedro
 */
public class Cliente2 {

    static Jogador jogador;
    static Jogador inimigo;

    static Socket mainSocket, updateSocket;

    static Scanner scan;
    static Conexao conexao;

    static String command;

    static boolean whileBreaker_gamingSession, whileBreaker_updater;

    public static void connectServer(String serverType) {
        if ("mainSocket".equals(serverType)) {
            try {
                mainSocket = new Socket("localhost", 20000);
                mainSocket.setSoTimeout(0);
                System.out.println(mainSocket.getSoTimeout());
                System.out.println("Criando o Main Socket...");
            } catch (IOException e) {
                System.out.println("Não foi criado o Main Socket.\nCódigo do erro: " + e);
            }
        }
        if ("updateSocket".equals(serverType)) {
            try {
                updateSocket = new Socket("localhost", 10000);
                updateSocket.setSoTimeout(0);
                System.out.println("Criando o Updater Socket...");
            } catch (IOException e) {
                System.out.println("Não foi criado o Updater Socket.\nCódigo do erro: " + e);
            }
        }
    }

    private static void gamingCommands(String commands) {
        switch (commands) {
            case "wrongCommand":
                System.out.println("Comando incorreto");
                break;

            case "sendingStatus":
                System.out.println(jogador.toString());
                System.out.println("");
                System.out.println(inimigo.toString());
                break;

            case "player1 defended":
                if (jogador.getId() == 1) {
                    System.out.println("Você se defendeu de um ataque");
                } else {
                    System.out.println("Jogador inimigo se defendeu");
                }
                break;

            case "player2 defended":
                if (jogador.getId() == 2) {
                    System.out.println("Você se defendeu de um ataque");
                } else {
                    System.out.println("Jogador inimigo se defendeu");
                }
                break;

            case "player1 attacked":
                if (jogador.getId() == 1) {
                    System.out.println("Você foi atacado");
                } else {
                    System.out.println("Você atacou o inimigo com sucesso");
                }
                break;

            case "player2 attacked":
                if (jogador.getId() == 2) {
                    System.out.println("Você foi atacado");
                } else {
                    System.out.println("Você atacou o inimigo com sucesso");
                }
                break;

            case "both attacked":
                System.out.println("Os dois atacaram um ao outro");
                break;
                
            case "defense increased":
                System.out.println("Defesa acrescida em 1");
                break;

            case "you defended nothing":
                System.out.println("Você se defendeu de nada");
                break;

            case "cure":
                System.out.println("Você se curou | +15 de vida");
                break;

            case "endgame player1win":
                if (jogador.getId() == 1) {
                    System.out.println("FIM DE JOGO\nVENCEDOR: " + jogador.getNome());
                } else {
                    System.out.println("FIM DE JOGO\nVENCEDOR: " + inimigo.getNome());
                }
                whileBreaker_gamingSession = true;
                whileBreaker_updater = true;
                break;

            case "endgame player2win":
                if (jogador.getId() == 2) {
                    System.out.println("FIM DE JOGO\nVENCEDOR: " + jogador.getNome());
                } else {
                    System.out.println("FIM DE JOGO\nVENCEDOR: " + inimigo.getNome());
                }
                whileBreaker_gamingSession = true;
                whileBreaker_updater = true;
                break;

            default:
                System.out.println("");
        }
        //System.out.println("");
        System.out.println("");
    }

    private static Runnable gamingSession = new Runnable() {
        @Override
        public void run() {
            whileBreaker_gamingSession = false;
            
            String command;

            System.out.println("Digite um dos comandos abaixo: \natacar | defender | curar | desistir | status");
            while (whileBreaker_gamingSession == false) {
                System.out.print(">> ");
                try {
                    conexao.sendCommand(mainSocket, scan.nextLine());

                    gamingCommands(conexao.receiveCommand(mainSocket).trim());
                    
                    mainSocket.setSoTimeout(1000);
                    command = conexao.receiveCommand(mainSocket).trim();
                    if (command != null) gamingCommands(command);
                    mainSocket.setSoTimeout(0);
                } catch (Exception e) {
                    System.out.println("Erro na sessão, erro: " + e);
                }
            }

        }
    };

    private static Runnable updater = new Runnable() {
        @Override
        public void run() {
            System.out.println("Servidor de update ligado");
            while (whileBreaker_updater) {
                try {
                    jogador = conexao.receivePlayer(updateSocket);
                    Thread.sleep(100);
                    inimigo = conexao.receivePlayer(updateSocket);
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Erro de conexão no updater, erro: " + e);
                }
            }
        }
    };

    public static void main(String[] args) {
        scan = new Scanner(in);
        conexao = new Conexao();

        System.out.print("Digite seu nome: ");
        jogador = new Jogador(scan.nextLine());

        try {
            connectServer("mainSocket");
            conexao.sendPlayer(mainSocket, jogador);
            Thread.sleep(1000);
            jogador = conexao.receivePlayer(mainSocket);
            System.out.println(jogador.toString());

        } catch (Exception e) {
            System.out.println("Erro ao iniciar uma conexão com o servidor, erro: " + e);
        }

        System.out.println("Aguarde o inimigo entrar para iniciar a sessão...");

        try {
            inimigo = conexao.receivePlayer(mainSocket);
            System.out.println("Preparando");

            command = conexao.receiveCommand(mainSocket);
            if ("startGame".equals(command.trim())) {
                System.out.println("Iniciando Jogo...");
                //new Thread(updater).start();
                //Thread.sleep(1000);
                new Thread(gamingSession).start();
            }

        } catch (Exception e) {
            System.out.println("Erro ao preparar a sessão, erro: " + e);
        }
    }
}

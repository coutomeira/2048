import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;
import static com.googlecode.lanterna.TerminalFacade.createSwingTerminal;

import java.util.*;
import java.io.*;

public class Jogo {

    //VARIAVEIS GLOBAIS DE ESCOLHA
    public static int vitoria = 2048;
    public static int size = 4;
    public static int modo = 0;

    //VARIAVEIS GLOBAIS
    public static int[][] matriz = new int[size][size];
    public static int[][] anterior = new int[size][size];
    public static int[][] temp = new int[size][size];
    public static int pontos = 0;
    public static int jogadas = 0;
    public static int best = 0;
    public static int ptemp;
    public static boolean voltou = false;
    public static boolean moveu = false;
    public static boolean valido = true;
    public static boolean first = true;
    private Terminal term;

    public Jogo() throws InterruptedException {

        ver_maximo();
        inserir();
        inserir();

        term = createSwingTerminal(45,38);
        term.enterPrivateMode();
        term.setCursorVisible(false);

        while(!perdeu() && !ganhou()) {

            Key input = term.readInput();

            if (input != null) {
                switch (input.getKind()) {


                    case Escape:
                        System.exit(0);
                    case ArrowLeft:
                        jogar('a');
                        break;
                    case ArrowRight:
                        jogar('d');
                    case ArrowDown:
                        jogar('s');
                        break;
                    case ArrowUp:
                        jogar('w');
                        break;
                    case Backspace:
                        voltar_atras();
                }
                if(moveu) {
                    voltou = false;
                    first = false;

                    ver_maximo();
                    guardar_anterior();
                    inserir();

                    term.clearScreen();
                    jogadas++;

                    moveu = false;
                }
                if(voltou)
                    term.clearScreen();
            }

            String string = null;
            String space1 = null;
            String space2 = null;

            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++) {
                    if (matriz[i][j] == 0) {
                        term.applyBackgroundColor(105,105,105);
                        string = "     ";
                    } else {
                        if (matriz[i][j] == 2){
                            term.applyBackgroundColor(245,245,245);
                            space1 = "  "; space2="  ";}
                        else if (matriz[i][j] == 4) {
                            term.applyBackgroundColor(238,232,170);
                            space1 = "  "; space2="  ";}
                        else if (matriz[i][j] == 8) {
                            term.applyBackgroundColor(255,160,122);
                            space1 = "  "; space2="  ";}
                        else if (matriz[i][j] == 16) {
                            term.applyBackgroundColor(250,128,114);
                            space1 = "  "; space2 = " ";}
                        else if (matriz[i][j] == 32) {
                            term.applyBackgroundColor(220,20,60);
                            space1 = "  "; space2 = " ";}
                        else if (matriz[i][j] == 64) {
                            term.applyBackgroundColor(139,0,0);
                            space1 = "  "; space2 = " ";}
                        else if (matriz[i][j] == 128) {
                            term.applyBackgroundColor(189,183,107);
                            space1 = " "; space2 = " ";}
                        else if (matriz[i][j] == 256) {
                            term.applyBackgroundColor(255,255,0);
                            space1 = " "; space2= " ";}
                        else if (matriz[i][j] == 512) {
                            term.applyBackgroundColor(255,215,0);
                            space1 = " "; space2 = " ";}
                        else if (matriz[i][j] == 1024) {
                            term.applyBackgroundColor(218,165,32);
                            space1 = " "; space2 = "";}
                        else if (matriz[i][j] == 2048) {
                            term.applyBackgroundColor(184,134,11);
                            space1 = " "; space2 = "";}
                        string = space1 + matriz[i][j] + space2 ;
                    }
                    term.applyForegroundColor(0,0,0);
                    show("     ", 10 + (j * 7), 10 + (i * 4));
                    show(string, 10 + (j * 7), 11 + (i * 4));
                    show("     ", 10 + (j * 7), 12 + (i * 4));

                }

            term.applyBackgroundColor(Terminal.Color.DEFAULT);
            term.applyForegroundColor(255,255,255);
            String pt = Integer.toString(pontos);
            String max = Integer.toString(best);
            String jog = Integer.toString(jogadas);

            if(!first && !voltou)
                show("BACKSPACE", 10,32);
            show("2048", 10, 4);
            show("GAME", 10, 5);
            showinverso("BEST", 28, 4);
            showinverso(max, 35, 4);
            showinverso("SCORE", 28, 5);
            showinverso(pt, 35, 5);
            showinverso("MOVES", 35, 32);
            showinverso(jog, 35, 33);


            try {
                Thread.sleep(5);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        String msg = null;
        if (ganhou()) {
            msg = "YOU WIN!";
            term.applyForegroundColor(Terminal.Color.GREEN);
        }
        if (perdeu()) {
            msg = "YOU LOSE";
            term.applyForegroundColor(Terminal.Color.RED);
        }

        show("********", 10, 27);
        show(msg, 10, 28);
        show("********", 10, 29);
        Thread.sleep(5000);
   }


    private void show(String str, int x, int y) {
        int len = str.length();
        term.moveCursor(x, y);

        for (int i = 0; i < len; i++)
            term.putCharacter(str.charAt(i));
    }

    //SHOW ENCOSTADO A DIREITA
    private void showinverso(String str, int x, int y) {
        int len = str.length();
        term.moveCursor(x-len+1, y);

        for (int i = 0; i < len; i++)
            term.putCharacter(str.charAt(i));
    }


    //GUARDA A ANTERIOR NO CASO DE TER HAVIDO MOVIMENTO
    public static void guardar_anterior()
    {
        for(int i=0; i<size ; i++)
            for(int j=0; j<size ; j++)
                anterior[i][j] = temp[i][j];
    }

    //ATUALIZAR A ATUAL PARA A ANTERIOR
    public static void voltar_atras() {
        if (!first && !voltou) {
            voltou = true;
            pontos -= ptemp;
            jogadas--;
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    matriz[i][j] = anterior[i][j];
        }
    }



    public static void ver_maximo () {
        try {
            int max;

            File file = new File("maximo.txt");
            file.createNewFile();

            Scanner ler = new Scanner(file);

            //PRIMEIRA VEZ QUE SE JOGA
            if (file.length() == 0)
                max = 0;

            else
                max = ler.nextInt();


            best = max;

            if (pontos >= max) {

                best = pontos;

                //GUARDA NO FICHEIRO
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(Integer.toString(pontos));


                bw.close();

            }
        }
        catch(IOException ie ){
            ie.printStackTrace();
        }

    }

    //DIZ SE PERMITE JUNTAR CASAS
    public static boolean iguais_juntos() {
        //PERCORRE COLUNA A COLUNA
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size - 1; j++)
                if (matriz[i][j] == matriz[i][j + 1])
                    return true;
        //PERCORRE LINHA A LINHA
        for (int j = 0; j < size; j++)
            for (int i = 0; i < size - 1; i++)
                if (matriz[i][j] == matriz[i + 1][j])
                    return true;
        return false;
    }

    //DIZ SE HA CASAS A ZERO
    public static boolean zeros() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (matriz[i][j] == 0)
                    return true;
        return false;
    }

    //VERIFICA SE PERDEU
    //ANALISANDO AS DUAS CONDICOES
    //PARA CONTINUAR
    public static boolean perdeu() {
        return (!zeros() && !iguais_juntos());
    }

    //VERIFICAR SE GANHOU
    public static boolean ganhou() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (matriz[i][j] == vitoria)
                    return true;
        return false;
    }

    //IMPRIMIR A MATRIZ
    public static void imprimir() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (matriz[i][j] == 0)
                    System.out.print("   ." + "  ");
                else
                    System.out.printf("%4d  ", matriz[i][j]);
            }
            System.out.println();
        }
        System.out.printf("\n\nPontos: %6d\nMaximo: %6d\nJogadas: %5d\n", pontos, best, jogadas);
    }

    //IMPRIMIR UMA MENSAGEM E SAIR
    //EM CASO DE VITORIA OU DERROTA
    //A JOGAR SEM TERMINAL
    public static void mensagem(int a) {
        System.out.println("*****************");
        if (a == 1)
            System.out.println("* GANHOU O JOGO *");
        if (a == 0)
            System.out.println("* PERDEU O JOGO *");
        System.out.println("*****************");
        System.exit(0);
    }


    //INSERIR NUMERO ALEATORIO
    //EM POSICOES LIVRES
    public static void inserir() {
        Random nrand = new Random();
        int n = 0;
        int vetorpares[][] = new int[2][size * size];

        //VER A MATRIZ PARA SABER QUAIS E QUANTOS LIVRES
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (matriz[i][j] == 0) {
                    vetorpares[0][n] = i;
                    vetorpares[1][n] = j;
                    n++;//CONTADOR DE POSICOES LIVRES
                }
        int pos = nrand.nextInt(n);//ESCOLHER POSICAO DISPONIVEL
        int val = nrand.nextInt(2);//ESCOLHER SE DOIS OU QUATRO
        if (val == 0) val = 2;
        else val = 4;

        //INSERIR
        matriz[vetorpares[0][pos]][vetorpares[1][pos]] = val;
    }


    //ANALISAR DIRECAO PRETENDIDA
    public static void jogar(char dir) {
        int[] v = new int[size];
        int a = 0;
        int b = 0;

    for(int i=0 ; i<size ; i++ )
        for(int j=0 ; j<size ; j++)
            temp[i][j] = matriz[i][j];

        //QUANTAS VEZES E NECESSARIO RODAR PARA A ESQUERDA
        //ESQUERDA
        if (dir == 'a' || dir == 'A') {
            a = 0;
            b = 0;
        }

        //CIMA
        else if (dir == 'w' || dir == 'W') {
            a = 1;
            b = 3;
        }

        //DIREITA
        else if (dir == 'd' || dir == 'D') {
            a = 2;
            b = 2;
        }

        //BAIXO
        else if (dir == 's' || dir == 'S') {
            a = 3;
            b = 1;
        }

        //ENTRADA INVALIDA
        else {
            System.out.println("---JOGADA INVALIDA---\n");
            valido = false;
            return;
        }
        //COLOCAR EM POSICAO PARA PROCESSAR
        for (int i = 0; i < a; i++)
            matriz = roda_esquerda();

        ptemp = 0;
        //PROCESSO DE MUDANCA
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++)
                v[j] = matriz[i][j];
            v = altera(v);
            for (int k = 0; k < size; k++)
                matriz[i][k] = v[k];
        }

        //COLOCAR NA POSICAO ORIGINAL
        for (int i = 0; i < b; i++)
            matriz = roda_esquerda();

        //INSERIR NOVO NUMERO NA MATRIZ
        //DEPOIS DE ALTERADA

    }

    //RODAR A MARTIZ PARA A ESQUERDA
    public static int[][] roda_esquerda() {

        int[][] ret = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                ret[i][j] = matriz[j][size - i - 1];
            }
        }
        return ret;
    }


    //MUDAR O VETOR
    public static int[] altera(int v[]) {

        //PRIMEIRO ENCOSTA TODOS OS NUMEROS
        //ELIMINANDO OS ZEROS
        encostar(v);

        int a = 0;
        for (int i = 0; i < size - 1; i++) {
            if (v[i] == v[i + 1] && v[i] != 0) {
                v[i] += v[i];
                v[i + 1] = 0;
                ptemp += v[i];
                pontos += v[i];//LOCAL ONDE SAO SOMADOS OS PONTOS
                moveu = true;
            }
        }

        //VOLTA A ENCOSTAR TODOS OS NUMEROS
        //ELIMINANDOO OS ZEROS
        encostar(v);
        return v;
    }

    //ENCOSTA OS NUMEROS PARA QUE NAO HAJA ZEROS NO MEIO
    public static void encostar(int v[]) {
        int temp;
        for (int k = 0; k < size; k++)
            for (int i = 0; i < size; i++)
                if (v[i] == 0)
                    for (int j = i; j < size - 1; j++) {
                        if (v[j + 1] != 0) {
                            temp = v[j];
                            v[j] = v[j + 1];
                            v[j + 1] = temp;
                            moveu = true;
                        }
                    }

    }


    //MAIN
    public static void main(String[] args) throws InterruptedException {
        if (modo == 0) {
            new Jogo();
            System.exit(0);
        }
        else {
            Scanner ler = new Scanner(System.in);

            //INSERIR DOIS ELEMENTOS NO INICIO E MOSTRAR
            ver_maximo();
            inserir();
            inserir();
            imprimir();

            char input;
            while (!ganhou() && !perdeu()) {

                input = ler.next().charAt(0);
                jogar(input);

                if (moveu) {
                    if (pontos > best)
                        ver_maximo();
                    jogadas++;
                    inserir();

                }
                if (valido)
                    imprimir();
                valido = true;
                moveu = false;

            }

            if (ganhou())
                mensagem(1);
            if (perdeu())
                mensagem(0);
        }
    }

}





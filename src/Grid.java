import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

public class Grid {


        //PROPRIEDADES DA CLASSE

        private Prompt prompt = new Prompt(System.in, System.out);

        private String[][] wordMatrix;
        private StringInputScanner inputScanner;
        private String lastTypedWord;

        private int playerScore = 0;
        private int countBlankSpaces;
        private int rows;
        private int cols;


        //CONSTRUCTOR
        public Grid(int rows, int cols) {

            //INICIALIZAÇÃO DE PROPRIEDADES
            this.rows = rows;
            this.cols = cols;
            wordMatrix = new String[rows][cols];
            countBlankSpaces = 0;

            inputScanner = new StringInputScanner();
            inputScanner.setMessage("Chose and type a word from the given Matrix: ");


            //INVOCAÇÃO DE MÉTODOS E BLOCOS QUE FAZEM CORRER O JOGO
            setWordsForMatrix();
            drawMatrix();
            showPlayerScore();

            while(true) {
                String playerInput = prompt.getUserInput(inputScanner);
                lastTypedWord = playerInput;

                checkPlayerInput();

                drawMatrix();
                showPlayerScore();

                if(!gameFinishChecker()){
                    break;
                }
            }
        }

        //SHOW PLAYER SCORE
        public void showPlayerScore() {
            System.out.println("Player score: " + playerScore);
        }

        //DESENHA UMA MATRIZ DE PALAVRAS NO TERMINAL
        public StringBuilder drawMatrix() {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < wordMatrix.length; i++) {

                for (int j = 0; j < wordMatrix[i].length; j++) {

                    sb.append(wordMatrix[i][j]);

                }
                sb.append("\n");
            }
            return sb;
        }

        //LISTA DE PALAVRAS -- EXTRA FEATURE A IMPLEMENTAR, PODER SER EXTENSÍVEL
        public void setWordsForMatrix(){

            wordMatrix[0][0] = "missile" + "   ";
            wordMatrix[0][1] = "damn" + "      ";
            wordMatrix[0][2] = "witness" + "   ";
            wordMatrix[0][3] = "acid" + "      ";
            wordMatrix[0][4] = "location" + "  ";
            wordMatrix[0][5] = "wake" + "      ";
            wordMatrix[0][6] = "X-ray" + "     ";
            wordMatrix[0][7] = "export" + "    ";
            wordMatrix[0][8] = "indirect" + "  ";
            wordMatrix[0][9] = "peanut" + "    ";
            wordMatrix[1][0] = "owl" + "       ";
            wordMatrix[1][1] = "campaign" + "  ";
            wordMatrix[1][2] = "bulb" + "      ";
            wordMatrix[1][3] = "quantity" + "  ";
            wordMatrix[1][4] = "introduce" + " ";
            wordMatrix[1][5] = "coalition" + " ";
            wordMatrix[1][6] = "sink" + "      ";
            wordMatrix[1][7] = "magazine" + "  ";
            wordMatrix[1][8] = "feature" + "   ";
            wordMatrix[1][9] = "slice" + "     ";
            wordMatrix[2][0] = "industry" + "  ";
            wordMatrix[2][1] = "purpose" + "   ";
            wordMatrix[2][2] = "knife" + "     ";
            wordMatrix[2][3] = "immune" + "    ";
            wordMatrix[2][4] = "product" + "   ";
            wordMatrix[2][5] = "sum" + "       ";
            wordMatrix[2][6] = "unrest" + "    ";
            wordMatrix[2][7] = "promise" + "   ";
            wordMatrix[2][8] = "allow" + "     ";
            wordMatrix[2][9] = "gesture" + "   ";
            wordMatrix[3][0] = "exploit" + "   ";
            wordMatrix[3][1] = "delete" + "    ";
            wordMatrix[3][2] = "bark" + "      ";
            wordMatrix[3][3] = "reactor" + "   ";
            wordMatrix[3][4] = "electron" + "  ";
            wordMatrix[3][5] = "laborer" + "   ";
            wordMatrix[3][6] = "episode" + "   ";
            wordMatrix[3][7] = "hand" + "      ";
            wordMatrix[3][8] = "grace" + "     ";
            wordMatrix[3][9] = "seminar" + "   ";
            wordMatrix[4][0] = "ideal" + "     ";
            wordMatrix[4][1] = "equinox" + "   ";
            wordMatrix[4][2] = "affinity" + "  ";
            wordMatrix[4][3] = "herb" + "      ";
            wordMatrix[4][4] = "oil" + "       ";
            wordMatrix[4][5] = "winter" + "    ";
            wordMatrix[4][6] = "embryo" + "    ";
            wordMatrix[4][7] = "vision" + "    ";
            wordMatrix[4][8] = "incident" + "  ";
            wordMatrix[4][9] = "pound" + "     ";

        }

        //METODO PARA VERIFICAR O INPUT DO PLAYER E COMPARAR COM AS PALAVRAS EXISTENTES NA MATRIZ
        public void checkPlayerInput(){

            for (int i = 0; i < wordMatrix.length; i++) {
                for (int j = 0; j < wordMatrix[i].length; j++) {

                    String trimmedWord = wordMatrix[i][j].trim();

                    if (lastTypedWord.equals(trimmedWord)){

                        System.out.println("Player Baljeet got word " + trimmedWord + " right and won "
                                + trimmedWord.length());
                        playerScore += trimmedWord.length();
                        wordMatrix[i][j] = "          ";
                    }
                }
            }
        }


        //METODO PARA VERIFICAR SE AINDA EXISTEM PALAVRAS NA TABELA
        public boolean gameFinishChecker(){

            for (int i = 0; i < wordMatrix.length; i++) {
                for (int j = 0; j < wordMatrix[i].length; j++) {
                    if(wordMatrix[i][j].equals("          ")){
                        countBlankSpaces++;
                        if(countBlankSpaces == wordMatrix.length * cols){
                            System.out.println("THERE ARE NO MORE WORDS! GAME FINISHED!");
                            return false;
                        }

                    }
                }
            }
            countBlankSpaces = 0;
            return true;
        }

    }

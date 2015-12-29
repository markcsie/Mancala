
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

class GameState {

    int[] board1;
    int[] board2;
    int[] mancala;
    final int boardLength;
    boolean anotherTurn;

    public GameState(int[] board1, int[] board2, int mancala1, int mancala2) {
        this.board1 = new int[board1.length];
        System.arraycopy(board1, 0, this.board1, 0, board1.length);
        this.board2 = new int[board2.length];
        System.arraycopy(board2, 0, this.board2, 0, board2.length);
        mancala = new int[2];
        mancala[0] = mancala1;
        mancala[1] = mancala2;
        boardLength = board1.length;
        anotherTurn = false;
    }

    public String toString2() {
        String string = "";
        for (int i = boardLength - 1; i >= 0; i--) {
            string += board2[i] + " ";
        }
        string += System.lineSeparator();

        for (int i = 0; i < boardLength; ++i) {
            string += board1[i] + " ";
        }
        string += System.lineSeparator();

        string += mancala[1] + System.lineSeparator();
        string += mancala[0];
        return string;
    }

    GameState nextState(int i, boolean player) {
        GameState nextState = new GameState(board1, board2, mancala[0], mancala[1]);

        int[] startingBoard;
        int[] opponentBoard;
        int startingMancala;
        if (player) {
            startingBoard = nextState.board1;
            opponentBoard = nextState.board2;
            startingMancala = 0;
        } else {
            startingBoard = nextState.board2;
            opponentBoard = nextState.board1;
            startingMancala = 1;
        }
//        System.out.println(i);
        int total = startingBoard[i];
        startingBoard[i] = 0;
        int j = i + 1;
        while (total > 0) {
            if (j == boardLength) {
                nextState.mancala[startingMancala]++;
                nextState.anotherTurn = true;
            } else if (j > boardLength) {
                opponentBoard[j - boardLength - 1]++;
                nextState.anotherTurn = false;
            } else {
                if (total == 1 && startingBoard[j] == 0) {
                    nextState.mancala[startingMancala] += opponentBoard[boardLength - j - 1] + 1;
                    opponentBoard[boardLength - j - 1] = 0;
                } else {
                    startingBoard[j]++;
                    nextState.anotherTurn = false;
                }
            }
            ++j;
            if (j == 2 * boardLength + 1) {
                j = 0;
            }
            total--;
        }
        return nextState;
    }

    boolean isTerminal() {
        boolean isTerminal = true;
        for (int i = 0; i < boardLength; ++i) {
            if (board1[i] != 0) {
                isTerminal = false;
                break;
            }
        }
        if (isTerminal) {
//            System.out.println("Reach the bottom of player 1");
            return isTerminal;
        }

        isTerminal = true;
        for (int i = 0; i < boardLength; ++i) {
            if (board2[i] != 0) {
                isTerminal = false;
                break;
            }
        }
        if (isTerminal) {
//            System.out.println("Reach the bottom of player 2");
        }
        return isTerminal;
    }

    int getUtility(boolean player) {
        if (isTerminal()) {
            for (int i = 0; i < boardLength; ++i) {
                mancala[0] += board1[i];
                board1[i] = 0;
            }

            for (int i = 0; i < boardLength; ++i) {
                mancala[1] += board2[i];
                board2[i] = 0;
            }
        }
        
        if (player) {
            return mancala[0] - mancala[1];
        } else {
            return mancala[1] - mancala[0];
        }
    }
}

public class mancala {

    private class BestAction {

        final List<String> actionStringList;
        final int value;

        public BestAction(List<String> actionStringList, int value) {
            this.actionStringList = new LinkedList<>();
            this.actionStringList.addAll(actionStringList);

            this.value = value;
        }
    }

    final boolean player;
    final boolean alphaBetaPruning;
    final int cuttingOffDepth;
    PrintWriter traverseLogWriter;
    PrintWriter nextStateWriter;

    public mancala(int player, boolean alphaBetaPruning, int cuttingOffDepth, int i) throws FileNotFoundException {
        this.player = player == 1;
        this.alphaBetaPruning = alphaBetaPruning;
        this.cuttingOffDepth = cuttingOffDepth;
        traverseLogWriter = new PrintWriter("traverse_log" + i + ".txt");
        nextStateWriter = new PrintWriter("next_state" + i + ".txt");
    }

    private BestAction miniMax(GameState gameState) {
        return maxValue(gameState, "root", -1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    private BestAction maxValue(GameState gameState, String actionString, int action, boolean keepMoving, int alpha, int beta, int depth) {
//        System.out.println("maxValue ");
//        System.out.println(gameState.toString2());
        if (gameState.isTerminal() || depth >= cuttingOffDepth && !keepMoving) {
            writeTraverseLog(actionString, depth, gameState.getUtility(player), alpha, beta);

            List<String> actionStringList = new LinkedList<>();
            actionStringList.add(actionString);
            return new BestAction(actionStringList, gameState.getUtility(player));
        }

        int v = Integer.MIN_VALUE;
        writeTraverseLog(actionString, depth, v, alpha, beta);

        List<String> bestActionStringList = new LinkedList<>();

        String actionName;
        int[] actionArray;
        int startingIndex;
        int endingIndex;
        int step;
        if (player) {
            actionName = "B";
            actionArray = gameState.board1;
            startingIndex = 0;
            endingIndex = gameState.boardLength - 1;
            step = 1;
        } else {
            actionName = "A";
            actionArray = gameState.board2;
            startingIndex = gameState.boardLength - 1;
            endingIndex = 0;
            step = -1;
        }
        int i = startingIndex;
        int count = 0;
        while (count <= Math.abs(endingIndex - startingIndex)) {
            if (actionArray[i] > 0) {
                GameState nextState = gameState.nextState(i, player);
                int nextAction;
                if (player) {
                    nextAction = i + 2;
                } else {
                    nextAction = gameState.boardLength - i + 1;
                }

                int nextDepth = depth;
                if (!keepMoving) {
                    nextDepth++;
                }
                BestAction bestAction;
                if (nextState.anotherTurn) {
                    bestAction = maxValue(nextState, actionName + nextAction, nextAction, nextState.anotherTurn, alpha, beta, nextDepth);
                } else {
                    bestAction = minValue(nextState, actionName + nextAction, nextAction, nextState.anotherTurn, alpha, beta, nextDepth);
                }

                if (bestAction.value > v) {
                    v = bestAction.value;
                    if (!nextState.anotherTurn) {
                        bestAction.actionStringList.clear();
                    }
                    bestAction.actionStringList.add(0, actionName + nextAction);
                    bestActionStringList = bestAction.actionStringList;
                }

                if (alphaBetaPruning && v >= beta) {
                    writeTraverseLog(actionString, depth, v, alpha, beta);
                    break;
                }
                alpha = Integer.max(alpha, v);
                writeTraverseLog(actionString, depth, v, alpha, beta);
            }
            count++;
            i += step;
        }
        return new BestAction(bestActionStringList, v);
    }

    private BestAction minValue(GameState gameState, String actionString, int action, boolean keepMoving, int alpha, int beta, int depth) {
//        System.out.println("minValue ");
//        System.out.println(gameState.toString2());
        if (gameState.isTerminal() || depth >= cuttingOffDepth && !keepMoving) {
            writeTraverseLog(actionString, depth, gameState.getUtility(player), alpha, beta);

            List<String> actionStringList = new LinkedList<>();
            actionStringList.add(actionString);
            return new BestAction(actionStringList, gameState.getUtility(player));
        }

        int v = Integer.MAX_VALUE;
        writeTraverseLog(actionString, depth, v, alpha, beta);

        List<String> bestActionStringList = new LinkedList<>();

        String actionName;
        int[] actionArray;
        int startingIndex;
        int endingIndex;
        int step;
//        System.out.println("player " + player);
        if (!player) {
            actionName = "B";
            actionArray = gameState.board1;
            startingIndex = 0;
            endingIndex = gameState.boardLength - 1;
            step = 1;
        } else {
            actionName = "A";
            actionArray = gameState.board2;
            startingIndex = gameState.boardLength - 1;
            endingIndex = 0;
            step = -1;
//            System.out.println("A moves");
        }
        int i = startingIndex;
        int count = 0;
        while (count <= Math.abs(endingIndex - startingIndex)) {
            if (actionArray[i] > 0) {
                int nextAction;
                GameState nextState = gameState.nextState(i, !player);
                if (!player) {
                    nextAction = i + 2;
                } else {
                    nextAction = gameState.boardLength - i + 1;
                }
                int nextDepth = depth;
                if (!keepMoving) {
                    nextDepth++;
                }
                BestAction bestAction;
                if (nextState.anotherTurn) {
                    bestAction = minValue(nextState, actionName + nextAction, nextAction, nextState.anotherTurn, alpha, beta, nextDepth);
                } else {
                    bestAction = maxValue(nextState, actionName + nextAction, nextAction, nextState.anotherTurn, alpha, beta, nextDepth);
                }
                if (bestAction.value < v) {
                    v = bestAction.value;
                    if (!nextState.anotherTurn) {
                        bestAction.actionStringList.clear();
                    }
                    bestAction.actionStringList.add(0, actionName + nextAction);
                    bestActionStringList = bestAction.actionStringList;
                }

                if (alphaBetaPruning && v <= alpha) {
                    writeTraverseLog(actionString, depth, v, alpha, beta);
                    break;
                }
                beta = Integer.min(beta, v);
                writeTraverseLog(actionString, depth, v, alpha, beta);
            }
            count++;
            i += step;
        }
        return new BestAction(bestActionStringList, v);
    }

    private void writeTraverseLog(String action, int depth, int value, int alpha, int beta) {
        traverseLogWriter.print(action + "," + depth + ",");
        if (value == Integer.MAX_VALUE) {
            traverseLogWriter.print("Infinity");
        } else if (value == Integer.MIN_VALUE) {
            traverseLogWriter.print("-Infinity");
        } else {
            traverseLogWriter.print(value);
        }
        if (alphaBetaPruning) {
            traverseLogWriter.print(",");

            if (alpha == Integer.MIN_VALUE) {
                traverseLogWriter.print("-Infinity");
            } else {
                traverseLogWriter.print(alpha);
            }
            traverseLogWriter.print(",");

            if (beta == Integer.MAX_VALUE) {
                traverseLogWriter.print("Infinity");
            } else {
                traverseLogWriter.print(beta);
            }
        }
        traverseLogWriter.println();
    }

    public static void main(String[] args) {
        for (int ii = 1; ii <= 30; ii++) {
            String fileName = "testInput_" + ii + ".txt";
            System.out.println(fileName);
            
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
                int taskType = Integer.valueOf(reader.readLine());
                int playerNumber = Integer.valueOf(reader.readLine());
                int cuttingOffDepth = Integer.valueOf(reader.readLine());

                String[] board2String = reader.readLine().split("\\s+");
                int boardLength = board2String.length;
                int[] board2 = new int[boardLength];
                for (int i = boardLength - 1; i >= 0; i--) {
                    board2[i] = Integer.valueOf(board2String[boardLength - i - 1]);
                }

                String[] board1String = reader.readLine().split("\\s+");
                int[] board1 = new int[boardLength];
                for (int i = 0; i < boardLength; ++i) {
                    board1[i] = Integer.valueOf(board1String[i]);
                }

                int mancala2 = Integer.valueOf(reader.readLine());
                int mancala1 = Integer.valueOf(reader.readLine());
                GameState gameState = new GameState(board1, board2, mancala1, mancala2);
                System.out.println("Initial ");
                System.out.println(gameState.toString2());

                mancala task = null;
                BestAction bestAction = null;
                switch (taskType) {
                    case 1: // Greedy
                        task = new mancala(playerNumber, false, 1, ii);
                        bestAction = task.miniMax(gameState);

                        break;
                    case 2: // MiniMax
                        task = new mancala(playerNumber, false, cuttingOffDepth, ii);
                        task.traverseLogWriter.println("Node,Depth,Value");
                        bestAction = task.miniMax(gameState);

                        break;
                    case 3: // Alpha-Beta
                        task = new mancala(playerNumber, true, cuttingOffDepth, ii);
                        task.traverseLogWriter.println("Node,Depth,Value,Alpha,Beta");
                        bestAction = task.miniMax(gameState);
                        break;
                    case 4: // Competition

                        break;
                    default:
                        System.out.println("WTF");
                }

                GameState nextState = gameState;
                System.out.println(bestAction.actionStringList);
                for (int i = 0; i < bestAction.actionStringList.size(); ++i) {
                    if (task.player) {
                        if (bestAction.actionStringList.get(i).charAt(0) != 'B') {
                            break;
                        }

                    } else if (bestAction.actionStringList.get(i).charAt(0) != 'A') {
                        break;
                    }
                    int move = Character.getNumericValue(bestAction.actionStringList.get(i).charAt(1));
//                    System.out.println("move " + move);
                    if (task.player) {
                        move = move - 2;
                    } else {
                        move = gameState.boardLength - move + 1;
                    }
                    nextState = nextState.nextState(move, task.player);
                }
                task.nextStateWriter.println(nextState.toString2());
//                System.out.println("Answer ");
//                System.out.println(nextState.toString2());

                // output
                task.traverseLogWriter.flush();
                task.nextStateWriter.flush();
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }
    }
}
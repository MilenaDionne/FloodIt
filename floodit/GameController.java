import java.awt.event.*;
import javax.swing.JOptionPane;
import java.io.*;

/**
 * The class <b>GameController</b> is the controller of the game. It has a method
 * <b>selectColor</b> which is called by the view when the player selects the next
 * color. It then computes the next step of the game, and updates model and view.
 *
 * @author Weiyun Lu, University of Ottawa
 */


public class GameController implements ActionListener {
    /**
     * Reference to the model of the game
     */
    protected GameModel gameModel;

    /**
     * Reference to the view of the board
     */
    protected GameView gameView;
    protected int gameSize;

    protected GenericLinkedStack<GameModel> previousStates;
    protected GenericLinkedStack<GameModel> nextStates;

    /**
     * Constructor used for initializing the controller. It creates the game's view 
     * and the game's model instances
     * 
     * @param size
     *            the size of the board on which the game will be played
     */
    public GameController(int size) {

        gameSize = size;

        File savedGame = new File("savedGame.ser");

        if (savedGame.exists()) {
            try {
                FileInputStream f_in = new FileInputStream("savedGame.ser");
                ObjectInputStream o_in = new ObjectInputStream(f_in);
                Object saved = o_in.readObject();
                f_in.close();
                GameModel savedModel = (GameModel) saved;
                if (savedModel.getSize() == gameSize) {
                    System.out.println("Restoring old game.");
                    gameModel = savedModel;
                    savedGame.delete();
                } else {
                    gameModel = new GameModel(gameSize);
                }
            }
            catch (Exception e) {
                System.out.println("Error restoring previously saved game... starting a new game instead.");
                gameModel = new GameModel(gameSize);
            }
        } else {
            gameModel = new GameModel(gameSize);
        }

        gameView = new GameView(gameModel, this);
        flood(); 
        gameView.update();

        previousStates = new GenericLinkedStack<GameModel>();
        nextStates = new GenericLinkedStack<GameModel>();
    }

    /**
     * resets the game
     */
    public void reset(){

        clearPreviousStates();
        clearNextStates();
        gameModel.reset();
        gameView.update();
    }

    /**
     * saves the current state of the game before an action, so that it can be undone
     */
    public void saveState() {
        try{
            GameModel stateCopy = gameModel.clone();
            previousStates.push(stateCopy);
            if (!gameView.undoButton.isEnabled()) {
                gameView.undoButton.setEnabled(true);
            }
        }
        catch (CloneNotSupportedException e) {
            System.out.println("Error: Failed to clone game state.");
        }
    }

    /**
     * clears the previous states that can be undone, when the game is reset
     */
    public void clearPreviousStates() {
        while (!previousStates.isEmpty()) {
            previousStates.pop();
        }
        gameView.undoButton.setEnabled(false);
    }

    /**
     * clears the states that can be redone whenever the user makes a new action
     */
    public void clearNextStates() {
        while (!nextStates.isEmpty()) {
            nextStates.pop();
        }
        gameView.redoButton.setEnabled(false);
    }

    /**
     * restores the game to the most recent previous state, and puts this state into the stack of redoable ones
     */
    public void restoreState() {
        GameModel previousState = previousStates.pop();
        nextStates.push(gameModel);
        gameModel = previousState;
        gameView.gameModel = previousState;
        gameView.update();
        if (!gameView.redoButton.isEnabled()) {
            gameView.redoButton.setEnabled(true);
        }
        if (previousStates.isEmpty()) {
            gameView.undoButton.setEnabled(false);
        }
    }

    /**
     * redoes an undone move, when available
     */
    public void redoState() {
        saveState();
        GameModel nextState = nextStates.pop();
        gameModel = nextState;
        gameView.gameModel = nextState;
        gameView.update();
        if (nextStates.isEmpty()) {
            gameView.redoButton.setEnabled(false);
        }
    }

    /**
     * serializes the game state for restoring
     */
    public void saveGame() {
        try {  
            FileOutputStream f_out = new FileOutputStream("savedGame.ser");
            ObjectOutputStream o_out = new ObjectOutputStream(f_out);
            o_out.writeObject(gameModel);
            o_out.close();
            System.out.println("Game saved.");
        } catch (IOException e) {
            System.out.println("Error saving the game.");
        }
    }

    /**
     * Callback used when the user clicks a button (reset or quit)
     *
     * @param e
     *            the ActionEvent
     */

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == gameView.resetButton) {
            reset();
        } else if (e.getSource() == gameView.quitButton) {
            saveGame();
            System.exit(0);
        } else if (e.getSource() == gameView.undoButton) {
            restoreState();
        } else if (e.getSource() == gameView.redoButton) {
            redoState();
        } else if (e.getSource() == gameView.settingsButton) {
            JOptionPane.showConfirmDialog(null, gameView.settingsBox, "Game Settings", JOptionPane.DEFAULT_OPTION);
        } else if (e.getSource() == gameView.planeButton) {
            if (gameModel.torusMode){
                saveState();
                clearNextStates();
                gameModel.torusMode = false;
            }
        } else if (e.getSource() == gameView.torusButton) {
            if (!gameModel.torusMode) {
                saveState();
                clearNextStates();
                gameModel.torusMode = true;
            }
        } else if (e.getSource() == gameView.orthogonalButton) {
            if (gameModel.diagonalMode) {
                saveState();
                clearNextStates();
                gameModel.diagonalMode = false;
            }
        } else if (e.getSource() == gameView.diagonalButton) {
            if (!gameModel.diagonalMode) {
                saveState();
                clearNextStates();
                gameModel.diagonalMode = true;
            }
        } else {
            for (int x = 0; x < gameSize; x++) {
                for (int y = 0; y < gameSize; y++) {
                    if (e.getSource() == gameView.board[x][y]) {
                        if (gameModel.getNumberOfSteps() < 0) {
                            saveState();
                            clearNextStates();
                            gameModel.capture(x, y);
                            selectColor(gameModel.model[x][y].getColor());
                        }
                        if (!gameModel.model[x][y].isCaptured()) {
                            saveState();
                            clearNextStates();
                            selectColor(gameModel.model[x][y].getColor());
                        }
                        break;
                    }
                }
            }
        }

    }

    /**
     * <b>selectColor</b> is the method called when the user selects a new color.
     * If that color is not the currently selected one, then it applies the logic
     * of the game to capture possible locations. It then checks if the game
     * is finished, and if so, congratulates the player, showing the number of
     * moves, and gives two options: start a new game, or exit
     * @param color
     *            the newly selected color
     */
    private void selectColor(int color){

        
        if(color != gameModel.getCurrentSelectedColor()) {
            gameModel.setCurrentSelectedColor(color);
            flood();
            gameModel.step();
            gameView.update();

            if(gameModel.isFinished()) {
                      Object[] options = {"Play Again",
                                "Quit"};
                        int n = JOptionPane.showOptionDialog(gameView,
                                "Congratulations, you won in " + gameModel.getNumberOfSteps() 
                                    +" steps!\n Would you like to play again?",
                                "Won",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]);
                        if(n == 0){
                            reset();
                        } else{
                            System.exit(0);
                        }   
                }            
            }        

    }

    /**
     * <b>flood</b> is the method that computes which new dots should be ``captured'' 
     * when a new color has been selected. The Model is updated accordingly
     */
    private void flood() {

        Stack<DotInfo> stack = new GenericArrayStack<DotInfo>(gameModel.getSize()*gameModel.getSize());
        for(int i =0; i < gameModel.getSize(); i++) {
           for(int j =0; j < gameModel.getSize(); j++) {
                if(gameModel.isCaptured(i,j)) {
                    stack.push(gameModel.get(i,j));
                }
           }
        }

        DotInfo dotInfo;
        while(!stack.isEmpty()){
            dotInfo = stack.pop();
            if((dotInfo.getX() > 0) && shouldBeCaptured (dotInfo.getX()-1, dotInfo.getY())) {
                gameModel.capture(dotInfo.getX()-1, dotInfo.getY());
                stack.push(gameModel.get(dotInfo.getX()-1, dotInfo.getY()));
            }  
            if((dotInfo.getX() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX()+1, dotInfo.getY())) {
                gameModel.capture(dotInfo.getX()+1, dotInfo.getY());
                stack.push(gameModel.get(dotInfo.getX()+1, dotInfo.getY()));
            }
            if((dotInfo.getY() > 0) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()-1)) {
                gameModel.capture(dotInfo.getX(), dotInfo.getY()-1);
                stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()-1));
            }  
            if((dotInfo.getY() < gameModel.getSize()-1) && shouldBeCaptured (dotInfo.getX(), dotInfo.getY()+1)) {
                gameModel.capture(dotInfo.getX(), dotInfo.getY()+1);
                stack.push(gameModel.get(dotInfo.getX(), dotInfo.getY()+1));
            }
        }
    }

    /**
     * <b>shouldBeCaptured</b> is a helper method that decides if the dot
     * located at position (i,j), which is next to a captured dot, should
     * itself be captured
     * @param i
     *            row of the dot
     * @param j
     *            column of the dot
     */
    
   private boolean shouldBeCaptured(int i, int j) {
        if(!gameModel.isCaptured(i, j) &&
        (gameModel.getColor(i,j) == gameModel.getCurrentSelectedColor())) {
            return true;
        } else {
            return false;
        }
    }

    

}

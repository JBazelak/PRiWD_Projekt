package com.example.polinav3_SGB;

public class GameController {

    private enum RobotState{
        ROCK,
        PAPER,
        SCISSORS
    };
    private enum PlayerState{
        ROCK,
        PAPER,
        SCISSORS
    };
    // indices for opponent states:
    //  0 - rock
    //  1 - paper
    //  2 - scissors
    // values for game results:
    //  0 - is defeated by
    //  1 - draw
    //  2 - defeats

    private int[][] gameResults= {{1, 0, 2},
                                  {2, 1, 0},
                                  {0, 2, 1}};

    private RobotState robotState = RobotState.ROCK;
    private PlayerState playerState = PlayerState.ROCK;

    private int robotScore, playerScore;

    public void resetGame(){
        robotState = RobotState.ROCK;
        playerState = PlayerState.ROCK;
        robotScore = 0;
        playerScore = 0;
    }

    public void setPlayerState(int playerAction){
        if (playerAction == 0){
            playerState = PlayerState.ROCK;
        } else if (playerAction == 1) {
            playerState = PlayerState.PAPER;
        } else if (playerAction == 2) {
            playerState = PlayerState.SCISSORS;
        }
    }

    public void setRobotState(int robotAction){
        if (robotAction == 0){
            robotState = RobotState.ROCK;
        } else if (robotAction == 1) {
            robotState = RobotState.PAPER;
        } else if (robotAction == 2) {
            robotState = RobotState.SCISSORS;
        }
    }

    private void roundResult(){
        robotScore += gameResults[robotState.ordinal()][playerState.ordinal()];
        playerScore += gameResults[playerState.ordinal()][robotState.ordinal()];
    }

    public int didRobotWinRound(){
        return gameResults[robotState.ordinal()][playerState.ordinal()];
    }

    private int[] endGame(){
        return new int[]{robotScore, playerScore};
    }

    public int[] playGame(int robotAction, int playerAction, boolean isEndGame){

        if (isEndGame){
            int[] result = endGame();
            resetGame();
            return result;
        }
        else{
            setPlayerState(playerAction);
            setRobotState(robotAction);
            roundResult();
            return new int[] {didRobotWinRound(), 0};
        }

    }



}

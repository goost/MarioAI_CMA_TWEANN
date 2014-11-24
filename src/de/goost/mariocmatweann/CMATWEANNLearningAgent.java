package de.goost.mariocmatweann;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.LearningTask;
import de.goost.jcmatweann.CMATWEANN;

/**
 * Copyright (c) 2014/11, Gleb Ostrowski, glebos at web dot de
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1) Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3) Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class CMATWEANNLearningAgent extends BasicMarioAIAgent implements LearningAgent {

    private LearningTask _learningTask;
    private CMATWEANN population;

    private long _evaluationQuota;
    private long _curEval;
    private int _curAgentNumber;

    private int _numIn;
    private int _numOut;
    private int _numHid;

    private boolean _isbestAgent;
    private boolean _bff;

    private double _probNode;
    private double _probEdge;

    private double _sigmaMin;
    private double _sigma;


    public CMATWEANNLearningAgent (int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, boolean bff){
        super("CMATWEANNLearningAgent");
        _numIn = numIn;
        _numHid = numHid;
        _numOut = numOut;
        _sigma = sigma;
        _sigmaMin = sigmaMin;
        _probEdge = probEdge;
        _probNode = probNode;
        _bff = bff;

    }

    public CMATWEANNLearningAgent () {
        //Default Params taken from test.cpp from the CMA-TWEANN Source
        //numIN and numOut and so changed accordingly, obviously
        //    nIn   nOut   nHid   sigma     sigmaMin    probNode  probEdge  bff
        this(15      ,5   ,8     ,0.5       ,0.5      ,0.01      ,0.1      ,false);
    }

    @Override
    public void learn() {
        while (_curEval < _evaluationQuota){
            for (int cnt = 0; cnt < population.getPopSize(); cnt++) {
                _curAgentNumber = cnt;
                population.setScore(cnt,-_learningTask.evaluate(this));//TODO negative score?
                if(_curEval++ > _evaluationQuota) {
                    break;
                }
                //TODO DEBUG
                System.out.println("CurVal: "+ _curEval);
            }
            population.proceedGen();
            //TODO DEBUG
            System.err.println("NextGenerationPopSize: " + population.getPopSize());
        }
    }

    @Override
    public void giveReward(float reward) {

    }

    @Override
    public void newEpisode() {

    }

    @Override
    public void setLearningTask(LearningTask learningTask) {
        _learningTask = learningTask;
    }

    @Override
    public void setEvaluationQuota(long num) {
        _evaluationQuota = num;
    }

    @Override
    public Agent getBestAgent() {
        setIsBestAgent(true);
        return this;
    }

    @Override
    public void init() {
        try {
            population = new CMATWEANN(_numIn, _numOut, _numHid, _sigma, _sigmaMin, _probNode, _probEdge, _bff);
        } catch (CMATWEANN.GetPointerFailedException e) {
            System.err.println("Something went wrong with creating the native C++ Class for the CMATWEANN.");
            System.err.println("This shouldn't happen.");
            System.err.println("Program will terminate.");
            e.printStackTrace();
            System.exit(-1);
        }
        population.produceOffspring();
    }

    @Override
    public boolean[] getAction() {
        //TODO various implementattion based on best cells
        //for now only top5Enemy and top5Level, taken from the paper
        //first change: only one Grid
        double[] inputs     = new double[_numIn];
        int[] top5Enemies   = {14,3,5,9,2};
        int[] top5Level     = {6,1,0,5,4};

        int curInput = 0;
        for (int cnt = 0; cnt < 5; cnt++) {
            //TODO use point array or something, instead of getRealCell method -> too long and ugly
            inputs[curInput++] = getRealCell(top5Enemies[cnt], enemies);
        }
        for (int cnt = 0; cnt < 5; cnt++) {
            //TODO use point array or something, instead of getRealCell method -> too long and ugly
            inputs[curInput++] = getRealCell(top5Level[cnt], levelScene);
        }

        if(marioMode==2)//fire Status
            inputs[inputs.length - 5] = 1;
         else
            inputs[inputs.length - 5] = 0;

        inputs[inputs.length - 4] = isJumpHole(levelScene,11);
        inputs[inputs.length - 3] = isMarioOnGround ? 1 : 0;
        inputs[inputs.length - 2] = isMarioAbleToJump ? 1 : 0;
        inputs[inputs.length - 1] = 1; //BIAS

        double[] outputs;

        if(_isbestAgent)
            outputs = population.activateBest(inputs, _numOut);
        else
            outputs = population.activate(_curAgentNumber, inputs, _numOut);

        boolean[] actions = new boolean[Environment.numberOfKeys];
        for (int i = 0; i < outputs.length; i++)
        {
            actions[i] = outputs[i] > 0;
        }
        return actions;
    }

    private double getRealCell(int myCellNumber, byte[][] sceneGrid) {
        //Copy/paste from PSO-source
        int realX = 0;
        int realY = 0;
        switch (myCellNumber) {
            case 0:
                realX = 7;
                realY = 7;
                break;
            case 1:
                realX = 7;
                realY = 8;
                break;
            case 2:
                realX = 7;
                realY = 9;
                break;
            case 3:
                realX = 7;
                realY = 10;
                break;
            case 4:
                realX = 7;
                realY = 11;
                break;
            case 5:
                realX = 8;
                realY = 7;
                break;
            case 6:
                realX = 8;
                realY = 8;
                break;
            case 7:
                realX = 8;
                realY = 9;
                break;
            case 8:
                realX = 8;
                realY = 10;
                break;
            case 9:
                realX = 8;
                realY = 11;
                break;
            case 10:
                realX = 9;
                realY = 7;
                break;
            case 11:
                realX = 9;
                realY = 8;
                break;
            case 12:
                realX = 9;
                realY = 9;
                break;
            case 13:
                realX = 9;
                realY = 10;
                break;
            case 14:
                realX = 9;
                realY = 11;
                break;
            case 15:
                realX = 10;
                realY = 7;
                break;
            case 16:
                realX = 10;
                realY = 8;
                break;
            case 17:
                realX = 10;
                realY = 9;
                break;
            case 18:
                realX = 10;
                realY = 10;
                break;
            case 19:
                realX = 10;
                realY = 11;
                break;
            case 20:
                realX = 11;
                realY = 7;
                break;
            case 21:
                realX = 11;
                realY = 8;
                break;
            case 22:
                realX = 11;
                realY = 9;
                break;
            case 23:
                realX = 11;
                realY = 10;
                break;
            case 24:
                realX = 11;
                realY = 11;
                break;
        }
        //TODO use real values?
        return (sceneGrid[realX][realY] != 0) ? 1 : 0;
    }

    private double isJumpHole(byte[][] levelScene,int x) {
        //COPY PASTE FROM PSO SOURCE
        if (!isHole(levelScene, x)){
            return 0;
        }
        return 1;
    }

    public boolean isHole(byte[][] levelScene, int yoko) {
        //COPY PASTE FROM PSO SOURCE
        for (int i = 9; i < levelScene[0].length; i++) {
            if (levelScene[i][yoko] != 0) {
                return false;
            }
        }
        return true;
    }

    public void setIsBestAgent(boolean bestAgent) {
        _isbestAgent = bestAgent;
    }
}

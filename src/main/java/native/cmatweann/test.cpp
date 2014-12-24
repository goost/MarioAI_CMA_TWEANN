//
//  main.cpp
//
//  Created by Hirotaka Moriguchi
//

#include "cmat.h"
#include "prob.h"

int const MAX_EVAL = 10000;

int main(){
    bool bSolved = false;
    int numEval;
    CMATWEANN* pCT;
    double score;

    // initialize parameters for CMA-TWEANN
    int numIn = 3;
    int numOut = 1;
    int numHid = 0;
    double sigma = 0.5;
    double sigmaMin = 0.5;
    double probNode = 0.01;
    double probEdge = 0.1;
    bool bFF = false;

    while(true){
        pCT = new CMATWEANN(numIn, numOut, numHid, sigma, sigmaMin, probNode, probEdge, bFF);
        numEval = 0;
        
        while(true){
            // produce a population
            pCT->produceOffspring();
            for(int i = 0; i < pCT->getPopSize(); i++){
                // evaluate i-th network on problems
                pb_double_eval(pCT->getNN(i), score);
                // set the score as the fitness of i-th network
                pCT->setScore(i, score);
                numEval++;
                // end condition
                if(score <= -10. || // task solved or
                   numEval > MAX_EVAL-1){ // evaluation number reached to the maximum
                    cout << numEval << endl;
                    bSolved = true;
                    break;
                }
            }
            if(bSolved){
                bSolved = false;
                break;
            }
            pCT->proceedGen();
        }
    }
}

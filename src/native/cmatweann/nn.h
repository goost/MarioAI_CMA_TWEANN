//
//  nn.h
//
//  Created by Hirotaka Moriguchi
//

#ifndef CMAT_nn_h
#define CMAT_nn_h

#include <iostream>
#include "Eigen/Core"
#include "Eigen/SVD"

using namespace std;
using namespace Eigen;

class NN{
    int numIn;
    int numOut;
    int numHid;
    MatrixXi connectionMatrix; // (in+rec) x (rec), initialized with -1's
    VectorXd weight;
    
    bool bFF;

    VectorXd node;
    VectorXd delay;
    VectorXd inputs;
    
    inline double sigmoid(double x){
        return x/(1+abs(x));
        //1/(1+e^(-1*x) only positive y
    }
    
public:
    NN(int in, int out, int hid, MatrixXi map, VectorXd w, bool ff){
        numIn = in; numOut = out; numHid = hid; connectionMatrix = map; weight = w; bFF = ff;
        node = VectorXd::Zero(numOut + numHid);
        delay = VectorXd::Zero(numOut + numHid);
    }
    
    NN(NN* nn):
    numIn(nn->numIn), numOut(nn->numOut), numHid(nn->numHid), connectionMatrix(nn->connectionMatrix), weight(nn->weight), bFF(nn->bFF), node(nn->node), delay(nn->delay), inputs(nn->inputs){}

    inline double inverseSigmoid(double x){
        if(x < 0){
            return x/(x+1.);
        }else{
            return -x/(x-1.);
        }
    }

    inline double ff_activation_iter(int node_no){
        double tmp_ret = 0;
        for(int i = 0; i < numIn; i++){
            if(connectionMatrix(i,node_no) != -1)
                tmp_ret += weight(connectionMatrix(i,node_no)) * inputs(i);
        }
        for(int i = numOut; i < numOut + numHid; i++){
            if(connectionMatrix(numIn+i,node_no) != -1){
                tmp_ret += weight(connectionMatrix(numIn+i,node_no))*ff_activation_iter(i);
            }
        }
        return sigmoid(tmp_ret);
    }
    
    inline void activate(VectorXd input, VectorXd& output){
        if(bFF){
            // feed-forward
            inputs = input;
            for(int i = 0; i < output.size(); i++){
                output(i) = ff_activation_iter(i);
            }
        }else{
            for(int j = 0; j < numOut + numHid; j++){
                node(j) = 0;
                for(int i = 0; i < numIn; i++){
                    if(connectionMatrix(i,j) != -1){
                        node(j) += weight(connectionMatrix(i,j)) * input(i);
                    }
                }
                for(int i = 0; i < numOut + numHid; i++){
                    if(connectionMatrix(i+numIn,j) != -1){
                        node(j) += weight(connectionMatrix(i+numIn,j)) * delay(i);
                    }
                }
            }
            for(int i = 0; i < numOut + numHid; i++){
                node(i) = sigmoid(node(i));
                delay(i) = node(i);
            }
            for(int i = 0; i < output.size(); i++){
                output(i) = node(i);
            }
        }
    }
    
    inline void setweight(VectorXd w){weight = w;}
    inline void reset(){delay = VectorXd::Zero(delay.size());}
    
    VectorXd getweight(){return weight;}
};

#endif

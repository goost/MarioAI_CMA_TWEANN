//
//  prob.h
//
//  Created by Hirotaka Moriguchi
//

#ifndef CMAT_prob_h
#define CMAT_prob_h

#include "nn.h"

#define STEPS 100000
#define H 0.01
#define PI 3.14159265358979323846

using namespace std;
using namespace Eigen;

// Bias activation for xor_eval()
#define BIAS 1

#define one_degree 0.0174532	/* 2pi/360 */
#define six_degrees 0.1047192
#define twelve_degrees 0.2094384
#define fifteen_degrees 0.2617993
#define thirty_six_degrees 0.628329
#define degrees64    1.2566580
#define fifty_degrees 0.87266


inline double sgn(double x){
    return (x >= 0) ? 1 : -1;
}

inline void rk_iter(VectorXd* k, VectorXd X, double F){
    static const double g = -9.8;
    static const double mc = 1.;
    static const double m1 = 0.1;
    static const double m2 = m1 * 0.1;
    static const double l1 = 1.;
    static const double _l1 = l1 * 0.5;
    static const double l2 = l1 * 0.1;
    static const double _l2 = l2 * 0.5;
    static const double mu_c = 5e-4;
    static const double mu1 = 2e-6;
    static const double mu2 = 2e-6;
    
    // double x = X[0]; //unused
    double a1 = X[1];
    double a2 = X[2];
    double xv = X[3];
    double av1 = X[4];
    double av2 = X[5];
    
    double _m1 = m1 * (1. - 0.75 * pow(cos(a1),2));
    double _m2 = m2 * (1. - 0.75 * pow(cos(a2),2));
    
    double _F1 = m1*_l1*av1*av1*sin(a1) + 0.75*m1*cos(a1)*((mu1*av1)/(m1*_l1)+g*sin(a1));
    double _F2 = m2*_l2*av2*av2*sin(a2) + 0.75*m2*cos(a2)*((mu2*av2)/(m2*_l2)+g*sin(a2));
    
    double xa = (F - mu_c * sgn(xv) + _F1 + _F2) / (mc + _m1 + _m2);
    double aa1 = (-0.75/_l1) * (xa * cos(a1) + g * sin(a1) + (mu1 * av1) / (m1 * _l1));
    double aa2 = (-0.75/_l2) * (xa * cos(a2) + g * sin(a2) + (mu2 * av2) / (m2 * _l2));
    
    (*k)(0) = xv;
    (*k)(1) = av1;
    (*k)(2) = av2;
    
    (*k)(3) = xa;
    (*k)(4) = aa1;
    (*k)(5) = aa2;
}

inline void rk_iter_single(VectorXd* k, VectorXd X, double F){
    static const double g = -9.8;
    static const double mc = 1.;
    static const double m1 = 0.1;
    static const double l1 = 1.;
    static const double _l1 = l1 * 0.5;
    static const double mu_c = 5e-4;
    static const double mu1 = 2e-6;
    
    double a1 = X[1];
    double xv = X[2];
    double av1 = X[3];
    
    double _m1 = m1 * (1. - 0.75 * pow(cos(a1),2));
    
    double _F1 = m1*_l1*av1*av1*sin(a1) + 0.75*m1*cos(a1)*((mu1*av1)/(m1*_l1)+g*sin(a1));
    
    double xa = (F - mu_c * sgn(xv) + _F1) / (mc + _m1);
    double aa1 = (-0.75/_l1) * (xa * cos(a1) + g * sin(a1) + (mu1 * av1) / (m1 * _l1));
    
    (*k)(0) = xv;
    (*k)(1) = av1;
    (*k)(2) = xa;
    (*k)(3) = aa1;
}

inline void pb_single_eval(NN* nn, double& dscore, bool b_partial=true){
    int in = 4;
    VectorXd x(in);
    VectorXd k1 = VectorXd::Zero(in);
    VectorXd k2 = VectorXd::Zero(in);
    VectorXd k3 = VectorXd::Zero(in);
    VectorXd k4 = VectorXd::Zero(in);
    
    x(0) = 0; x(1) = one_degree*4.; x(2) = 0; x(3) = 0;
    
    double score = 0;
    for(int i = 0; i < STEPS; i++){
        // set actuation F
        
        VectorXd nnin;
        if(b_partial){
            nnin = VectorXd::Zero(2);
            nnin << x(0)/2.4, x(1)/twelve_degrees;
        }else{
            nnin = VectorXd::Zero(4);
            nnin << x(0)/2.4, x(1)/twelve_degrees, x(2)/10.0, x(3)/5.0;
        }
        
        VectorXd nnout = VectorXd::Zero(1);
        nn->activate(nnin, nnout);
        double F = nnout(0)*10.;
        if(F>=0 && F < 10./256.)
            F = 10./256.;
        else if(F < 0 && F > -10./256.)
            F = -10./256.;
        
        
        // proceed 2*H(sec)
        for(int s = 0; s < 2; s++){ 
            rk_iter_single(&k1, x, F);
            rk_iter_single(&k2, x+H*0.5*k1, F);
            rk_iter_single(&k3, x+H*0.5*k2, F);
            rk_iter_single(&k4, x+H*k3, F);
            x += (H/6.)*(k1+2*k2+2*k3+k4);
        }
        
        // end condition (sucess || failure)
        if(i == STEPS-1 || abs(x(1)) > twelve_degrees || abs(x(0)) > 2.4){
            score += 0.1*(i+1)/1000.;
            if(i == STEPS-1){
	      score = 10.;
            }
            break;
        }
    }
    
    dscore = -score;
}

inline void pb_double_eval(NN* nn, double& dscore, bool b_partial=true, bool b_damping=true){
    int in = 6;
    VectorXd x(in);
    VectorXd k1 = VectorXd::Zero(in);
    VectorXd k2 = VectorXd::Zero(in);
    VectorXd k3 = VectorXd::Zero(in);
    VectorXd k4 = VectorXd::Zero(in);
    
    x(0) = 0; x(1) = one_degree * 4.; x(2) = 0; x(3) = 0; x(4) = 0; x(5) = 0;  
    
    double score = 0;
    VectorXd damping;
    if(b_damping) damping = VectorXd::Zero(STEPS);
    
    for(int i = 0; i < STEPS; i++){
        // set actuation F
        VectorXd nnin;
        if(b_partial){
            nnin = VectorXd::Zero(3);
            nnin << x(0)/2.4, x(1)/thirty_six_degrees, x(2)/thirty_six_degrees;
        }else{
            nnin = VectorXd::Zero(6);
            nnin << x(0)/2.4, x(1)/thirty_six_degrees, x(2)/thirty_six_degrees, x(3)/10.0, x(4)/5.0, x(5)/16.0;
        }
        
        VectorXd nnout = VectorXd::Zero(1);
        nn->activate(nnin, nnout);
        double F = nnout(0)*10.;
        
        if(F>=0 && F < 10./256.)
            F = 10./256.;
        else if(F < 0 && F > -10./256.)
            F = -10./256.;
        
        // 1st runge-kutta
        for(int s = 0; s < 2; s++){ 
            rk_iter(&k1, x, F);
            rk_iter(&k2, x+H*0.5*k1, F);
            rk_iter(&k3, x+H*0.5*k2, F);
            rk_iter(&k4, x+H*k3, F);
            x += (H/6.)*(k1+2*k2+2*k3+k4);
        }
        
        // for damping fitness
        if(b_damping) damping[i] = abs(x(0))+abs(x(3))+abs(x(4))+abs(x(5));
        
        // end condition (sucess || failure)
        if(i == STEPS-1 || (abs(x(1)) > thirty_six_degrees || abs(x(2)) > thirty_six_degrees || abs(x(0)) > 2.4)){
            // fitness
            score += 0.1*(i+1)/1000.;
            // additional damping fitness
            if(b_damping){
                if(i > 100){
                    double d = 0.;
                    for(int j = i-99; j < i+1; j++) d += damping[j];
                    score += 0.9*0.75/d;
                }
            }
            if(i == STEPS-1){
	      score = 10.;
            }
            break;
        }
    }
    dscore = -score;
}

#endif

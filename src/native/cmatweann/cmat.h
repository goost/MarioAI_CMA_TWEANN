//
//  cmat.h
//
//  Created by Hirotaka Moriguchi
//

/* GLeb Ostrowski:
*  Changed all random() calls on marked lines
*  to rand() calls, otherwise it would not compile
*  applies only to windows, current status REVERTED
*/

#ifndef CMAT_cmat_h
#define CMAT_cmat_h

#include "nn.h"
#include "Eigen/Core"

#include <vector>
#include <algorithm>


#define PI 3.14159265358979323846
using namespace std;
using namespace Eigen;

inline bool scoreComp(pair<double, int> a, pair<double, int> b){
  return a.first < b.first;
}

class CMATWEANN{
 private:
  inline double randn(double mu, double sigma) {
    static bool deviateAvailable=false;
    static float storedDeviate;
    double dist, angle;
    if (!deviateAvailable) {
      dist=sqrt( -2.0 * log(double(rand()) / double(RAND_MAX)) );
      angle=2.0 * PI * (double(rand()) / double(RAND_MAX));
      storedDeviate=dist*cos(angle);
      deviateAvailable=true;
      return dist * sin(angle) * sigma + mu;
    }
    else {
      deviateAvailable=false;
      return storedDeviate*sigma + mu;
    }
  }
    
  inline void expandM(MatrixXd& m, int rdiff, int cdiff){
    MatrixXd newM = MatrixXd::Zero(m.rows()+rdiff,m.cols()+cdiff);
    newM.block(0,0,m.rows(),m.cols()) = m;
    m = newM;
  }
  inline void expandMiC(MatrixXi& m, int rdiff, int cdiff, int value){
    MatrixXi newM = MatrixXi::Constant(m.rows()+rdiff,m.cols()+cdiff,value);
    newM.block(0,0,m.rows(),m.cols()) = m;
    m = newM;
  }
  inline void expandVZ(VectorXd& v, int sdiff){
    VectorXd newV = VectorXd::Zero(v.size()+sdiff);
    newV.head(v.size()) = v;
    v = newV;
  }
  inline void expandVC(VectorXd& v, int sdiff, double value){
    VectorXd newV = VectorXd::Constant(v.size()+sdiff,value);
    newV.head(v.size()) = v;
    v = newV;
  }

  inline void initializeCMAParameters(){
    lambda = max((int)(4+(int)(3*log(dim))),5);
    mu = (int)(lambda/2);
    cc = (4./(4.+(double)dim));
        
    ccov = 2./pow(dim+sqrt(2.),2);
        
    wmu = VectorXd(mu);
    for(int i = 0; i < mu; i++){
      wmu(i) = log((lambda+1)/2) - log(i+1);
    }
    wmu/=wmu.sum();
    cw = wmu.sum()/wmu.norm();
        
    mueff = 1/pow(wmu.norm(),2);
    mucov = mueff;
        
    csigma = (mueff+2)/(dim+mueff+3);

    dsigma = 1+2*max(0.,sqrt((mueff-1.)/(dim+1.))-1.)+csigma;
        
    ccu = sqrt(cc*(2-cc));
    csigmau = sqrt(csigma*(2-csigma));
    chin = dim*0.5*(1-1/(4*dim)+1/(21*dim*dim));
        
    score = VectorXd(lambda);
    rank = VectorXi(lambda);
  }
  inline void updateCMAParameters(int ddiff, int ndiff){
    dim+=ddiff; numHid+=ndiff;
    initializeCMAParameters();
        
    expandVZ(pc,ddiff);
        
    expandVZ(psigma,ddiff);
        
    expandM(C,ddiff,ddiff);
        
    C.bottomRightCorner(ddiff,ddiff) = VectorXd::Constant(ddiff,pow(sigmaInit/sigma,2)).asDiagonal();
        
    expandVZ(xbase,ddiff);

    expandVZ(zbase,ddiff);
        
    expandM(X,ddiff,lambda-(int)X.cols());
    expandM(Z,ddiff,lambda-(int)Z.cols());
        
    expandM(B,ddiff,ddiff);
    expandM(D,ddiff,ddiff);
        
    expandVZ(xvec,ddiff);
  }

  int numIn;
  int numOut;
  int numHid;
  double sigma;
  double sigmaInit;
  double sigmaMin;
  double probNode;
  double probEdge;
  bool bFF;
    
  int nctr;
  /* following parameters will vary upon augmentation */
  int dim;
  int lambda;
  int mu;
  double cc;
  double ccov;
  double csigma;
  double dsigma;
  double ccu;
  double csigmau;
  double chin;
    
  VectorXd wmu;
  double cw;
  double mueff;
  double mucov;
  VectorXd score;
  VectorXi rank;
    
  VectorXd pc;
  VectorXd psigma;
  MatrixXd C;
  VectorXd xbase;
    
  VectorXd zbase;
    
  MatrixXd X;
  MatrixXd Z;
    
  MatrixXd B;
  MatrixXd D;
    
  VectorXd xvec;
    
  MatrixXi connectionMatrix;
    
  VectorXd w;
  NN* nn;
  /* above parameters will vary upon augmentation */
    
 public:
 CMATWEANN(int numIn, int numOut, int numHid, double sigma, double sigmaMin, double probNode, double probEdge, bool bFF):
  numIn(numIn),
    numOut(numOut),
    numHid(numHid),
    sigma(sigma),
    sigmaInit(sigma),
    sigmaMin(sigmaMin),
    probNode(probNode),
    probEdge(probEdge),
    bFF(bFF)
    {
      connectionMatrix = MatrixXi::Constant(numIn+numOut+numHid,numOut+numHid,-1);

      // setup connections from input to hidden-output nodes
      nctr = 0;
      for(int i = 0; i < numIn; i++){
	for(int j = 0; j < numOut+numHid; j++){
	  connectionMatrix(i,j) = nctr;
	  nctr++;
	}
      }

      // setup feed-forward connections
      if(bFF){
	for(int i = numOut; i < numOut+numHid; i++){
	  for(int j = 0; j < numOut; j++){
	    connectionMatrix(i+numIn,j) = nctr;
	    nctr++;
	  }
	}
      }else{
	for(int i = 0; i < numOut+numHid; i++){
	  for(int j = 0; j < numOut+numHid; j++){
	    connectionMatrix(i+numIn,j) = nctr;
	    nctr++;
	  }
	}
      }
        
      dim = nctr;
      initializeCMAParameters();
        
      pc = VectorXd::Zero(dim);
      psigma = VectorXd::Zero(dim);
      C = VectorXd::Constant(dim,1).asDiagonal();
      xbase = VectorXd::Zero(dim);
      zbase = VectorXd::Zero(dim);
        
      X = MatrixXd::Zero(dim,lambda);
      Z = MatrixXd::Zero(dim,lambda);
        
      B = VectorXd::Constant(dim,1).asDiagonal();
      D = VectorXd::Constant(dim,1).asDiagonal();
        
      xvec = VectorXd::Zero(dim);
        
      nn = new NN(numIn, numOut, numHid, connectionMatrix, xbase, bFF);
    }
    
  inline int getPopSize(){return lambda;}
  NN* getNN(int nnID){
    nn->setweight(X.col(nnID));
    nn->reset();
    return nn;
  }
  inline void setScore(int nnID, double tmpScore){score[nnID] = tmpScore;}
  inline void produceOffspring(){
    for(int i = 0; i < lambda; i++){
      // produce mutation base
      for(int j = 0; j < dim; j++) Z(j,i) = randn(0.,1.);
      // mutation (x += C*z)
      X.col(i) = xbase + sigma*B*D*Z.col(i);
    }
  }

  void proceedGen(){
    // sort z & x
    // 1. reveal rank
    double bestscore = 0;
    
    vector<pair<double, int> > rankScore(lambda);
    for(int i = 0; i < lambda; i++){
      rankScore[i] = make_pair(score[i],i);
    }
    sort(rankScore.begin(), rankScore.end(), scoreComp);
    for(int i = 0; i < lambda; i++) rank[i] = rankScore[i].second;
    
    // 2. weighted sum
    xbase = VectorXd::Zero(dim);
    zbase = VectorXd::Zero(dim);
    for(int i = 0; i < mu; i++){
      xbase += X.col(rank[i])*wmu(i);
      zbase += Z.col(rank[i])*wmu(i);
    }
        
    // update pc and c
    pc = (1-cc)*pc + ccu*sqrt(mueff)*B*D*zbase;
        
    // rank-mu update
    MatrixXd Zmu = MatrixXd::Zero(dim,dim);
    
    for(int i = 0; i < mu; i++){
      Zmu += Z.col(rank[i])*(Z.col(rank[i]).transpose())*wmu(i);
    }

    MatrixXd BDt = ((B*D).transpose());
    Zmu = B*D*Zmu*BDt;
    // rank-mu update
    C = (1-ccov)*C + ccov*((1/mucov)*pc*pc.transpose() + (1-1/mucov)*Zmu);
        
    // update psigma and sigma
    psigma = (1-csigma)*psigma + csigmau*sqrt(mueff)*B*zbase;
    sigma = sigma * exp(csigma*(psigma.norm()-chin)/(dsigma*chin));
        
    if(bestscore > score[rank[0]]) bestscore = score[rank[0]];
    /* augment topology here if needed */
    // update wmap
    double rand_mut = (double)random()/(double)RAND_MAX; //changed random
    if(rand_mut < probNode){
      expandMiC(connectionMatrix,1,1,-1);
      connectionMatrix(rand()%(numIn+numOut+numHid),numOut+numHid) = nctr; //changed random
      nctr++;
      connectionMatrix(numIn+numOut+numHid,(random()%(numOut+numHid))) = nctr; //Changed random
      nctr++;
      updateCMAParameters(2,1);
      nn = new NN(numIn, numOut, numHid, connectionMatrix, xbase, bFF);
    }else if(rand_mut < (probNode+probEdge) && connectionMatrix.minCoeff() < 0){
      double rand_row, rand_col;
      while(1){
	rand_row = random()%(numIn+numOut+numHid); //changed random
	rand_col = random()%(numOut+numHid); //changed random
	if(connectionMatrix(rand_row,rand_col) == -1) {
	  connectionMatrix(rand_row,rand_col) = nctr;
	  nctr++;
	  break;
	}
      }
      updateCMAParameters(1,0);
      nn = new NN(numIn, numOut, numHid, connectionMatrix, xbase, bFF);
    }
        
    JacobiSVD<MatrixXd> svd(C, ComputeFullU | ComputeFullV);
    B = svd.matrixU();
    D = svd.singularValues().asDiagonal();
    D = D.array().sqrt().matrix();
            
    // lower bound on variance
    if(sigma*D(dim-1,dim-1) < sigmaMin*sigmaInit)
      sigma = sigmaMin*sigmaInit/D(dim-1,dim-1);
  }

  //added by Gleb Ostrowski for retrieving the best NN
  NN* getBestNN(){
    vector<pair<double, int> > rankScore(lambda);
    for(int i = 0; i < lambda; i++){
      rankScore[i] = make_pair(score[i],i);
    }
    sort(rankScore.begin(), rankScore.end(), scoreComp);
    std::cout << "Best Score: " <<rankScore[0].first <<" FromNNNummer: "<<  rankScore[0].second << std::endl;
    std::cout << "Worst Score: " <<rankScore[lambda-1].first <<" FromNNNummer: "<<  rankScore[lambda-1].second << std::endl;
    return getNN(rankScore[0].second);
  }

};

#endif

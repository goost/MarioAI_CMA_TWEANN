MarioAI CMA-TWEANN
==================

A Learning Agent for Mario AI Benchmark using CMA-TWEANN.

For more information regarding Mario AI look
[here](http://www.marioai.org/).

For more information regarding CMA-TWEANN see
[this](http://www.honiden.nii.ac.jp/~hmori/files/gecco12.pdf)
paper (or read the next section for a overview).   
This repository includes the following sources:

- Mario AI Benchmark v.0.2.0 (*ch.idsia*, *competition* and *amico* packages)
- CMA-TWEANN (Jul. 16, 2012) + Eigen lib (ver. 3.0.4) (*native/cmatweann* folder)
- The wrapper C++ CMA-TWEANN code (*de.goost.jcmatweann* package and *native* folder)
- The agent used as controller (*de.goost.mariocmatweann* package)

Each source is under its own license, if any.
Additionally, following libraries are used for compiling Mario AI:
**asm-all-3.3.jar, jdom.jar, junit-4.8.2.jar, testng-6.8.8.jar**

Parameters and Quick Overview
--------------------------------------
CMA-TWEANN (CMA-Topology and Weight Evolved Neural Network) adjusts, as the name suggest,
not only the weights, but also the topology of the network.
Two techniques exist for this purpose: *AddEdge* and *AddNode*.
*AddEdge* adds a additional edge (connection) to already existing nodes, in a recurrent net both sides can be the same node.
The initial weight of the new connection is *0*, therefore any previous learning is not undone by adding this connection.  
*AddNode* works similar, it adds a new hidden node and connects it randomly to two other nodes.
In a recurrent network the new input node can also be one of the output nodes.
Like in *AddEdge* the initial weight of this two new connections is *0*, ensuring not breaking any previously learned behaviour.  
For weight-adjustment a form of CMA is used (with changes to make it work with a changing topology).
Please check the mentioned paper for detailed information.

Most of the experiments were tested with this level:  
**-vis off -ld 1 -ls 0**  
The input cells are selected from a 5x5 grid around Mario (with him as the center).

```
   |  0 |  1 |  2 |  3 |  4 |
   |  5 |  6 |  7 |  8 |  9 |
   | 10 | 11 |  M | 13 | 14 |
   | 15 | 16 | 17 | 18 | 19 |
   | 20 | 21 | 22 | 23 | 24 |
```

Two different maps are used, one for enemy perception (**eM**) and one for SceneObject perception (**sM**), 
5 inputs are selected from every map plus 5 additional inputs.   
A merged map with 10 inputs was also tested, but the results were worse.    
The Input were as follows:

|Nr.| Inputs      |
|:-:|:-----------:| 
|1 |  eM Cell 14  | 
|2 |  eM Cell 3   | 
|3 |  eM Cell 5   | 
|4 |  eM Cell 9   | 
|5 |  eM Cell 2   | 
|6 |  sM Cell 6   | 
|7 |  sM Cell 1   | 
|8 |  sM Cell 0   | 
|9 |  sM Cell 5   | 
|10|  sM Cell 4   | 
|11|  fireMode ? 1 : 0| 
|12|  hole ahead ? 1 : 0|
|13|  onGround ? 1 : 0   |
|14|  ableToJump ? 1 : 0  |
|15|  Bias : 1   | 

The chosen cells and the level are taken from [this](http://dl.acm.org/citation.cfm?id=2571923) paper.
For the interpretation of the value in the cells *zLevelEnemy = 1* and *zLevelScene = 2*
were used together with a custom interpret method (see **de.goost.mariocmatweann.CMATWEANNLearningAgent.interpretScene()**).
It basically distinguishes between passable (0), pickable object (1), enemy (2) and walls/non-passable (3).

The outputs were then mapped to pressed buttons, which control Mario.

|Output Nr.| Button pressed if|
|:-:|:-----------:| 
|1 |  LeftButton < -0.25, RightButton > 0.25  | 
|2 |  DownButton > 0.0   | 
|3 |  JumpButton > 0.0  | 
|4 |  Speed/FireButton > 0.0   | 

After some testing, the following values were used most of the time:
- number of initial hidden nodes : 0 (the algorithm should take care of all the topology)
- sigma: high, around 25-50 (a high value avoids trapping in a local optimum right from the beginning)
- minSigma: low, around 0.5 (if many iterations are taken, a small value in the end allows for fine adjustments)
- probNode: low, 0.01 (make 10000 Iterations and you get a few hidden nodes, must be higher if less iterations are used)
- prodEdge: low, higher then probNode, around 0.1, same logic  are probNode applies here, higher, if less iterations
- bff: false (recurrent allows more possibilities)

Usually around 1000 to 10000 iterations were being used.  
While lower iterations tend to be very luck (what weights are rolled) depending, higher iterations are showing very often a successful result.
The higher the iterations, the more the probability for a fitting topology increases and the net can fine adjust.
50000 and 100000 iterations improved the result even more, but the not as much as the increase from 1000 to 10000.
Solely with 10000 iterations successful runs are possible.


#### ASM License:

Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holders nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.



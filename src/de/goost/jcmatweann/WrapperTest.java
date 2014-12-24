package de.goost.jcmatweann;

import java.util.Arrays;

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
public class WrapperTest {

    public static void main(String[] args) {
        try {
            CMATWEANN t = new CMATWEANN(3,10,1,1,0.2,0.4,0.5,false);
            t.produceOffspring();
            System.out.println(t.getPopSize());
            System.out.println(Arrays.toString(t.activate(0,new double[]{2, 3, 4}, 10)));
            for (int i = 0; i < 1; i++) {
                //System.out.println(t.getPopSize());
                t.setScore(0,3);
                t.setScore(1,23);
                t.setScore(2,54);
                t.setScore(3,3);
                t.setScore(4,445);
                t.setScore(5,3);
                t.proceedGen();
            }
            System.out.println(Arrays.toString(t.activate(0,new double[]{2, 3, 4},10)) );
        } catch (CMATWEANN.GetPointerFailedException e) {
            System.err.println("ERROOR");
        }

    }
}

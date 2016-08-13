/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.Random;

/**
 *
 * @author JackD
 */
public class doRandom {
    
   public static short[] getRandom(short[] array) {
        short arr[] = new short[6];
        int rnd;
            for (int i =0; i < 6; i++){            
                rnd = new Random().nextInt(array.length);       
                arr[i] = array[rnd];
            }
        return arr;
    }
    
}

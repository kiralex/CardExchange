package com.derniamepoirier.CardGeneration;

/**
 * Error throwed when Pixabay respond code is an error code
 */
public class PixabayResponseCodeException extends Exception {
    PixabayResponseCodeException(String str){
        super(str);
    }
}
package com.derniamepoirier.CardGeneration;

/**
 * Error throwed when receiving incorrect page number or number of result per page
 */
public class PixabayIncorrectParameterException extends Exception {
    PixabayIncorrectParameterException(String str){
        super(str);
    }
}

package com.derniamepoirier.Pixabay;

/**
 * Error throwed when Pixabay API key is missing in environment
 */
public class PixabayApiKeyMissingException extends Exception{
    PixabayApiKeyMissingException(String msg){
        super(msg);
    }
}
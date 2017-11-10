package com.derniamepoirier.Pixabay;

public class PixabayApiKeyMissingException extends Exception{
    PixabayApiKeyMissingException(String msg){
        super(msg);
    }
}
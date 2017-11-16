package com.derniamepoirier.CardGeneration;

public class PixabayAPIExceptions {
    private PixabayAPIExceptions(){}

    /**
     * Exception throwed when Pixabay API key is missing in environment
     */
    public static class PixabayApiKeyMissingException extends Exception{
        PixabayApiKeyMissingException(String msg){
            super(msg);
        }
    }

    /**
     * Exception throwed when receiving incorrect page number or number of result per page
     */
    public static class PixabayIncorrectParameterException extends Exception {
        PixabayIncorrectParameterException(String str){
            super(str);
        }
    }

    /**
     * Exception throwed when Pixabay respond code is an error code
     */
    public static class PixabayResponseCodeException extends Exception {
        PixabayResponseCodeException(String str){
            super(str);
        }
    }

    /**
     * Exception throwed when page is too big or too small
     */
    public static class PixabayPageOutValidRangeException extends Exception{
        public PixabayPageOutValidRangeException(String str){
            super(str);
        }
    }
}

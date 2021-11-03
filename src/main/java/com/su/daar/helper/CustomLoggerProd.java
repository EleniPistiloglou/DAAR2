package com.su.daar.helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLoggerProd {

    public static Logger getLogger(String class_, String logfilename){

        Logger logger = Logger.getLogger(logfilename);

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.SEVERE);

        CustomFormatter formatter = new CustomFormatter();
        FileHandler handler;
        try {
            handler = new FileHandler(logfilename);
            handler.setFormatter(formatter);
            logger.addHandler(handler);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        return logger;
    }
}

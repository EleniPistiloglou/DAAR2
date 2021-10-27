package com.su.daar.helper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLogger {

    public static Logger getLogger(String class_, String logfilename){

        //LoggerContext loggerContext = Configurator.initialize("CandidateController","log4j2.xml");
        Logger logger = Logger.getLogger(logfilename);

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);

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

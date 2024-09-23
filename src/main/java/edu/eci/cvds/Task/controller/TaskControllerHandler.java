package edu.eci.cvds.Task.controller;

import edu.eci.cvds.Task.TaskManagerException;
import edu.eci.cvds.Task.services.FilePersistenceException;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TaskControllerHandler {
    private final Logger logger = LoggerFactory.getLogger(TaskControllerHandler.class);

    // Para atrapar errores de TaskManagerException
    @ExceptionHandler(TaskManagerException.class)
    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTaskManagerException(TaskManagerException ex) {
        logger.error(ex.getMessage());
        return "Internal Server Error";
    }

    // Para atrapar errores genericos
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex) {
        logger.error(ex.getMessage());
        return "Internal Server Error";
    }

    @ExceptionHandler(FilePersistenceException.class)
    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    public String handlePersistenceException(PersistenceException ex) {
        logger.error(ex.getMessage());
        return "Persistence Error";
    }



}

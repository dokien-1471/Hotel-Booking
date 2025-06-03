package com.project.hotel.exception;

public class RoomAlreadyExistsException extends RuntimeException {
  public RoomAlreadyExistsException(String message) {
    super(message);
  }
}
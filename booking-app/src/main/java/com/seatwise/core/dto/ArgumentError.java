package com.seatwise.core.dto;

import lombok.Data;

@Data
public class ArgumentError {

  private String argumentName;

  private String message;
}

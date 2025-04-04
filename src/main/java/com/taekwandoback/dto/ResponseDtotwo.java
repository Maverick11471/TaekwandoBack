package com.taekwandoback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDtotwo<T> {

    private T item;
    private int statusCode;
    private String statusMessage;
    private String errorMessage;
}

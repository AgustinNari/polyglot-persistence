package com.tpo.modelo.usuario;


public enum CondicionIVA {
    REGIMEN_GENERAL,
    MONOTRIBUTISTA,
    EXENTO,
    EXPORTADOR;


    public static CondicionIVA desdeString(String s) {
        if (s == null) return null;
        switch (s.trim().toUpperCase()) {
            case "REGIMEN_GENERAL":
            case "REGIMEN GENERAL":
            case "GENERAL":
                return REGIMEN_GENERAL;
            case "MONOTRIBUTISTA":
                return MONOTRIBUTISTA;
            case "EXENTO":
                return EXENTO;
            case "EXPORTADOR":
                return EXPORTADOR;
            default:
                throw new IllegalArgumentException("Condición IVA inválida: " + s);
        }
    }
}

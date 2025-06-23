package com.tpo.modelo.usuario;


public enum CondicionIVA {
    RESPONSABLE_INSCRIPTO,
    MONOTRIBUTISTA,
    EXENTO,
    EXPORTADOR;


    public static CondicionIVA desdeString(String s) {
        if (s == null) return null;
        switch (s.trim().toUpperCase()) {
            case "RESPONSABLE_INSCRIPTO":
            case "RESPONSABLE INSCRIPTO":
            case "INSCRIPTO":
                return RESPONSABLE_INSCRIPTO;
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

package com.tpo.modelo.usuario;

/**
 * Representa la condición fiscal ante IVA del usuario/cliente.
 */
public enum CondicionIVA {
    REGIMEN_GENERAL,      // Puede deducir IVA, paga IVA
    MONOTRIBUTISTA,       // Paga IVA pero no deduce
    EXENTO,               // No paga IVA
    EXPORTADOR;           // No paga IVA (o alícuota 0%)

    /**
     * Parsea desde String ingresado por usuario, por ej. "REGIMEN_GENERAL", "monotributista", etc.
     */
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

package com.tpo.util;

import com.tpo.modelo.usuario.CondicionIVA;
import java.math.BigDecimal;

public class ImpuestoUtil {

    public static BigDecimal calcularIVA(BigDecimal baseConDescuento, CondicionIVA condicion, BigDecimal alicuota) {
        if (baseConDescuento == null || condicion == null || alicuota == null) {
            return BigDecimal.ZERO;
        }
        switch (condicion) {
            case EXENTO:
            case EXPORTADOR:
                return BigDecimal.ZERO;
            case MONOTRIBUTISTA:
            case REGIMEN_GENERAL:
            default:
                return baseConDescuento.multiply(alicuota).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}

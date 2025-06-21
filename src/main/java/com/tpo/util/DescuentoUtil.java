package com.tpo.util;

import com.tpo.modelo.usuario.CategoriaUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescuentoUtil {

    public static BigDecimal porcentajeDescuento(CategoriaUsuario categoria) {
        if (categoria == null) return BigDecimal.ZERO;
        switch (categoria) {
            case TOP:
                return BigDecimal.valueOf(0.10); // 10%
            case MEDIUM:
                return BigDecimal.valueOf(0.05); // 5%
            case LOW:
            default:
                return BigDecimal.ZERO;
        }
    }


    public static BigDecimal calcularDescuento(BigDecimal base, BigDecimal porcentaje) {
        if (base == null || porcentaje == null) {
            throw new IllegalArgumentException("Parámetros inválidos para descuento");
        }
        return base.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
    }
}

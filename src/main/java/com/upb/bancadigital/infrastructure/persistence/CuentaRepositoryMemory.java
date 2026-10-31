package com.upb.bancadigital.infrastructure.persistence;

import com.upb.bancadigital.domain.model.Cuenta;
import com.upb.bancadigital.domain.repository.CuentaRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CuentaRepositoryMemory implements CuentaRepositoryPort {

    private final Map<Long, Cuenta> baseDatos = new HashMap<>();

    public CuentaRepositoryMemory() {
        // Usuario precargado por defecto para pruebas inmediatas en laboratorio y sustentación
        Cuenta cuentaInicial = new Cuenta(
                1L,
                "Cliente UPB",
                "cliente@upb.edu.co",
                "3001234567",
                "Cra 9 # 45-20, Bucaramanga",
                "admin123",
                "1234",      // PIN de 4 dígitos inicial
                2500000.0    // Saldo inicial: $2.500.000 COP
        );
        baseDatos.put(1L, cuentaInicial);
    }

    @Override
    public Cuenta buscarPorId(Long id) {
        return baseDatos.get(id);
    }

    @Override
    public Cuenta guardar(Cuenta cuenta) {
        baseDatos.put(cuenta.getId(), cuenta);
        return cuenta;
    }
}
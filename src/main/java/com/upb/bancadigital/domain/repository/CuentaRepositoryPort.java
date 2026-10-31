package com.upb.bancadigital.domain.repository;

import com.upb.bancadigital.domain.model.Cuenta;

public interface CuentaRepositoryPort {
    Cuenta buscarPorId(Long id);
    Cuenta guardar(Cuenta cuenta);
}
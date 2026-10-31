package com.upb.bancadigital.application.usecase;

import com.upb.bancadigital.domain.model.Cuenta;
import com.upb.bancadigital.domain.repository.CuentaRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CuentaUseCase {

    private final CuentaRepositoryPort cuentaRepository;

    public CuentaUseCase(CuentaRepositoryPort cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    // 1. Consulta de Saldo y estado de cuenta
    public Cuenta consultarCuenta(Long id) {
        Cuenta cuenta = cuentaRepository.buscarPorId(id);
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta solicitada no existe.");
        }
        // Reiniciar el acumulador diario si cambió el día
        if (!LocalDate.now().equals(cuenta.getFechaUltimoRetiro())) {
            cuenta.setRetiradoHoy(0.0);
            cuenta.setFechaUltimoRetiro(LocalDate.now());
            cuentaRepository.guardar(cuenta);
        }
        return cuenta;
    }

    // 2. Depósito de Fondos
    public Cuenta depositarFondos(Long id, double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a $0 COP.");
        }
        Cuenta cuenta = consultarCuenta(id);
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        return cuentaRepository.guardar(cuenta);
    }

    // 3. Actualización de Datos Personales
    public Cuenta actualizarDatosPersonales(Long id, String correo, String telefono, String direccion) {
        if (correo == null || correo.isBlank() || telefono == null || telefono.isBlank() || direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Todos los campos personales (correo, teléfono y dirección) son obligatorios.");
        }
        Cuenta cuenta = consultarCuenta(id);
        cuenta.setCorreo(correo.trim());
        cuenta.setTelefono(telefono.trim());
        cuenta.setDireccion(direccion.trim());
        return cuentaRepository.guardar(cuenta);
    }

    // 4. Modificación de Credenciales
    public Cuenta modificarCredenciales(Long id, String claveActual, String nuevaClave, String nuevoPin) {
        Cuenta cuenta = consultarCuenta(id);

        if (!cuenta.getClaveAcceso().equals(claveActual)) {
            throw new IllegalArgumentException("La clave de acceso actual es incorrecta.");
        }
        if (nuevaClave == null || nuevaClave.trim().length() < 4) {
            throw new IllegalArgumentException("La nueva clave de acceso debe tener al menos 4 caracteres.");
        }
        if (nuevoPin == null || !nuevoPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("El nuevo PIN de seguridad debe contener exactamente 4 dígitos numéricos.");
        }

        cuenta.setClaveAcceso(nuevaClave.trim());
        cuenta.setPinSeguridad(nuevoPin);
        return cuentaRepository.guardar(cuenta);
    }

    // 5. Retiro de Efectivo (Módulo Crítico con Algoritmo de Dispensación)
    public Map<String, Object> retirarEfectivo(Long id, double monto, String pin) {
        Cuenta cuenta = consultarCuenta(id);

        // Regla 1: Seguridad Transaccional (PIN de exactamente 4 dígitos)
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Formato de PIN inválido: debe introducir una contraseña de seguridad de exactamente 4 dígitos.");
        }
        if (!cuenta.getPinSeguridad().equals(pin)) {
            throw new IllegalArgumentException("Transacción denegada: el PIN de seguridad ingresado es incorrecto.");
        }

        // Regla 2: Monto Mínimo Permitido ($10.000 COP)
        if (monto < 10000) {
            throw new IllegalArgumentException("Transacción denegada: el monto mínimo permitido por retiro es de $10.000 COP.");
        }

        // Regla 3: Fraccionamiento exacto en denominaciones ($100k, $50k, $20k, $10k)
        if (monto % 10000 != 0) {
            throw new IllegalArgumentException("Transacción denegada: el monto solicitado ($" + (long) monto + 
                " COP) no puede ser fraccionado exactamente con billetes de $100.000, $50.000, $20.000 y $10.000 COP.");
        }

        // Regla 4: Validar Saldo Disponible
        if (monto > cuenta.getSaldo()) {
            throw new IllegalArgumentException("Transacción denegada: fondos insuficientes. Su saldo actual es de $" + (long) cuenta.getSaldo() + " COP.");
        }

        // Regla 5: Tope Máximo Diario ($1.000.000 COP por jornada)
        if ((cuenta.getRetiradoHoy() + monto) > 1000000) {
            double cupoRestante = 1000000 - cuenta.getRetiradoHoy();
            throw new IllegalArgumentException("Transacción denegada: supera el tope máximo diario de $1.000.000 COP. " +
                "Hoy ya ha retirado $" + (long) cuenta.getRetiradoHoy() + " COP. Cupo disponible hoy: $" + (long) cupoRestante + " COP.");
        }

        // Algoritmo Voraz de Dispensación (Menor cantidad de billetes posible)
        long montoRestante = (long) monto;
        int[] denominaciones = {100000, 50000, 20000, 10000};
        Map<String, Integer> billetesEntregados = new LinkedHashMap<>();

        for (int billete : denominaciones) {
            int cantidad = (int) (montoRestante / billete);
            if (cantidad > 0) {
                billetesEntregados.put("$" + (billete / 1000) + ".000 COP", cantidad);
                montoRestante %= billete;
            }
        }

        // Actualizar saldo y acumulado diario
        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuenta.setRetiradoHoy(cuenta.getRetiradoHoy() + monto);
        cuentaRepository.guardar(cuenta);

        // Construir respuesta detallada
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("mensaje", "Retiro exitoso. Por favor retire su dinero.");
        resultado.put("montoRetirado", monto);
        resultado.put("nuevoSaldo", cuenta.getSaldo());
        resultado.put("acumuladoDiario", cuenta.getRetiradoHoy());
        resultado.put("billetesDispensados", billetesEntregados);

        return resultado;
    }
}
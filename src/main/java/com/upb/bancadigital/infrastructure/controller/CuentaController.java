package com.upb.bancadigital.infrastructure.controller;

import com.upb.bancadigital.application.usecase.CuentaUseCase;
import com.upb.bancadigital.domain.model.Cuenta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cuenta")
@CrossOrigin(origins = "*")
public class CuentaController {

    private final CuentaUseCase cuentaUseCase;
    private static final Long CUENTA_ID_ACTIVA = 1L;

    public CuentaController(CuentaUseCase cuentaUseCase) {
        this.cuentaUseCase = cuentaUseCase;
    }

    // 1. Endpoint Consulta de Saldo (GET http://localhost:8080/api/cuenta/saldo)
    @GetMapping("/saldo")
    public ResponseEntity<Object> consultarSaldo() {
        Cuenta cuenta = cuentaUseCase.consultarCuenta(CUENTA_ID_ACTIVA);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("exito", true);
        respuesta.put("titular", cuenta.getNombreTitular());
        respuesta.put("saldoDisponible", cuenta.getSaldo());
        respuesta.put("retiradoHoy", cuenta.getRetiradoHoy());
        respuesta.put("topeDiarioRestante", 1000000.0 - cuenta.getRetiradoHoy());
        respuesta.put("correo", cuenta.getCorreo());
        respuesta.put("telefono", cuenta.getTelefono());
        respuesta.put("direccion", cuenta.getDireccion());
        return ResponseEntity.ok(respuesta);
    }

    // 2. Endpoint Depósito de Fondos (POST o GET para pruebas rápidas por URL)
    @RequestMapping(value = "/deposito", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Object> depositar(
            @RequestParam(required = false) Double monto,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            double valor = (monto != null) ? monto : Double.parseDouble(body.get("monto").toString());
            Cuenta cuenta = cuentaUseCase.depositarFondos(CUENTA_ID_ACTIVA, valor);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("exito", true);
            res.put("mensaje", "Depósito realizado exitosamente.");
            res.put("nuevoSaldo", cuenta.getSaldo());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return respuestaError(e.getMessage());
        }
    }

    // 3. Endpoint Actualización de Datos Personales
    @RequestMapping(value = "/datos", method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Object> actualizarDatos(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String nuevoCorreo = (correo != null) ? correo : body.get("correo");
            String nuevoTel = (telefono != null) ? telefono : body.get("telefono");
            String nuevaDir = (direccion != null) ? direccion : body.get("direccion");

            Cuenta cuenta = cuentaUseCase.actualizarDatosPersonales(CUENTA_ID_ACTIVA, nuevoCorreo, nuevoTel, nuevaDir);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("exito", true);
            res.put("mensaje", "Datos personales actualizados correctamente.");
            res.put("correo", cuenta.getCorreo());
            res.put("telefono", cuenta.getTelefono());
            res.put("direccion", cuenta.getDireccion());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return respuestaError(e.getMessage());
        }
    }

    // 4. Endpoint Modificación de Credenciales
    @RequestMapping(value = "/credenciales", method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Object> modificarCredenciales(
            @RequestParam(required = false) String claveActual,
            @RequestParam(required = false) String nuevaClave,
            @RequestParam(required = false) String nuevoPin,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String cActual = (claveActual != null) ? claveActual : body.get("claveActual");
            String cNueva = (nuevaClave != null) ? nuevaClave : body.get("nuevaClave");
            String nPin = (nuevoPin != null) ? nuevoPin : body.get("nuevoPin");

            cuentaUseCase.modificarCredenciales(CUENTA_ID_ACTIVA, cActual, cNueva, nPin);

            Map<String, Object> res = new LinkedHashMap<>();
            res.put("exito", true);
            res.put("mensaje", "Clave de acceso y PIN de 4 dígitos actualizados con éxito.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return respuestaError(e.getMessage());
        }
    }

    // 5. Endpoint Retiro de Efectivo - Módulo Crítico
    @RequestMapping(value = "/retiro", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Object> retirarEfectivo(
            @RequestParam(required = false) Double monto,
            @RequestParam(required = false) String pin,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            double valor = (monto != null) ? monto : Double.parseDouble(body.get("monto").toString());
            String pinSeguridad = (pin != null) ? pin : body.get("pin").toString();

            Map<String, Object> resultado = cuentaUseCase.retirarEfectivo(CUENTA_ID_ACTIVA, valor, pinSeguridad);
            resultado.put("exito", true);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return respuestaError(e.getMessage());
        }
    }

    private ResponseEntity<Object> respuestaError(String mensaje) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("exito", false);
        error.put("mensaje", mensaje);
        return ResponseEntity.badRequest().body(error);
    }
}
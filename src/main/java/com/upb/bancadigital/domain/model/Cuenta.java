package com.upb.bancadigital.domain.model;

import java.time.LocalDate;

public class Cuenta {
    private Long id;
    private String nombreTitular;
    private String correo;
    private String telefono;
    private String direccion;
    private String claveAcceso;
    private String pinSeguridad; // Exactamente 4 dígitos
    private double saldo;
    private double retiradoHoy;
    private LocalDate fechaUltimoRetiro;

    public Cuenta() {
    }

    public Cuenta(Long id, String nombreTitular, String correo, String telefono, 
                  String direccion, String claveAcceso, String pinSeguridad, double saldo) {
        this.id = id;
        this.nombreTitular = nombreTitular;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.claveAcceso = claveAcceso;
        this.pinSeguridad = pinSeguridad;
        this.saldo = saldo;
        this.retiradoHoy = 0.0;
        this.fechaUltimoRetiro = LocalDate.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getClaveAcceso() { return claveAcceso; }
    public void setClaveAcceso(String claveAcceso) { this.claveAcceso = claveAcceso; }

    public String getPinSeguridad() { return pinSeguridad; }
    public void setPinSeguridad(String pinSeguridad) { this.pinSeguridad = pinSeguridad; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public double getRetiradoHoy() { return retiradoHoy; }
    public void setRetiradoHoy(double retiradoHoy) { this.retiradoHoy = retiradoHoy; }

    public LocalDate getFechaUltimoRetiro() { return fechaUltimoRetiro; }
    public void setFechaUltimoRetiro(LocalDate fechaUltimoRetiro) { this.fechaUltimoRetiro = fechaUltimoRetiro; }
}
# Parcial Final - Banca Digital UPB
flowchart LR
    Cliente(["👤 Cliente Autenticado"])
    
    subgraph Sistema ["Plataforma de Banca Digital"]
        UC1("Consultar Saldo en Tiempo Real")
        UC2("Depositar Fondos")
        UC3("Actualizar Datos Personales")
        UC4("Modificar Credenciales y PIN")
        UC5("Retirar Efectivo (Cajero Inteligente)")
        
        UC5_1("Validar PIN de 4 dígitos")
        UC5_2("Verificar Tope Diario ($1.000.000) y Mínimo ($10.000)")
        UC5_3("Calcular Menor Cantidad de Billetes")
    end

    Cliente --> UC1
    Cliente --> UC2
    Cliente --> UC3
    Cliente --> UC4
    Cliente --> UC5

    UC5 -.->|<<include>>| UC5_1
    UC5 -.->|<<include>>| UC5_2
    UC5 -.->|<<include>>| UC5_3

    sequenceDiagram
    autonumber
    actor Usuario as Cliente
    participant UI as Frontend (Panel Web)
    participant Ctrl as CuentaController
    participant UC as CuentaUseCase
    participant Repo as CuentaRepositoryMemory

    Usuario->>UI: Ingresa monto y PIN (4 dígitos) y pulsa "Retirar"
    UI->>Ctrl: POST /api/cuenta/retiro {monto, pin}
    Ctrl->>UC: retirarEfectivo(idCuenta, monto, pin)
    UC->>Repo: buscarPorId(idCuenta)
    Repo-->>UC: Retorna objeto Cuenta
    
    alt Validaciones fallidas (PIN != 4 dígitos/incorrecto, Monto < 10k, Tope Diario > 1M, No múltiplo de 10k o Sin Saldo)
        UC-->>Ctrl: Lanza IllegalArgumentException("Motivo exacto del rechazo")
        Ctrl-->>UI: HTTP 400 Bad Request {exito: false, mensaje: "Motivo exacto"}
        UI-->>Usuario: Muestra alerta con el motivo del rechazo
    else Validaciones exitosas
        UC->>UC: Algoritmo voraz (100k, 50k, 20k, 10k)
        UC->>UC: Descuenta saldo y suma al acumulado diario
        UC->>Repo: guardar(cuenta)
        Repo-->>UC: Cuenta actualizada
        UC-->>Ctrl: Retorna Map con desglose de billetes y nuevo saldo
        Ctrl-->>UI: HTTP 200 OK {exito: true, billetes, nuevoSaldo}
        UI-->>Usuario: Muestra billetes dispensados y actualiza el saldo en pantalla
    end
    classDiagram
    class Cuenta {
        -Long id
        -String nombreTitular
        -String correo
        -String telefono
        -String direccion
        -String claveAcceso
        -String pinSeguridad
        -double saldo
        -double retiradoHoy
        -LocalDate fechaUltimoRetiro
        +getters()
        +setters()
    }

    class CuentaRepositoryPort {
        <<interface>>
        +buscarPorId(Long id) Cuenta
        +guardar(Cuenta cuenta) Cuenta
    }

    class CuentaRepositoryMemory {
        -Map~Long, Cuenta~ baseDatosMemoria
        +buscarPorId(Long id) Cuenta
        +guardar(Cuenta cuenta) Cuenta
    }

    class CuentaUseCase {
        -CuentaRepositoryPort repository
        +consultarCuenta(Long id) Cuenta
        +depositar(Long id, double monto) Cuenta
        +actualizarDatos(Long id, String correo, String tel, String dir) Cuenta
        +modificarCredenciales(Long id, String claveActual, String claveNueva, String nuevoPin) Cuenta
        +retirarEfectivo(Long id, double monto, String pin) Map~String, Object~
    }

    class CuentaController {
        -CuentaUseCase cuentaUseCase
        +obtenerSaldo() ResponseEntity
        +depositar(Map body) ResponseEntity
        +actualizarDatos(Map body) ResponseEntity
        +cambiarCredenciales(Map body) ResponseEntity
        +retirar(Map body) ResponseEntity
    }

    CuentaRepositoryPort <|.. CuentaRepositoryMemory : implementa
    CuentaUseCase --> CuentaRepositoryPort : usa
    CuentaUseCase --> Cuenta : gestiona
    CuentaController --> CuentaUseCase : invoca
    flowchart TB
    subgraph Cliente ["Capa de Presentación (Cliente)"]
        FE["🌐 Frontend Web\n(index.html + Fetch API)"]
    end

    subgraph Servidor ["Microservicio Spring Boot (Clean Architecture)"]
        subgraph Infraestructura ["Capa de Infraestructura"]
            REST["🔌 CuentaController\n(API REST / JSON)"]
            MEM["🗄️ CuentaRepositoryMemory\n(Persistencia en Memoria)"]
        end
        
        subgraph Aplicacion ["Capa de Aplicación"]
            UC["⚙️ CuentaUseCase\n(Lógica de Negocio y Dispensador)"]
        end
        
        subgraph Dominio ["Capa de Dominio"]
            MOD["📦 Entidad Cuenta\n y CuentaRepositoryPort"]
        end
    end

    FE <-->|"Peticiones HTTP / JSON"| REST
    REST --> UC
    UC --> MOD
    MEM .->|"Implementa puerto"| MOD
    UC --> MEM
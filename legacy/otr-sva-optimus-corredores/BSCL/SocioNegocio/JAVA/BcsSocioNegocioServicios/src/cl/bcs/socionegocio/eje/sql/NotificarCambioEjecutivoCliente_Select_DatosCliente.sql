SELECT
    "BSCL.Plataforma.BD.Secuencias::EventoNotificacion".nextval AS "IdSecuencia",
    CC."NombreCompleto"                        AS "CLIENTE",
    CC."DocumentoIdentidad"                    AS "DOCUMENTOIDENTIDAD",
    CC."TipoDocumentoIdentidad"                AS "TIPODOCUMENTOIDENTIDAD",
    CPT."Numero"                               AS "TELEFONO",
    CPDE."Direccion"                           AS "CORREO",
    CEMP."NombreCompleto"                      AS "AGENTE",
    CCDG."RazonSocial"                         AS "CORREDORA",
    CCDG."Telefono"                            AS "ATENCIONCLIENTE",
    CC."IdPersona"                             AS "IdPersonaDestinatario",
    CC."IdDireccionElectronicaPrincipal"        AS "IdDireccionElectronicaDestinatario",
    CAST(:IdPersonaRemitente AS BIGINT)           AS "IdPersonaRemitente",
    CAST(:IdDireccionElectronicaRemitente AS BIGINT) AS "IdDireccionElectronicaRemitente",
    N."IdCliente"                              AS "IdCliente",
    N."IdPersonaNuevoEjecutivo"                AS "IdPersonaNuevoEjecutivo"
FROM "BSCL.SocioNegocio.BD::EJE.NotificarCambioEjecutivoCliente_Entrada" N
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
    ON N."IdCliente" = CC."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP
    ON CC."IdPersona" = CP."IdPersona"
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT
    ON CC."IdTelefonoPrincipal" = CPT."IdTelefono"
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE
    ON CPDE."IdDireccionElectronica" = CC."IdDireccionElectronicaPrincipal"
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP
    ON N."IdPersonaNuevoEjecutivo" = CEMP."IdEmpleado"
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG
    ON CCDG."IdEmpresa" = :IdEmpresa

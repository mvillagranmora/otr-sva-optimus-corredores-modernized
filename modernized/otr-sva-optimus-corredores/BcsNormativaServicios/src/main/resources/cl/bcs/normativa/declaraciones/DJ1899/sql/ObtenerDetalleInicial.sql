SELECT
    d."ID",
    d."IdDocumento",
    d."VboUsuario",
    d."Observaciones",
    d."Version",
    d."FuenteDatos",
    ----
    d."Numero",
    d."IdMoneda",
    d."RutCliente",
    d."IdCliente",
    d."ActivoPensionado",
    d."RutEmpleador",
    --"IdEmpleador",
    d."FechaOperacion",
    d."DepositoEmpleador",--uf
    ---Aporte Empleador
    d."AporteEmpleador",--uf
    d."TraspasoEmpleador",--uf
    d."RetiroEmpleadorActualizado",--($)
    d."MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    d."AhorrosModalidadIndirectaEmpleador",
    d."AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    d."CotizacionesModalidadIndirectaEmpleador",--uf
    d."CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    d."CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    d."MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    d."RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    d."MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    d."NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    d."NumeroCertificadoEmpleador"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1899.DatosActualizarMontos" AS  d
WHERE d."Version" = :Version;
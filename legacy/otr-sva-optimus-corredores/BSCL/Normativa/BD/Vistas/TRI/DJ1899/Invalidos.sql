SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Invalidos" (
    "CodigoValidacion",
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    ----
    "Numero",
    "IdMoneda",
    "RutCliente",
    "IdCliente",
    "ActivoPensionado",
    "RutEmpleador",
    --"IdEmpleador",
    "FechaOperacion",
    "DepositoEmpleador",--uf
    ---Aporte Empleador
    "AporteEmpleador",--uf
    "TraspasoEmpleador",--uf
    "RetiroEmpleadorActualizado",--($)
    "MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    "AhorrosModalidadIndirectaEmpleador",
    "AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    "CotizacionesModalidadIndirectaEmpleador",--uf
    "CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    "CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    "MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    "RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    "MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    "NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    "NumeroCertificadoEmpleador"
) AS
SELECT
    b."CodigoValidacion",
    a."ID",
    a."IdDocumento",
    a."VboUsuario",
    a."Observaciones",
    a."FuenteDatos",
    ----
    a."Numero",
    a."IdMoneda",
    a."RutCliente",
    a."IdCliente",
    a."ActivoPensionado",
    a."RutEmpleador",
    --"IdEmpleador",
    a."FechaOperacion",
    a."DepositoEmpleador",--uf
    ---Aporte Empleador
    a."AporteEmpleador",--uf
    a."TraspasoEmpleador",--uf
    a."RetiroEmpleadorActualizado",--($)
    a."MontoRetencionActualizado",--15%($)
    --Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    a."AhorrosModalidadIndirectaEmpleador",
    a."AhorrosModalidadDirectaTrabajador",
    --DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    --del ART. 42 Nª1 de la LIR en UF
    a."CotizacionesModalidadIndirectaEmpleador",--uf
    a."CotizacionesModalidadDirectaTrabajador",--uf
    --DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    --del Art.42 Nª2 de la LIR en UF
    a."CotizacionesTrabajadorIndependiente",--uf
    --Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    --del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    --los aportes del empleador, actualizados en $
    a."MontoRetiroPorAhorrosIncisoPrimero",
    --Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    a."RetencionImpuestoSobreRetiros",--15%($)
    --Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    a."MontoRetiroPorAhorrosIncisoSegundo",
    --N° CERTIFICADO TRABAJADOR
    a."NumeroCertificadoTrabajador",
    --N° CERTIFICADO EMPLEADOR
    a."NumeroCertificadoEmpleador"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1899.PreDatos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1899.Invalidos" to bcs_desarrollador with grant option;

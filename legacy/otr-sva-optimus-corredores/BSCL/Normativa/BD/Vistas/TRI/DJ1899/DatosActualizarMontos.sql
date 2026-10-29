SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.DatosActualizarMontos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1899.DatosActualizarMontos" AS
SELECT
    d."ID",
    d."IdDocumento",
    d."VboUsuario",
    d."Observaciones",
    d."FuenteDatos",
    doc."Version",
    ----
    d."Numero",
    d."IdMoneda",
    d."RutCliente",
    d."IdCliente",
    d."ActivoPensionado",
    d."RutEmpleador",
    --"IdEmpleador",
    to_date(d."FechaOperacion") AS "FechaOperacion",
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
FROM "BSCL.Normativa.BD::TRI.DatosDJ1899" AS d
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" AS doc ON (doc."IdDocumento" = d."IdDocumento")
--WHERE d."EjecutadoComplementoMontos" IS NULL
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1899.DatosActualizarMontos" to bcs_desarrollador with grant option;
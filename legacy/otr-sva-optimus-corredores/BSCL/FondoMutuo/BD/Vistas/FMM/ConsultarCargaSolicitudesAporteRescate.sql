SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCargaSolicitudesAporteRescate";
 
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCargaSolicitudesAporteRescate" AS

SELECT
 "Carga"."IdDetalleCargaSolicitudesAporteRescate" AS "IdRegistroCarga"
, "Carga"."IdCargaSolicitudesAporteRescate" AS "IdCarga"
, "Carga"."RutEmisor"
, "Carga"."CodigoPortafolioEmisor"
, "Carga"."TipoMovimiento"
, "Carga"."FechaMovimiento"
, "Carga"."FechaValorCuota"
, "Carga"."IdSolicitudAporteRescate"
, "Carga"."DocumentoIdentidad"
, "Carga"."CodigoPortafolio"
, "Carga"."IdSerieFondo"
, "Carga"."CalculoPorMontoCuota"
, CASE "Carga"."CalculoPorMontoCuota" WHEN 'C' THEN 'UI_NUMERO_CUOTAS' WHEN 'T' THEN 'UI_TOTAL' ELSE 'UI_MONTO' END AS "CalculoPorMontoCuotaUI"
, "Carga"."NumeroCuotas"
, "Carga"."ValorCuota"
, "Carga"."Monto"
, "Carga"."MontoComision"
, "Carga"."IdMoneda"
, "Carga"."IdMonedaPago"
, "Carga"."IdMedioDePago"
, "Carga"."IdSerieFondoDestino"
, "Carga"."IdCliente"
, "Carga"."IdPortafolio"
, "Carga"."IdEmisor"
, "Carga"."IndicadorMLT"
, CASE "Carga"."IndicadorMLT" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsIndicadorMLTUI"
, "Carga"."Concepto"
, "Carga"."FechaCarga"
, "Carga"."FechaIngresoRegistro"
, "Carga"."Estado"
, "Carga"."DescEstado"
, "Carga"."EstadoProceso"
, "Carga"."DescEstadoProceso"
, "Carga"."EsActualizacion"
, "Carga"."Eliminado"
, "Carga"."IdEmpresa"
, "Carga"."IdMoneda" AS "IdMonedaFondo"
, "Cliente"."RazonSocial" AS "RazonSocialCliente"
, "Cliente"."NombreCompleto" AS "NombreCompletoCliente"
, "Portafolio"."DescripcionPortafolio" AS "DescripcionPortafolioCliente"
, "Estado"."Nombre" AS "NombreEstado"
, "EstadoProceso"."Nombre" AS "NombreEstadoProceso"
, "TipoMovimiento"."Descripcion" AS "DescripcionTipoMovimiento"
, COALESCE("SerieFondo"."DecimalesCuota",0) AS "DecimalesCuota"
, COALESCE("SerieFondo"."DecimalesValorCuota",0) AS "DecimalesValorCuota"
, "Carga"."IdRegimenTributarioOrigen"
, "Carga"."IdRegimenTributarioDestino"
FROM "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate" AS "Carga"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "Carga"."IdCliente")
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "Portafolio" ON ("Carga"."IdPortafolio" = "Portafolio"."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Estado"."IdEstado" = "Carga"."Estado")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "EstadoProceso" ON ("EstadoProceso"."IdEstado" = "Carga"."EstadoProceso")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimiento" ON ("Carga"."TipoMovimiento" = "TipoMovimiento"."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("Carga"."IdSerieFondo" = "SerieFondo"."IdSerieFondo")
WHERE "Carga"."Eliminado" = 'N';

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCargaSolicitudesAporteRescate" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

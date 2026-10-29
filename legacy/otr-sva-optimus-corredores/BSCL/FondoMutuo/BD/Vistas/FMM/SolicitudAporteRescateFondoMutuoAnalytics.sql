
SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuoAnalytics";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuoAnalytics"
AS

SELECT 
 "SolicitudAporteRescate"."IdSolicitudAporteRescate"
, CASE WHEN "SolicitudAporteRescate"."IdCliente" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdCliente" END AS "IdCliente"
, CASE WHEN "SolicitudAporteRescate"."IdPortafolio" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdPortafolio" END AS "IdPortafolio"
, CASE WHEN "SolicitudAporteRescate"."TipoSolicitud" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."TipoSolicitud" AS NVARCHAR(3)) END AS "TipoSolicitud" 
, CASE WHEN "SerieDeFondo"."IdSerieFondo" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdSerieFondo" AS NVARCHAR(40)) END AS "IdSerieFondo"-- es el mismo nemotecnico
, CASE WHEN "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) END AS "IdTipoMovimientoFondoMutuo" 
, CASE WHEN "SolicitudAporteRescate"."IdDividendo" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdDividendo" END AS "IdDividendo"
, CASE WHEN "SolicitudAporteRescate"."IdSerieFondoDestino" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdSerieFondoDestino" AS NVARCHAR(40)) END AS "IdSerieFondoDestino" 
, CASE WHEN "SolicitudAporteRescate"."IdEmisorDestino" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdEmisorDestino" AS NVARCHAR(40)) END AS "IdEmisorDestino" 
, CASE WHEN "SolicitudAporteRescate"."IdClienteDestino" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdClienteDestino" END AS "IdClienteDestino"
, CASE WHEN "SolicitudAporteRescate"."IdPortafolioDestino" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdPortafolioDestino" END AS "IdPortafolioDestino"
, "SolicitudAporteRescate"."IndicadorMLT"
, "SolicitudAporteRescate"."Overnight"
, CASE WHEN "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado" END AS "IdSolicitudAporteRescateRelacionado"
, CASE WHEN "SolicitudAporteRescate"."IdTipoCartera" = '' THEN CAST("SolicitudAporteRescate"."IdTipoCartera" AS NVARCHAR(3)) END AS "IdTipoCartera"
, "SolicitudAporteRescate"."CalculoPorMontoCuota"
, CAST("SolicitudAporteRescate"."FechaEmision" AS CHAR(10)) AS "FechaEmision"
, "SolicitudAporteRescate"."FechaAceptacion"
, "SolicitudAporteRescate"."FechaCertificacion" 
, "SolicitudAporteRescate"."FechaLiquidacion"
, "SolicitudAporteRescate"."FechaAnulacion" 
, "SolicitudAporteRescate"."Monto"
, "SolicitudAporteRescate"."NumeroCuotas"
, "SolicitudAporteRescate"."ValorCuota"
, "SolicitudAporteRescate"."CostoUF"
, "SolicitudAporteRescate"."CostoMonedaLocal"
, "SolicitudAporteRescate"."ValorUFFechaInversion"
, "SolicitudAporteRescate"."ParidadFechaInversion"
, CASE WHEN "SolicitudAporteRescate"."IdMoneda" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."IdMoneda" AS NVARCHAR(3))END  AS "IdMoneda" 
, "SolicitudAporteRescate"."MontoComision"
, "SolicitudAporteRescate"."MontoPago"
, "SolicitudAporteRescate"."IdEmpresa"
, CASE WHEN "SolicitudAporteRescate"."Estado" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."Estado" AS NVARCHAR(3)) END AS "Estado" 
, CASE WHEN "SolicitudAporteRescate"."IdUsuario" = 0 THEN NULL ELSE "SolicitudAporteRescate"."IdUsuario" END AS "IdUsuario"
, CASE WHEN "SolicitudAporteRescate"."IdMedioDePago" = '' THEN CAST("SolicitudAporteRescate"."IdMedioDePago" AS NVARCHAR(3)) END AS "IdMedioDePago" 
, CASE WHEN "SolicitudAporteRescate"."EstadoLiquidacion" = '' THEN NULL ELSE CAST("SolicitudAporteRescate"."EstadoLiquidacion" AS NVARCHAR(3)) END AS "EstadoLiquidacion"
, "SolicitudAporteRescate"."MontoInversion"
, "SolicitudAporteRescate"."NumeroCuotasInversion"
, "SolicitudAporteRescate"."ValorCuotaInversion"
, "SolicitudAporteRescate"."CostoMonedaLocalInversion"
, "SolicitudAporteRescate"."ParidadFechaCertificacion"
, "SolicitudAporteRescate"."MontoPagoLocal"
, "SolicitudAporteRescate"."MontoComisionLocal"
--, CASE WHEN "SolicitudAporteRescate"."IdMoneda" != "Fondo"."IdMoneda" THEN 'S' ELSE 'N' END AS "EsDiferido" 				Campo calculado
, "SolicitudAporteRescate"."OrigenOperacion"
, "SolicitudAporteRescate"."SolicitudBatch"
, "SolicitudAporteRescate"."ComisionInformada"
, CASE WHEN "SerieDeFondo"."IdInstrumento" = 0 THEN NULL ELSE  "SerieDeFondo"."IdInstrumento" END AS "IdInstrumento"
, CAST("SerieDeFondo"."NombreSerieFondo" AS NVARCHAR(100)) AS "NombreSerieFondo" 
, CASE WHEN "SerieDeFondo"."CodigoSerie" = '' THEN NULL ELSE CAST("SerieDeFondo"."CodigoSerie" AS NVARCHAR(10)) END AS "CodigoSerie" 
, "SerieDeFondo"."MontoMinimo"
, "SerieDeFondo"."MontoMaximo"
, "SerieDeFondo"."DiasRetencionesRescate"
--, "SerieDeFondo"."DiasRetencionesReinversion"
, "SerieDeFondo"."ExcluyeGarantia"
, CASE WHEN "SerieDeFondo"."IdSerieFondoOrigen" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdSerieFondoOrigen" AS NVARCHAR(40)) END AS "IdSerieFondoOrigen" 
, "SerieDeFondo"."CobraComision"
, "SerieDeFondo"."Omnibus"
, "SerieDeFondo"."Diferido"
, "SerieDeFondo"."DecimalesValorCuota"
, "SerieDeFondo"."DecimalesCuota"
, CASE WHEN "SerieDeFondo"."IdCliente" = 0 THEN NULL ELSE "SerieDeFondo"."IdCliente" END AS "IdClienteAGF"
, CASE WHEN "SerieDeFondo"."IdPortafolio" = 0 THEN NULL ELSE "SerieDeFondo"."IdPortafolio" END  AS "IdPortafolioAGF"
, CASE WHEN "SerieDeFondo"."IdMedioDePagoAbono" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdMedioDePagoAbono" AS NVARCHAR(3)) END AS "IdMedioDePagoAbonoAGF"
, CASE WHEN "SerieDeFondo"."IdMedioDePagoCargo" = '' THEN NULL ELSE CAST("SerieDeFondo"."IdMedioDePagoCargo" AS NVARCHAR(3)) END AS "IdMedioDePagoCargoAGF"
, CASE WHEN "Fondo"."IdFondo" = '' THEN NULL ELSE CAST("Fondo"."IdFondo" AS NVARCHAR(40)) END AS "IdFondo" 
, CASE WHEN "Fondo"."IdEmisor" = '' THEN NULL ELSE CAST("Fondo"."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor" 
, CAST("Fondo"."Nemotecnico" AS NVARCHAR(40)) AS "Nemotecnico" 
, CAST("Fondo"."NombreFondo" AS NVARCHAR(100)) AS "NombreFondo" 
, CASE WHEN "Fondo"."IdTipoFondo" = '' THEN NULL ELSE CAST("Fondo"."IdTipoFondo" AS NVARCHAR(10)) END AS "IdTipoFondo" 
, CASE WHEN "Fondo"."IdPaisOrigen" = 0 THEN NULL ELSE "Fondo"."IdPaisOrigen" END AS "IdPaisOrigen"
, CAST("Fondo"."CodigoSVS" AS NVARCHAR(40)) AS "CodigoSVS" 
, "Fondo"."MoneyMarket"
, CASE WHEN "Fondo"."IdCategoriaPerfilFondoMutuo" = '' THEN NULL ELSE CAST("Fondo"."IdCategoriaPerfilFondoMutuo" AS NVARCHAR(40)) END AS "IdCategoriaPerfilFondoMutuo" 
, "Fondo"."FechaCreacion" 
, "Fondo"."FechaConstitucion"
, "Fondo"."FechaCierre" 
, "Fondo"."Vigencia"
, CASE WHEN "Fondo"."IdTipoInstrumento" = '' THEN NULL ELSE CAST("Fondo"."IdTipoInstrumento" AS NVARCHAR(3)) END AS "IdTipoInstrumento" 
, CASE WHEN "Fondo"."IdMoneda" = '' THEN NULL ELSE CAST("Fondo"."IdMoneda" AS NVARCHAR(3)) END AS "IdMonedaFondo"
, CASE WHEN "ClienteAGF"."IdPersona" = 0 THEN NULL ELSE "ClienteAGF"."IdPersona" END AS "IdPersonaAGF"
, CAST("TipoFondo"."NombreCorto" AS NVARCHAR(40)) AS "NombreCorto" 
, CAST("TipoFondo"."Descripcion" AS NVARCHAR(100)) AS "Descripcion" 
, CAST("TipoMovimientoFondoMutuo"."Descripcion" AS NVARCHAR(100)) AS "DescripcionTipoMovimiento"
, CAST("Estado"."Nombre" AS NVARCHAR(100)) AS "NombreEstado"
, CAST("ConsultarEmpleado"."NombreCompleto" AS NVARCHAR(302)) AS "NombreEmpleado"
, CAST("MedioDePago"."Nombre" AS NVARCHAR(100)) AS "TextoPreferenciaPago"
, CAST("EstadoLiquidacion"."Nombre" AS NVARCHAR(100)) AS "NombreEstadoLiquidacion"
, CASE WHEN "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" = '' THEN NULL ELSE CAST("SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) END AS "IdTipoMovimientoFondoMutuoRelacionado"
, CAST("TipoMovimientoFondoMutuoRelacionado"."Descripcion" AS NVARCHAR(100)) AS "DescripcionTipoMovimientoRelacionado"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoFondo" AS "TipoFondo" ON ("Fondo"."IdTipoFondo" = "TipoFondo"."IdTipoFondo") 
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Estado"."IdEstado" = "SolicitudAporteRescate"."Estado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "EstadoLiquidacion" ON ("EstadoLiquidacion"."IdEstado" = "SolicitudAporteRescate"."EstadoLiquidacion")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "ConsultarEmpleado" ON ("ConsultarEmpleado"."IdEmpleado" = "SolicitudAporteRescate"."IdUsuario")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioDePago" ON ("MedioDePago"."IdMedioDePago" = "SolicitudAporteRescate"."IdMedioDePago")
LEFT OUTER  JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "PortafolioAGF" ON ("PortafolioAGF"."IdPortafolio" = "SerieDeFondo"."IdPortafolio")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ClienteAGF" ON ("SerieDeFondo"."IdCliente" = "ClienteAGF"."IdCliente")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescateRelacionado" ON ("SolicitudAporteRescateRelacionado"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuoRelacionado" ON ("TipoMovimientoFondoMutuoRelacionado"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuoAnalytics" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuo";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuo"
AS

SELECT 
 "SolicitudAporteRescate"."IdSolicitudAporteRescate"
, "SolicitudAporteRescate"."IdCliente"
, "SolicitudAporteRescate"."IdPortafolio"
, CAST("SolicitudAporteRescate"."TipoSolicitud" AS NVARCHAR(3)) AS "TipoSolicitud" 
, CAST("SerieDeFondo"."IdSerieFondo" AS NVARCHAR(40)) AS "IdSerieFondo"-- es el mismo nemotecnico
, CAST("SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) AS "IdTipoMovimientoFondoMutuo" 
, "SolicitudAporteRescate"."IdDividendo"
, CAST("SolicitudAporteRescate"."IdSerieFondoDestino" AS NVARCHAR(40)) AS "IdSerieFondoDestino" 
, CAST("SolicitudAporteRescate"."IdEmisorDestino" AS NVARCHAR(40)) AS "IdEmisorDestino" 
, "SolicitudAporteRescate"."IdClienteDestino"
, "SolicitudAporteRescate"."IdPortafolioDestino"
, "SolicitudAporteRescate"."IndicadorMLT"
, "SolicitudAporteRescate"."Overnight"
, "SolicitudAporteRescate"."IdSolicitudAporteRescateRelacionado"
, CAST("SolicitudAporteRescate"."IdTipoCartera" AS NVARCHAR(3)) AS "IdTipoCartera"
, "SolicitudAporteRescate"."CalculoPorMontoCuota"
, CAST("SolicitudAporteRescate"."FechaEmision" AS NVARCHAR(10)) AS "FechaEmision" 
, CAST("SolicitudAporteRescate"."FechaAceptacion" AS NVARCHAR(10)) AS "FechaAceptacion" 
, CAST("SolicitudAporteRescate"."FechaCertificacion" AS NVARCHAR(10)) AS "FechaCertificacion"
, CAST("SolicitudAporteRescate"."FechaLiquidacion" AS NVARCHAR(10)) AS "FechaLiquidacion" 
, CAST("SolicitudAporteRescate"."FechaAnulacion" AS NVARCHAR(10)) AS "FechaAnulacion" 
, "SolicitudAporteRescate"."Monto"
, "SolicitudAporteRescate"."NumeroCuotas"
, "SolicitudAporteRescate"."ValorCuota"
, "SolicitudAporteRescate"."CostoUF"
, "SolicitudAporteRescate"."CostoMonedaLocal"
, "SolicitudAporteRescate"."ValorUFFechaInversion"
, "SolicitudAporteRescate"."ParidadFechaInversion"
, CAST("SolicitudAporteRescate"."IdMoneda" AS NVARCHAR(3)) AS "IdMoneda" 
, "SolicitudAporteRescate"."MontoComision"
, "SolicitudAporteRescate"."MontoPago"
, "SolicitudAporteRescate"."IdEmpresa"
, CAST("SolicitudAporteRescate"."Estado" AS NVARCHAR(3)) AS "Estado" 
, "SolicitudAporteRescate"."IdUsuario"
, CAST("SolicitudAporteRescate"."IdMedioDePago" AS NVARCHAR(3)) AS "IdMedioDePago" 
, CAST("SolicitudAporteRescate"."EstadoLiquidacion" AS NVARCHAR(3)) AS "EstadoLiquidacion"
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
, "SerieDeFondo"."IdInstrumento"
, CAST("SerieDeFondo"."NombreSerieFondo" AS NVARCHAR(100)) AS "NombreSerieFondo" 
, CAST("SerieDeFondo"."CodigoSerie" AS NVARCHAR(10)) AS "CodigoSerie" 
, "SerieDeFondo"."MontoMinimo"
, "SerieDeFondo"."MontoMaximo"
, "SerieDeFondo"."DiasRetencionesRescate"
--, "SerieDeFondo"."DiasRetencionesReinversion"
, "SerieDeFondo"."ExcluyeGarantia"
, CAST("SerieDeFondo"."IdSerieFondoOrigen" AS NVARCHAR(40)) AS "IdSerieFondoOrigen" 
, "SerieDeFondo"."CobraComision"
, "SerieDeFondo"."Omnibus"
, "SerieDeFondo"."Diferido"
, "SerieDeFondo"."DecimalesValorCuota"
, "SerieDeFondo"."DecimalesCuota"
, "SerieDeFondo"."IdCliente" AS "IdClienteAGF"
, "SerieDeFondo"."IdPortafolio" AS "IdPortafolioAGF"
, CAST("SerieDeFondo"."IdMedioDePagoAbono" AS NVARCHAR(3)) AS "IdMedioDePagoAbonoAGF"
, CAST("SerieDeFondo"."IdMedioDePagoCargo" AS NVARCHAR(3)) AS "IdMedioDePagoCargoAGF"
, CAST("Fondo"."IdFondo" AS NVARCHAR(40)) AS "IdFondo" 
, CAST("Fondo"."IdEmisor" AS NVARCHAR(40)) AS "IdEmisor" 
, CAST("Fondo"."Nemotecnico" AS NVARCHAR(40)) AS "Nemotecnico" 
, CAST("Fondo"."NombreFondo" AS NVARCHAR(100)) AS "NombreFondo" 
, CAST("Fondo"."IdTipoFondo" AS NVARCHAR(10)) AS "IdTipoFondo" 
, "Fondo"."IdPaisOrigen"
, CAST("Fondo"."CodigoSVS" AS NVARCHAR(40)) AS "CodigoSVS" 
, "Fondo"."MoneyMarket"
, CAST("Fondo"."IdCategoriaPerfilFondoMutuo" AS NVARCHAR(40)) AS "IdCategoriaPerfilFondoMutuo" 
, CAST("Fondo"."FechaCreacion" AS NVARCHAR(10)) AS "FechaCreacion"
, CAST("Fondo"."FechaConstitucion" AS NVARCHAR(10)) AS "FechaConstitucion" 
, CAST("Fondo"."FechaCierre" AS NVARCHAR(10)) AS "FechaCierre" 
, "Fondo"."Vigencia"
, CAST("Fondo"."IdTipoInstrumento" AS NVARCHAR(3)) AS "IdTipoInstrumento" 
, CAST("Fondo"."IdMoneda" AS NVARCHAR(3)) AS "IdMonedaFondo"
, "ClienteAGF"."IdPersona" AS "IdPersonaAGF"
, CAST("TipoFondo"."NombreCorto" AS NVARCHAR(40)) AS "NombreCorto" 
, CAST("TipoFondo"."Descripcion" AS NVARCHAR(100)) AS "Descripcion" 
, CAST("TipoMovimientoFondoMutuo"."Descripcion" AS NVARCHAR(100)) AS "DescripcionTipoMovimiento"
, CAST("Estado"."Nombre" AS NVARCHAR(100)) AS "NombreEstado"
, CAST("ConsultarEmpleado"."NombreCompleto" AS NVARCHAR(302)) AS "NombreEmpleado"
, CAST("MedioDePago"."Nombre" AS NVARCHAR(100)) AS "TextoPreferenciaPago"
, CAST("EstadoLiquidacion"."Nombre" AS NVARCHAR(100)) AS "NombreEstadoLiquidacion"
, CAST("SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo" AS NVARCHAR(10)) AS "IdTipoMovimientoFondoMutuoRelacionado"
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
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuoRelacionado" ON ("TipoMovimientoFondoMutuoRelacionado"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescateRelacionado"."IdTipoMovimientoFondoMutuo");;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.SolicitudAporteRescateFondoMutuo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
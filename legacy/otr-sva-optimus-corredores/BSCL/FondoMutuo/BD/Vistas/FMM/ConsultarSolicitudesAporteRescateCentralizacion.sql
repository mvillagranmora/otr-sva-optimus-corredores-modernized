SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion"
AS
SELECT 
	 "SolicitudAporteRescate"."IdSolicitudAporteRescate"
	, "SolicitudAporteRescate"."IdSerieFondo"
	, "SolicitudAporteRescate"."IdSerieFondoDestino"
	, "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo"
	, "SolicitudAporteRescate"."MontoPago"
	, "SolicitudAporteRescate"."IdMoneda"
	, "SolicitudAporteRescate"."Estado"
	, "SolicitudAporteRescate"."FechaCertificacion"
	, "SolicitudAporteRescate"."Spread"
	, "TipoMovimientoFondoMutuo"."IdTipoOperacion"
	, "ConsultarCliente"."DocumentoIdentidad"
	, "ConsultarCliente"."NombreCompleto"
	, "ConsultarPortafolio"."CodigoPortafolio"
	, "ConsultarPortafolio"."IdPortafolio"
	, "ConsultarClienteDes"."DocumentoIdentidad" AS "DocumentoIdentidadDes"
	, "ConsultarClienteDes"."NombreCompleto" AS "NombreCompletoDes"
	, "ConsultarPortafolioDes"."CodigoPortafolio" AS "CodigoPortafolioDes"
	, "ConsultarPortafolioDes"."IdPortafolio" AS "IdPortafolioDes"
	, 'INFM' AS "IdTipoProducto"
	, 'FM'	 AS "IdClasificacionTipoInstrumento"
	, "Fondo"."IdTipoInstrumento"
	, "Fondo"."IdEmisor"
	, "SolicitudAporteRescate"."IdEmpresa"
	,COALESCE("ConsultarEjecutivo"."CodigoEmpleado",'') AS "Agente"
	,COALESCE("ConsultarEjecutivo"."NombreCompleto",'') AS "NombreAgente"
	,COALESCE("ConsultarEjecutivo"."CodigoSucursal",'') AS "SucursalAgente"
	,COALESCE("ConsultarEjecutivo"."NombreSucursal",'') AS "NombreSucursalAgente"
	, "SolicitudAporteRescate"."IdMonedaSistema"
	, "SolicitudAporteRescate"."MontoMonedaSistema"
	, "SolicitudAporteRescate"."MontoPagoLocal"
	, "Fondo"."IdMoneda" AS "IdMonedaFondo"
	, "SolicitudAporteRescate"."IdCliente" 
	

	, "SerieDeFondo"."IdFondo"
	, "SolicitudAporteRescate"."TipoSolicitud"
	, "SolicitudAporteRescate"."IdMedioDePago"
	, "SolicitudAporteRescate"."IdOperacionSpot"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
  LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" 
    ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo")
  LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" 
    ON ("SerieDeFondo"."IdFondo" = "Fondo"."IdFondo")
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" 
    ON  ( "ConsultarPortafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio" )
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" 
    ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
  LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" 
    ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo")
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" "ConsultarEjecutivo" 
    ON ("ConsultarEjecutivo"."IdEmpleadoEjecutivo" = "SolicitudAporteRescate"."IdUsuario")
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolioDes" 
    ON  ( "ConsultarPortafolioDes"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolioDestino" )  
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarClienteDes" 
    ON  ( "ConsultarClienteDes"."IdCliente" = "ConsultarPortafolioDes"."IdCliente" );
GO

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesAporteRescateCentralizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
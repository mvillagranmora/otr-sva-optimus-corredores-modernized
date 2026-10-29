SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja"
AS
SELECT 
 SolicitudAporteRescate."IdPortafolio"
, SolicitudAporteRescate."IdMoneda"
, SolicitudAporteRescate."IdMonedaPago"
, SolicitudAporteRescate."FechaLiquidacion"
, SolicitudAporteRescate."FechaEmision"
, SolicitudAporteRescate."TipoSolicitud"
, SolicitudAporteRescate."CostoMonedaLocal"
, SolicitudAporteRescate."IdEmpresa"
, SolicitudAporteRescate."Estado" AS "EstadoSolicitud"
, SolicitudAporteRescate."EstadoLiquidacion"
, SolicitudAporteRescate."MontoComision"
, SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
, SolicitudAporteRescate."IdMedioDePago"
, SolicitudAporteRescate."IdSolicitudAporteRescate"
, SolicitudAporteRescate."IdSolicitudAporteRescateRelacionado"
, SolicitudAporteRescate."AdministracionLiquidez"
, SolicitudAporteRescate."Visado"
, SolicitudAporteRescate."FechaAceptacion"  -- FechaAceptacion = FechaCurse
, Spot."IdMonedaPrincipal"
, Spot."IdMonedaSecundaria"
, Spot."MontoMonedaSecundaria"
, Spot."MontoMonedaPrincipal"
, COALESCE(Spot."IdOperacionSpotOrigen",0) AS "IdOperacionSpot"
, SolicitudAporteRescate."IdTipoCanalIngreso"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescate
LEFT OUTER JOIN 
				  (	 select OperSpot."TipoCambioCierre", OperSpot."IdOperacionSpotOrigen", OperSpot."MontoMonedaSecundaria"
				  ,OperSpot."MontoMonedaPrincipal",OperSpot."IdMonedaPrincipal",OperSpot."IdMonedaSecundaria" from 
					  (SELECT  ROW_NUMBER() OVER(PARTITION BY "IdOperacionSpotOrigen" ORDER BY "IdOperacionSpot" DESC ) AS r,
					  "TipoCambioCierre","IdOperacionSpotOrigen","MontoMonedaSecundaria","MontoMonedaPrincipal","IdMonedaPrincipal","IdMonedaSecundaria"
						FROM "BSCL.Mesa.BD::SPT.OperacionSpot" ) as OperSpot
					 where OperSpot.r=1
				  ) AS Spot ON (Spot."IdOperacionSpotOrigen"=CASE WHEN SolicitudAporteRescate."IdOperacionSpot" > 0 THEN SolicitudAporteRescate."IdOperacionSpot" ELSE NULL END);

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

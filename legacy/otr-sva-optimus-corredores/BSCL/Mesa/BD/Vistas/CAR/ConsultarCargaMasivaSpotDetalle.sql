SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpotDetalle";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpotDetalle" AS
SELECT
	 CMS."IdCargaMasiva"
	,CMD."IdCargaMasivaDetalle"	 
	,CMD."DocumentoIdentidad"
	,POR."CodigoPortafolio"			
	,CMD."ProductoAgrupador"	
	,CMD."TipoOperacion"		
	,CMD."FechaOperacion"		
	,CMD."HoraOperacion"		
	,TCI."IdTipoCanalIngreso" AS "CanalIngreso"			
	,CMD."MontoMonedaPrincipal"	
	,CMD."TipoCambioCierre"		
	,CMD."TipoCambioCosto"		
	,CMD."PuntaCompra"			
	,CMD."PuntaVenta"			
	,CMD."FormaPagoEntregamos"	
	,CMD."FormaPagoRecibimos"	
	,CMD."Observacion"			
	,CMD."AfectoADerivado"		
	,CMD."DolarObservado"		
	,CMD."CodigoComercio"		
	,CMD."DetalleValidacion"
	,TCMS."CodigoTipoCargaMasiva"
	,CMD."TipoComprobanteFacturacion"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaSpotDetalle" AS CMD
LEFT OUTER JOIN "BSCL.Mesa.BD::CAR.CargaMasivaSpot" AS CMS ON (CMS."IdCargaMasiva" = CMD."IdCargaMasiva")
LEFT OUTER JOIN "BSCL.Mesa.BD::CAR.TipoCargaMasivaSpot" AS TCMS ON (TCMS."IdTipoCargaMasiva" = CMS."IdTipoCargaMasiva")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."DocumentoIdentidad" = CMD."DocumentoIdentidad")
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TCI ON (TCI."IdTipoCanalIngreso" = CMD."CanalIngreso")
WITH READ ONLY;

GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::CAR.ConsultarCargaMasivaSpotDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
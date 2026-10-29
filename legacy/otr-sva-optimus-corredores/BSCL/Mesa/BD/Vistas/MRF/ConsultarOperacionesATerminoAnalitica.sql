SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperacionesATerminoAnalitica";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperacionesATerminoAnalitica" AS
SELECT 
		"IdOperacion"
		,"FolioOperacion"
		,"FolioCartera"
		,LEFT(TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD HH24:MI:SS'), 2000) AS "FechaOperacion"
		,"IdTipoProducto"
		,"IdTipoOperacion"
		,"NombreTipoOperacion"
		,"Intermesa"
		,"IdMesaOrigen"
		,"NombreMesaOrigen"
		,case when "IdMesaDestino" = 0 then NULL else "IdMesaDestino" end as "IdMesaDestino"
		,"NombreMesaDestino"
		,"IdSocioDeNegocio"
		,"IdCliente"
		,"NombreCompleto"
		,"DocumentoIdentidad"
		,"IdClienteCorredor"
		,"IdPortafolio"
		,"CodigoPropositoPortafolio"
		,"AfectaCustodia"
		,"IdPortafolioCorredor"
		,"IdEmpresa"
		,"MontoOperacionInicial"
		,"MontoOperacionFinal"
		,"IdTipoReajuste"
		,"Tasa"
		,"IdMoneda"
		,"MontoOperacionInicialMoneda"
		,"MontoOperacionFinalMoneda"
		,"IdMonedaLiquidacion"
		,"PlazoEnDias"
		,LEFT(TO_NVARCHAR("FechaVencimiento",'YYYY-MM-DD HH24:MI:SS'), 2000) AS "FechaVencimiento"
		,LEFT(TO_NVARCHAR("FechaLiquidacion",'YYYY-MM-DD HH24:MI:SS'), 2000) AS "FechaLiquidacion"
		,"IdCondicionLiquidacion"
		,"NombreCondicionLiquidacion"
		,LEFT(TO_NVARCHAR("FechaCondicionLiquidacion",'YYYY-MM-DD HH24:MI:SS'), 2000) AS "FechaCondicionLiquidacion"
		,case when "IdAgente" = 0 then NULL else "IdAgente" end as "IdAgente" 
		,coalesce ("NombreEjecutivo",'') as "NombreEjecutivo"
		,"IdEstado"
		,"NombreEstado"
		,LEFT(TO_NVARCHAR("FechaCreacion",'YYYY-MM-DD HH24:MI:SS'), 2000) AS "FechaCreacion"
		,case when "IdUsuarioCreacion" = 0 then null else "IdUsuarioCreacion" end as "IdUsuarioCreacion"
		,"CodigoUsuario"
		,"IdTipoCanalIngreso"
		,"Glosa"
		,"MontoOperacionInicialAnticipo"
		,"MontoOperacionFinalAnticipo"
		,"TasaAnticipo"
		,"MontoOperacionInicialMonedaAnticipo"
		,"MontoOperacionFinalMonedaAnticipo"
		,case when "IdMonedaLiquidacionAnticipo" = '' then NULL else "IdMonedaLiquidacionAnticipo" end as "IdMonedaLiquidacionAnticipo"
		,case when "IdCondicionLiquidacionAnticipo" = '' then NULL else "IdCondicionLiquidacionAnticipo" end as "IdCondicionLiquidacionAnticipo" 
		,coalesce("NombreCondicionLiquidacionAnticipo",'')  as "NombreCondicionLiquidacionAnticipo"
		,LEFT(TO_NVARCHAR("FechaCondicionLiquidacionAnticipo",'YYYY-MM-DD'), 2000) AS "FechaCondicionLiquidacionAnticipo"
FROM (
	SELECT 
		*,
		ROW_NUMBER() OVER(PARTITION BY "IdOperacion" ORDER BY "IdOperacion") AS rn
	FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF"
	WHERE 	"IdTipoOperacion" IN ('VENTER','COMTER') AND "IdEstado" <> 'ANU') WHERE rn = 1;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperacionesATerminoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
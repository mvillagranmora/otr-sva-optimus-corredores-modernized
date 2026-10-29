SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo";

CREATE PROCEDURE 
-- 
--  
--  
"BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
    IN  p_Ordenes                  	TABLE(
    									"IdInterno"	BIGINT,
    									"IdOrden" BIGINT),
    IN  p_NuevoEstado				NVARCHAR(3),
    OUT p_out_SalidaDetalle			"BSCL.Orden.BD.Tipos::SalidaDetalle",
    OUT p_out_SalidaResumen			"BSCL.Orden.BD.Tipos::SalidaResumen"
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	RESULTADO_CambiarEstadoOrdenMasivo1 = 
			SELECT 	p_ord."IdInterno",p_ord."IdOrden",
					CASE WHEN	(ord."IdEstadoOrden" = 'PDA' and (:p_NuevoEstado = 'NAU' OR :p_NuevoEstado = 'ANU' OR :p_NuevoEstado = 'ING')) THEN 'OK'		--DE PENDIENTE DE AUTORIZAR A NO AUTORIZADA,INGRESADA,ANULADA
				
							WHEN	(ord."IdEstadoOrden" = 'ING' and :p_NuevoEstado = 'ANU') 											THEN 'OK'	--DE INGRESADA A ANULADA
							WHEN	(ord."IdEstadoOrden" = 'ING' AND ord."IdTipoCanalDistribucion" = 'MAN' and :p_NuevoEstado = 'OFE') 	THEN 'OK'	--DE INGRESADA A OFERTADA, SI EL CANAL ES MANUAL (CANTADA)
							WHEN	(ord."IdEstadoOrden" = 'ING' AND ord."IdTipoCanalDistribucion" != 'MAN' and :p_NuevoEstado = 'ENR') THEN 'OK'	--DE INGRESADA A EN RUTEO, SI EL CANAL NO ES MANUAL (SE ASUME RUTEO)
				
				   			WHEN	(ord."IdEstadoOrden" = 'OFE' AND ord."IdTipoCanalDistribucion" = 'MAN' and :p_NuevoEstado = 'ING') 	THEN 'OK'	--SI LA CANTE LA PUEDO DESCANTAR
				   			WHEN	(ord."IdEstadoOrden" = 'OFE' AND ord."IdTipoCanalIngreso" = 'TRA' and ord."IdTipoCanalDistribucion" = 'MAN' and :p_NuevoEstado = 'ANU') THEN 'OK'	--SOLO PUEDO ANULAR SI NO ES CANTADA (RUTEO),EXCEPTO SI ES POSTRERA
				   			
				   			WHEN	(ord."IdEstadoOrden" = 'OFE' and ord."IdTipoCanalDistribucion" != 'MAN' and :p_NuevoEstado = 'ANU')	THEN 'OK' 	--SI NO ES CANTADA, LA PUEDO ANULAR INMEDIANTAMENTE
							WHEN	(ord."IdEstadoOrden" = 'OFE' and :p_NuevoEstado = 'ANP')	 										THEN 'OK' 	--SI NO ES CANTADA, LA PUEDO ANULAR PARCIALMENTE
							WHEN	(ord."IdEstadoOrden" = 'ENR' and :p_NuevoEstado = 'OFE') 											THEN 'OK'	--DE EN PROCESO DE NEGOCIACION A OFERTADA(EN NEGOCIACION)
							WHEN	(ord."IdEstadoOrden" = 'ENR' and :p_NuevoEstado = 'ANU') 											THEN 'OK'
							WHEN	(ord."IdEstadoOrden" = 'ENR' and :p_NuevoEstado = 'RZD')											THEN 'OK'	--DE EN PROCESO DE NEGOCIACION A RECHAZADA(EN NEGOCIACION)
					ELSE 'RECHAZO' END AS "Resultado"
					FROM  :p_Ordenes p_ord
					INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord on ord."IdOrden" = p_ord."IdOrden";
	
	UPDATE ord 
  	SET 
  		"IdEstadoOrden" = p_NuevoEstado,
  		"OrdenVigente" = CASE :p_NuevoEstado WHEN 'ANU' THEN 'N' WHEN 'ANP' THEN 'N' WHEN 'RZD' THEN 'N' WHEN 'NAU' THEN 'N' ELSE "OrdenVigente" END
  	FROM "BSCL.Orden.BD::ORD.Orden" ord 
  	INNER JOIN :RESULTADO_CambiarEstadoOrdenMasivo1 RES on RES."IdOrden" = ord."IdOrden"
	WHERE RES."IdOrden" = ord."IdOrden" AND RES."Resultado" = 'OK';
	
	p_out_SalidaDetalle	= 	SELECT "IdInterno", "IdOrden" as "IdOperacion", "Resultado", CASE "Resultado" WHEN 'OK' THEN 'CAMBIO_ESTADO_OK' ELSE 'CAMBIO_ESTADO_INVALIDO' END AS "Codigo", 
									'IdOrden' as "CodigoDato1",CAST("IdOrden" AS NVARCHAR(100)) AS "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
							FROM :RESULTADO_CambiarEstadoOrdenMasivo1;
							
	
	p_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);
	
	--DROP TABLE #RESULTADO_CambiarEstadoOrdenMasivo;
END



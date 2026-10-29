SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CambiarEstadoAsignacionMasivo";

CREATE PROCEDURE
-- 
--  
--  
"BSCL.Orden.BD.Servicios::CambiarEstadoAsignacionMasivo" (
    IN  p_asignaciones              TABLE("IdAsignacion" BIGINT),
    IN  p_NuevoEstado				NVARCHAR(3),
    
    OUT p_out_Salida				"BSCL.Orden.BD.Tipos::SalidaDetalle"
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	--------------------
	--LOGICA 
	--------------------

	CREATE LOCAL TEMPORARY TABLE #RESULTADO_CambiarEstadoAsignacionMasivo (
		"IdAsignacion" BIGINT, 
		"Resultado" NVARCHAR(40), 
		"Codigo" NVARCHAR(40)
	);

	insert into #RESULTADO_CambiarEstadoAsignacionMasivo 
	SELECT p_asig."IdAsignacion", 'RECHAZO' as "Resultado", 'CAMBIO_ESTADO_INVALIDO' as "Codigo"
	FROM :p_asignaciones p_asig;

	UPDATE res
	SET "Resultado"='OK' ,"Codigo" = 'CAMBIO_ESTADO_OK'
	FROM #RESULTADO_CAMBIARESTADOASIGNACIONMASIVO res
	INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig on asig."IdAsignacion" = res."IdAsignacion"
	WHERE ((asig."IdEstadoAsignacion" ='NEF') and (:p_NuevoEstado = 'ANU' or :p_NuevoEstado = 'EAF')
    OR (asig."IdEstadoAsignacion" ='EAF') and (:p_NuevoEstado = 'NEF' or :p_NuevoEstado = 'ANU'));
	
	
	
  	UPDATE  asig SET "IdEstadoAsignacion" = p_NuevoEstado
  	FROM "BSCL.Orden.BD::ORD.Asignacion" asig INNER JOIN #RESULTADO_CambiarEstadoAsignacionMasivo RES
	ON RES."IdAsignacion" = asig."IdAsignacion" AND RES."Resultado" = 'OK';
	
	p_out_Salida = select "IdAsignacion" as "IdInterno", "IdAsignacion" as "IdOperacion", "Resultado", "Codigo",'IdAsignacion' as "CodigoDato1",CAST("IdAsignacion" AS NVARCHAR(100)) AS "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					from #RESULTADO_CambiarEstadoAsignacionMasivo;
	
	DROP TABLE #RESULTADO_CambiarEstadoAsignacionMasivo;

END



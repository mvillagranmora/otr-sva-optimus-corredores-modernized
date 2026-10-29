SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAporteRescate";

CREATE PROCEDURE 
--  Proposito: Cambia Estado Solicitud Aporte Rescate
--  Autor: Gat-Blac 
--  Fecha Creacion: 20/06/2016
"BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAporteRescate" (
    IN  p_Solicitudes               TABLE("IdSolicitudAporteRescate" BIGINT, "Estado" NVARCHAR(3)),
    OUT p_out_Resultado				TABLE("IdSolicitudAporteRescate" BIGINT, "Resultado" NVARCHAR(1), "Codigo" NVARCHAR(40))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	p_out_Resultado	= SELECT 0 AS "IdSolicitudAporteRescate", '' AS "Resultado", '' AS "Codigo" FROM dummy WHERE 1=2;
	--------------------
	--LOGICA 
	--------------------

	CREATE LOCAL TEMPORARY TABLE #RESULTADO_CambiarEstadoSolicitudAporteRescateMasivo (
		"IdSolicitudAporteRescate" BIGINT, 
		"Resultado" NVARCHAR(1), 
		"Codigo" NVARCHAR(40)
	);

	INSERT INTO #RESULTADO_CambiarEstadoSolicitudAporteRescateMasivo 
	SELECT p_sol."IdSolicitudAporteRescate", 'N' as "Resultado", 'CAMBIO_ESTADO_INVALIDO' as "Codigo"
	FROM :p_Solicitudes p_sol;

	UPDATE RES
	SET "Resultado"='S' ,"Codigo" = 'CAMBIO_ESTADO_OK'
	FROM #RESULTADO_CAMBIARESTADOSOLICITUDAPORTERESCATEMASIVO RES
	INNER JOIN :p_Solicitudes p_sol ON (RES."IdSolicitudAporteRescate" = p_sol."IdSolicitudAporteRescate")
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol ON (sol."IdSolicitudAporteRescate" = p_sol."IdSolicitudAporteRescate")
	WHERE (sol."Estado" ='ING' AND p_sol."Estado" IN ('RZD','ACP','ANU','CRT'))
    OR (sol."Estado" ='ACP' AND p_sol."Estado" IN ('ANU','CRT' ))
   	OR (sol."Estado" ='PDA' AND p_sol."Estado" IN ('ING','ANU' ))
   	OR (sol."Estado" ='CRT' AND p_sol."Estado" IN ('ANU','ING'));
	
	
  	UPDATE sol SET "Estado" = p_sol."Estado"
  	FROM  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
  	INNER JOIN #RESULTADO_CambiarEstadoSolicitudAporteRescateMasivo RES ON (sol."IdSolicitudAporteRescate" = RES."IdSolicitudAporteRescate")
  	INNER JOIN :p_Solicitudes p_sol ON (sol."IdSolicitudAporteRescate" = p_sol."IdSolicitudAporteRescate")
	WHERE RES."Resultado" = 'S';
	
	p_out_Resultado	= select "IdSolicitudAporteRescate", "Resultado", "Codigo" from #RESULTADO_CambiarEstadoSolicitudAporteRescateMasivo;
	
	DROP TABLE #RESULTADO_CambiarEstadoSolicitudAporteRescateMasivo;

END;



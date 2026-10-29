SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CambiarEstadoTransaccionMasivo";

CREATE PROCEDURE 
-- 
--  
--  
"BSCL.Orden.BD.Servicios::CambiarEstadoTransaccionMasivo" (
    IN  p_transacciones             TABLE("IdTransaccion" BIGINT),
    IN  p_NuevoEstado				NVARCHAR(3),
   	OUT p_out_SalidaDetalle			"BSCL.Orden.BD.Tipos::SalidaDetalle",
    OUT p_out_SalidaResumen			"BSCL.Orden.BD.Tipos::SalidaResumen"
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	CREATE LOCAL TEMPORARY TABLE #RESULTADO_CambiarEstadoTransaccionMasivo (
		"IdTransaccion" BIGINT, 
		"Resultado" 	NVARCHAR(40), 
		"Codigo" 		NVARCHAR(40)
	);

	insert into #RESULTADO_CambiarEstadoTransaccionMasivo 
	SELECT p_tran."IdTransaccion", 'RECHAZO' as "Resultado", 'CAMBIO_ESTADO_INVALIDO' as "Codigo"
	FROM :p_transacciones p_tran;

	UPDATE res
	SET "Resultado"='OK' ,"Codigo" = 'CAMBIO_ESTADO_OK'
	FROM #RESULTADO_CambiarEstadoTransaccionMasivo res INNER JOIN :p_transacciones p_tran on res."IdTransaccion" = p_tran."IdTransaccion"
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on tran."IdTransaccion" = p_tran."IdTransaccion"
	WHERE tran."IdEstado" ='ING' and :p_NuevoEstado = 'ANU';
	
  	UPDATE tran SET "IdEstado" = p_NuevoEstado
  	FROM "BSCL.Orden.BD::ORD.Transaccion" tran INNER JOIN #RESULTADO_CambiarEstadoTransaccionMasivo RES on RES."IdTransaccion" = tran."IdTransaccion"
	WHERE RES."IdTransaccion" = tran."IdTransaccion" AND RES."Resultado" = 'OK';
	
	p_out_SalidaDetalle	= SELECT "IdTransaccion" AS "IdInterno", "IdTransaccion" as "IdOperacion", "Resultado", "Codigo", 'IdTransaccion' as "CodigoDato1",CAST("IdTransaccion" AS NVARCHAR(100)) AS "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2"
						FROM #RESULTADO_CambiarEstadoTransaccionMasivo;
	
	p_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);
	
	DROP TABLE #RESULTADO_CambiarEstadoTransaccionMasivo;

END



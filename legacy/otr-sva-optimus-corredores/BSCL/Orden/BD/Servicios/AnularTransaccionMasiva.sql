SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::AnularTransaccionMasiva";
GO
CREATE PROCEDURE
-- 
--  Proposito: anular transacciones
--  Autor: TEAM ORDENES
--  Fecha Creacion: 07/01/2016
--  
"BSCL.Orden.BD.Servicios::AnularTransaccionMasiva" ( 
    IN p_IdEmpresa                      BIGINT,
    IN p_IdUsuario                      BIGINT,
    IN p_IdModulo                       NVARCHAR(3), 
	
	IN p_transacciones					table ("IdTransaccion" BIGINT,"Motivo" NVARCHAR(5000),"Observaciones" NVARCHAR(5000),"FechaAnulacion" NVARCHAR(19)),
	
	OUT p_out_SalidaDetalle		 		"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))	
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion",'' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" FROM DUMMY WHERE 1=2; 
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :v_Errores,
		
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		
		p_out_ErroresContador = 0;
		p_out_SalidaDetalle =  SELECT * FROM :v_Errores;
		DROP TABLE #RESULTADO_AnularTransaccionMasiva;
	END;
	
	CREATE LOCAL TEMPORARY TABLE #RESULTADO_AnularTransaccionMasiva (
		"IdTransaccion" 		BIGINT,
		"IdTransaccionReal"		BIGINT,
		"IdEstado"				NVARCHAR(3),
		"CantidadAsignaciones"	BIGINT,
		"IdEstadoModulo"		NVARCHAR(3),
		"NombreModulo"			NVARCHAR(100),
		"FechaAnulacion"		SECONDDATE,
		"Motivo"				NVARCHAR(5000),
		"Observaciones"			NVARCHAR(5000),
		"Cumple" 				NVARCHAR(1)
	);
		
	INSERT INTO #RESULTADO_AnularTransaccionMasiva("IdTransaccion","IdTransaccionReal","IdEstado","CantidadAsignaciones","IdEstadoModulo","NombreModulo","FechaAnulacion","Motivo","Observaciones","Cumple")
	SELECT p_tran."IdTransaccion",tran."IdTransaccion" as "IdTransaccionReal",tran."IdEstado",0,CRE."TipoEstado" as "IdEstadoModulo",CRE."Nombre" AS "NombreModulo",
	COALESCE("FechaAnulacion",CURRENT_TIMESTAMP) as "FechaAnulacion",COALESCE("Motivo",'') as "Motivo",COALESCE("Observaciones",'') AS "Observaciones",'S'as "Cumple" 
	FROM :p_transacciones p_tran
	LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON p_tran."IdTransaccion" = tran."IdTransaccion"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE ON CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = :p_IdEmpresa;		
	
	UPDATE anutran
	SET "CantidadAsignaciones" = COALESCE(TT."CantidadAsignaciones",0)
	FROM #RESULTADO_AnularTransaccionMasiva anutran 
	INNER JOIN (
		SELECT tran."IdTransaccion", SUM("CantidadAsignada")  as "CantidadAsignaciones"
		FROM #RESULTADO_AnularTransaccionMasiva anutran 
		INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON anutran."IdTransaccion" = tran."IdTransaccion"
		LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON asig."IdTransaccion" = tran."IdTransaccion" and asig."IdEstadoAsignacion" != 'ANU' 
		GROUP BY tran."IdTransaccion") 
	AS TT ON tt."IdTransaccion" = anutran."IdTransaccion"; 
	
	
	v_validaciones =	SELECT "IdTransaccion",
							CASE WHEN "IdTransaccionReal" IS NULL THEN 'RECHAZO' ELSE 'OK' END AS "TRANSACCION_NO_EXISTE",
							CASE "IdEstado"	WHEN 'ANU' THEN 'RECHAZO' ELSE 'OK' END AS "TRANSACCION_YA_ANULADA",
							CASE WHEN "CantidadAsignaciones" > 0 THEN 'RECHAZO' ELSE 'OK' END AS "TRANSACCION_POSEE_ASIGNACIONES",
							CASE "IdEstadoModulo" WHEN 'C' THEN 'RECHAZO' ELSE 'OK' END AS "MODULO_CERRADO"
							FROM #RESULTADO_AnularTransaccionMasiva;
	
	v_TablaRequisitos =	SELECT "IdTransaccion","Resultado","Codigo"
						FROM (
							select * ,
								COALESCE(
								"1" ||"TRANSACCION_NO_EXISTE", 
								"2" ||"TRANSACCION_YA_ANULADA",
								"3" ||"TRANSACCION_POSEE_ASIGNACIONES",
								"4" ||"MODULO_CERRADO"
								)  
								AS "Resultado",
								COALESCE(
								"1" ||'TRANSACCION_NO_EXISTE', 
								"2" ||'TRANSACCION_YA_ANULADA',
								"3" ||'TRANSACCION_POSEE_ASIGNACIONES',
								"4" ||'MODULO_CERRADO'
								)
								AS "Codigo" 
								FROM :v_validaciones,"BSCL.Plataforma.BD::PLA.Identidad"
						)
						WHERE "Resultado" IS NOT NULL;
	
	v_Errores = SELECT "IdTransaccion" as "IdInterno","IdTransaccion" as "IdOperacion","Resultado","Codigo",'IdTransaccion' as "CodigoDato1",CAST("IdTransaccion" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
				FROM :v_TablaRequisitos val;
	
	
	
	v_Errores = SELECT tran."IdTransaccion" as "IdInterno",tran."IdTransaccion" as "IdOperacion","Resultado","Codigo",
						CASE "Codigo"
							WHEN 'TRANSACCION_NO_EXISTE' THEN 'IdTransaccion'
							WHEN 'TRANSACCION_YA_ANULADA' THEN 'IdTransaccion'
							WHEN 'TRANSACCION_POSEE_ASIGNACIONES' THEN 'IdTransaccion'
							WHEN 'MODULO_CERRADO' THEN 'NombreModulo'
						END AS "CodigoDato1",
						CASE "Codigo"
							WHEN 'TRANSACCION_NO_EXISTE' THEN CAST(tran."IdTransaccion" as NVARCHAR(100))
							WHEN 'TRANSACCION_YA_ANULADA' THEN CAST(tran."IdTransaccion" as NVARCHAR(100))
							WHEN 'TRANSACCION_POSEE_ASIGNACIONES' THEN CAST(tran."IdTransaccion" as NVARCHAR(100))
							WHEN 'MODULO_CERRADO' THEN CAST(tran."NombreModulo" as NVARCHAR(100))
						END AS "ValorDato1",
						'' as "CodigoDato2", '' as "ValorDato2"
				FROM :v_TablaRequisitos req
				INNER JOIN #RESULTADO_AnularTransaccionMasiva tran on tran."IdTransaccion" = req."IdTransaccion";
	
	v_ErroresResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);

	UPDATE tran
	SET "Cumple" = 'N'
	FROM #RESULTADO_AnularTransaccionMasiva tran
	INNER JOIN :v_ErroresResumen a1 on tran."IdTransaccion" = a1."IdInterno" AND "Resultado" = 'RECHAZO';
	
	v_TransaccionesAnular = SELECT p_tran."IdTransaccion"
							FROM #RESULTADO_AnularTransaccionMasiva p_tran WHERE "Cumple" = 'S';
							
	
	
	CALL "BSCL.Orden.BD.Servicios::CambiarEstadoTransaccionMasivo" (
		p_transacciones				=> :v_TransaccionesAnular,
		p_NuevoEstado				=> 'ANU',
   		p_out_SalidaDetalle			=> v_out_SalidaDetalle,
		p_out_SalidaResumen			=> v_out_SalidaResumen);
    	
    UPDATE tran
	SET "Cumple" = 'N'
	FROM #RESULTADO_AnularTransaccionMasiva tran
	INNER JOIN :v_out_SalidaResumen a1 on tran."IdTransaccion" = a1."IdOperacion" AND "Resultado" = 'RECHAZO';
    	
    v_Errores = SELECT * FROM :v_Errores UNION ALL 
    			SELECT * FROM :v_out_SalidaDetalle UNION ALL
 				SELECT "IdTransaccion" as "IdInterno","IdTransaccion" as "IdOperacion",'INFORMACION' AS "Resultado",'TRANSACCION_ANULADA' AS "Codigo",'IdTransaccion' as "CodigoDato1",CAST("IdTransaccion" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
				FROM #RESULTADO_AnularTransaccionMasiva p_tran WHERE "Cumple" = 'S';
		
	SIGNAL SALIDA_CONTROLADA;

END;
GO
SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::AnularMovFacturarAsignacionMasiva";
GO
CREATE PROCEDURE
-- 
--  Proposito: cambio de estado de asignaciones por anulacion de mov a facturar
--  Autor: Renzo Ulloa
--  Fecha Creacion: 20/07/2015 
--  
"BSCL.Orden.BD.Servicios::AnularMovFacturarAsignacionMasiva" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                      BIGINT,
    IN p_IdUsuario                      BIGINT,
    IN p_IdModulo                       NVARCHAR(3),
	
	IN p_asignaciones 					TABLE("IdAsignacion" BIGINT),
	
	OUT p_out_SalidaResumen				"BSCL.Orden.BD.Tipos::SalidaResumen",
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT, "IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT, "IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle";
	DECLARE v_out_InformacionContador	 INT;
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :v_Errores,
			
			p_out_InformacionContador		=> v_out_InformacionContador,
			p_out_InformacionDetalle		=> v_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		

		p_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		
		DROP TABLE #ASIG;
	END;
		
	BEGIN
		
		CREATE LOCAL TEMPORARY TABLE #ASIG (
			"IdAsignacion"				BIGINT,
			"IdEmpresa"					BIGINT,
			"IdAsignacionReal"			BIGINT,
			"IdEstadoAsignacion"		NVARCHAR(3),
			"IdEstadoModulo"			NVARCHAR(3),
			"NombreModulo"				NVARCHAR(100),
			"Cumple" 					NVARCHAR(1)			
		);
		
		INSERT INTO #ASIG("IdAsignacion","IdEmpresa","IdAsignacionReal","IdEstadoAsignacion","IdEstadoModulo","NombreModulo","Cumple")
		SELECT p_asig."IdAsignacion",tran."IdEmpresa",asig."IdAsignacion" AS "IdAsignacionReal", "IdEstadoAsignacion",CRE."TipoEstado" AS "IdEstadoModulo",CRE."Nombre" as "NombreModulo",'S' AS "Cumple"
		FROM :p_asignaciones p_asig
		LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON p_asig."IdAsignacion" = asig."IdAsignacion"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON  tran."IdTransaccion" = asig."IdTransaccion"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE on CRE."IdEmpresa" = tran."IdEmpresa" AND CRE."IdModulo" = 'ORD';
		
		--variable tabla con operaciones fallidas
		--update a tabla de operacion
		--recalculo de errores
		
		v_validaciones =	SELECT "IdAsignacion",
								CASE WHEN "IdAsignacionReal" IS NULL THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_NO_EXISTE",
								CASE "IdEstadoAsignacion" WHEN 'NEF' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_NO_ENVIADA_FACTURAR",
								CASE "IdEstadoAsignacion" WHEN 'ANU' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_YA_ANULADA",
								CASE "IdEstadoModulo" WHEN 'CER' THEN 'RECHAZO' ELSE 'OK' END AS "MODULO_CERRADO"
								FROM #ASIG;
								
		v_TablaRequisitos = 	SELECT "IdAsignacion","Resultado","Codigo"
							FROM (
								select * ,
									COALESCE(
									"1" ||"ASIGNACION_NO_EXISTE", 
									"2" ||"ASIGNACION_NO_ENVIADA_FACTURAR",
									"3" ||"ASIGNACION_YA_ANULADA",
									"4" ||"MODULO_CERRADO"
									)  
									AS "Resultado",
									COALESCE(
									"1" ||'ASIGNACION_NO_EXISTE', 
									"2" ||'ASIGNACION_NO_ENVIADA_FACTURAR',
									"3" ||'ASIGNACION_YA_ANULADA',
									"4" ||'MODULO_CERRADO'
									)  
									AS "Codigo" 
									FROM :v_validaciones,"BSCL.Plataforma.BD::PLA.Identidad"
							)
							WHERE "Resultado" IS NOT NULL;
		
		
		v_Errores = SELECT asig."IdAsignacion" as "IdInterno",asig."IdAsignacion" as "IdOperacion","Resultado","Codigo",
							CASE "Codigo"
								WHEN 'ASIGNACION_NO_EXISTE' THEN 'IdAsignacion'
								WHEN 'ASIGNACION_ENVIADA_FACTURAR' THEN 'IdAsignacion'
								WHEN 'ASIGNACION_YA_ANULADA' THEN 'IdAsignacion'
								WHEN 'MODULO_CERRADO' THEN 'NombreModulo'
							END AS "CodigoDato1",
							CASE "Codigo"
								WHEN 'ASIGNACION_NO_EXISTE' THEN CAST(asig."IdAsignacion" as NVARCHAR(100))
								WHEN 'ASIGNACION_ENVIADA_FACTURAR' THEN CAST(asig."IdAsignacion" as NVARCHAR(100))
								WHEN 'ASIGNACION_YA_ANULADA' THEN CAST(asig."IdAsignacion" as NVARCHAR(100))
								WHEN 'MODULO_CERRADO' THEN CAST(asig."NombreModulo" as NVARCHAR(100))
							END AS "ValorDato1",
							'' as "CodigoDato2", '' as "ValorDato2"
					FROM :v_TablaRequisitos req
					INNER JOIN #ASIG asig on asig."IdAsignacion" = req."IdAsignacion";
		
		v_ErroresResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		
		UPDATE asig
		SET "Cumple" = 'N'
		FROM #ASIG asig
		INNER JOIN :v_ErroresResumen a1 on asig."IdAsignacion" = a1."IdInterno" AND "Resultado" = 'RECHAZO';
		
		v_asignaciones_temp = SELECT "IdAsignacion"
								FROM #ASIG WHERE "Cumple" = 'S';
				
		call "BSCL.Orden.BD.Servicios::CambiarEstadoAsignacionMasivo" (
			p_asignaciones 	=> :v_asignaciones_temp,
			p_NuevoEstado	=> 'NEF',
			p_out_Salida	=> v_out_Salida
		);
		
		v_Errores = SELECT * FROM :v_Errores UNION ALL SELECT * FROM :v_out_Salida;
				
		SIGNAL SALIDA_CONTROLADA;
	
	END;
END;
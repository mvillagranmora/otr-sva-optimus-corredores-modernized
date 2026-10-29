SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::AnularAsignacionMasivaOptimizada";
GO
CREATE PROCEDURE
-- 
--  Proposito: anular asignaciones de forma masiva  (TEMPORAL PARA USAR DESDE JAVA MIENTRAS NO SE MIGRE)
--  Autor: TEAM ORDENES
--  Fecha Creacion: 20/07/2015 
--  
"BSCL.Orden.BD.Servicios::AnularAsignacionMasivaOptimizada" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        	BIGINT,
    IN p_IdUsuario                        	BIGINT,
    IN p_IdModulo                         	NVARCHAR(3),
	
	OUT p_out_OrdenesAnuladas				TABLE("IdOrden"	BIGINT),
	
	OUT	p_out_SalidaDetalle					"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_InformacionContador 			INT,
	OUT p_out_InformacionDetalle			TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_AdvertenciasContador 			INT,
	OUT p_out_AdvertenciasDetalle 			TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno" BIGINT, "IdOperacion" BIGINT,"Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion", '' as "Resultado",'' as "Codigo", '' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	DECLARE v_resultadoRegla	NVARCHAR(40);
	DECLARE v_FechaSistema NVARCHAR(10);
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		
		p_out_ErroresContador = 0;
		p_out_SalidaDetalle =  SELECT * FROM :v_Errores;
		DROP TABLE #ASIG;
		
	END;

	
	BEGIN
		
		CREATE LOCAL TEMPORARY TABLE #ASIG (
			"IdAsignacion" 				BIGINT,
			"IdAsignacionReal" 			BIGINT,
			"IdEstadoAsignacion" 		NVARCHAR(3),
			"IdTipoCanalDistribucion" 	NVARCHAR(3),
			"FechaAsignacion"			SECONDDATE,
			"EsAPlazo"				 	NVARCHAR(1),
			"IdEstadoModulo"			NVARCHAR(3),
			"NombreModulo"				NVARCHAR(100),
			"Cumple"					NVARCHAR(1)
		);
		
		----VALIDACION REQUISITOS BASICOS
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' then
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion", 'RECHAZO' as "Resultado","Codigo", '' as "CodigoDato1",'' as "ValorDato1" ,'' as "CodigoDato2" ,'' as "ValorDato2" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		----FIN VALIDACION REQUISITOS BASICOS	
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		INSERT INTO #ASIG("IdAsignacion","IdAsignacionReal","IdEstadoAsignacion","IdTipoCanalDistribucion","EsAPlazo","FechaAsignacion","Cumple")
		SELECT p_asig."IdAsignacion",asig."IdAsignacion" as "IdAsignacionReal", asig."IdEstadoAsignacion",ord."IdTipoCanalDistribucion",asig."EsAPlazo",asig."FechaAsignacion",'S' as "Cumple"
		FROM "BSCL.Orden.BD::ORD.AnularAsignacionMasivaOptimizada_Entrada_Asignaciones" p_asig
		LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON p_asig."IdAsignacion" = asig."IdAsignacion"
		INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON ord."IdOrden" = asig."IdOrden";
		
		--ASIGNACIONES SIMULTANEA QUE NO ESTEN EN LA LISTA, PERO QUE SE DEBEN ANULAR JUNTAS
		v_AsignacionesSIM = SELECT asig_contado."IdAsignacionRef" as "IdAsignacion"
							FROM #ASIG t_asig
							INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig_contado ON t_asig."IdAsignacion" = asig_contado."IdAsignacion"
							INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON tran."IdTransaccion" = asig_contado."IdTransaccion"
							WHERE (tran."IdTipoInstrumento" = 'SIM' OR tran."IdClasificacionTipoInstrumento" = 'RE') AND asig_contado."IdAsignacionRef" > 0
							EXCEPT
							SELECT "IdAsignacion" FROM #ASIG;
		
		INSERT INTO #ASIG("IdAsignacion","IdAsignacionReal","IdEstadoAsignacion","IdTipoCanalDistribucion","EsAPlazo","FechaAsignacion","Cumple")
		SELECT p_asig."IdAsignacion",asig."IdAsignacion" as "IdAsignacionReal", asig."IdEstadoAsignacion",ord."IdTipoCanalDistribucion",asig."EsAPlazo",asig."FechaAsignacion",'S' as "Cumple"
		FROM :v_AsignacionesSIM p_asig
		LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON p_asig."IdAsignacion" = asig."IdAsignacion"
		INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON ord."IdOrden" = asig."IdOrden";
		
		UPDATE ASIG
		SET "IdEstadoModulo" = CRE."TipoEstado","NombreModulo" = "Nombre"
		FROM #ASIG ASIG
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE ON CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = :p_IdEmpresa;
		
		v_validaciones = SELECT "IdAsignacion",
								CASE WHEN "IdAsignacionReal" IS NULL THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_NO_EXISTE",
								CASE "IdEstadoAsignacion" WHEN 'EAF' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_ENVIADA_FACTURAR",
								CASE "IdEstadoAsignacion" WHEN 'ANU' THEN 'RECHAZO' ELSE 'OK' END AS "ASIGNACION_YA_ANULADA",
								CASE "IdEstadoModulo" WHEN 'C' THEN 'RECHAZO' ELSE 'OK' END AS "MODULO_CERRADO"
								FROM #ASIG;
		
		--SELECT TO_DATE("FechaAsignacion"),TO_DATE(:v_FechaSistema),CASE WHEN TO_DATE("FechaAsignacion") < TO_DATE(:v_FechaSistema) THEN 1 ELSE 0 END ,* FROM #ASIG;
		
		v_TablaRequisitos = SELECT "IdAsignacion","Resultado","Codigo"
							FROM (
								select * ,
									COALESCE(
									"1" ||"ASIGNACION_NO_EXISTE", 
									"2" ||"ASIGNACION_ENVIADA_FACTURAR",
									"3" ||"ASIGNACION_YA_ANULADA",
									"4" ||"MODULO_CERRADO"
									)  
									AS "Resultado",
									COALESCE(
									"1" ||'ASIGNACION_NO_EXISTE', 
									"2" ||'ASIGNACION_ENVIADA_FACTURAR',
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
					INNER JOIN #ASIG asig on asig."IdAsignacion" = req."IdAsignacion"
					--WHERE "Resultado" != 'OK'
					;
		--SELECT * FROM :v_Errores;
		v_ErroresResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		
		UPDATE asig
		SET "Cumple" = 'N'
		FROM #ASIG asig
		INNER JOIN :v_ErroresResumen a1 on asig."IdAsignacion" = a1."IdInterno" AND "Resultado" != 'OK';
		
		v_AsignacionesAnular = select "IdAsignacion" FROM #ASIG WHERE "Cumple" = 'S';
		
		CALL "BSCL.Orden.BD.Servicios::CambiarEstadoAsignacionMasivo" (
			p_asignaciones 	=> :v_AsignacionesAnular,
			p_NuevoEstado	=> 'ANU',
			p_out_Salida	=> v_out_SalidaCambiarEstadoAsignacionMasivo
		);
		
		UPDATE asig
		SET "Cumple" = 'N'
		FROM #ASIG asig
		INNER JOIN :v_out_SalidaCambiarEstadoAsignacionMasivo a1 on asig."IdAsignacion" = a1."IdOperacion" AND "Resultado" = 'RECHAZO';
				
		v_Errores = SELECT * FROM :v_Errores UNION ALL
					SELECT * FROM :v_out_SalidaCambiarEstadoAsignacionMasivo UNION ALL
					SELECT "IdAsignacion" as "IdInterno","IdAsignacion" as "IdOperacion",'INFORMACION' AS "Resultado",'ASIGNACION_ANULADA' AS  "Codigo",'IdAsignacion' as "CodigoDato1",CAST("IdAsignacion" as NVARCHAR(100)) as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2"
					FROM #ASIG asig
					INNER JOIN :v_out_SalidaCambiarEstadoAsignacionMasivo a1 on asig."IdAsignacion" = a1."IdOperacion" AND "Resultado" = 'OK';
		
		
		--SI SON POSTRERAS, CAMBIAMOS EL ESTADO DE LA ORDEN A ANULADA
		v_ordenesPostrerasXAnular = SELECT distinct asig."IdOrden" as "IdInterno",asig."IdOrden"
									FROM #ASIG p_asig
									INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON p_asig."IdAsignacion" = asig."IdAsignacion"
									INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON asig."IdOrden" = ord."IdOrden" and ord."IdTipoCanalIngreso" = 'TRA' AND "Cumple" = 'S';
											
		CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
			p_Ordenes			=> :v_ordenesPostrerasXAnular,
			p_NuevoEstado		=> 'ANU',
			p_out_SalidaDetalle	=> v_resultado_ord_postrera,
			p_out_SalidaResumen	=> v_out_SalidaResumen);
		
		p_out_OrdenesAnuladas = select "IdOperacion" as "IdOrden" from :v_out_SalidaResumen res where res."Resultado" = 'OK';
		
		SIGNAL SALIDA_CONTROLADA;
	
	END;
END;
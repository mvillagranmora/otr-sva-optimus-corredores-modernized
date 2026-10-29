SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoAperturaCiereBloqueo";

CREATE PROCEDURE
--  Proposito: Modificar Estado Cierre, Apertura, Bloqueo Fondo Mutuo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 11/07/2016
"BSCL.FondoMutuo.BD.Servicios::CambiarEstadoAperturaCiereBloqueo"(
	IN 	p_IdEmpresa				   BIGINT,
	IN  p_Fondo			  		   TABLE(
											"IdInterno"			BIGINT,
											"IdFondo"			NVARCHAR(40),
											"Perfil"			NVARCHAR(40),
											"EstadoAporte"		NVARCHAR(1),
											"EstadoRescate"		NVARCHAR(1),
											"EstadoReinversion"	NVARCHAR(1),
											"EstadoLiquidez"	NVARCHAR(1),
											"BloqueoAporte"		NVARCHAR(1),
											"BloqueoRescate"	NVARCHAR(1),
											"BloqueoIndefinido"	NVARCHAR(1),
											"FechaBloqueoDesde"	NVARCHAR(10),
											"FechaBloqueoHasta"	NVARCHAR(10)
										),
	 OUT p_out_Resultado			TABLE("IdInterno" BIGINT, "IdFondo" NVARCHAR(40), "Resultado" NVARCHAR(1), "Codigo" NVARCHAR(40))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_FechaOperacionSecondate 	SECONDDATE;
	
	p_out_Resultado	= SELECT 0 AS "IdInterno", '' AS "IdFondo", '' AS "Resultado", '' AS "Codigo" FROM dummy WHERE 1=2;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);
	--------------------
	--LOGICA 
	--------------------

	CREATE LOCAL TEMPORARY  TABLE #RESULTADO_CambiarAperturaCierreBloqueo (
		"IdInterno" 	BIGINT,
		"IdFondo" 		NVARCHAR(40),
		"Resultado" 	NVARCHAR(1), 
		"Codigo" 		NVARCHAR(40),
		"FechaSistema"	SECONDDATE
	);
	
	INSERT INTO #RESULTADO_CambiarAperturaCierreBloqueo 
	SELECT p_fondo."IdInterno", p_fondo."IdFondo", 'N' as "Resultado", 'CAMBIO_ESTADO_INVALIDO' as "Codigo", :v_FechaOperacionSecondate AS "FechaSistema"
	FROM :p_Fondo p_fondo;
	
	UPDATE AperturaCierre
		SET AperturaCierre."Resultado" = 'S',
			AperturaCierre."Codigo" = 'CAMBIO_ESTADO_OK'
	FROM #RESULTADO_CambiarAperturaCierreBloqueo AS AperturaCierre
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" Fondo ON (Fondo."IdFondo" = AperturaCierre."IdFondo");
	
	UPDATE AperturaCierre
		SET AperturaCierre."Resultado" = 'N',
			AperturaCierre."Codigo" = 'CAMBIO_ESTADO_INVALIDO'
	FROM #RESULTADO_CambiarAperturaCierreBloqueo AS AperturaCierre
	INNER JOIN :p_Fondo AS Estados ON (Estados."IdFondo" = AperturaCierre."IdFondo")
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" Fondo ON (Fondo."IdFondo" = AperturaCierre."IdFondo")
	WHERE (Estados."EstadoAporte" != '' AND Estados."EstadoRescate" != '' AND Estados."EstadoReinversion" != '' AND Estados."EstadoLiquidez" != '')  
		  AND (((Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND (Estados."EstadoAporte" = Fondo."EstadoAporteComercial" AND Estados."EstadoRescate" = Fondo."EstadoRescateComercial" AND Fondo."EstadoReinversionComercial" = Estados."EstadoReinversion" AND Estados."EstadoLiquidez" = Fondo."EstadoLiquidezComercial"))
		  	   OR ((Estados."Perfil" = 'PERFIL_BACKOFFICE'  OR Estados."Perfil" = 'TODOS') AND (Estados."EstadoAporte" = Fondo."EstadoAporteBackoffice" AND Estados."EstadoRescate" = Fondo."EstadoRescateBackoffice" AND Fondo."EstadoReinversionBackoffice" = Estados."EstadoReinversion" AND Estados."EstadoLiquidez" = Fondo."EstadoLiquidezBackoffice"))
		  	  );
	
	UPDATE Fondo
		SET "EstadoAporteComercial" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoAporte" 		!= '' THEN Estados."EstadoAporte" 		ELSE Fondo."EstadoAporteComercial" 			END,
			"EstadoRescateComercial" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoRescate" 		!= '' THEN Estados."EstadoRescate" 		ELSE Fondo."EstadoRescateComercial" 		END,
			"EstadoReinversionComercial" 	= CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoReinversion" 	!= '' THEN Estados."EstadoReinversion" 	ELSE Fondo."EstadoReinversionComercial" 	END,
			"EstadoLiquidezComercial" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoLiquidez" 	!= '' THEN Estados."EstadoLiquidez"     ELSE Fondo."EstadoLiquidezComercial"		END,
			"EstadoAporteBackoffice" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoAporte" 		!= '' THEN Estados."EstadoAporte" 		ELSE Fondo."EstadoAporteBackoffice" 		END,
			"EstadoRescateBackoffice" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoRescate" 		!= '' THEN Estados."EstadoRescate" 		ELSE Fondo."EstadoRescateBackoffice" 		END,
			"EstadoReinversionBackoffice" 	= CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoReinversion" 	!= '' THEN Estados."EstadoReinversion" 	ELSE Fondo."EstadoReinversionBackoffice" 	END,
			"EstadoLiquidezBackoffice" 		= CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoLiquidez" 	!= '' THEN Estados."EstadoLiquidez" 	ELSE Fondo."EstadoLiquidezBackoffice" 		END,
			"BloqueoAporte"					= CASE WHEN Estados."BloqueoAporte" 		!= '' 	THEN Estados."BloqueoAporte" 	 ELSE Fondo."BloqueoAporte" 	END,
			"BloqueoRescate"				= CASE WHEN Estados."BloqueoRescate" 		!= '' 	THEN Estados."BloqueoRescate"    ELSE Fondo."BloqueoRescate"    END,
			"BloqueoIndefinido"				= CASE WHEN Estados."BloqueoIndefinido" 	!= '' 	THEN Estados."BloqueoIndefinido" ELSE Fondo."BloqueoIndefinido" END,
			"FechaBloqueoDesde"				= CASE WHEN Estados."FechaBloqueoDesde" 	!= '' 	THEN Estados."FechaBloqueoDesde" ELSE Fondo."FechaBloqueoDesde" END,
			"FechaBloqueoHasta"				= CASE WHEN Estados."FechaBloqueoHasta" 	!= '' 	THEN Estados."FechaBloqueoHasta" ELSE Fondo."FechaBloqueoHasta" END,
			"UltimoFechaHoraCierreAporteComercial" 		 = CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoAporte" 		= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreAporteComercial" 		END,
			"UltimoFechaHoraCierreRescateComercial" 	 = CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoRescate" 	= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreRescateComercial" 		END,
			"UltimoFechaHoraCierreReinversionComercial"  = CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoRescate" 	= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreReinversionComercial" 	END,
			"UltimoFechaHoraCierreLiquidezComercial" 	 = CASE WHEN (Estados."Perfil" = 'PERFIL_COMERCIAL'  OR Estados."Perfil" = 'TODOS') AND Estados."EstadoLiquidez" 	= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreLiquidezComercial" 		END,
			"UltimoFechaHoraCierreAporteBackOffice" 	 = CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoAporte" 		= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreAporteBackOffice" 		END,
			"UltimoFechaHoraCierreRescateBackOffice" 	 = CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoRescate" 	= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreRescateBackOffice" 		END,
			"UltimoFechaHoraCierreReinversionBackOffice" = CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoReinversion" = 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreReinversionBackOffice" 	END,
			"UltimoFechaHoraCierreLiquidezBackOffice"    = CASE WHEN (Estados."Perfil" = 'PERFIL_BACKOFFICE' OR Estados."Perfil" = 'TODOS') AND Estados."EstadoLiquidez" 	= 'C' THEN RES."FechaSistema" ELSE Fondo."UltimoFechaHoraCierreLiquidezBackOffice" 		END,
			"FechaFondo" = RES."FechaSistema"
	FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo
	INNER JOIN #RESULTADO_CambiarAperturaCierreBloqueo AS RES ON (RES."IdFondo" = Fondo."IdFondo")
	INNER JOIN :p_Fondo AS Estados ON (Estados."IdFondo" = RES."IdFondo")
	WHERE RES."Resultado" = 'S' AND Fondo."IdEmpresa" = :p_IdEmpresa;
	
	p_out_Resultado	= select "IdInterno", "IdFondo", "Resultado", "Codigo" from #RESULTADO_CambiarAperturaCierreBloqueo;
	
	DROP TABLE #RESULTADO_CambiarAperturaCierreBloqueo;

END;
	

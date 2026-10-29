SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AgregarLogAperturaCierreBloqueo";

CREATE PROCEDURE
--  Proposito: Agrega Log pra Apertura, Cierre y Bloqueo de Fondos
--  Autor: Gat-Blac 
--  Fecha Creacion: 20/06/2016
"BSCL.FondoMutuo.BD.Servicios::AgregarLogAperturaCierreBloqueo"(
	IN p_Log	TABLE	(	
							"IdUsuario"								BIGINT,
							"IdEmpresa"								BIGINT,
	                   		"IdFondo"								NVARCHAR(40),
							"FechaSistema"				 			DATE,
							"EstadoAporteComercialAntiguo" 			NVARCHAR(1),
							"EstadoRescateComercialAntiguo"			NVARCHAR(1),
							"EstadoReinversionComercialAntiguo"		NVARCHAR(1),
							"EstadoLiquidezComercialAntiguo"		NVARCHAR(1),
							"EstadoAporteBackofficeAntiguo"			NVARCHAR(1),
							"EstadoRescateBackofficeAntiguo"		NVARCHAR(1),
							"EstadoReinversionBackofficeAntiguo"	NVARCHAR(1),
							"EstadoLiquidezBackofficeAntiguo"		NVARCHAR(1),
							"BloqueoAporteAntiguo"					NVARCHAR(1),
							"BloqueoRescateAntiguo"					NVARCHAR(1),
							"BloqueoIndefinidoAntiguo"				NVARCHAR(1),
							"FechaBloqueoDesdeAntiguo"				DATE,
							"FechaBloqueoHastaAntiguo"				DATE,
							"EstadoAporteComercialNuevo"			NVARCHAR(1),
							"EstadoRescateComercialNuevo"			NVARCHAR(1),
							"EstadoReinversionComercialNuevo"		NVARCHAR(1),
							"EstadoLiquidezComercialNuevo"			NVARCHAR(1),
							"EstadoAporteBackofficeNuevo"			NVARCHAR(1),
							"EstadoRescateBackofficeNuevo"			NVARCHAR(1),
							"EstadoReinversionBackofficeNuevo"		NVARCHAR(1),
							"EstadoLiquidezBackofficeNuevo"			NVARCHAR(1),
							"BloqueoAporteNuevo"					NVARCHAR(1),
							"BloqueoRescateNuevo"					NVARCHAR(1),
							"BloqueoIndefinidoNuevo"				NVARCHAR(1),
							"FechaBloqueoDesdeNuevo"				DATE,
							"FechaBloqueoHastaNuevo"				DATE
						)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL

AS
BEGIN
	--------------------------------------------------------- CONTROL DE ERRORES
	--- Excepcion
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		RESIGNAL; 
	END;
	BEGIN
		v_log = SELECT 
						"BSCL.FondoMutuo.BD.Secuencias::LogAperturaCierreBloqueo".NEXTVAL AS "IdLogAperturaCierreBloqueo"
						,"IdUsuario"
						,"IdEmpresa"
						,"IdFondo"
						,"FechaSistema"
						,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
						,"EstadoAporteComercialAntiguo"
						,"EstadoRescateComercialAntiguo"
						,"EstadoReinversionComercialAntiguo"
						,"EstadoLiquidezComercialAntiguo"
						,"EstadoAporteBackofficeAntiguo"
						,"EstadoRescateBackofficeAntiguo"
						,"EstadoReinversionBackofficeAntiguo"
						,"EstadoLiquidezBackofficeAntiguo"
						,"BloqueoAporteAntiguo"
						,"BloqueoRescateAntiguo"
						,"BloqueoIndefinidoAntiguo"
						,"FechaBloqueoDesdeAntiguo"
						,"FechaBloqueoHastaAntiguo"
						,"EstadoAporteComercialNuevo"
						,"EstadoRescateComercialNuevo"
						,"EstadoReinversionComercialNuevo"
						,"EstadoLiquidezComercialNuevo"
						,"EstadoAporteBackofficeNuevo"
						,"EstadoRescateBackofficeNuevo"
						,"EstadoReinversionBackofficeNuevo"
						,"EstadoLiquidezBackofficeNuevo"
						,"BloqueoAporteNuevo"
						,"BloqueoRescateNuevo"
						,"BloqueoIndefinidoNuevo"
						,COALESCE("FechaBloqueoDesdeNuevo",'0001-01-01') AS "FechaBloqueoDesdeNuevo"
						,COALESCE("FechaBloqueoHastaNuevo",'0001-01-01') AS "FechaBloqueoHastaNuevo"
					FROM :p_Log;
		
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.LogAperturaCierreBloqueo"
			(
				"IdLogAperturaCierreBloqueo", "IdUsuario", "IdEmpresa", "IdFondo", "FechaSistema", "FechaIngresoRegistro", "EstadoAporteComercialAntiguo",
				"EstadoRescateComercialAntiguo", "EstadoReinversionComercialAntiguo", "EstadoLiquidezComercialAntiguo", "EstadoAporteBackofficeAntiguo",
				"EstadoRescateBackofficeAntiguo", "EstadoReinversionBackofficeAntiguo", "EstadoLiquidezBackofficeAntiguo", "BloqueoAporteAntiguo",
				"BloqueoRescateAntiguo", "BloqueoIndefinidoAntiguo", "FechaBloqueoDesdeAntiguo", "FechaBloqueoHastaAntiguo", "EstadoAporteComercialNuevo",
				"EstadoRescateComercialNuevo", "EstadoReinversionComercialNuevo", "EstadoLiquidezComercialNuevo", "EstadoAporteBackofficeNuevo",
				"EstadoRescateBackofficeNuevo", "EstadoReinversionBackofficeNuevo", "EstadoLiquidezBackofficeNuevo", "BloqueoAporteNuevo",
				"BloqueoRescateNuevo", "BloqueoIndefinidoNuevo", "FechaBloqueoDesdeNuevo", "FechaBloqueoHastaNuevo" 
			)
			SELECT "IdLogAperturaCierreBloqueo", "IdUsuario", "IdEmpresa", "IdFondo", "FechaSistema", "FechaIngresoRegistro", "EstadoAporteComercialAntiguo",
				"EstadoRescateComercialAntiguo", "EstadoReinversionComercialAntiguo", "EstadoLiquidezComercialAntiguo", "EstadoAporteBackofficeAntiguo",
				"EstadoRescateBackofficeAntiguo", "EstadoReinversionBackofficeAntiguo", "EstadoLiquidezBackofficeAntiguo", "BloqueoAporteAntiguo",
				"BloqueoRescateAntiguo", "BloqueoIndefinidoAntiguo", "FechaBloqueoDesdeAntiguo", "FechaBloqueoHastaAntiguo", "EstadoAporteComercialNuevo",
				"EstadoRescateComercialNuevo", "EstadoReinversionComercialNuevo", "EstadoLiquidezComercialNuevo", "EstadoAporteBackofficeNuevo",
				"EstadoRescateBackofficeNuevo", "EstadoReinversionBackofficeNuevo", "EstadoLiquidezBackofficeNuevo", "BloqueoAporteNuevo",
				"BloqueoRescateNuevo", "BloqueoIndefinidoNuevo", "FechaBloqueoDesdeNuevo", "FechaBloqueoHastaNuevo" 
			FROM :v_log;
			
	END;
	
END;

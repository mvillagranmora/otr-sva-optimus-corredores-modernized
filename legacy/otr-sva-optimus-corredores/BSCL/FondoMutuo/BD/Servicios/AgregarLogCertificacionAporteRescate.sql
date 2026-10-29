SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AgregarLogCertificacionAporteRescate";

CREATE PROCEDURE
--  Proposito: Agrega Log para Certificacion de aporte y rescates
--  Autor: Gat-Blac 
--  Fecha Creacion: 22/08/2016
"BSCL.FondoMutuo.BD.Servicios::AgregarLogCertificacionAporteRescate"(
	IN p_Log	TABLE	(	
							"IdSolicitudAporteRescate"		BIGINT,
							"Codigo"						NVARCHAR(100),
							"IdEstado"						NVARCHAR(3),
							"TipoSolicitud"					NVARCHAR(3),
							"IdTipoMovimientoFondoMutuo"	NVARCHAR(10),
							"IdUsuario"						BIGINT,
							"IdEmpresa"						BIGINT,
							"FechaSistema"					DATE,
							"IdSerieFondo"					NVARCHAR(40),
							"IdEmisor"						NVARCHAR(40),
							"Monto"							DECIMAL(26,8),
							"IdMoneda"						NVARCHAR(3),
							"MontoOriginal"					DECIMAL(26,8),
							"TipoCambio"					DECIMAL(26,8),
							"IdMonedaOriginal"				NVARCHAR(3),
							"ValorCuota"					DECIMAL(26,8),
							"NumeroCuotas"					DECIMAL(26,8),
							"IdCliente"						BIGINT,
							"IdPortafolio"					BIGINT,
							"Simulacion"					NVARCHAR(1)
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
						"BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate".NEXTVAL AS "IdLogCertificacionAporteRescate"
						,"IdSolicitudAporteRescate"
						,"Codigo"
						,"IdEstado"
						,"TipoSolicitud"
						,"IdTipoMovimientoFondoMutuo"
						,"IdUsuario"
						,"IdEmpresa"
						,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
						,"FechaSistema"
						,"IdSerieFondo"
						,"IdEmisor"
						,"Monto"
						,"IdMoneda"
						,"MontoOriginal"
						,"TipoCambio"
						,"IdMonedaOriginal"
						,"ValorCuota"
						,"NumeroCuotas"
						,"IdCliente"
						,"IdPortafolio"
						,"Simulacion"
					FROM :p_Log;
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate"
			(
				"IdLogCertificacionAporteRescate", "IdSolicitudAporteRescate", "Codigo", "IdEstado", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "IdUsuario", "IdEmpresa", "FechaIngresoRegistro", "FechaSistema",
				"IdSerieFondo", "IdEmisor", "Monto", "IdMoneda", "MontoOriginal", "TipoCambio", "IdMonedaOriginal", "ValorCuota", "NumeroCuotas", "IdCliente", "IdPortafolio","Simulacion"
			)
			SELECT "IdLogCertificacionAporteRescate", "IdSolicitudAporteRescate", "Codigo", "IdEstado", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "IdUsuario", "IdEmpresa", "FechaIngresoRegistro", "FechaSistema",
				"IdSerieFondo", "IdEmisor", "Monto", "IdMoneda", "MontoOriginal", "TipoCambio", "IdMonedaOriginal", "ValorCuota", "NumeroCuotas", "IdCliente", "IdPortafolio","Simulacion" 
			FROM :v_log;
			
	END;
END;

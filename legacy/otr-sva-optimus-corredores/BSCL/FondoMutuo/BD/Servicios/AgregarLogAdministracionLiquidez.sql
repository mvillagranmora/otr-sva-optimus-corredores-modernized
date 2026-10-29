SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::AgregarLogAdministracionLiquidez";

CREATE PROCEDURE
--  Proposito: Agrega Log para Administracion de Liquidez
--  Autor: Gat-Blac 
--  Fecha Creacion: 14/12/2016
"BSCL.FondoMutuo.BD.Servicios::AgregarLogAdministracionLiquidez"(
	IN p_Log	TABLE(
					"IdCliente"					BIGINT,
					"IdPortafolio" 				BIGINT,
					"IdSerieFondo"				NVARCHAR(40),
					"IdMoneda"					NVARCHAR(3),
					"Saldo"						DECIMAL(26,8),
					"MontoMinimo"				DECIMAL(26,8),
					"IdSolicitudAporteRescate"	BIGINT,
					"FechaIngreso"				DATE,
					"Codigo"					NVARCHAR(100),
					"IdEmpresa"					BIGINT
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
						"BSCL.FondoMutuo.BD.Secuencias::LogAdministracionLiquidez".NEXTVAL AS "IdLogAdministracionLiquidez"
						,"IdCliente"
						,"IdPortafolio"
						,"IdSerieFondo"
						,"IdMoneda"
						,"Saldo"
						,"MontoMinimo"
						,"IdSolicitudAporteRescate"
						,"FechaIngreso"
						,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
						,"Codigo"
						,"IdEmpresa"
					FROM :p_Log;
		
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.LogAdministracionLiquidez"
			(
				"IdLogAdministracionLiquidez","IdCliente","IdPortafolio","IdSerieFondo","IdMoneda","Saldo","MontoMinimo","IdSolicitudAporteRescate",
				"FechaIngreso","FechaIngresoRegistro","Codigo","IdEmpresa"
			)
			SELECT "IdLogAdministracionLiquidez","IdCliente","IdPortafolio","IdSerieFondo","IdMoneda","Saldo","MontoMinimo","IdSolicitudAporteRescate",
				   "FechaIngreso","FechaIngresoRegistro","Codigo","IdEmpresa"
			FROM :v_log;
			
	END;
	
END;

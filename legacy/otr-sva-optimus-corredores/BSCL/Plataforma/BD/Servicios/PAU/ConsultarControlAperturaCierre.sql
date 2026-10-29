SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.ConsultarControlAperturaCierre";
GO
--  Proposito: Consulta de logs de control para apertura y cierre de modulos.
--  Autor: Gat-Blac 
--  Fecha Creacion: 23/06/23

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.ConsultarControlAperturaCierre" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa                		BIGINT,
    IN p_IdUsuario                		BIGINT,
    IN p_IdModulo                 		NVARCHAR(3), 
  
	IN p_FechaSistema					NVARCHAR(10),
	IN p_CodigoModulo 					NVARCHAR(10),
	IN p_TipoProceso 					NVARCHAR(15),
		
	-- Parametros de Salida
   	OUT p_out_tbl table (
   		 "IdEmpresa" 			BIGINT
   		,"NombreModulo" 		NVARCHAR(100)
   		,"CodigoProceso" 		NVARCHAR(100)
   		,"NombreEstadoProceso" 	NVARCHAR(100)
   		,"EstadoProceso" 		NVARCHAR(100)
  		,"FechaSistema" 		NVARCHAR(10)
  		,"FechaEjecucion" 		TIMESTAMP
  		,"DescripcionProceso" 	NVARCHAR(100)
  		,"CodigoModulo" 		NVARCHAR(100)
  		,"CodigoUsuario" 		NVARCHAR(40)
  		,"TipoProceso" 			NVARCHAR(100)
  		,"Orden" 				BIGINT
		,"ResultadoProceso"     NVARCHAR(100)
   	  )
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	tbl_max_sis_ejecutados = SELECT P."IdEmpresa", 
						   			MODULO."CodigoModulo",
								   	P."CodigoProceso",
								   	MAX(CP."FechaRegistro") AS "FechaEjecucion"
							FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
							JOIN "BSCL.Plataforma.BD::PAU.Proceso" P ON (P."CodigoModulo" = MODULO."CodigoModulo") 
							JOIN "BSCL.Plataforma.BD::PAU.ControlProceso" CP ON (CP."CodigoModulo" = P."CodigoModulo" AND CP."CodigoProceso" = P."CodigoProceso")
							JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo")
							WHERE CP."FechaSistema" = :p_FechaSistema AND P."CodigoModulo" != 'SIS'
							GROUP BY MODULO."CodigoModulo",P."CodigoProceso",P."IdEmpresa";
	
	tbl_ejecutados = SELECT P."IdEmpresa", 
						   	MODULOPLA."Nombre" AS "NombreModulo",
						   	P."CodigoProceso", 
						   	'EJE' AS "EstadoProceso",
						   	CP."FechaSistema",
						   	CP."FechaRegistro" AS "FechaEjecucion",
						   	P."DescripcionProceso",
						   	MODULO."CodigoModulo",
						   	CP."CodigoUsuario",
						   	CASE P."EsApertura" WHEN 'S' THEN 'APERTURA' ELSE 'CIERRE' END AS "TipoProceso",
						   	P."Orden",
							CP."ResultadoProceso"
					FROM "BSCL.Plataforma.BD::PAU.Modulo" MODULO
					JOIN "BSCL.Plataforma.BD::PAU.Proceso" P ON (P."CodigoModulo" = MODULO."CodigoModulo") 
					JOIN "BSCL.Plataforma.BD::PAU.ControlProceso" CP ON (CP."CodigoModulo" = P."CodigoModulo" AND CP."CodigoProceso" = P."CodigoProceso")
					JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo")
					JOIN :tbl_max_sis_ejecutados EJECUTADOS ON (EJECUTADOS."CodigoProceso" = P."CodigoProceso" 
																AND MODULO."CodigoModulo" = EJECUTADOS."CodigoModulo" 
																AND P."IdEmpresa" = EJECUTADOS."IdEmpresa"
																AND CP."FechaRegistro" = EJECUTADOS."FechaEjecucion")
					WHERE CP."FechaSistema" = :p_FechaSistema AND P."CodigoModulo" != 'SIS';
							
	
	p_tbl_noejecutados = SELECT DISTINCT  P."IdEmpresa", 
						   	MODULOPLA."Nombre" AS "NombreModulo",
						   	P."CodigoProceso", 
						   	'NEJ' AS "EstadoProceso",
						   	:p_FechaSistema AS "FechaSistema",
						   	'0001-01-01 00:00:00' AS "FechaEjecucion",
						   	P."DescripcionProceso",
						   	MODULO."CodigoModulo",
						   	'' AS "CodigoUsuario",
						   	CASE P."EsApertura" WHEN 'S' THEN 'APERTURA' ELSE 'CIERRE' END AS "TipoProceso",
						   	P."Orden",
							'' as "ResultadoProceso"
	                     FROM "BSCL.Plataforma.BD::PAU.Proceso" P
	                     JOIN "BSCL.Plataforma.BD::PAU.Modulo" MODULO ON (P."CodigoModulo" = MODULO."CodigoModulo") 
						 JOIN "BSCL.Plataforma.BD::PLA.Modulo" MODULOPLA ON (MODULOPLA."IdModulo" = MODULO."CodigoModulo")
						 WHERE P."CodigoModulo" != 'SIS' AND NOT EXISTS (SELECT 1 FROM :tbl_ejecutados EJECUTADOS 
						 										WHERE EJECUTADOS."CodigoProceso" = P."CodigoProceso" 
																AND MODULO."CodigoModulo" = EJECUTADOS."CodigoModulo" 
																AND P."IdEmpresa" = EJECUTADOS."IdEmpresa");
							
	p_out_tbl = SELECT * FROM (SELECT A."IdEmpresa", 
					   A."NombreModulo", 
					   A."CodigoProceso",
				       ESTADO."Nombre" AS "NombreEstadoProceso",
				       A."EstadoProceso",
				       A."FechaSistema",
				       A."FechaEjecucion",
				       A."DescripcionProceso",
				       A."CodigoModulo",
				       A."CodigoUsuario",
				       A."TipoProceso",
				       A."Orden",
					   A."ResultadoProceso"
				      FROM :tbl_ejecutados A
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON (ESTADO."IdEstado" = a."EstadoProceso")
				WHERE (:p_TipoProceso = A."TipoProceso" OR :p_TipoProceso = '') 
					  AND (:p_CodigoModulo = A."CodigoModulo" OR :p_CodigoModulo = '')
				UNION ALL 
				SELECT A."IdEmpresa", 
					   A."NombreModulo", 
					   A."CodigoProceso",
				       ESTADO."Nombre" AS "NombreEstadoProceso",
				       A."EstadoProceso",
				       A."FechaSistema",
				       A."FechaEjecucion",
				       A."DescripcionProceso",
				       A."CodigoModulo",
				       A."CodigoUsuario",
				       A."TipoProceso",
				       A."Orden",
					   A."ResultadoProceso"
				      FROM :p_tbl_noejecutados A
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON (ESTADO."IdEstado" = a."EstadoProceso")
				WHERE (:p_TipoProceso = A."TipoProceso" OR :p_TipoProceso = '')
					  AND (:p_CodigoModulo = A."CodigoModulo" OR :p_CodigoModulo = '')) A
				ORDER BY A."Orden" ASC;
	
END;
GO
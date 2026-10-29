SET SCHEMA "BSCL";

GO

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.ConsultarPauta";

GO

--  Proposito: Consulta para pauta nocturna.
--  Autor: Gat-Blac 
--  Fecha Creacion: 06-07-2017

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PAU.ConsultarPauta" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa                		BIGINT,
    IN p_IdUsuario                		BIGINT,
    IN p_IdModulo                 		NVARCHAR(3), 
  
	IN p_FechaEjecucion					NVARCHAR(10),
		
	OUT p_out_tbl table (
   		 "IdEmpresa" 		BIGINT,
   		 "RutaLog"			NVARCHAR(100),
   		 "FechaSistema"		NVARCHAR(10),
   		 "CodigoProceso"	NVARCHAR(40),
   		 "Descripcion"		NVARCHAR(500),
   		 "FechaFin"			SECONDDATE,
   		 "Estado"			NVARCHAR(3),
   		 "FechaInicio"		SECONDDATE,
   		 "Orden"			BIGINT,
   		 "Mensaje"			NVARCHAR(500),
   		 "IdEjecucion"		BIGINT
   		 
   	  )
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	p_out_tbl = SELECT PROC."IdEmpresa",EJE."RutaLog",EJE."FechaSistema",PROC."CodigoProceso",TIPO."Descripcion",EJEDET."FechaFin",
					EJEDET."Estado",EJEDET."FechaInicio",PROC."Orden",
					EJEDET."Mensaje", EJEDET."IdEjecucion"
					FROM "BSCL.Plataforma.BD::PAU.Ejecucion" EJE
					LEFT JOIN "BSCL.Plataforma.BD::PAU.EjecucionDetalle" EJEDET ON EJE."FechaSistema" = EJEDET."FechaSistema" AND EJE."IdEmpresa" = EJEDET."IdEmpresa"
					LEFT JOIN "BSCL.Plataforma.BD::PAU.Proceso" PROC ON PROC."CodigoProceso" = EJEDET."CodigoProceso"
					LEFT JOIN "BSCL.Plataforma.BD::BAT.TipoPrograma" TIPO on TIPO."IdTipoPrograma" = PROC."CodigoProceso"
					WHERE PROC."IdEmpresa" = :p_IdEmpresa AND EJE."FechaSistema" = :p_FechaEjecucion
					AND PROC."CodigoModulo" = 'SIS'
					ORDER BY "Orden";
					
	
	
END;

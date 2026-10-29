SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaRelacionAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaRelacionAnalitica" AS
SELECT 								
		 CASE WHEN "IdPersonaOrigen" = 0 THEN NULL ELSE "IdPersonaOrigen" END AS "IdPersonaOrigen"
		,CASE WHEN "IdRelacionPersona" = 0 THEN -1 ELSE "IdRelacionPersona" END AS "IdRelacionPersona" 
		,CASE WHEN "IdPersonaDestino" = 0 THEN NULL ELSE "IdPersonaDestino" END AS "IdPersonaDestino"
		,TO_CHAR(IFNULL("FechaNacimiento",'0001-01-01')) AS "FechaNacimiento"
		,CASE WHEN "IdPaisResidencia" = 0 THEN NULL ELSE "IdPaisResidencia" END AS "IdPaisResidencia" 
		,"IdEmpresa"
		,CAST("Administracion" AS NVARCHAR(40)) AS "Administracion"
		,"IdTipoRelacion"
		,CAST("NombreTipoRelacion" AS NVARCHAR(100)) AS "NombreTipoRelacion"
		,CASE WHEN IFNULL("FechaInicioRelacion",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  "FechaInicioRelacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS ("FechaInicioRelacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaInicioRelacion"
		,CASE WHEN "IdEstado" = '' THEN NULL ELSE "IdEstado" END AS "IdEstado"
		,CASE WHEN IFNULL("FechaFinRelacion",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  "FechaFinRelacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS ("FechaFinRelacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaFinRelacion"
		,CAST("NombreEstado" AS NVARCHAR(100)) AS "NombreEstado"
		,"CodigoTipoPersona"
		,CAST("DocumentoIdentidadDefecto" AS NVARCHAR(40)) AS "DocumentoIdentidadDefecto"
		,CAST("NombreCompleto" AS NVARCHAR(302)) AS "NombreCompleto" 
		,CAST("RazonSocial" AS NVARCHAR(100)) AS "RazonSocial" 
		,CAST("Alias" AS NVARCHAR(100)) AS "Alias"
		,CAST("Nombres" AS NVARCHAR(100)) AS "Nombres"
		,CAST("ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno"
		,CAST("ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno"
		,"IdTipoDocumentoIdentidadDefecto"
		,CAST("CondicionesRelacion" AS NVARCHAR(255)) AS "CondicionesRelacion"
		from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
		CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
		            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaRelacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
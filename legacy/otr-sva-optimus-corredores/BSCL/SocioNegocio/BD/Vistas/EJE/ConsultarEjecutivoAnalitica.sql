SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoAnalitica" AS
SELECT 	  
		EJE."IdEmpleadoEjecutivo"
		,CASE WHEN EJE."IdEmpleadoSupervisor" = 0 THEN NULL ELSE EJE."IdEmpleadoSupervisor" END AS "IdEmpleadoSupervisor" 
		,CASE WHEN IFNULL(EJE."FechaCreacion",'')= '' THEN '0001-01-01 00:00:00' 
		 	                              ELSE  
		 	                                  CASE WHEN  EJE."FechaCreacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
		 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EJE."FechaCreacion", CONFIGURACION."Valor")),19)
		 	                                  END 
		 END AS "FechaCreacion"           
		,CASE WHEN EJE."IdUsuarioCreacion" = 0 THEN NULL ELSE EJE."IdUsuarioCreacion" END AS "IdUsuarioCreacion"
		,CASE WHEN EJE."IdEstado" = '' THEN NULL ELSE EJE."IdEstado" END AS "IdEstado"
		,CASE WHEN EJE."IdEmpleadoReemplaza" = 0 THEN NULL ELSE EJE."IdEmpleadoReemplaza" END as "IdEmpleadoReemplaza"
		,CASE WHEN IFNULL(EJE."FechaInicioReemplazo",'')= '' THEN '0001-01-01 00:00:00' 
		 	                              ELSE  
		 	                                  CASE WHEN  EJE."FechaInicioReemplazo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
		 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EJE."FechaInicioReemplazo", CONFIGURACION."Valor")),19)
		 	                                  END 
		 END AS "FechaInicioReemplazo"
		,CASE WHEN IFNULL(EJE."FechaFinReemplazo",'')= '' THEN '0001-01-01 00:00:00' 
		 	                              ELSE  
		 	                                  CASE WHEN EJE."FechaFinReemplazo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
		 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (EJE."FechaFinReemplazo", CONFIGURACION."Valor")),19)
		 	                                  END 
		  END AS "FechaFinReemplazo"
		 ,CAST(COALESCE(PER1."NombreCompleto",'') AS NVARCHAR(302)) AS "NombreCompletoSupevisor"
		 ,CAST(COALESCE(EMP1."CodigoEmpleado",'') AS NVARCHAR(40)) AS "CodigoEmpleadoSupervisor"
		 ,CASE WHEN EMP2."IdPersona" = 0 THEN NULL ELSE EMP2."IdPersona" END as "IdPersonaEmpleado"
		 
		 ,CAST(COALESCE(EMP2."CodigoEmpleado",'') AS NVARCHAR(40)) AS "CodigoEmpleado"
		 ,CAST(COALESCE(PER2."NombreCompleto",'') AS NVARCHAR(302)) AS "NombreCompleto"
		 
		 ,CAST(COALESCE(EMP3."CodigoEmpleado",'') AS NVARCHAR(40)) AS "CodigoEmpleadoReemplazante"
		 ,CAST(COALESCE(PER3."NombreCompleto",'') AS NVARCHAR(302)) as "NombreCompletoReemplazante"
		  
		from "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE
		inner join "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP1
			on EMP1."IdEmpleado" = EJE."IdEmpleadoSupervisor"
		left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER1
		    on PER1."IdPersona" = EMP1."IdPersona"
		left join "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP2
			on EMP2."IdEmpleado" = EJE."IdEmpleadoEjecutivo"
		inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER2
		    on PER2."IdPersona" = EMP2."IdPersona"
		left join "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP3
			on EMP3."IdEmpleado" = EJE."IdEmpleadoReemplaza"
		left join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER3
		    on PER3."IdPersona" = EMP3."IdPersona"   
		CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
							            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
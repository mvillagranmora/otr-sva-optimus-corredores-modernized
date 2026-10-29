SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica" AS
SELECT 
		 CLI."IdCliente"
		,CASE WHEN CLI."IdPersona" = 0 THEN NULL ELSE CLI."IdPersona" END AS "IdPersona" 
		,CASE WHEN CLI."IdEmpresa" = 0 THEN NULL ELSE CLI."IdEmpresa" END AS "IdEmpresa" 
		,CASE WHEN CLI."IdEstado" = '' THEN NULL ELSE CLI."IdEstado" END AS "IdEstado" 
		,CASE WHEN CLI."IdSegmentoCliente" = 0  THEN NULL ELSE CLI."IdSegmentoCliente" END as "IdSegmentoCliente"
		,CASE WHEN CLI."IdCategoriaInversionista" = '' THEN NULL ELSE CAST(CLI."IdCategoriaInversionista" AS NVARCHAR(40)) END as "IdCategoriaInversionista"
		,CASE WHEN CLI."IdTipoBloqueo" = '' THEN NULL ELSE CAST(CLI."IdTipoBloqueo" AS NVARCHAR(40)) END AS "IdTipoBloqueo"
		,CASE WHEN IFNULL(CLI."FechaBloqueo",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  CLI."FechaBloqueo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CLI."FechaBloqueo", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaBloqueo"
		,CAST(CLI."ComentarioBloqueo" AS NVARCHAR(520)) AS "ComentarioBloqueo"
		,CASE WHEN IFNULL(CLI."FechaIngreso",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  CLI."FechaIngreso" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CLI."FechaIngreso", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaIngreso"
 	        ,CASE WHEN IFNULL(CLI."FechaCreacion",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  CLI."FechaCreacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CLI."FechaCreacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaCreacion"
		,CASE WHEN IFNULL(CLI."FechaModificacion",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  CLI."FechaModificacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CLI."FechaModificacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaModificacion"
		,CASE WHEN CLI."IdUsuarioCreador" = 0 THEN NULL ELSE CLI."IdUsuarioCreador" END AS "IdUsuarioCreador"
		,CASE WHEN CLI."IdPortafolioDefecto" = 0 THEN NULL ELSE CLI."IdPortafolioDefecto" END AS "IdPortafolioDefecto"  
		,CASE WHEN CLI."IdTipoDesbloqueo" = '' THEN NULL ELSE CAST(CLI."IdTipoDesbloqueo" AS NVARCHAR (40)) END AS "IdTipoDesbloqueo"
		,CASE WHEN IFNULL(CLI."FechaDesbloqueo",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  CLI."FechaDesbloqueo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (CLI."FechaDesbloqueo", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaDesbloqueo"
		,CAST(CLI."ComentarioDesbloqueo" AS NVARCHAR(520)) AS "ComentarioDesbloqueo"
		,CASE WHEN CLI."IdPersonaEjecutivo" = 0 THEN NULL ELSE CLI."IdPersonaEjecutivo" END AS "IdPersonaEjecutivo" 
		,CAST(CLI."CodigoEjecutivoExterno" AS NVARCHAR(40)) AS "CodigoEjecutivoExterno"
		,CASE WHEN CLI."IdTipoCliente" = '' THEN 'RETAIL' ELSE CAST(CLI."IdTipoCliente"  AS NVARCHAR(40)) END AS "IdTipoCliente"
		,CASE WHEN PER."IdDocumentoIdentidad" = 0 THEN NULL ELSE PER."IdDocumentoIdentidad" END AS "IdDocumentoIdentidad" 
		,PER."Tipo"
		,CAST(PER."Nombres" AS NVARCHAR(100)) AS "Nombres"
		,CAST(PER."ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno"
		,CAST(PER."ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno"
		,CAST(PER."RazonSocial" AS NVARCHAR(100)) AS "RazonSocial"
		,CASE WHEN PER."IdGiroPrincipal" = 0 THEN NULL ELSE PER."IdGiroPrincipal" END AS "IdGiroPrincipal" 
		,CASE WHEN PER."IdPaisNacimiento" = 0 THEN NULL ELSE PER."IdPaisNacimiento" END  AS "IdPaisNacimiento" 
		,CASE WHEN PER."IdPaisResidencia" = 0 THEN NULL ELSE PER."IdPaisResidencia" END AS "IdPaisResidencia" 
		,IFNULL(TO_CHAR(PER."FechaNacimiento"),'0001-01-01') AS "FechaNacimiento"
		,IFNULL(TO_CHAR(PER."FechaDefuncion"),'0001-01-01') AS "FechaDefuncion"
		,PER."Sexo"
		,PER."EsExtranjero"
		,PER."EstadoCivil"
		,PER."RegimenConyugal"
		,CAST(CARGO."NombreTipoCargoPersona" AS NVARCHAR(100)) AS "Cargo"
		,CAST(PER."Empleador" AS NVARCHAR(100)) AS  "Empleador"
		,CASE WHEN PER."IdDireccionFisicaPrincipal" = 0 THEN NULL ELSE PER."IdDireccionFisicaPrincipal" END  AS "IdDireccionFisicaPrincipal"
		,CASE WHEN PER."IdDireccionElectronicaPrincipal" = 0 THEN NULL ELSE PER."IdDireccionElectronicaPrincipal" END AS "IdDireccionElectronicaPrincipal"
		,CASE WHEN PER."IdTelefonoPrincipal" = 0 THEN NULL ELSE PER."IdTelefonoPrincipal" END AS "IdTelefonoPrincipal" 
		,CASE WHEN PER."IdProfesion" = 0 THEN NULL ELSE PER."IdProfesion" END AS "IdProfesion" 
		,CASE WHEN IFNULL(PER."FechaInicioActividad",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  PER."FechaInicioActividad" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (PER."FechaInicioActividad", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaInicioActividad"
		,PER."EsEnrolado"
		,PER."IdCarga"
		,CAST(PER."Alias" AS NVARCHAR(100)) AS "Alias"
		,CASE WHEN PER."IdOrigen" = 0 THEN NULL ELSE PER."IdOrigen" END  AS "IdOrigen"
		,CLI."Borrado"
		,CASE WHEN PER."IdPersonaConyuge" = 0 THEN NULL ELSE PER."IdPersonaConyuge" END AS "IdPersonaConyuge"
		,CASE WHEN PER."IdPersonaRepresentante" = 0 THEN NULL ELSE PER."IdPersonaRepresentante" END  AS "IdPersonaRepresentante" 
		,CASE WHEN PER."IdPaisNacionalidadPrincipal" = 0 THEN NULL ELSE PER."IdPaisNacionalidadPrincipal" END AS "IdPaisNacionalidadPrincipal" 
		,CAST(PER."GlobalLegalEntity" AS NVARCHAR(520)) AS "GlobalLegalEntity"
		,CASE WHEN PER."IdPersonaEmpleador" = 0 THEN NULL ELSE PER."IdPersonaEmpleador" END  AS "IdPersonaEmpleador" 
		,CASE WHEN IFNULL(PER."FechaInicioConyuge",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  PER."FechaInicioConyuge" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (PER."FechaInicioConyuge", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaInicioConyuge"
 	        ,CASE WHEN IFNULL(PER."FechaInicioRepresentante",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  PER."FechaInicioRepresentante" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (PER."FechaInicioRepresentante", CONFIGURACION."Valor")),19)
 	                                  END 
 	         END AS "FechaInicioRepresentante"
		,CAST(PER."DetalleCargo" AS NVARCHAR (40)) AS "DetalleCargo"
		,CASE WHEN NFANTA."IdNombreFantasia" = 0 THEN NULL ELSE NFANTA."IdNombreFantasia" END  as "IdNombreFantasiaPrincipal"
		,CASE WHEN EMP."IdEmpleado" = 0 THEN NULL ELSE EMP."IdEmpleado" END as "IdEmpleado"
		,CASE WHEN EMP."CodigoEmpleado" = '' THEN NULL ELSE CAST(EMP."CodigoEmpleado" AS NVARCHAR(40)) END as "CodigoEmpleado"
		,PER."CodigoTipoCargoEmpleado"  as "CodigoTipoCargoEmpleado"
		,CAST(SEG."DescripcionSegmento" AS NVARCHAR(40)) AS "DescripcionSegmento" 
		from "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
		inner join "BSCL.SocioNegocio.BD::PER.Persona" AS PER
			ON PER."IdPersona" =  CLI."IdPersona"
		left join "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP 
			ON EMP."IdPersona" = PER."IdPersona" 
		left join "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NFANTA
			ON NFANTA."IdNombreFantasia" = PER."IdNombreFantasiaPrincipal"
		left join "BSCL.SocioNegocio.BD::CLI.TipoSegmento" as SEG
			on SEG."IdSegmentoCliente" = CLI."IdSegmentoCliente"	
		left join (SELECT "NombreTipoCargoPersona","CodigoTipoCargoPersona",R FROM 
			(SELECT "NombreTipoCargoPersona","CodigoTipoCargoPersona", ROW_NUMBER() OVER (PARTITION BY "CodigoTipoCargoPersona") AS R
			FROM "BSCL.SocioNegocio.BD::PER.TipoCargoPersona" CARGO) WHERE R = 1) CARGO 
			ON PER."CodigoTipoCargoEmpleado" = CARGO."CodigoTipoCargoPersona"
		CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
		            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

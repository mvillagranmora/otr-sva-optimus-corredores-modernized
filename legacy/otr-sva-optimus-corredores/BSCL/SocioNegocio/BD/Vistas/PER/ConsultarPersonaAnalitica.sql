SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaAnalitica"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaAnalitica" AS
SELECT 
		 PER."IdPersona"
		,CASE WHEN PER."IdEstado" = '' THEN NULL ELSE PER."IdEstado" END AS "IdEstadoPersona"
		,CASE WHEN PER."IdDocumentoIdentidad" = 0 THEN NULL ELSE PER."IdDocumentoIdentidad" END AS "IdDocumentoIdentidad" 
		,PER."Tipo" as "TipoPersona"
		,CAST(PER."Nombres" AS NVARCHAR(100)) AS "Nombres"
		,CAST(PER."ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno"
		,CAST(PER."ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno"
		,CAST(PER."RazonSocial" AS NVARCHAR(100)) AS "RazonSocial"
		,CASE WHEN PER."IdGiroPrincipal" = 0 THEN NULL ELSE PER."IdGiroPrincipal" END AS "IdGiroPrincipal" 
		,CASE WHEN PER."IdPaisNacimiento" = 0 THEN NULL ELSE PER."IdPaisNacimiento" END AS "IdPaisNacimiento" 
		,CASE WHEN PER."IdPaisResidencia" = 0 THEN NULL ELSE PER."IdPaisResidencia" END AS "IdPaisResidencia"
		,IFNULL(TO_CHAR(PER."FechaNacimiento"),'0001-01-01') AS "FechaNacimiento"
		,CASE WHEN PER."FechaDefuncion" = '0001-01-01' THEN NULL WHEN PER."FechaDefuncion" IS NULL THEN NULL ELSE TO_CHAR(PER."FechaDefuncion") END AS "FechaDefuncion"
		,coalesce(PER."Sexo", '') as "Sexo"
		,coalesce(PER."EsExtranjero",'') as "EsExtranjero"
		,coalesce (PER."EstadoCivil" ,'') AS "EstadoCivil"
		,PER."RegimenConyugal"
		,CAST(PER."Cargo" AS NVARCHAR(100)) AS "Cargo"
		,CAST(PER."Empleador" AS NVARCHAR(100)) AS "Empleador"
		,CASE WHEN PER."IdDireccionFisicaPrincipal" = 0 THEN NULL ELSE PER."IdDireccionFisicaPrincipal" END AS "IdDireccionFisicaPrincipal" 
		,CASE WHEN PER."IdDireccionElectronicaPrincipal" = 0 THEN NULL ELSE PER."IdDireccionElectronicaPrincipal" END AS "IdDireccionElectronicaPrincipal"
		,CASE WHEN PER."IdTelefonoPrincipal" = 0 THEN NULL ELSE PER."IdTelefonoPrincipal" END AS "IdTelefonoPrincipal" 
		,CASE WHEN PER."IdProfesion" = 0 THEN NULL ELSE PER."IdProfesion" END  AS "IdProfesion" 
		,CASE WHEN IFNULL(PER."FechaInicioActividad",'')= '' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  PER."FechaInicioActividad" = '0001-01-01 00:00:00' THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (PER."FechaInicioActividad", CONFIGURACION."Valor")),19)
 	                                  END 
 	                     END AS "FechaInicioActividad"
		,CAST(PER."Alias" AS NVARCHAR(100)) AS "AliasPersona"
		,CASE WHEN DOC."IdTipoDocumentoIdentidad" = '' THEN NULL ELSE DOC."IdTipoDocumentoIdentidad" END AS "IdTipoDocumentoIdentidad"
		,CAST(DOC."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad"
		,DOC."NumeroSerie" AS "NumeroSerie"
		,IFNULL(TO_CHAR(DOC."FechaEmision"),'0001-01-01') AS "FechaEmisionDocumentoIdentidad"
		,IFNULL(TO_CHAR(DOC."FechaVencimiento"),'0001-01-01') AS "FechaVencimientoDocumentoIdentidad"
		,CASE WHEN DOC."IdEstado" = '' THEN NULL ELSE DOC."IdEstado" END AS "IdEstadoDocumentoIdentidad"
		,CAST(TPER."Descripcion" AS NVARCHAR(100))  AS "DescripcionTipoPersona"
		,CASE WHEN ( SOC."IdContraparte" > 0 ) THEN 'SI' ELSE 'NO' END  AS "EsContraparte"
		,CASE WHEN ( SOC."IdProveedor" > 0 ) THEN 'SI' ELSE 'NO' END  AS "EsProveedor"
		,CASE WHEN ( SOC."IdEmpleado" > 0 ) THEN 'SI' ELSE 'NO' END AS "EsEmpleado"
		,CASE WHEN ( SOC."IdCliente" > 0 ) THEN 'SI' ELSE 'NO' END AS "EsCliente"
		,PER."ResidenciaTributariaExtranjera"
		,PER."CodigoPropositoTransaccion" as "PropositoTransaccion"
		,PER."CodigoRangoNivelVenta" AS "RangoNivelVenta"
		,PER."CodigoRangoNivelVenta" AS "RangoRenta"
		from "BSCL.SocioNegocio.BD::PER.Persona" as PER
		inner join "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
			ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
		inner join "BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterConsultarSocioDeNegocio" as SOC
			ON PER."IdPersona" = SOC."IdPersona"
		inner join "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TDOC
		    ON TDOC."IdTipoDocumentoIdentidad" = DOC."IdTipoDocumentoIdentidad" 
		inner join "BSCL.SocioNegocio.BD::PER.TipoPersona" as TPER
			ON TPER."IdTipoPersona" = PER."Tipo"
		CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion"
	            where "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
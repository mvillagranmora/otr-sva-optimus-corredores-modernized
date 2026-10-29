SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS
SELECT "CLIENTE"."IdDocumentoIdentidad" , "CLIENTE"."Tipo" , "CLIENTE"."NombreCompleto" , "CLIENTE"."Nombres" , "CLIENTE"."ApellidoPaterno" , "CLIENTE"."ApellidoMaterno" , "CLIENTE"."RazonSocial" , "CLIENTE"."IdGiroPrincipal" , "CLIENTE"."IdPaisNacimiento" , "CLIENTE"."IdPaisResidencia" , "CLIENTE"."IdPaisResidenciaSecundaria" , "CLIENTE"."IdPaisNacionalidadPrincipal" , "CLIENTE"."IdPaisNacionalidadSecundaria" , "CLIENTE"."FechaNacimiento" , "CLIENTE"."FechaDefuncion" , "CLIENTE"."Sexo" , "CLIENTE"."EsExtranjero" , "CLIENTE"."EstadoCivil" , "CLIENTE"."RegimenConyugal" , "CLIENTE"."Cargo" , "CLIENTE"."Empleador" , "CLIENTE"."IdDireccionFisicaPrincipal" , "CLIENTE"."IdDireccionElectronicaPrincipal" , "CLIENTE"."IdTelefonoPrincipal" , "CLIENTE"."IdProfesion" , "CLIENTE"."IdNombreFantasiaPrincipal" , "CLIENTE"."DocumentoIdentidad" , "CLIENTE"."TipoDocumentoIdentidad" , "CLIENTE"."DocumentoIdentidadAdicional" , "CLIENTE"."TipoDocumentoIdentidadAdicional" , "CLIENTE"."IdPersonaConyuge" , "CLIENTE"."IdPersonaRepresentante" , "CLIENTE"."IdGiroCIIU" , "CLIENTE"."IdCliente" , "CLIENTE"."IdPersona" , "CLIENTE"."IdEmpresa" , "CLIENTE"."IdSegmentoCliente" , "CLIENTE"."NombreSegmentoCliente" , "CLIENTE"."DescripcionSegmentoCliente" , "CLIENTE"."DescripcionSegmento" , "CLIENTE"."IdCategoriaInversionista" , "CLIENTE"."NombreCategoriaInversionista" , "CLIENTE"."IdPersonaEjecutivo" , "CLIENTE"."IdEmpleadoEjecutivo" , "CLIENTE"."IdSucursalEjecutivo" , "CLIENTE"."CodigoEmpleadoEjecutivo" , "CLIENTE"."NombreEmpleadoEjecutivo" , "CLIENTE"."CodigoEjecutivoExterno" , "CLIENTE"."IdTipoCliente" , "CLIENTE"."IdTipoBloqueo" , "CLIENTE"."FechaBloqueo" , "CLIENTE"."ComentarioBloqueo" , "CLIENTE"."IdUsuarioCreador" , "CLIENTE"."FechaIngreso" , "CLIENTE"."FechaCreacion" , "CLIENTE"."FechaModificacion" , "CLIENTE"."EsEnrolado" , "CLIENTE"."IdEstado" , "CLIENTE"."IdEstadoPersona" , "CLIENTE"."IdEstadoTabla" , "CLIENTE"."IdPlantillaLimiteExtra" , "CLIENTE"."IdTipoPersona" , "CLIENTE"."NombreTipoPersona" , "CLIENTE"."CodigoAdicionalHolding" , "CLIENTE"."IdSucursalPreferencia" , "CLIENTE"."IdPersonaBrokerExtranjero" , "CLIENTE"."IdClaseCliente" , "CLIENTE"."IdSocioDeNegocio" , "CLIENTE"."NombreTipoBloqueo" , "CLIENTE"."IdPortafolioDefecto" , "CLIENTE"."CodigoPortafolioDefecto" , "CLIENTE"."EsClienteFinal" , "ESTCLIENTE"."Nombre" AS "NombreEstado", "CLIENTE"."TipoEnrolamiento" FROM (
	SELECT 
			-- Datos de Personas
			"PER"."IdDocumentoIdentidad",
			"PER"."Tipo",
			"PER"."NombreCompleto",
			"PER"."Nombres",
			"PER"."ApellidoPaterno",
			"PER"."ApellidoMaterno",
			"PER"."RazonSocial",
			"PER"."IdGiroPrincipal",
			"PER"."IdPaisNacimiento",
			"PER"."IdPaisResidencia",
			"PER"."IdPaisResidenciaSecundaria",
			"PER"."IdPaisNacionalidadPrincipal",
			"PER"."IdPaisNacionalidadSecundaria",
			"PER"."FechaNacimiento",
			"PER"."FechaDefuncion",
			"PER"."Sexo",
			"PER"."EsExtranjero",
			"PER"."EstadoCivil",
			"PER"."RegimenConyugal",
			"PER"."Cargo",
			"PER"."Empleador",
			"PER"."IdDireccionFisicaPrincipal",
			"PER"."IdDireccionElectronicaPrincipal",
			"PER"."IdTelefonoPrincipal",
			"PER"."IdProfesion",
			"PER"."IdNombreFantasiaPrincipal",
			"PER"."DocumentoIdentidad",
			"PER"."TipoDocumentoIdentidad",
			"PER"."DocumentoIdentidadAdicional",
			"PER"."TipoDocumentoIdentidadAdicional",
			"PER"."IdPersonaConyuge",
			"PER"."IdPersonaRepresentante",
			"PER"."IdGiroCIIU",
			--Datos Cliente
			"CLI"."IdCliente",
			"PER"."IdPersona",
			"CLI"."IdEmpresa",
			COALESCE("CLI"."IdSegmentoCliente",0) as "IdSegmentoCliente",
			COALESCE("SEG"."NombreSegmentoCliente",'') as "NombreSegmentoCliente",
			COALESCE("SEG"."DescripcionSegmento",'') as "DescripcionSegmentoCliente",
			"SEG"."DescripcionSegmento",
			"CLI"."IdCategoriaInversionista",
			"CAT"."NombreCategoriaInversionista",
			"CLI"."IdPersonaEjecutivo",
			"EMP"."IdEmpleado" as "IdEmpleadoEjecutivo",
			"EMP"."IdSucursal" as "IdSucursalEjecutivo",
			"EMP"."CodigoEmpleado" as "CodigoEmpleadoEjecutivo",
			"EMP"."NombreCompleto" AS "NombreEmpleadoEjecutivo",
			"CLI"."CodigoEjecutivoExterno",
			"CLI"."IdTipoCliente",
			"CLI"."IdTipoBloqueo",
			"CLI"."FechaBloqueo",
			"CLI"."ComentarioBloqueo",
			"CLI"."IdUsuarioCreador",
			"CLI"."FechaIngreso",
			"CLI"."FechaCreacion",
			"CLI"."FechaModificacion",
			"CLI"."EsEnrolado",
			-- Estado Operacional
			CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
			"PER"."IdEstado" AS "IdEstadoPersona",
			"CLI"."IdEstado" AS "IdEstadoTabla",
			"CLI"."IdPlantillaLimiteExtra",
			"TIPPER"."IdTipoPersona",
			"TIPPER"."Descripcion" as "NombreTipoPersona",
			--Datos FK
			--PERU:
			"CLI"."CodigoAdicionalHolding",
			"CLI"."IdSucursalPreferencia",
			"CLI"."IdPersonaBrokerExtranjero",
			"CLI"."IdClaseCliente",
			"SOC"."IdSocioDeNegocio",
			"TIPBLO"."NombreTipoBloqueo",
			"CLI"."IdPortafolioDefecto",
			"POR"."CodigoPortafolio" AS "CodigoPortafolioDefecto",
			"CLI"."EsClienteFinal",
			"CLI"."TipoEnrolamiento"
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"   ON ( "PER"."IdPersona" = "CLI"."IdPersona" )
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as "SOC" ON "CLI" ."IdCliente" = "SOC"."IdCliente"
  	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS  "SEG" ON ( "SEG"."IdSegmentoCliente" = "CLI"."IdSegmentoCliente" )
	INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" as "CAT" on ("CAT"."IdCategoriaInversionista" ="CLI"."IdCategoriaInversionista" AND "CAT"."IdEmpresa" = "CLI"."IdEmpresa")  
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS "EMP"  ON("EMP"."IdPersona" = "CLI"."IdPersonaEjecutivo")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "TIPPER" ON ("TIPPER"."IdTipoPersona" = "PER"."Tipo")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoBloqueoSocioDeNegocio" AS "TIPBLO" ON ("TIPBLO"."IdTipoBloqueo" = "CLI"."IdTipoBloqueo" AND "TIPBLO"."IdEmpresa" = "CLI"."IdEmpresa" AND "IdTipoEntidad" = 'CLIENTE')	
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "POR" ON ("POR"."IdPortafolio" = "CLI"."IdPortafolioDefecto")
	WHERE "CLI"."Borrado"='N') "CLIENTE"
LEFT JOIN 
	"BSCL.Plataforma.BD::PLA.Estado" "ESTCLIENTE" 
		ON ("ESTCLIENTE"."IdEstado" = "CLIENTE"."IdEstado");
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

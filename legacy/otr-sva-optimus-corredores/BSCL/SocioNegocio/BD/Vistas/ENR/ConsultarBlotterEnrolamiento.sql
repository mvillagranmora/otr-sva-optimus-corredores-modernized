SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBlotterEnrolamiento";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBlotterEnrolamiento" AS
SELECT 
	ENROLAMIENTO."IdEnrolamiento" , 
	ENROLAMIENTO."FechaCreacion" , 
	ENROLAMIENTO."FechaModificacion" , 
	DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDefecto" ,
	TIPO_DOCUMENTO_IDENTIDAD."NombreTipoDocumentoIdentidad"    AS "NombreTipoDocumentoIdentidadDefecto" , 
	TIPO_DOCUMENTO_IDENTIDAD."NombreAbrTipoDocumentoIdentidad" AS "NombreAbrTipoDocumentoIdentidadDefecto" , 
	CASE ENROLAMIENTO."Tipo" WHEN 'N' THEN ENROLAMIENTO."Nombres" || ' ' || ENROLAMIENTO."ApellidoPaterno" || ' ' || ENROLAMIENTO."ApellidoMaterno" 
	ELSE ENROLAMIENTO."RazonSocial" END AS "NombreCompleto" , 
	DOCUMENTO_IDENTIDAD."DocumentoIdentidad"                   AS "DocumentoIdentidadDefecto" , 
	ENROLAMIENTO."Tipo"                                        AS "CodigoTipoPersona" , 
	TIPO_PERSONA."Descripcion"                                 AS "TipoPersona" , 
	ENROLAMIENTO."RazonSocial" , 
	ENROLAMIENTO."Nombres" , 
	ENROLAMIENTO."ApellidoPaterno" , 
	ENROLAMIENTO."ApellidoMaterno" , 
	ENROLAMIENTO."FechaNacimiento" ,
	TO_NVARCHAR(ENROLAMIENTO."FechaNacimiento") AS "FechaNacimientoString",
	ENROLAMIENTO."IdPaisResidencia" ,
	CIUDAD."IdPais" AS "IdPaisResidenciaDireccion" ,
	PAIS."CodigoPais" AS "CodigoPaisResidencia" , 
	PAIS."NombrePais" AS "NombrePaisResidencia" , 
	DIRECCION_FISICA."Calle"                                   AS "DireccionCalle" , 
	DIRECCION_FISICA."Numero"                                  AS "DireccionNumero" , 
	CIUDAD."NombreCiudad" , 
	DIRECCION_ELECTRONICA."Direccion"                          AS "DireccionElectronica" , 
	TELEFONO."Numero"                                          AS "NumeroTelefono" ,
	ENROLAMIENTO."CLI_AreaCliente"							   AS "CLI_AreaCliente" ,
	ENROLAMIENTO."CLI_IdTipoCliente"                           AS "CLI_IdTipoCliente" , 
	ENROLAMIENTO."CLI_IdCategoriaInversionista"                AS "CLI_IdCategoriaInversionista" , 
	ENROLAMIENTO."CLI_IdSegmentoCliente"                       AS "CLI_IdSegmentoCliente" , 
	ENROLAMIENTO."CLI_IdEmpleadoEjecutivo"                     AS "CLI_IdEmpleadoEjecutivo" , 
	ENROLAMIENTO."CLI_CodigoEjecutivoExterno"                  AS "CLI_CodigoEjecutivoExterno" , 
	ENROLAMIENTO."CLI_IdSucursalPreferencia"                   AS "CLI_IdSucursalPreferencia" , 
	ENROLAMIENTO."CLI_EsClienteFinal"                          AS "CLI_EsClienteFinal" , 
	ENROLAMIENTO."EstadoCivil"            AS "EstadoCivil" , 
	ENROLAMIENTO."Sexo"               AS "Sexo" , 	
	ENROLAMIENTO."IdPaisNacimiento"            AS "IdPaisNacimiento",
	--NUEVOS CAMPOS AGREGADOS A LA VISTA
	ENROLAMIENTO."IdPaisNacionalidadPrincipal" ,
	ENROLAMIENTO."FechaInicioActividad" ,
	TO_NVARCHAR(ENROLAMIENTO."FechaInicioActividad", 'YYYY-MM-DD') AS "FechaInicioActividadString",
	ENROLAMIENTO."IdPersonaConyuge" ,
	ENROLAMIENTO."RegimenConyugal" ,
	ENROLAMIENTO."GlobalLegalEntity" ,
	ENROLAMIENTO."IdPersonaRepresentante" ,
	ENROLAMIENTO."IdGiroPrincipal" ,
	ENROLAMIENTO."IdProfesion" ,
	ENROLAMIENTO."IdPersonaEmpleador" ,
	ENROLAMIENTO."IdTransparenciaFiscal" ,
	ENROLAMIENTO."CodigoTipoCargoEmpleado",
	ENROLAMIENTO."DetalleCargo" ,
	ENROLAMIENTO."IdRangoPatrimonio" ,
	ENROLAMIENTO."EsExtranjero" ,
	ENROLAMIENTO."ResidenciaTributariaExtranjera",
	ENROLAMIENTO."IdSituacionLaboral",
	CIUDAD."IdCiudad",
	ENROLAMIENTO."CodigoRangoRenta",
	ENROLAMIENTO."CodigoRangoNivelVenta",
	ENROLAMIENTO."CodigoPropositoTransaccion"
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS ENROLAMIENTO 
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON (ENROLAMIENTO."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad") 
LEFT JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS TIPO_DOCUMENTO_IDENTIDAD ON (DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" = TIPO_DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad") 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.TipoPersona" AS TIPO_PERSONA ON (TIPO_PERSONA."IdTipoPersona" = ENROLAMIENTO."Tipo") 
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = ENROLAMIENTO."IdDireccionFisicaPrincipal") 
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DireccionElectronica" DIRECCION_ELECTRONICA ON (DIRECCION_ELECTRONICA."IdDireccionElectronica" = ENROLAMIENTO."IdDireccionElectronicaPrincipal") 
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.Telefono" TELEFONO ON (TELEFONO."IdTelefono" = ENROLAMIENTO."IdTelefonoPrincipal") 
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIUDAD ON (CIUDAD."IdCiudad" = DIRECCION_FISICA."IdCiudad") 
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON (PAIS."IdPais" = CIUDAD."IdPais");

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBlotterEnrolamiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
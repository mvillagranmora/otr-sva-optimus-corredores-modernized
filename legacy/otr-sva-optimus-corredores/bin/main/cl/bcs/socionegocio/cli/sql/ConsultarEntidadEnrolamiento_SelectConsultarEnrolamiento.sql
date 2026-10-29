SELECT 
ENROLAMIENTO."IdEnrolamiento"
,ENROLAMIENTO."IdEmpresa"
,ENROLAMIENTO."IdUsuario"
,ENROLAMIENTO."IdTipoEntidad"
,ENROLAMIENTO."IdEntidadEnrolada"
,ENROLAMIENTO."IdTipoDocumentoIdentidad"
,ENROLAMIENTO."DocumentoIdentidad"
,ENROLAMIENTO."IdTipoDocumentoIdentidadAdicional"
,ENROLAMIENTO."DocumentoIdentidadAdicional"
,ENROLAMIENTO."Tipo"
,ENROLAMIENTO."Nombres"
,ENROLAMIENTO."ApellidoPaterno"
,ENROLAMIENTO."ApellidoMaterno"
,ENROLAMIENTO."RazonSocial"
,ENROLAMIENTO."IdPaisNacimiento"
,ENROLAMIENTO."FechaNacimiento"
,ENROLAMIENTO."Sexo"
,ENROLAMIENTO."EstadoCivil"
,COALESCE(CIUDAD."IdPais", 0) 					AS "IdPais"
,COALESCE(DIVISION_PAIS."IdDivisionPais", 0) 		AS "IdDivisionPais"
,ENROLAMIENTO."IdCiudad"
,ENROLAMIENTO."IdTipoTelefono"
,ENROLAMIENTO."Telefono"
,ENROLAMIENTO."Email"
,ENROLAMIENTO."DireccionCalle"
,ENROLAMIENTO."DireccionNumero"
,ENROLAMIENTO."DireccionDepartamento"
,ENROLAMIENTO."FechaCreacion"
,ENROLAMIENTO."FechaModificacion"
,ENROLAMIENTO."IdPersonaConyuge"
,ENROLAMIENTO."RegimenConyugal"
,ENROLAMIENTO."CLI_IdCategoriaInversionista"
,ENROLAMIENTO."CLI_IdTipoCliente"
,ENROLAMIENTO."CLI_IdTipoClienteCMF"
,ENROLAMIENTO."CLI_IdClaseCliente"
,ENROLAMIENTO."CLI_IdEmpleadoEjecutivo"
,ENROLAMIENTO."CLI_CodigoAdicionalHolding"
,ENROLAMIENTO."POR_IdPerfilComercial"
,ENROLAMIENTO."EMP_CodigoEmpleado"
,ENROLAMIENTO."EMP_CodigoTipoCargoEmpleado"
,ENROLAMIENTO."EMP_IdEmpleadoSupervisor"
,ENROLAMIENTO."EMP_IdSucursal"
,ENROLAMIENTO."EMP_FechaInicioCargo"
,ENROLAMIENTO."EMP_FechaTerminoCargo"
,ENROLAMIENTO."CNT_CodigoInscripcion"
,ENROLAMIENTO."CNT_Emisor"
,ENROLAMIENTO."CNT_Depositos"
,ENROLAMIENTO."CNT_Bolsa"
,ENROLAMIENTO."CNT_Corredora"
,ENROLAMIENTO."CNT_CamaraDeCompensacion"
,ENROLAMIENTO."CNT_Banco"
,ENROLAMIENTO."CNT_Boveda"
,ENROLAMIENTO."CNT_Otros"
,ENROLAMIENTO."PRO_IdAreaProveedor"
,ENROLAMIENTO."EMP_CargoIndefinido"
,ENROLAMIENTO."TipoEnrolamiento"
FROM 
	"BSCL.SocioNegocio.BD::SOC.Enrolamiento"  AS ENROLAMIENTO
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIUDAD 
	ON (CIUDAD."IdCiudad" = ENROLAMIENTO."IdCiudad")
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DIVISION_PAIS
	ON (DIVISION_PAIS."IdCiudad" = ENROLAMIENTO."IdCiudad" AND DIVISION_PAIS."IdDivisionPais" = ENROLAMIENTO."IdDivisionPais")
WHERE
"IdTipoEntidad" = :IdTipoEntidad 
AND "IdEmpresa" = :IdEmpresa
AND "IdTipoDocumentoIdentidad" = CASE WHEN :DocumentoIdentidad='' then "IdTipoDocumentoIdentidad" ELSE :IdTipoDocumentoIdentidad END
AND "DocumentoIdentidad" = CASE WHEN :DocumentoIdentidad='' then "DocumentoIdentidad" ELSE :DocumentoIdentidad END
AND "IdTipoDocumentoIdentidadAdicional" = CASE WHEN :DocumentoIdentidadAdicional = '' THEN "IdTipoDocumentoIdentidadAdicional" ELSE :IdTipoDocumentoIdentidadAdicional END
AND "DocumentoIdentidadAdicional" = CASE WHEN :DocumentoIdentidadAdicional = '' THEN "DocumentoIdentidadAdicional" ELSE :DocumentoIdentidadAdicional END		
AND "Tipo" = (CASE WHEN :CodigoPais = 'PE' THEN :IdTipoPersona ELSE "Tipo" END)
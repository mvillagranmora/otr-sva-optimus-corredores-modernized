SELECT
TO_BIGINT(0) 																														AS "IdEnrolamiento"
,TO_BIGINT(0) 																														AS "IdEmpresa"
,TO_BIGINT(0) 																														AS "IdUsuario"
,:IdTipoEntidad 																													AS "IdTipoEntidad"
,TO_BIGINT(0) 																														AS "IdEntidadEnrolada"
,DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" 																					AS "IdTipoDocumentoIdentidad"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"																							AS "DocumentoIdentidad"		
,DOCUMENTO_IDENTIDAD_ADICIONAL."IdTipoDocumentoIdentidad" 																			AS "IdTipoDocumentoIdentidadAdicional"
,DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad" 																				AS "DocumentoIdentidadAdicional"
,PERSONA."Tipo" 																													AS "Tipo"
,PERSONA."Nombres" 																													AS "Nombres"
,PERSONA."ApellidoPaterno" 																											AS "ApellidoPaterno"
,PERSONA."ApellidoMaterno" 																											AS "ApellidoMaterno"
,PERSONA."RazonSocial" 																												AS "RazonSocial"
,COALESCE(PERSONA."IdPaisNacimiento", ENROLAMIENTO_CLIENTE."IdPaisNacimiento") 														AS "IdPaisNacimiento"
,CASE WHEN PERSONA."FechaNacimiento" = '0001-01-01' THEN COALESCE(ENROLAMIENTO_CLIENTE."FechaNacimiento", TO_DATE('0001-01-01')) ELSE PERSONA."FechaNacimiento" END 	AS "FechaNacimiento"
,PERSONA."Sexo"																														AS "Sexo"
,COALESCE(PERSONA."EstadoCivil", ENROLAMIENTO_CLIENTE."EstadoCivil") 																AS "EstadoCivil"
,COALESCE(CIUDAD."IdPais", 0) 																										AS "IdPais"
,COALESCE(DIRECCION_FISICA."IdDivisionPais", ENROLAMIENTO_CLIENTE."IdDivisionPais")													AS "IdDivisionPais"
,COALESCE(DIRECCION_FISICA."IdCiudad", 0) 																							AS "IdCiudad"
,COALESCE(TELEFONO."IdTipoTelefono", ENROLAMIENTO_CLIENTE."IdTipoTelefono") 														AS "IdTipoTelefono"
,COALESCE(TELEFONO."Numero", ENROLAMIENTO_CLIENTE."Telefono") 																		AS "Telefono"
,COALESCE(DIRECCION_ELECTRONICA."Direccion", ENROLAMIENTO_CLIENTE."Email") 															AS "Email"
,COALESCE(DIRECCION_FISICA."Calle", ENROLAMIENTO_CLIENTE."DireccionCalle")															AS "DireccionCalle"
,COALESCE(DIRECCION_FISICA."Numero", ENROLAMIENTO_CLIENTE."DireccionNumero") 														AS "DireccionNumero"
,COALESCE(DIRECCION_FISICA."Departamento", ENROLAMIENTO_CLIENTE."DireccionDepartamento") 											AS "DireccionDepartamento"
,PERSONA."FechaCreacion"																											AS "FechaCreacion"
,PERSONA."FechaModificacion"																										AS "FechaModificacion"
,CASE WHEN PERSONA."IdPersonaConyuge" = 0 THEN ENROLAMIENTO."IdPersonaConyuge" ELSE PERSONA."IdPersonaConyuge" 	END 				AS "IdPersonaConyuge"
,CASE WHEN PERSONA."RegimenConyugal"  = '' THEN ENROLAMIENTO."RegimenConyugal"  ELSE PERSONA."RegimenConyugal"  END 				AS "RegimenConyugal"
,COALESCE(CLIENTE."IdCategoriaInversionista", COALESCE(ENROLAMIENTO."CLI_IdCategoriaInversionista", ''))							AS "CLI_IdCategoriaInversionista"
,COALESCE(CLIENTE."IdTipoCliente", COALESCE(ENROLAMIENTO."CLI_IdTipoCliente", '')) 													AS "CLI_IdTipoCliente"
,COALESCE(ENROLAMIENTO."CLI_IdTipoClienteCMF", 0)							 														AS "CLI_IdTipoClienteCMF"
,COALESCE(CLIENTE."IdClaseCliente",COALESCE(ENROLAMIENTO."CLI_IdClaseCliente", '')) 												AS "CLI_IdClaseCliente"
,COALESCE(CLIENTE_EMPLEADO."IdEmpleado",COALESCE(ENROLAMIENTO."CLI_IdEmpleadoEjecutivo", 0)) 										AS "CLI_IdEmpleadoEjecutivo"
,COALESCE(CLIENTE."CodigoAdicionalHolding",COALESCE(ENROLAMIENTO."CLI_CodigoAdicionalHolding", '')) 								AS "CLI_CodigoAdicionalHolding"
,COALESCE(PORTAFOLIO."IdPerfilComercialBaseOrigen",COALESCE(ENROLAMIENTO."POR_IdPerfilComercial", '')) 								AS "POR_IdPerfilComercial"
,COALESCE(EMPLEADO."CodigoEmpleado",COALESCE(ENROLAMIENTO."EMP_CodigoEmpleado", '')) 												AS "EMP_CodigoEmpleado"
,COALESCE(EMPLEADO."CodigoTipoCargoEmpleado",COALESCE(ENROLAMIENTO."EMP_CodigoTipoCargoEmpleado", '')) 								AS "EMP_CodigoTipoCargoEmpleado"
,COALESCE(EMPLEADO."IdEmpleadoSupervisor",COALESCE(ENROLAMIENTO."EMP_IdEmpleadoSupervisor", 0))  									AS "EMP_IdEmpleadoSupervisor"
,COALESCE(EMPLEADO."IdSucursal",COALESCE(ENROLAMIENTO."EMP_IdSucursal", 0))  														AS "EMP_IdSucursal"
,COALESCE(EMPLEADO."FechaInicioCargo",COALESCE(ENROLAMIENTO."EMP_FechaInicioCargo", '0001-01-01'))  								AS "EMP_FechaInicioCargo"
,COALESCE(EMPLEADO."FechaTerminoCargo",COALESCE(ENROLAMIENTO."EMP_FechaTerminoCargo", '0001-01-01'))								AS "EMP_FechaTerminoCargo"
,COALESCE(CONTRAPARTE."CodigoInscripcion", COALESCE(ENROLAMIENTO."CNT_CodigoInscripcion", '')) 	 									AS "CNT_CodigoInscripcion"
,COALESCE(CONTRAPARTE."Emisor", COALESCE(ENROLAMIENTO."CNT_Emisor", ''))		 													AS "CNT_Emisor"
,COALESCE(CONTRAPARTE."Depositos", COALESCE(ENROLAMIENTO."CNT_Depositos", ''))   													AS "CNT_Depositos"
,COALESCE(CONTRAPARTE."Bolsa", COALESCE(ENROLAMIENTO."CNT_Bolsa", '')) 		 														AS "CNT_Bolsa"
,COALESCE(CONTRAPARTE."Corredora", COALESCE(ENROLAMIENTO."CNT_Corredora", ''))   													AS "CNT_Corredora"
,COALESCE(CONTRAPARTE."CamaraDeCompensacion",COALESCE(ENROLAMIENTO."CNT_CamaraDeCompensacion", '')) 								AS "CNT_CamaraDeCompensacion"
,COALESCE(CONTRAPARTE."Banco", COALESCE(ENROLAMIENTO."CNT_Banco", '')) 																AS "CNT_Banco"
,COALESCE(CONTRAPARTE."Boveda", COALESCE(ENROLAMIENTO."CNT_Boveda", '')) 	 														AS "CNT_Boveda"
,COALESCE(CONTRAPARTE."Otros", COALESCE(ENROLAMIENTO."CNT_Otros", '')) 	 															AS "CNT_Otros"
,COALESCE(PROVEEDOR."IdAreaProveedor", COALESCE(ENROLAMIENTO."PRO_IdAreaProveedor", 0)) 											AS "PRO_IdAreaProveedor"
,COALESCE(EMPLEADO."CargoIndefinido", COALESCE(ENROLAMIENTO."EMP_CargoIndefinido", '')) 											AS "EMP_CargoIndefinido"
,COALESCE(ENROLAMIENTO."TipoEnrolamiento", '')																						AS "TipoEnrolamiento"
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOCIO_NEGOCIO
JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERSONA 
	ON (PERSONA."IdPersona"=SOCIO_NEGOCIO."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD
	ON (PERSONA."IdPersona"=DOCUMENTO_IDENTIDAD."IdPersona" 
	AND DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad 
	AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = :DocumentoIdentidad
	AND PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD_ADICIONAL
	ON (PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD_ADICIONAL."IdPersona"  
	AND DOCUMENTO_IDENTIDAD_ADICIONAL."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidadAdicional 
	AND (DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad" = :DocumentoIdentidadAdicional OR DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad" = DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad")
	AND DOCUMENTO_IDENTIDAD_ADICIONAL."IdDocumentoIdentidad" = PERSONA."IdDocumentoIdentidadAdicional")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" DIRECCION_FISICA
	ON (PERSONA."IdDireccionFisicaPrincipal" = DIRECCION_FISICA."IdDireccionFisica")
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD
	ON (DIRECCION_FISICA."IdCiudad" IS NOT NULL AND DIRECCION_FISICA."IdCiudad" = CIUDAD."IdCiudad")						
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DIRECCION_ELECTRONICA
	ON (PERSONA."IdDireccionElectronicaPrincipal" = DIRECCION_ELECTRONICA."IdDireccionElectronica")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TELEFONO
	ON (PERSONA."IdTelefonoPrincipal" = TELEFONO."IdTelefono")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLIENTE
	ON (SOCIO_NEGOCIO."IdCliente" IS NOT NULL AND SOCIO_NEGOCIO."IdCliente" = CLIENTE."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" CLIENTE_EMPLEADO
	ON (CLIENTE."IdPersonaEjecutivo" IS NOT NULL AND CLIENTE."IdPersonaEjecutivo" = CLIENTE_EMPLEADO."IdPersona" AND CLIENTE_EMPLEADO."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORTAFOLIO
	ON (SOCIO_NEGOCIO."IdCliente" IS NOT NULL AND SOCIO_NEGOCIO."IdCliente" = PORTAFOLIO."IdCliente" AND PORTAFOLIO."CodigoPortafolio" = '0')
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMPLEADO
	ON (SOCIO_NEGOCIO."IdEmpleado" IS NOT NULL AND SOCIO_NEGOCIO."IdEmpleado" = EMPLEADO."IdEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" PROVEEDOR
	ON (SOCIO_NEGOCIO."IdProveedor" IS NOT NULL AND SOCIO_NEGOCIO."IdProveedor" = PROVEEDOR."IdProveedor")
LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" CONTRAPARTE
	ON (SOCIO_NEGOCIO."IdContraparte" IS NOT NULL AND SOCIO_NEGOCIO."IdContraparte" = CONTRAPARTE."IdContraparte")
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.Enrolamiento" ENROLAMIENTO 
	ON (ENROLAMIENTO."IdTipoEntidad" = :IdTipoEntidad AND SOCIO_NEGOCIO."IdEmpresa" = ENROLAMIENTO."IdEmpresa"
	AND ENROLAMIENTO."IdTipoDocumentoIdentidad" = CASE WHEN :DocumentoIdentidad = '' THEN ENROLAMIENTO."IdTipoDocumentoIdentidad" ELSE :IdTipoDocumentoIdentidad END
	AND ENROLAMIENTO."DocumentoIdentidad" = CASE WHEN :DocumentoIdentidad = '' THEN ENROLAMIENTO."DocumentoIdentidad" ELSE :DocumentoIdentidad END
	AND ENROLAMIENTO."IdTipoDocumentoIdentidadAdicional" = CASE WHEN :DocumentoIdentidadAdicional = '' THEN ENROLAMIENTO."IdTipoDocumentoIdentidadAdicional" ELSE :IdTipoDocumentoIdentidadAdicional END
	AND ENROLAMIENTO."DocumentoIdentidadAdicional" = CASE WHEN :DocumentoIdentidadAdicional = '' THEN ENROLAMIENTO."DocumentoIdentidadAdicional" ELSE :DocumentoIdentidadAdicional END)
,(SELECT DOCUMENTO_IDENTIDAD_CLIENTE."DocumentoIdentidad" FROM 
	"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD_CLIENTE
	,"BSCL.SocioNegocio.BD::PER.Persona" PERSONA_CLIENTE
	WHERE DOCUMENTO_IDENTIDAD_CLIENTE."IdDocumentoIdentidad" = PERSONA_CLIENTE."IdDocumentoIdentidad" AND DOCUMENTO_IDENTIDAD_CLIENTE."DocumentoIdentidad" = :DocumentoIdentidad) AS PERSONA_DOCUMENTO_IDENTIDAD_CLIENTE
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.Enrolamiento" ENROLAMIENTO_CLIENTE	
	ON (ENROLAMIENTO_CLIENTE."DocumentoIdentidad" IS NOT NULL AND ENROLAMIENTO_CLIENTE."DocumentoIdentidad" = PERSONA_DOCUMENTO_IDENTIDAD_CLIENTE."DocumentoIdentidad"
	AND ENROLAMIENTO_CLIENTE."IdTipoEntidad" = 'CLIENTE')	
WHERE SOCIO_NEGOCIO."IdPersona" = :IdPersona AND SOCIO_NEGOCIO."IdEmpresa" = :IdEmpresa;
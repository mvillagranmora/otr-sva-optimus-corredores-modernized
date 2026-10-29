SELECT
	FATCA."IdPersona",
 	FATCA."IdEstado",
 	FATCA."IdClasificacionFatca",
 	FATCA."IdClasificacionEmpresaFatca",
 	FATCA."NumeroGIIN",
	FATCA."FechaFirmaFormulario",
 	FATCA."AutorizacionInformarOrganismo",
 	--INDICIOS
	PER."IdPaisNacimiento",
	PER."IdPaisResidencia",
	PER."IdPaisNacionalidadPrincipal" as "IdPaisNacionalidad1", --la de ficha persona!
 	FATCA."NACEEUU",
 	FATCA."NACEEUUExplicacion",
 	FATCA."EntidadFinanciera",
 	FATCA."TipoInstitucionFinanciera",
 	FATCA."EntidadGestionada",
 	FATCA."IngresosPasivos",
 	FATCA."GenerarIngresosPasivos",
 	FATCA."CategoriaEntidad",
 	FATCA."GIINEntidad",
 	FATCA."RazonSocial",
 	FATCA."FechaLiquidacion",
 	FATCA."NombreMercado",
 	FATCA."FechaConstitucion",
 	FATCA."ObligacionesFiscales",
 	FATCA."ObligacionesFiscalesExplicacion",
 	FATCA."IndividuoControl",
 	--EXCEPCIONES
 	FATCA."ExcepcionResidente",
 	FATCA."ExcepcionNacimiento",
 	FATCA."ExcepcionDomicilio",
 	FATCA."ExcepcionTelefono",
 	FATCA."ExcepcionOrdenPermanente",	
 	FATCA."ExcepcionFirma",
 	FATCA."ParticipacionSociedadesUSA",
 	FATCA."OrdenPermanente",
 	--
 	FATCA."FechaIngreso",
 	FATCA."FechaCreacion",
 	FATCA."FechaModificacion",
 	FATCA."IdUsuarioCreador",
 	FATCA."IdUsuarioModificador",
	CLI."TipoEnrolamiento",
	CLI."IdCliente",
	PER."Tipo"
FROM 
	"BSCL.SocioNegocio.BD::PER.Fatca" as FATCA
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER
		ON (PER."IdPersona" = FATCA."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE FATCA."Borrado" = 'N'
AND FATCA."IdPersona" IN (:IdFatcas);
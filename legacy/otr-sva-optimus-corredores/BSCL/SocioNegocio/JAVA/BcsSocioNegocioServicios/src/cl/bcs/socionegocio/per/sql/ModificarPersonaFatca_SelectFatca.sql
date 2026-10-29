-- Obtiene datos del Fatca de la Persona a modificar
SELECT
	FATCA."IdPersona",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
 	FATCA."IdEstado",
 	ESTFATCA."Nombre" as "NombreEstado", -- Estado Facta!
 	FATCA."IdClasificacionFatca",
 	CLASIFFATCA."Nombre" as "NombreClasificacionFatca",
 	FATCA."IdClasificacionEmpresaFatca",
 	CLASIFEMPFATCA."Nombre" as "NombreClasificacionEmpresaFatca",
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
 	FATCA."DeclaracionCRS",
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
 	FATCA."IdUsuarioModificador"
FROM 
	"BSCL.SocioNegocio.BD::PER.Fatca" as FATCA
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER
		ON (PER."IdPersona" = FATCA."IdPersona")	
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" CLASIFFATCA
		ON (CLASIFFATCA."IdClasificacionFatca" = FATCA."IdClasificacionFatca")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca" CLASIFEMPFATCA
		ON (CLASIFEMPFATCA."IdClasificacionEmpresaFatca" = FATCA."IdClasificacionEmpresaFatca")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTFATCA
		ON (ESTFATCA."IdEstado" = FATCA."IdEstado")
WHERE FATCA."Borrado" = 'N'
AND FATCA."IdPersona" = :IdPersona;
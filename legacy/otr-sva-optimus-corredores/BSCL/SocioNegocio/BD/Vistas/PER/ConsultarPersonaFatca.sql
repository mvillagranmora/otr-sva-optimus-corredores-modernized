SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca"
AS

SELECT
	FATCA."IdPersona",
 	FATCA."IdEstado",
 	ESTFATCA."Nombre" as "NombreEstado", -- Estado Facta!
 	FATCA."IdClasificacionFatca",
 	CLASIFFATCA."Nombre" as "NombreClasificacionFatca",
 	FATCA."IdClasificacionEmpresaFatca",
 	CLASIFEMPFATCA."Nombre" as "NombreClasificacionEmpresaFatca",
 	FATCA."NumeroGIIN",
	FATCA."FechaFirmaFormulario",
	TO_NVARCHAR(FATCA."FechaFirmaFormulario", 'YYYY-MM-DD') AS "FechaFirmaFormularioString",
 	FATCA."AutorizacionInformarOrganismo",
 	--INDICIOS
	PER."IdPaisNacimiento",
	PER."IdPaisResidencia",
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
 	TO_NVARCHAR(FATCA."FechaLiquidacion") AS "FechaLiquidacionString",
 	FATCA."NombreMercado",
 	FATCA."FechaConstitucion",
 	TO_NVARCHAR(FATCA."FechaConstitucion") AS "FechaConstitucionString",
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
WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaFatca" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
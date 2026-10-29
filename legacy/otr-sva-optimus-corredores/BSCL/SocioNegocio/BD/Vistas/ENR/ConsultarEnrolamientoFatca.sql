SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatca"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatca"
AS
SELECT
	FATCA."IdEnrolamiento",
 	FATCA."IdClasificacionFatca",
 	CLASIFFATCA."Nombre" as "NombreClasificacionFatca",
 	FATCA."IdClasificacionEmpresaFatca",
 	CLASIFEMPFATCA."Nombre" as "NombreClasificacionEmpresaFatca",
 	FATCA."NumeroGIIN",
	FATCA."FechaFirmaFormulario",
	TO_NVARCHAR(FATCA."FechaFirmaFormulario", 'YYYY-DD-MM') AS "FechaFirmaFormularioString",
 	FATCA."AutorizacionInformarOrganismo",
 	--INDICIOS
	ENROLAMIENTO."IdPaisNacimiento",
	ENROLAMIENTO."IdPaisResidencia",
	ENROLAMIENTO."IdPaisNacionalidadPrincipal", --la de ficha persona!
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
 	TO_NVARCHAR(FATCA."FechaLiquidacion") AS "FechaLiquidacionString",
 	FATCA."NombreMercado",
 	FATCA."FechaConstitucion",
 	TO_NVARCHAR(FATCA."FechaConstitucion") AS "FechaConstitucionString",
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
 	FATCA."OrdenPermanente"
FROM "BSCL.SocioNegocio.BD::ENR.Fatca" as FATCA
	INNER JOIN "BSCL.SocioNegocio.BD::ENR.Enrolamiento" as ENROLAMIENTO
		ON (ENROLAMIENTO."IdEnrolamiento" = FATCA."IdEnrolamiento")	
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.ClasificacionFatca" CLASIFFATCA
		ON (CLASIFFATCA."IdClasificacionFatca" = FATCA."IdClasificacionFatca")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.ClasificacionEmpresaFatca" CLASIFEMPFATCA
		ON (CLASIFEMPFATCA."IdClasificacionEmpresaFatca" = FATCA."IdClasificacionEmpresaFatca")
WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamientoFatca" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

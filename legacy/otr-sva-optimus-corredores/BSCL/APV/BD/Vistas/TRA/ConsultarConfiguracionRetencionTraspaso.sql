SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarConfiguracionRetencionTraspaso";

CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarConfiguracionRetencionTraspaso" AS

SELECT ConfiguracionRetencion."IdConfiguracionRetencionTraspaso"	
	 , ConfiguracionRetencion."IdEmpresa"							
	 , ConfiguracionRetencion."CodigoRetiro"						
	 , ConfiguracionRetencion."NombreRetiro"						
	 , ConfiguracionRetencion."IdPersonaRetencion"
	 , SocioNegocio."IdSocioDeNegocio"
	 , SocioNegocio."DocumentoIdentidadDefecto"
	 , SocioNegocio."RazonSocial"
	 , ConfiguracionRetencion."IdRegimenTributario"	
	 , RegimenTributario."Codigo"
	 , ConfiguracionRetencion."PorcentajeRetencion"					
	 , ConfiguracionRetencion."MontoTopeRetencion"					
	 , ConfiguracionRetencion."DescripcionRetencion"				
	 , ConfiguracionRetencion."IdEstado"								
FROM "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso" AS ConfiguracionRetencion
LEFT OUTER JOIN "BSCL.APV.BD::REC.RegimenTributario" AS RegimenTributario
			 ON RegimenTributario."IdRegimenTributario" = ConfiguracionRetencion."IdRegimenTributario"
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocio
			 ON	SocioNegocio."IdPersona" = ConfiguracionRetencion."IdPersonaRetencion"
			AND SocioNegocio."IdEmpresa" = ConfiguracionRetencion."IdEmpresa";

GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarConfiguracionRetencionTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
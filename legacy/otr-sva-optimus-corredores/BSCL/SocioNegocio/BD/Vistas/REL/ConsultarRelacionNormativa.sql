SET SCHEMA "BSCL";
---BSCL.SocioNegocio.BD::REL.Servicios.ConsultarRelacionNormativa
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionNormativa";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionNormativa"
--AS SELECT 1 FROM DUMMY;

 ( "IdSocioDeNegocio",
	 "IdPersona",
	 "IdContraparte",
	 "FechaIngreso",
	 "FechaCreacion",
	 "FechaModificacion",
	 "IdDocumentoIdentidadDefecto",
	 "DocumentoIdentidadDefecto",
	 "IdDocumentoIdentidad",
	 "DocumentoIdentidad",
	 "RazonSocial",
	 "IdPaisNacto",
	 "CodigoPaisNacto",
	 "NombrePaisNacto",
	 "SocioBusqueda",
	 "CodigoContraparte",
	 "EsBanco",
	 "EsBolsa",
	 "EsCamaraDeCompensacion",
	 "EsCorredora",
	 "PermiteDepositos",
	 "EsEmisor",
	 "EstadoContraparte" ) AS SELECT
	 "SocioDeNegocio_$0"."IdSocioDeNegocio" ,
	 "SocioDeNegocio_$0"."IdPersona" ,
	 "SocioDeNegocio_$0"."IdContraparte" ,
	 "Persona_$2"."FechaIngreso" ,
	 "Persona_$2"."FechaCreacion" ,
	 "Persona_$2"."FechaModificacion" ,
	 "Persona_$2"."IdDocumentoIdentidad" AS "IdDocumentoIdentidadDefecto" ,
	 "DocumentoIdentidad_$3"."DocumentoIdentidad" AS "DocumentoIdentidadDefecto" ,
	 "DocumentoIdentidad_$5"."IdDocumentoIdentidad" AS "IdDocumentoIdentidad" ,
	 "DocumentoIdentidad_$5"."DocumentoIdentidad" AS "DocumentoIdentidad" ,
	 CONCAT ( COALESCE ( CONCAT ( "ConsultarContraparte_$1"."CodigoInscripcion" ,
	 ' - ' ) ,
	 '' ) ,
	 COALESCE ( "Persona_$2"."RazonSocial" ,
	 '' ) ) AS "RazonSocial" ,
	 "Pais_$6"."IdPais" AS "IdPaisNacto" ,
	 "Pais_$6"."CodigoPais" AS "CodigoPaisNacto" ,
	 "Pais_$6"."NombrePais" AS "NombrePaisNacto" ,
	 CONCAT ( COALESCE ( CONCAT ( "ConsultarContraparte_$1"."CodigoInscripcion" ,
	 ' - ' ) ,
	 "DocumentoIdentidad_$5"."DocumentoIdentidad" ) ,
	 COALESCE ( "Persona_$2"."RazonSocial" ,
	 '' ) ) AS "SocioBusqueda" ,
	 "ConsultarContraparte_$1"."CodigoInscripcion" AS "CodigoContraparte" ,
	 "ConsultarContraparte_$1"."Banco" AS "EsBanco" ,
	 "ConsultarContraparte_$1"."Bolsa" AS "EsBolsa" ,
	 "ConsultarContraparte_$1"."CamaraDeCompensacion" AS "EsCamaraDeCompensacion" ,
	 "ConsultarContraparte_$1"."Corredora" AS "EsCorredora" ,
	 "ConsultarContraparte_$1"."Depositos" AS "PermiteDepositos" ,
	 "ConsultarContraparte_$1"."Emisor" AS "EsEmisor" ,
	 "ConsultarContraparte_$1"."IdEstado" AS "EstadoContraparte" 
FROM ( ( ( ( ( ( "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SocioDeNegocio_$0" 
						LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" AS "ConsultarContraparte_$1" ON ( "ConsultarContraparte_$1"."IdContraparte" = "SocioDeNegocio_$0"."IdContraparte" ) ) 
					LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona_$2" ON ( "Persona_$2"."IdPersona" = "SocioDeNegocio_$0"."IdPersona" ) ) 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "DocumentoIdentidad_$3" ON ( "DocumentoIdentidad_$3"."IdDocumentoIdentidad" = "Persona_$2"."IdDocumentoIdentidad" ) ) 
			LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad" AS "PersonaDocumentoIdentidad_$4" ON ( "PersonaDocumentoIdentidad_$4"."IdPersona" = "Persona_$2"."IdPersona" ) ) 
		LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DocumentoIdentidad_$5" ON ( "DocumentoIdentidad_$5"."IdDocumentoIdentidad" = "PersonaDocumentoIdentidad_$4"."IdDocumentoIdentidad" ) ) 
	LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS "Pais_$6" ON ( "Pais_$6"."IdPais" = "Persona_$2"."IdPaisNacimiento" ) ) 
WHERE ( "SocioDeNegocio_$0"."IdContraparte" IS NOT NULL ) WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionNormativa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionadosPEP";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionadosPEP" AS
				SELECT "DOC"."DocumentoIdentidad","CONIN"."CodigoTipoIndicador","CONIN"."ValorIndicador","REL"."IdPersonaOrigen","REL"."IdPersonaDestino"
                FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS "CONIN"
                LEFT JOIN "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS "REL" 
                ON "CONIN"."IdPersona" = "REL"."IdPersonaDestino"
                LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
                ON "DOC"."IdPersona" = "REL"."IdPersonaOrigen"
                WHERE "CONIN"."CodigoTipoIndicador" = 'PEP' AND "CONIN"."ValorIndicador" = 'S';
	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionadosPEP" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 
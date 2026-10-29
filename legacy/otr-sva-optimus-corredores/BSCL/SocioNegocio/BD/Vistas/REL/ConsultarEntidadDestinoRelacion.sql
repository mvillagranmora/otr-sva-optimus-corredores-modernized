SET SCHEMA "BSCL";

--BSCL.SocioNegocio.BD::REL.Servicios.ConsultarEntidadDestinoRelacion

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarEntidadDestinoRelacion";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarEntidadDestinoRelacion" (
	 "IdTipoEntidad",
	 "IdEstado" ) AS SELECT DISTINCT
	 "TipoRelacionDestino_$0"."IdTipoEntidad",
	 "TipoRelacionDestino_$0"."IdEstado" 
FROM "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" AS "TipoRelacionDestino_$0"  WITH READ ONLY;
	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarEntidadDestinoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

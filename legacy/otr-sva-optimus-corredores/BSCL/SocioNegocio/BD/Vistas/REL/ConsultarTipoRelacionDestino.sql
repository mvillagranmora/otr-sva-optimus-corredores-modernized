SET SCHEMA "BSCL";
---BSCL.SocioNegocio.BD::REL.Servicios.ConsultarTipoRelacionDestino
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" ( "IdTipoRelacionDestino",
	 "IdTipoRelacion",
	 "IdTipoEntidad",
	 "IdEstado" ) AS SELECT
	 "DES"."IdTipoRelacionDestino" ,
	 "REL"."IdTipoRelacion" AS "IdTipoRelacion" ,
	 "DES"."IdTipoEntidad" ,
	 "DES"."IdEstado" 
FROM "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino" AS "DES" 
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "REL" ON "REL"."IdTipoRelacion" = "DES"."IdTipoRelacion" WITH READ ONLY;
	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionDestino" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionAnalitica";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionAnalitica" AS
SELECT
	 "IdTipoRelacion",
	 "EsPersonaNatural",
	 "IdTipoEntidadOrigen",
	 "Administracion",
	 "NombreTipoRelacion",
	 case when trim("IdTipoRelacionDestino") = '' then "IdTipoRelacion" else "IdTipoRelacionDestino" end as "IdTipoRelacionDestino",
	 "EsIndefinida",
	 "IdEstado",
	 "IdTipoEntidadDestino",
	 "IdEmpresa",
	 "UsaPorcentajeParticipacion",
	 "TextoUsaPorcentajeParticipacion" 
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

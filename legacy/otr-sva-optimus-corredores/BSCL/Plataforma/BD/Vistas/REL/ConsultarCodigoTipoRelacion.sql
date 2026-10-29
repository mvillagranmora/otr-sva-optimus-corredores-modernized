SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::REL.ConsultarCodigoTipoRelacion";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::REL.ConsultarCodigoTipoRelacion"
AS
	SELECT  "IdEmpresa", "CodigoTipoRelacion", "DescripcionTipoRelacion"
	FROM "BSCL.Plataforma.BD::REL.CodigoTipoRelacion" Relacion; 
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REL.ConsultarCodigoTipoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
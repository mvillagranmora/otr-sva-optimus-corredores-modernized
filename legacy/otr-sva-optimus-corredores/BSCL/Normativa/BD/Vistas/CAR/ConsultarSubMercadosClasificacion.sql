SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarSubMercadosClasificacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarSubMercadosClasificacion" AS
    SELECT sub."IdArbolClasificacionActivo" AS "IdSubMercado",
		sub. "Descripcion" as "DescripcionSubMercado", 
		merc."Descripcion" as "Mercado"  
		FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" sub  
		INNER JOIN "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" merc ON( merc."Padre" = 0 AND merc."IdArbolClasificacionActivo" = Sub."Padre")
	WHERE sub."Padre" != 0;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarSubMercadosClasificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoLibroOrden";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoLibroOrden"
AS SELECT 
	"TIPLIB"."IdTipoLibroOrden",
	"TIPLIB"."NombreTipoLibroOrden",
	"TIPLIB"."EsLibroNormal",
	"TIPLIB"."EsLibroOfertaPublica",
	"TIPLIB"."IdTipoOperacionOfertaPublica"
	
FROM "BSCL.Orden.BD::ORD.TipoLibroOrden" AS "TIPLIB";
	
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoLibroOrden" TO BCS_DESARROLLADOR WITH GRANT OPTION;

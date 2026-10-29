SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoOperacionCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoOperacionCobro" 
AS
SELECT DISTINCT
TIPOOPE."IdTipoOperacion" , 
TIPOOPE."Codigo" , 
TIPOOPE."Nombre" as "NombreTipoOperacion" , 
TIPOOPE."Accion" , 
TIPOOPE."IdTipoProducto",
PRODUCTO."Nombre" AS "NombreTipoProducto", 
"AccionOrden"
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPE
INNER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRODUCTO ON PRODUCTO."IdTipoProducto" = TIPOOPE."IdTipoProducto"
WHERE "PermiteCobros" = 'S';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoOperacionCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


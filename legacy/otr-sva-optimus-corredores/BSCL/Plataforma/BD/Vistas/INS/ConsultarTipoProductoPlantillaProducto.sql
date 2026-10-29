SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoPlantillaProducto"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoPlantillaProducto" AS
SELECT
	 TIPPRO."IdTipoProducto"
	,TIPPRO."Codigo"
	,TIPPRO."Nombre"
	,TIPPRO."IdEstado"
FROM "BSCL.Plataforma.BD::INS.TipoProducto" AS TIPPRO;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProductoPlantillaProducto" TO BCS_DESARROLLADOR WITH GRANT OPTION;

SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarSucursalesBancarias";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarSucursalesBancarias" AS
SELECT 	"IdSucursal"
		,"NombreSucursal"
		,"IdBanco"
		,"IdEstado"
 FROM"BSCL.Tesoreria.BD::TES.SucursalesBancarias";
 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarSucursalesBancarias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

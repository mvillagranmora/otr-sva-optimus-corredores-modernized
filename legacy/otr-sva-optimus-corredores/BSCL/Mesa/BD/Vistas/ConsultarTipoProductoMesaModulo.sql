SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" AS

SELECT
	TPR."IdTipoProductoMesaModulo",
	TPR."IdTipoProductoMesa",
	TPR."IdTipoProductoModulo",
	TPR."Codigo",
	TPR."Nombre",
	TPR."IdEstado",
	TPR."IdEmpresa",
	TPR."IdTipoMesa",
	TPR."IdTipoInstrumento"
FROM "BSCL.Mesa.BD::MES.TipoProductoMesaModulo" AS TPR;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" TO BCS_DESARROLLADOR WITH GRANT OPTION;

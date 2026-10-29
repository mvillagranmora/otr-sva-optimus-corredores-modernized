SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarModalidadLiquidacion";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarModalidadLiquidacion" AS 
SELECT
	"IdEmpresa",
	"IdParametro",
	"NombreTipoParametro",
	"ValorParametro",
	"NombreParametro",
	"IdEstado"
FROM "BSCL.Mesa.BD::MES.Parametro"
WHERE "NombreTipoParametro" = 'ModalidadLiquidacion';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarModalidadLiquidacion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

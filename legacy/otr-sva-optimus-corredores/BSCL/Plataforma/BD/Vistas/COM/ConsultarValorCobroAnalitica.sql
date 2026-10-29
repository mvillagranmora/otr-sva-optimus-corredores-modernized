SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobroAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobroAnalitica" AS
SELECT
	"IdValorCobro",
	"DescripcionValorCobro",
	"IdTipoProducto",
	"IdCobro",
	"IdMoneda",
	"IdEstado",
	TO_TIMESTAMP("FechaCreacion") AS "FechaCreacion",
	"ValorCobro",
	"ValorTopeMinimoOrden" as "ValorMinimo",
	"IdEmpresa"
FROM "BSCL.Plataforma.BD::COM.ValorCobro";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarValorCobroAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

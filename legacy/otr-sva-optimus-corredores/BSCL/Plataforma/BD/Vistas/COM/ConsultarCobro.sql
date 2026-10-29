SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro"  
AS

SELECT 
	C."IdCobro",
	C."NombreCobro",
	C."FormaCobro",
	C."ValorCobro", --defaults
	C."ValorTopeMinimoOrden", --defaults
	C."UnidadDeCobro",
	C."PermiteCambiarValor",
	CC."IdCobroClasificacion",
	CC."CodigoCobroClasificacion",
	CC."DescripcionCobroClasificacion",
	C."IdEmpresa",
	C."EsRecalculable",
	C."PermiteAsignarCorredorCorresponsal",
	C."PermiteAsignarPortafolio",
	C."IdMoneda"
FROM 
	"BSCL.Plataforma.BD::COM.Cobro" as C
	INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" as CC 
		on (CC."IdCobroClasificacion" = C."IdCobroClasificacion");


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

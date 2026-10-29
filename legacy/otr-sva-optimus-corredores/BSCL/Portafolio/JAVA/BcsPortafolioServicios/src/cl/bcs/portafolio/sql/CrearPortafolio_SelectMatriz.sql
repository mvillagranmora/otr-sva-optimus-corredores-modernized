SELECT 
	"IdMatrizAgrupadorAPV"	AS "IdMatrizAgrupadorAPV",
	"Codigo"				AS "CodigoPortafolio",
	"IdProductoAgrupador"	AS "IdProductoAgrupador",
	"Producto"				AS "ProductoAgrupador" 	
FROM "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario"
WHERE "Codigo" = :CodigoRegimen
  AND "IdPropositoPortafolio" = :PropositoPortafolio
  AND "NombrePlan" = :NombrePlan
  AND "CodigoAgrupador" = :CodigoAgrupador;  
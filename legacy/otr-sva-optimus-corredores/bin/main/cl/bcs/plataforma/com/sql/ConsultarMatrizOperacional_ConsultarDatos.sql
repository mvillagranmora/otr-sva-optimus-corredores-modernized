SELECT tipoprod."IdTipoProducto",tipoprod."Nombre" AS "NombreTipoProducto",
tipoope."IdTipoOperacion",
tipoope."Nombre" AS "NombreTipoOperacion",
"IdTipoCanalIngreso",
tipocanal."Nombre" AS "NombreTipoCanalIngreso",
"IdBolsa","NombreBolsa"
FROM "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope
INNER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" tipoprod ON tipoprod."IdTipoProducto" = tipoope."IdTipoProducto",
"BSCL.Plataforma.BD::PLA.TipoCanalIngreso" tipocanal,
(SELECT "IdBolsa","Nombre" AS "NombreBolsa" FROM "BSCL.Plataforma.BD::INS.Bolsa" UNION ALL SELECT '0' AS "IdBolsa",'TODOS' AS "NombreBolsa" FROM DUMMY) AS bolsa
WHERE tipocanal."IdTipoCanalIngreso" IN ('AGE','OWP','CLI','TRA') AND (tipoope."PermiteCobros" = 'S');
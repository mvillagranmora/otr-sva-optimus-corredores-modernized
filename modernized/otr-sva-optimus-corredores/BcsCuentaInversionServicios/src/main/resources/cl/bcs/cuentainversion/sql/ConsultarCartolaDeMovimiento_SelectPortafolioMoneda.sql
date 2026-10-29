SELECT DISTINCT
	CuentaInversion."IdPortafolio" AS "IdPortafolio"
   ,CuentaInversion."IdMoneda" AS "IdMoneda"
   ,Portafolio."IdEmpleadoEjecutivo"
FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CuentaInversion
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CuentaInversion."IdPortafolio" AND Portafolio."IdCliente" = CuentaInversion."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE 
	(:p_IdCuentaInversion IS NULL OR CuentaInversion."IdCuentaInversion" = :p_IdCuentaInversion)
AND (
		(:p_IdCliente			IS NULL OR CuentaInversion."IdCliente"    = :p_IdCliente)
	AND (:p_IdPortafolio		IS NULL OR CuentaInversion."IdPortafolio" = :p_IdPortafolio)
	AND (:p_IdMoneda			IS NULL OR CuentaInversion."IdMoneda"     = :p_IdMoneda)
	AND (:p_IdProductoAgrupador	IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
);
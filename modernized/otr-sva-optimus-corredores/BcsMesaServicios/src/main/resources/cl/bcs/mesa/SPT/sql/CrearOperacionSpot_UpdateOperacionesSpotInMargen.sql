UPDATE PROC
SET
	 PROC."Margen"		= :Margen
	,PROC."PuntaCompra"	= :PuntaCompra
	,PROC."PuntaVenta"	= :PuntaVenta
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" PROC
WHERE PROC."NumeroOperacion" IN (:NumeroOperacion);

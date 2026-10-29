SELECT VCIPO."IdPortafolio"
	 , VCIPO."OperaCustodiaRV"
	 , VCIPO."OperaCustodiaRFIF"
	 , VCIPO."OperaCuentaInversion"
	 , VCIPO."CodigoIfrs"
	 , TO_NVARCHAR(VCIPO."IdLibroOrdenes") AS "IdLibroOrdenes"
FROM "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" VCIPO
WHERE VCIPO."IdPortafolio" IN (:IdsPortafolios);
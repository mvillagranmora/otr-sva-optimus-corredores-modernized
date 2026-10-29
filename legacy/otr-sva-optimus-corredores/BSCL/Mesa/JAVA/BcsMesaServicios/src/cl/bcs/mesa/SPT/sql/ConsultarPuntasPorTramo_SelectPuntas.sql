SELECT
	 "IdPuntas"
	,"IdEmpresa"
	,"Fecha"
	,"IdUsuario"
	,"AperturaPrecio"
	,"PuntaCompra"
	,"PuntaVenta"
	,"IdMonedaPunta"
	,"CheckPrecioPorTramo"
	,"Estado"
FROM "BSCL.Mesa.BD::SPT.Puntas"
WHERE "IdMonedaPunta" = :IdMoneda
  AND "IdEmpresa" = :IdEmpresa;
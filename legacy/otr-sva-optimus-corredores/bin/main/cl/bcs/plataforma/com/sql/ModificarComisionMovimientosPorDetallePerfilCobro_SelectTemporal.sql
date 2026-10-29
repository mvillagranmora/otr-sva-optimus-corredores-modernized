SELECT 
	  TEMP."IdDetallePerfilCobros"
	 , TEMP."IdBolsa"
	 , TEMP."IdTipoProducto"
	 , TEMP."IdTipoOperacion"
	 , TEMP."IdTipoCanalIngreso"
	 , TEMP."IdPortafolio"
	 , TEMP."IdCobro"
	 , TEMP."IdMoneda"
	 , TEMP."ValorCobro"
	 , TEMP."Borrado"
FROM "#ModificarComisionMovimientosPorDetallePerfilCobro_DetallePerfilCobro" AS TEMP;
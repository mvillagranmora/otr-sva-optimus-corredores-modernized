UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET
	 "INGFechaValorizacionCartera"	= :FechaValorizacionCartera
	,"INGFechaValorizacionAnterior"	= :FechaValorizacionAnterior
	,"INGFechaValorizacionActual"	= :FechaValorizacionActual
	,"INGMontoValorRazonableActivo"	= :MontoValorRazonableActivo
	,"INGMontoValorRazonablePasivo"	= :MontoValorRazonablePasivo
	,"INGMontoValorRazonableNeto"	= :MontoValorRazonableNeto
	,"INGTasaValorRazonableActivo"	= :TasaValorRazonableActivo
	,"INGTasaValorRazonablePasivo"	= :TasaValorRazonablePasivo
WHERE "IdCartera" IN (:IdCartera)
  AND "IdTipoProducto" = 'MEFW'
  AND "IdEmpresa" = :IdEmpresa
;
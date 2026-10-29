SELECT *
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE   "IdEstado" = 'VIG'
AND     "IdPortafolio" in (:Portafolios)
AND     "IdClasificacionTipoInstrumento" = :Instrumento    
AND		"FechaLiquidacion" 	<= :FSistema                
AND     "FechaOperacion" 	<= :FSistema
AND     "IdTipoInstrumento" != 'PAC'
AND     "IdCartera" NOT IN (SELECT "IdCarteraOrigen" FROM "BSCL.Custodia.BD::GTA.Garantia"  WHERE "IdEstado" = 'VIG' )
AND     "IdCartera" NOT IN (SELECT "IdCarteraOrigen" FROM "BSCL.Custodia.BD::CUS.Reserva"  WHERE "IdEstado" = 'VIG' )
AND (
  		("IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S')
  	OR
  		"IdClasificacionTipoInstrumento" <> 'IF'
	)
ORDER BY :Orden ;
  
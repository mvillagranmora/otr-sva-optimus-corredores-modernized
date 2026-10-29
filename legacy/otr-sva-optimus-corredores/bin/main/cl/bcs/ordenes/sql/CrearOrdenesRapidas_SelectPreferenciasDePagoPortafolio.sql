SELECT 
	COALESCE(CPPP."IdPreferenciaPagoPortafolio", 0) AS "IdPreferenciaPagoPortafolio",
	PASO."IdTipoOperacion",
	PASO."IdMoneda",
	PASO."IdPortafolio"
FROM "BSCL.Orden.BD::GBL.CrearOrdenesRapidas_PreferenciasPagoPortafolio_Paso" AS PASO
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS CPPP ON CPPP."IdPortafolio" = PASO."IdPortafolio" AND CPPP."IdMoneda" = PASO."IdMoneda" AND CPPP."IdTipoOperacion" = PASO."IdTipoOperacion" AND CPPP."IdMedioDePago" != 'CIN' AND "PorDefecto" = 'S';
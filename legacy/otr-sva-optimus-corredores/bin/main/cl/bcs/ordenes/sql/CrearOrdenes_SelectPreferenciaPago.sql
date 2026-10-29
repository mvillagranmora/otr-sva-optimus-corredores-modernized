SELECT pago."IdPreferenciaPagoPortafolio" as "IdPreferenciaPagoPortafolio",
		pago."IdMedioDePago",
		pago."IdCuentaBancaria",
		pago."IdTipoCuentaBancaria",
		pago."NumeroCuentaBancaria",
		pago."IdTarjetaBancaria",
		pago."IdTipoTarjetaBancaria",
		pago."NumeroTarjetaBancaria",
		moda."IdModalidadDePago",
		CASE 
			WHEN pago."UsaCuentaBancaria" = 'S' THEN pago."IdBancoCuentaBancaria" 
			WHEN pago."UsaTarjetaBancaria" = 'S' THEN pago."IdBancoTarjetaBancaria" 
			ELSE 0 END AS "IdBanco"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" pago
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" MED on pago."IdMedioDePago" = MED."IdMedioDePago"
LEFT JOIN "BSCL.Plataforma.BD::PAG.ModalidadDePago" moda on MED."IdModalidadDePago" = moda."IdModalidadDePago"
WHERE pago."IdMedioDePago" IS NOT NULL AND "IdPreferenciaPagoPortafolio" IN (:idsPreferenciasPagoPortafolio);

-- Proposito : Obtiene la preferencias de pago para VEPRBO (venta por prepago bono)
-- Autor     : BCS - JVILLAGRAN 
-- Fecha     : 23-04-2018

	SELECT
		("IdEmpresa" || "IdPortafolio"  || "IdMoneda") AS "Llave",
		"IdPortafolio",
		"IdTipoOperacion",
		"IdCuentaBancaria",
		"IdBancoCuentaBancaria",
		"NumeroCuentaBancaria",
		"IdTipoCuentaBancaria",
		"NombreTipoCuentaBancaria",
		"IdMedioDePago",
		"IdMoneda",		
		"IdTarjetaBancaria",
		"IdTipoTarjetaBancaria",
		"NumeroTarjetaBancaria"
	
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"
	WHERE "IdTipoOperacion" IN ('VEPRBO') --  VEPRBO: VENTA PREPAGO BONO
	AND   "IdEstado" = 'ACT'
	AND ("IdEmpresa" || "IdPortafolio"  || "IdMoneda") IN (:LlavePrePagPor);

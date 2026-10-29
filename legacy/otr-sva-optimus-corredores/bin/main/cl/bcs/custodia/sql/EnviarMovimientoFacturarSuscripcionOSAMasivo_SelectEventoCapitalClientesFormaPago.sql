SELECT  
	EvCapCliFPag."IdEventoCapitalClienteFormaDePago",
	EvCapCliFPag."IdEventoCapitalCliente",
	EvCapCliFPag."IdTipoMovimiento",
	EvCapCliFPag."IdModalidadPago",
	EvCapCliFPag."IdMedioDePago",
	EvCapCliFPag."NombreMedioDePago",
	EvCapCliFPag."IdBanco",
	EvCapCliFPag."NombreBanco",
	EvCapCliFPag."IdTipoCuentaBancaria",
	EvCapCliFPag."NombreTipoCuentaBancaria",
	EvCapCliFPag."IdCuentaBancaria",
	EvCapCliFPag."NombreCuentaBancaria",
	EvCapCliFPag."NumeroCuentaBancaria",
	EvCapCliFPag."IdTipoTarjetaBancaria",
	EvCapCliFPag."NumeroTarjetaBancaria",
	EvCapCliFPag."NumeroMedioDePago",
	EvCapCliFPag."Sucursal",
	TO_VARCHAR(EvCapCliFPag."FechaVctoDocumento")  AS "FechaVctoDocumento",
	EvCapCliFPag."Monto",
	TO_VARCHAR(EvCapCliFPag."FechaHoraIngresoRegistro") AS "FechaHoraIngresoRegistro",
	EvCapCliFPag."IdUsuarioIngreso",
	EvCapCliFPag."IdEstado",
	EvCapCliFPag."IdPreferenciaPagoPortafolio",
	PrefPag."IdMoneda",
	PrefPag."IdTarjetaBancaria"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"									AS EvCap
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 					AS EvCapCli
	ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital" 	
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS EvCapCliFPag
	ON EvCapCli."IdEventoCapitalCliente" = EvCapCliFPag."IdEventoCapitalCliente"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"	AS PrefPag
	ON (PrefPag."IdPortafolio" = EvCapCli."IdPortafolio")
	
WHERE EvCapCli."IdEventoCapitalCliente" in (:ListaIdEventoCapitalCliente)
  AND EvCapCli."IdTipoEventoCapital" 	= 'SU'
  AND PrefPag."IdTipoOperacion" 	 	= 'INGSUS'

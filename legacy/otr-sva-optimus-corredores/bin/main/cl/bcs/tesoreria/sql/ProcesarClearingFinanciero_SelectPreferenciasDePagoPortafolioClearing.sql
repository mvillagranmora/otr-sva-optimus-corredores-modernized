SELECT 
	POR."IdPreferenciaPago",
	POR."IdCuentaBancaria",
	POR."IdBancoCuentaBancaria",
	POR."NombreBancoCuentaBancaria",
	POR."NumeroCuentaBancaria",
	POR."IdEstado",
	CASE 
  		WHEN POR."IdTipoOperacion" = 'INGCLE' THEN 'MOVEGR'
  		WHEN POR."IdTipoOperacion" = 'EGRCLE' THEN 'MOVING'
	END AS "TipoMovimiento",
	POR."TipoOperacion",
	POR."NombreTipoCuentaBancaria",
	POR."IdTipoCuentaBancaria",
	POR."IdMedioDePago",
	POR."MedioDePago",
	POR."IdMoneda",
	POR."RecibePagaPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" POR
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT ON ( POR."IdPortafolio" = PORT."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON ( SOC."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion" CAM ON (CAM."IdPersona" = SOC."IdPersona" AND CAM."IdPais" = :IdPaisCamara AND CAM."CodigoPortafolio" = PORT."CodigoPortafolio")
WHERE POR."IdPreferenciaPago" IN ('INGCLE_TLR_CLP','EGRCLE_TLR_CLP')
AND POR."IdTipoOperacion" 	IN ('INGCLE','EGRCLE')		
AND POR."IdEstado" 			= 'ACT'
AND POR."IdEmpresa"			= :IdEmpresa;
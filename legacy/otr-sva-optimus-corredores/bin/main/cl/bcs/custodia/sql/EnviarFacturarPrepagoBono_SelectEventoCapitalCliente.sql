SELECT
	 ECC."IdEventoCapital"
	,ECC."IdEventoCapitalCliente"
	,ECC."IdTipoEventoCapital"
	,ECC."IdEstado"
	,EC."IdEmpresa"
	,ECC."IdMovimientoComprobanteFac"
	
	,COALESCE(SOC."IdCliente", 0) AS "IdCliente" 	
	,ECC."IdPortafolio"
	,ECC."IdInstrumento"
	,INS."IdEmisor"
	,ECC."IdMoneda"
	,ECC."IdBoveda"
	,ECC."IdBolsa"
	,ECC."CantidadNueva"
	,ECC."PrecioNuevo"
	,ECC."MontoInteres"
	,ECC."Total"
	,ECC."IdCuentaCustodioPortafolio"

FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS ECC
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EC ON (EC."IdEventoCapital" = ECC."IdEventoCapital")
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdSocioDeNegocio" = ECC."IdSocioNegocio")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = ECC."IdInstrumento")
WHERE "IdEventoCapitalCliente" IN (:IdEventoCapitalCliente);


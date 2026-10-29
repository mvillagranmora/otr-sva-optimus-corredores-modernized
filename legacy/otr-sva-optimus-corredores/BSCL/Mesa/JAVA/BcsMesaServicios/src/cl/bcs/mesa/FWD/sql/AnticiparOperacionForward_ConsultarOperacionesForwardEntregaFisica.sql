SELECT
	 CAR."IdCartera"
	,INS."IdInstrumento"
	,CAR."IdTipoOperacion"
	,CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdEstado"
	,CAR."EGRIdTipoOperacion"
	,CAR."EGRFechaOperacion"
	,CAR."FechaVencimientoOperacion"
	,CAR."IdMonedaPrincipal"
	,CAR."IdMonedaSecundaria"
	,CAR."INGMontoMonedaPrincipal"
	,CAR."INGMontoMonedaSecundaria"
	,CAR."EGRMontoMonedaPrincipal"
	,CAR."EGRMontoMonedaSecundaria"
	,CAR."ValorTipoCambioCierre"
	,CAR."ValorParidadCierre"
	,CAR."ValorTipoCambioCosto"
	,CAR."ValorParidadCosto"
	,COALESCE(MES."IdMesa",0) AS "IdMesa"
	,CAR."IdAgente"
	,CAR."INGGlosa"
	,CAR."FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
	ON (INS."IdTipoInstrumento" = 'SPT' AND
		INS."Nemotecnico" IN ('INTERB','ARBINT') AND
		CAR."Nemotecnico" = CASE WHEN INS."Nemotecnico" = 'INTERB' THEN 'SECANO' 
								 WHEN INS."Nemotecnico" = 'ARBINT' THEN 'ARBITR' END)
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" AS MES
	ON (MES."IdTipoProducto" = 'MEME' AND
		MES."IdEstadoRegistro" = 'ACT' AND
		MES."PorDefecto" = 'S')
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa" AND
		PAR."ValorParametro" = 'ENTREGA_FISICA')
WHERE CAR."IdCartera" IN (:IdCarteraParteAnticipada)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND (CAR."IdMonedaPrincipal" <> 'UF' AND CAR."IdMonedaSecundaria" <> 'UF')
  AND CAR."IdEstado" = 'ANT'
  AND CAR."IdEmpresa" = :IdEmpresa;

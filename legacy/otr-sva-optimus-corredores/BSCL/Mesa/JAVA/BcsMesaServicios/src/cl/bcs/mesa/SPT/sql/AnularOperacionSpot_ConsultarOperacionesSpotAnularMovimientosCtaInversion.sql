SELECT
	 CAST (SPT."IdOperacionSpot" AS NVARCHAR) AS "NumeroOperacion"
	,SPT."IdOperacionSpot"
	,SPT."IdCliente"
	,SPT."IdPortafolio"
	,CABN."IdCargoAbonoMovimiento" as "IdMovimiento"
	,SPT."IdTipoOperacion"
	,INS."Nemotecnico"
	,SPT."MontoMonedaSecundaria"
	,SPT."IdMonedaPrincipal"
	,SPT."TipoCambioCierre"
	,SPT."Estado"
	,CABN."IdEstado"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (SPT."IdInstrumento" = INS."IdInstrumento")
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CABN ON SPT."IdOperacionSpot" = CABN."IdOperacion" AND CABN."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
WHERE SPT."IdOperacionSpotOrigen" IN (:NumeroOperacion)
  AND SPT."Estado" = 'VIG'
  AND SPT."IdEmpresa" = :IdEmpresa
  AND SPT."OperacionFicticia" = 'S'
  AND (CABN."IdEstado" = 'CRT');
SELECT
	 CAST (SPT."IdOperacionSpot" AS NVARCHAR) AS "NumeroOperacion"
	,SPT."IdOperacionSpot"
	,SPT."IdCliente"
	,SPT."IdPortafolio"
	,IE."IdIngresoEgreso" AS "IdMovimiento"
	,SPT."IdTipoOperacion"
	,INS."Nemotecnico"
	,SPT."MontoMonedaSecundaria"
	,SPT."IdMonedaPrincipal"
	,SPT."TipoCambioCierre"
	,SPT."Estado"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (SPT."IdInstrumento" = INS."IdInstrumento")
INNER JOIN BSCL."BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON IE."IdOperacion" = SPT."IdOperacionSpot" AND IE."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
WHERE SPT."IdOperacionSpotOrigen" IN (:NumeroOperacion)
  AND SPT."Estado" = 'VIG'
  AND SPT."IdEmpresa" = :IdEmpresa
  AND SPT."OperacionFicticia" = 'S'
  AND (IE."IdEstado" = 'ING');
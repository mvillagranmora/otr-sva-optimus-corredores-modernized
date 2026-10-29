SELECT DISTINCT
	 CAR."IdCartera"
	,CAR."FolioCartera"
	,CAR."IdOperacion"
	,CAR."IdEstado"
	,CAR."IdModalidadCumplimiento"	AS "Modalidad"
	,PAR."ValorParametro"			AS "ModalidadLiquidacionValor"
	,CAR."EGRIdTipoOperacion"
	,CAR."EGRIdSpotAsociado"
	,COALESCE(CTA."IdEstado",'')	AS "IdEstadoCuentaInversion"
	--,COALESCE(TES."IdEstado",'')	AS "IdEstadoTesoreria"
	,COALESCE(COM."IdEstado",'')	AS "IdEstadoTesoreria"
	,COM."IdComprobante"	AS "ComprobanteTesoreria"
	,COALESCE(SPT."Estado",'')		AS "IdEstadoSpot"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" AS CAR
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
	ON (OPE."IdOperacion" = CAR."IdOperacion" AND
		OPE."IdTipoProducto" = CAR."IdTipoProducto")
INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PAR
	ON (PAR."IdParametro" = CAR."IdModalidadCumplimiento" AND
		PAR."NombreTipoParametro" = 'ModalidadLiquidacion' AND
		PAR."IdEmpresa" = CAR."IdEmpresa")
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CTA
	ON (CAR."FolioCartera" = CTA."IdComprobante" AND
		CAR."FolioCartera" = CTA."IdOperacion")
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" TES
	ON (CAR."FolioCartera" = TES."IdComprobanteOrigen" AND
		CAR."IdOperacion" = TES."IdOperacion")
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
	ON (CAR."EGRIdSpotAsociado" = SPT."IdOperacionSpot" AND
		CAR."FolioCartera" = SPT."FolioOrigenFW")
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COM
	ON (TES."IdComprobanteOrigen" = COM."IdComprobanteOrigen")
WHERE CAR."IdCartera" IN (:IdCartera)
  AND CAR."IdTipoProducto" = 'MEFW'
  AND CAR."IdEstado" IN ('LIQ','VCD')
  AND CAR."EGRIdTipoOperacion" = 'ANTFWD'
  AND OPE."IdEmpresa" = :IdEmpresa;

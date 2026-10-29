SELECT 
"IdSolicitudAporteRescate"
,"IdPersona"
,"IdCuentaBancaria"
,"IdBanco"
,"IdTipoCuentaBancaria"
,"IdMoneda"
,"NumeroCuentaBancaria"
,"NombreTipoCuentaBancaria"
FROM(
	SELECT 
	row_number() OVER (partition by TRA_DET."IdSolicitudAporteRescate" order BY TRA_DET."IdSolicitudAporteRescate" DESC) AS "Row"
	,TRA_DET."IdSolicitudAporteRescate"
	,ENT."IdPersona"
	,COALESCE(CUENTA."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria"
	,COALESCE(CUENTA."IdBanco",TO_BIGINT(0)) AS "IdBanco"
	,COALESCE(CUENTA."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancaria"
	,COALESCE(CUENTA."IdMoneda",'') AS "IdMoneda"
	,COALESCE(CUENTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
	,COALESCE(TIPO_CUENTA."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria"
	FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" TRA_DET
	INNER JOIN "BSCL.FondoMutuo.BD::TRA.Traspaso" TRA ON TRA."IdTraspaso" = TRA_DET."IdTraspaso"
	INNER JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" ENT ON ENT."IdEntidadRecaudadora" = TRA."IdEntidadRecaudadora"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CUENTA ON CUENTA."IdPersona" = ENT."IdPersona" AND CUENTA."IdEstado" = 'ACT' 
	LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TIPO_CUENTA ON TIPO_CUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria"
	WHERE TRA_DET."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
)
WHERE "Row" = 1
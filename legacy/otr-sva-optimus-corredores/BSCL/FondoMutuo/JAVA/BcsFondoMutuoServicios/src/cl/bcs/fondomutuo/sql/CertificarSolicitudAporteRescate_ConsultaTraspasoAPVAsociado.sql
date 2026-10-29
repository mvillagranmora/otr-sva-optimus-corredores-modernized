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
	row_number() OVER (partition by OT."IdSolicitudAporteRescate" order BY OT."IdSolicitudAporteRescate" DESC) AS "Row"
	,OT."IdSolicitudAporteRescate"
	,PER."IdPersona"
	,COALESCE(CUENTA."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria"
	,COALESCE(CUENTA."IdBanco",TO_BIGINT(0)) AS "IdBanco"
	,COALESCE(CUENTA."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancaria"
	,COALESCE(CUENTA."IdMoneda",'') AS "IdMoneda"
	,COALESCE(CUENTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
	,COALESCE(TIPO_CUENTA."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria"
	FROM "BSCL.APV.BD::TRA.OrdenTraspaso" AS OT
	INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS TRA ON OT."IdTraspaso" = TRA."IdTraspaso" AND TRA."RazonRetiro" != 'RES'
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."DocumentoIdentidad" = TRA."DocumentoEntidad"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CUENTA ON CUENTA."IdPersona" = PER."IdPersona" AND CUENTA."IdEstado" = 'ACT' 
	LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TIPO_CUENTA ON TIPO_CUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria" 
	WHERE OT."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
)
WHERE "Row" = 1;
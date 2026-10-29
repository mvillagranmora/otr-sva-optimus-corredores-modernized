SELECT EV."IdEventoCapitalCliente" AS "IdEventoCapitalClienteCli", ECC."IdEventoCapitalCliente" AS "IdEventoCapitalClienteCnt",
ECC."Total", ECC."MontoImpuestoExtranjero", ECC."MontoGastoTransferencia" 
FROM (SELECT "IdEventoCapital","IdEventoCapitalCliente","TipoSocioNegocio","IdConceptoGarantia","TipoCartera"
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" WHERE "IdEventoCapitalCliente" IN (:IdsEventoCapitalCliente) ) EV
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECC ON (
	EV."IdEventoCapital"=ECC."IdEventoCapital" AND
	EV."IdConceptoGarantia"=ECC."IdConceptoGarantia" AND
	EV."TipoCartera"=ECC."TipoCartera" AND
	ECC."TipoSocioNegocio" = 'CNT'	
)
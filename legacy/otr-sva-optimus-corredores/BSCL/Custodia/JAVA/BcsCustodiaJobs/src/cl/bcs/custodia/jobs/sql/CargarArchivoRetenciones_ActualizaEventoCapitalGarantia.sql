UPDATE EvCapCli SET
	EvCapCli."MontoRetencionArchivo"		= :Retencion,
	EvCapCli."DiferenciaMontoRetencion"		= EvCapCli."MontoRetencion" - :Retencion
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
WHERE EvCapCli."IdEventoCapital" = :IdEventoCapital
  AND EvCapCli."IdSocioNegocio" IN (:IdSocioNegocio)
  AND EvCapCli."IdPortafolio" IN (:IdPortafolio)
  AND EvCapCli."TipoCartera" = 'GAR'
  AND EvCapCli."IdBoveda" = 'CAV'
  AND EvCapCli."CantidadDerecho" > 0;
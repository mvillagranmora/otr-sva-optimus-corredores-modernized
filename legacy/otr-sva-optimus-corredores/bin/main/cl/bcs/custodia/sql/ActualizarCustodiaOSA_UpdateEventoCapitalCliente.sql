UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado" = 'ACT'
	, "IdCartera" = :IdCartera
	, "FolioCartera" = :FolioCartera
WHERE "IdEventoCapital" = :IdEventoCapital
  AND "IdEventoCapitalCliente" = :IdEventoCapitalCliente
  AND "IdTipoEventoCapital" = 'EP';
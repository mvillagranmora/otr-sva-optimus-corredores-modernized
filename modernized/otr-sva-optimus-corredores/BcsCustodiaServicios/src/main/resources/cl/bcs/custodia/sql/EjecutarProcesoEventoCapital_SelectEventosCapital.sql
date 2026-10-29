SELECT	"IdEventoCapital",
		"IdTipoEventoCapital",
		"Secuencia",
		"NumeroDividendo",
		"IdClasificacionTipoInstrumento",
		"IdTipoInstrumento",
		"IdInstrumento",
		"Nemo"								AS "Nemotecnico",
		"Factor",
		"Paridad",
		"IdMoneda",
		"IdInstrumentoNuevo",
		"NemoNuevo",
		"FactorNuevo",
		"FechaLimite",
		"FechaPago",
		"IdEstado",
		"Indice"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "FechaLimite" = '2016-07-25'
  AND "IdTipoEventoCapital" = 'DV'
  AND "IdEstado" IN ('VIG','MOD');
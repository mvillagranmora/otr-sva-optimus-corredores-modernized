SELECT 
	"IdEventoCapital",
	"FechaLimite",
	"Factor", 
	"IdEstado",
	"IdTipoEventoCapital",
	"FechaPago",
	"NumeroDividendo",
	"Descripcion",
	"EsInscrito",
	"Impuesto",
	"FactorNuevo",
	"IdMoneda",
    "IdMonedaLiquidacion",
    "Paridad",
    "IdBoveda",
    "ISFUT",
    "TasaEfectiva",
    "Secuencia"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" 
WHERE "IdEmpresa" = :p_IdEmpresa 
AND ("IdEventoCapital" IN (:p_IdEventoCapital)
OR ("Secuencia" IN (:p_Secuencia) AND "IdTipoEventoCapital" = 'SU'));
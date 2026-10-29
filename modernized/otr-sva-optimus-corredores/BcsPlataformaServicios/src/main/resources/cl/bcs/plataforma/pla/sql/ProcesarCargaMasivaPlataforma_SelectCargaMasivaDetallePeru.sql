SELECT 
	CM."IdTipoCargaMasiva",
	CMD."IdCargaMasiva",
	CMD."IdCargaMasivaDetalle",
	CMD."IdEstado"	AS "EstadoDetalle",
	CMD."ObservacionValidacion",
	CMD."Nemotecnico",
	CMD."RF_PorcentajeDescuento",
	CMD."RV_IdEntidadClasificadoraRiesgo",
	CMD."RV_Valor"
FROM "BSCL.Plataforma.BD::PLA.CargaMasivaDetalle" 		AS CMD 
	INNER JOIN "BSCL.Plataforma.BD::PLA.CargaMasiva"	AS CM
		ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
WHERE CM."IdEmpresa" = :IdEmpresa
  AND CMD."IdCargaMasiva" = :IdCargaMasiva
 

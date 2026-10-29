SELECT
	DISTINCT(INS."Nemotecnico") as "Nemotecnico",
	 0 AS "IdInstrumento",
	INS."IdClasificacionTipoInstrumento",
	INS."IdTipoInstrumento",
	INS."IdMoneda"
FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" INS
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
	ON (INS."IdCliente" = MES."IdCliente" AND 
		INS."IdEmpresa" = MES."IdEmpresa" 
		AND MES."IdEstadoMesa" = 'C' 
		AND MES."IdEstadoRegistro" = 'ACT' AND 
		MES."IdTipoProducto" = 'MERF')
WHERE INS."IdEmpresa" = :p_IdEmpresa
AND INS."Nemotecnico" IN(:nemotecnicosNoRepetidos, :nemotecnicosRepetidos);
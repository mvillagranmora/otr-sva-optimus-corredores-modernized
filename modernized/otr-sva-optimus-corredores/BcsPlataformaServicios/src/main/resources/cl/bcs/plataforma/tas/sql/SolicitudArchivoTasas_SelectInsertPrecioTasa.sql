	SELECT
		DISTINCT CT."Nemotecnico" as "Nemotecnico",
		0 AS "IdInstrumento",
		CT."IdClasificacionTipoInstrumento",
		CT."IdTipoInstrumento",
		CT."IdMoneda"
	FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarInstrumentoCustodiaParaTasas" CT
	INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
		ON (CT."IdCliente" = MES."IdCliente" AND 
			CT."IdEmpresa" = MES."IdEmpresa" 
			AND MES."IdEstadoMesa" = 'C' 
			AND MES."IdEstadoRegistro" = 'ACT' AND 
			MES."IdTipoProducto" = 'MERF')
	WHERE CT."IdEmpresa" = :p_IdEmpresa
	AND CT."Nemotecnico" IN(:instrumentos)
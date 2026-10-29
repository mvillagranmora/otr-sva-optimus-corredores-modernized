SELECT 	 DJ."IdCliente"
		,DJ."AnoAhorro"
		,DJ."AhorrosDeposito" AS "AhorrosDJ"
		,DJ."RetirosDeposito" AS "RetirosDJ"
		,Bonificacion."IdBonificacion"
		,Bonificacion."MontoBonificacionAfiliadoNeto"
		,Bonificacion."IdEstado"
		,Bonificacion."MontoReajustadoPagadoAfiliado"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos" DJ
INNER JOIN "BSCL.APV.BD::INV.Bonificacion" Bonificacion
	ON DJ."IdCliente" = Bonificacion."IdCliente"
	AND DJ."AnoAhorro" = Bonificacion."AnoAhorro"
	AND Bonificacion."IdEmpresa" = :IdEmpresa
WHERE DJ."AnoAhorro" = :AnoAhorro
AND Bonificacion."IdEstado" = 'ING' AND DJ."AhorrosDeposito" != 0;
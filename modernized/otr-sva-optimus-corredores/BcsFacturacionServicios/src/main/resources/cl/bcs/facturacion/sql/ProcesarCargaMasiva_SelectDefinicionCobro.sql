SELECT top 1
	'' AS "IdCobro",
	DEF."Descripcion" AS "NombreCobro",
		IFNULL(DEF."PorcentualFijo_ValorCobro",0) AS "ValorCobro"
		FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DEF
		LEFT JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" CLASE ON DEF."IdClaseCobro" = CLASE."IdClaseCobro"
		INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" TIPO ON DEF."IdTipoCobro" = TIPO."IdTipoCobro"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO ON DEF."IdTipoProducto" = PRO."IdTipoProducto"
		WHERE "CodigoDefinicionCobro" = :CodigoDefinicionCobro;
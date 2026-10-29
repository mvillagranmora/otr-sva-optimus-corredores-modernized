SELECT
	MOD."IdTipoEntidad" AS "SubRegistro",
	MOD."CampoModificado" AS "CampoRegistro",
	CASE
		WHEN MOD."CampoModificado" = 'IdBanco' AND CAST(BANCANT."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", '') THEN BANCANT."NombreBanco"
		WHEN MOD."CampoModificado" = 'IdTipoTarjetaBancaria' AND TPTARJANT."IdTipoTarjetaBancaria" = COALESCE(MOD."ValorAntiguo", '') THEN TPTARJANT."NombreTipoTarjetaBancaria"
		ELSE MOD."ValorAntiguo"
	END AS "ValorAntiguo",
	CASE
		WHEN MOD."CampoModificado" = 'IdBanco' AND CAST(BANCNUE."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", '') THEN BANCNUE."NombreBanco"
		WHEN MOD."CampoModificado" = 'IdTipoTarjetaBancaria' AND TPTARJNUE."IdTipoTarjetaBancaria" = COALESCE(MOD."ValorNuevo", '') THEN TPTARJNUE."NombreTipoTarjetaBancaria"
		ELSE MOD."ValorNuevo"
	END AS "ValorNuevo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" MOD
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BANCANT ON (CAST(BANCANT."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", ''))
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BANCNUE ON (CAST(BANCNUE."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", ''))
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" TPTARJANT ON (TPTARJANT."IdTipoTarjetaBancaria" = COALESCE(MOD."ValorAntiguo", ''))
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" TPTARJNUE ON (TPTARJNUE."IdTipoTarjetaBancaria" = COALESCE(MOD."ValorNuevo", ''))
WHERE MOD."IdSolicitudCambio" IN (:p_IdSolicitudCambio)

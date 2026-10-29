UPDATE TAS
			SET
				TAS."Tasa"				= :TasaMod,
				TAS."TasaEstimada"		= :TasaEstimadaMod,
				TAS."Plazo"				= :Plazo,
				TAS."IdEstado"			= 'CAR',
				TAS."IdUsuarioModificacion" = :p_IdUsuario,
				TAS."FechaModificacion"	= CURRENT_UTCTIMESTAMP
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
			WHERE TAS."IdPrecioTasa" = :IdPrecioTasa
			AND TAS."IdEstado" IN ('SOL','CRS','MOD','CAR');
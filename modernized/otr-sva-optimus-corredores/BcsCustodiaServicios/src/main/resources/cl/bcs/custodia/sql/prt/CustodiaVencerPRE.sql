SELECT DISTINCT
	c."FolioEmprestito"

FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" c

LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON TPO."CodigoTipoIndicador" = CASE WHEN c."IdClasificacionTipoInstrumentoSubyacente" = 'RF' THEN 'ORDOCUSTRF' ELSE 'ORDOCUSTRV' END
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND ATR."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdEntidad" = TO_VARCHAR(c."IdPortafolio")

LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO2 ON TPO2."CodigoTipoIndicador" = CASE WHEN c."IdClasificacionTipoInstrumentoSubyacente" = 'RF' THEN 'ORDOCUSTRF' ELSE 'ORDOCUSTRV' END
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR2 ON ATR2."IdTipoIndicador" = TPO2."IdTipoIndicador" AND ATR2."IdTipoEntidad" = 'PORTAFOLIO' AND ATR2."IdEntidad" = TO_VARCHAR(c."IdPortafolioPrestamo")

LEFT JOIN  "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TipPre ON (TipPre."IdTipoPrestamo" = c."IdTipoPrestamo")

WHERE c."IdEmpresa" = :p_IdEmpresa AND c."FechaVencimientoEmprestito" = (:p_Fecha)
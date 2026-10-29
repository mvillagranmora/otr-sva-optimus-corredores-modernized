SELECT 
        inst."IdInstrumento",
        inst."Nemotecnico",
        inst."ISIN",
        inst."CorteMinimo",
        inst."EstaInscrito",
        inst."EsDivisible",
        inst."RazonSocial",
        inst."FechaEmision",
        inst."FechaVencimiento",
        inst."IdInstrumentoRelacionado",
        inst."IdEmisor",
        IDENT."DocumentoIdentidad" AS "DocumentoIdentidad",
        inst."IdTipoReajuste",
        inst."Reajustable",
        inst."IdMoneda",
        inst."IdMonedaEmision",
        inst."IdTipoInstrumento",
        inst."IdEstado",
        inst."DecimalesPrecio",
        tipoins."Nombre" AS "NombreTipoInstrumento",
        tipoins."IdClasificacionTipoInstrumento",
        clasi."Nombre" AS "NombreClasificacionTipoInstrumento",
        CASE PER."Tipo" 
            WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' ||PER."ApellidoMaterno" 
            ELSE PER."RazonSocial"
        END AS "NombreCompletoEmisor",
        inst."Tamano" AS "Tamano",
        inst."TipoValorizacion",
        inst."IndicadorBolson",
        inst."Tasa",
        bon."TasaEmision",
        inst."Precio",
        inst."ValorizaPor",
        inst."TieneCupon",
        inst."PorcentajeCastigo",
        inst."FrecuenciaCupon",
        inst."InstrumentoAcreditado",
        inst."PrecioStrike",
        inst."EsPutEsCall",
        inst."EsAutorizado",
        inst."IdInstrumentoRaiz",
		inst."EsInstrumentoCalificado",
        COUNT(inst."Nemotecnico") OVER(PARTITION BY inst."Nemotecnico") AS "ConteoNemotecnico"
 
FROM
(
    SELECT
        inst."IdInstrumento",
        inst."Nemotecnico",
        inst."ISIN",
        inst."CorteMinimo",
        inst."EstaInscrito",
        inst."EsDivisible",
        inst."RazonSocial",
        inst."FechaEmision",
        inst."FechaVencimiento",
        inst."IdInstrumentoRelacionado",
        inst."IdEmisor",
        inst."IdTipoReajuste",
        inst."Reajustable",
        inst."IdMoneda",
        inst."IdMonedaEmision",
        inst."IdTipoInstrumento",
        inst."IdEstado",
        inst."DecimalesPrecio",
        inst."Tamano" AS "Tamano",
        inst."TipoValorizacion",
        inst."IndicadorBolson",
        inst."Tasa",
        inst."Precio",
        inst."ValorizaPor",
        inst."TieneCupon",
        inst."PorcentajeCastigo",
        inst."FrecuenciaCupon",
        inst."InstrumentoAcreditado",
        inst."PrecioStrike",
        inst."EsPutEsCall",
        inst."EsAutorizado",
        inst."IdInstrumentoRaiz",
        inst."EsInstrumentoCalificado"
    FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins             ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
    WHERE inst."IdTipoInstrumento" not in ('SIM','RRV','RRF','PAC') 
        AND (inst."IdTipoInstrumento" IN (:idsTipoInstrumento) OR 1 = :filtroPorTipo) 
        AND (tipoins."IdClasificacionTipoInstrumento" IN (:idsClasificacionTipoInstrumento) OR 1 = :filtroPorClasificacion)
        AND (inst."IdInstrumento" IN (:IdInstrumento) OR inst."Nemotecnico" IN (:Nemotecnico))
        AND inst."IdEstado" IN (:estadoInstrumento)
        AND inst."EsAutorizado" =  CASE inst."IdTipoInstrumento" 
                            WHEN 'CFI' THEN  'S' 
                            WHEN 'CFX' THEN  'S'
                            ELSE inst."EsAutorizado" END
         AND (:filtroInstrumentoNegociacion = 1 OR inst."IdInstrumentoRaiz" IN (:filtroInstrumentoNegociacion))		
    UNION
    SELECT
        inst."IdInstrumento",
        inst."Nemotecnico",
        inst."ISIN",
        inst."CorteMinimo",
        inst."EstaInscrito",
        inst."EsDivisible",
        inst."RazonSocial",
        inst."FechaEmision",
        inst."FechaVencimiento",
        inst."IdInstrumentoRelacionado",
        inst."IdEmisor",
        inst."IdTipoReajuste",
        inst."Reajustable",
        inst."IdMoneda",
        inst."IdMonedaEmision",
        inst."IdTipoInstrumento",
        inst."IdEstado",
        inst."DecimalesPrecio",
        inst."Tamano" AS "Tamano",
        inst."TipoValorizacion",
        inst."IndicadorBolson",
        inst."Tasa",
        inst."Precio",
        inst."ValorizaPor",
        inst."TieneCupon",
        inst."PorcentajeCastigo",
        inst."FrecuenciaCupon",
        inst."InstrumentoAcreditado",
        inst."PrecioStrike",
        inst."EsPutEsCall",
        inst."EsAutorizado",
        inst."IdInstrumentoRaiz",
        inst."EsInstrumentoCalificado"
    FROM "BSCL.Plataforma.BD::INS.Instrumento" AS inst
    LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" sub on sub."IdTipoInstrumento" IN ('SIM','RRV','RRF') and inst."IdInstrumento" = sub."IdInstrumento" and sub."IdInstrumento" IS NOT NULL
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins             ON tipoins."IdTipoInstrumento" = sub."IdTipoInstrumento"
    WHERE inst."IdTipoInstrumento" not in ('SIM','RRV','RRF','PAC') 
        AND (sub."IdTipoInstrumento" IN (:idsTipoInstrumento) OR 1 = :filtroPorTipo) 
        AND (tipoins."IdClasificacionTipoInstrumento" IN (:idsClasificacionTipoInstrumento) OR 1 = :filtroPorClasificacion)
        AND (inst."IdInstrumento" IN (:IdInstrumento) OR inst."Nemotecnico" IN (:Nemotecnico))
        AND inst."IdEstado" IN (:estadoInstrumento)
        AND inst."EsAutorizado" =  CASE inst."IdTipoInstrumento" 
                                        WHEN 'CFI' THEN  'S' 
                                        WHEN 'CFX' THEN  'S'
                                        ELSE inst."EsAutorizado" END
        AND (:filtroInstrumentoNegociacion = 1 OR inst."IdInstrumentoRaiz" IN (:filtroInstrumentoNegociacion))		
) AS INST
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" AS bon            ON bon."IdInstrumento" = inst."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS tipoins             ON tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS clasi             ON clasi."IdClasificacionTipoInstrumento" = tipoins."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi             on inst."IdEmisor" = emi."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER on PER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" 

	

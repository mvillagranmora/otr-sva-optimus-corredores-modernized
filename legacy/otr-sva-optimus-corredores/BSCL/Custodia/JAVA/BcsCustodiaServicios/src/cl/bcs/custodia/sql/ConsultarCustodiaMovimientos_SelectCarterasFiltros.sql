SELECT
CAR."IdCartera"
,CAR."FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (CAR."FolioCartera" = ULTSUB."FolioCarteraRelacionada")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as SBY on (SBY."IdSubyacente" = ULTSUB."IdSubyacente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CAR."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORTAFOLIO."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
INNER JOIN "BSCL.Custodia.BD::CUS.ConsultarCustodiaMovimientos_Filtros" AS VP ON
(
		CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else CAR."IdCliente" END
	AND CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else CAR."IdPortafolio" END
	AND CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else CAR."FolioCartera" END
	AND CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else CAR."IdClasificacionTipoInstrumento" END
	AND COALESCE(SBY."IdTipoInstrumento", CAR."IdTipoInstrumento") = CASE WHEN VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" ELSE COALESCE(SBY."IdTipoInstrumento", CAR."IdTipoInstrumento") END
	AND COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") end
	AND CAR."Nemotecnico" = case when VP."Nemotecnico" <> '' then VP."Nemotecnico" else CAR."Nemotecnico" END
	)
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO ON TipoInstrumento."IdTipoInstrumento" = CAR."IdTipoInstrumento"
WHERE
	CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdTipoInstrumento" NOT IN (:NotInIdTipoInstrumento)
AND CAR."INGFechaOperacion" >= :FechaInicio
AND CAR."INGFechaOperacion" <= :FechaTermino
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND :TipoOperacion IN ('INGRESO','')
AND CAR."IdConcepto" = CASE WHEN :IdConcepto <> '' THEN :IdConcepto ELSE CAR."IdConcepto" END
AND CAR."IdBoveda" = CASE WHEN :IdBoveda <> '' THEN :IdBoveda ELSE CAR."IdBoveda" END
AND CAR."IdClasificacionTipoInstrumento" != 'DV'
UNION ALL
SELECT
CAR."IdCartera"
,CAR."FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ULTSUB on (CAR."FolioCartera" = ULTSUB."FolioCarteraRelacionada")
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as SBY on (SBY."IdSubyacente" = ULTSUB."IdSubyacente")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = CAR."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = PORTAFOLIO."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
INNER JOIN "BSCL.Custodia.BD::CUS.ConsultarCustodiaMovimientos_Filtros" AS VP ON
(
		CAR."IdCliente" 		= case when VP."IdCliente" > 0 then VP."IdCliente" else CAR."IdCliente" END
	AND CAR."IdPortafolio" 		= case when VP."IdPortafolio" > 0 then VP."IdPortafolio" else CAR."IdPortafolio" END
	AND CAR."FolioCartera"		= case when VP."FolioCartera" > 0 then VP."FolioCartera" else CAR."FolioCartera" END
	AND CAR."IdClasificacionTipoInstrumento" = case when VP."IdClasificacionTipoInstrumento" <> '' then VP."IdClasificacionTipoInstrumento" else CAR."IdClasificacionTipoInstrumento" END
	AND COALESCE(SBY."IdTipoInstrumento", CAR."IdTipoInstrumento") = CASE WHEN VP."IdTipoInstrumento" <> '' then VP."IdTipoInstrumento" ELSE COALESCE(SBY."IdTipoInstrumento", CAR."IdTipoInstrumento") END
	AND COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") 	= case when VP."IdInstrumento" > 0 then VP."IdInstrumento" else COALESCE(SBY."IdInstrumento",CAR."IdInstrumento") end
	AND CAR."Nemotecnico" = case when VP."Nemotecnico" <> '' then VP."Nemotecnico" else CAR."Nemotecnico" END
)
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO ON TipoInstrumento."IdTipoInstrumento" = CAR."IdTipoInstrumento"
WHERE
	CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdTipoInstrumento" NOT IN (:NotInIdTipoInstrumento)
AND CAR."EGRCantidad" <> 0
AND CAR."EGRRetiroPorFusion" = 'N'
AND CAR."EGRFechaOperacion" >= :FechaInicio
AND CAR."EGRFechaOperacion" <= :FechaTermino
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND :TipoOperacion IN ('RETIRO','')
AND CAR."INGIdConcepto" = CASE WHEN :IdConcepto <> '' THEN :IdConcepto ELSE CAR."INGIdConcepto" END
AND CAR."IdBoveda" = CASE WHEN :IdBoveda <> '' THEN :IdBoveda ELSE CAR."IdBoveda" END
AND CAR."IdClasificacionTipoInstrumento" != 'DV'

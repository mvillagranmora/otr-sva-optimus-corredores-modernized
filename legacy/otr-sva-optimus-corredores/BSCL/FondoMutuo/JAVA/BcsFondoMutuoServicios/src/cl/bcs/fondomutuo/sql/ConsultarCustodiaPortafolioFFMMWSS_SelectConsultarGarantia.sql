SELECT
GAR."IdClasificacionTipoInstrumento"
,CLINS."Nombre" AS "NombreClasificacionTipoInstrumento"
,GAR."IdTipoInstrumento"
,TINS."Nombre" as "NombreTipoInstrumento"
,GAR."IdInstrumento"
,GAR."Nemotecnico"
,GAR."IdEmisor"
,GAR."IdCliente"
,GAR."IdPortafolio"
,CAR."IdBoveda" as "IdBoveda"
,cast(IFNULL(GAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
,CAR."IdRegimenFondoMutuo"
,CLI."IdPersona"
,"IDENT"."DocumentoIdentidad"
,PER."RazonSocial"
,Portafolio."CodigoPortafolio"
,SOC."IdSocioDeNegocio"
,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS on CAR."IdInstrumento" = INS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TINS on TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as CLINS on CLINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = GAR."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "PER" ON "PER"."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad" AND "IDENT"."Predeterminado" ='S'
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC ON CLI."IdPersona" = SOC."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = GAR."IdInstrumento" AND Serie."IdEmpresa" = :IdEmpresa
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
WHERE
	GAR."IdEmpresa" = :IdEmpresa
AND GAR."IdCliente" = :IdCliente
AND GAR."IdEstado" <> 'ANU'
AND :FechaConsulta BETWEEN GAR."INGFechaLiquidacion" and CASE WHEN IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
AND :FechaConsulta < CASE WHEN IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
AND GAR."IdClasificacionTipoInstrumento" = 'FM'
AND GAR."IdTipoInstrumento" = 'FFM'
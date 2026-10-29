SELECT 
	tran."IdInterno",
	"BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL as "IdTransaccion",
	inst."FechaEmision" AS "FechaEmisionInstrumento",
	inst."FechaVencimiento" AS "FechaVencimientoInstrumento",
	inst."Nemotecnico" AS "NemotecnicoInstrumento",
	tipoins."IdClasificacionTipoInstrumento",
	inst."IdTipoInstrumento",
	tipoope."AccionOrden",
	
	inst."IdEmisor" AS "IdEmisorInstrumento",
	inst."IdTipoReajuste" AS "IdTipoReajusteInstrumento",
	inst."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
	inst."IdMoneda" AS "IdMonedaInstrumento",
	bol."IdPais" AS "IdPaisBolsa",
	COALESCE(sub."Nemotecnico",'') AS "NemotecnicoInstrumentoSubyacente",
	COALESCE(sub."IdInstrumento",0) AS "IdInstrumentoSubyacente",	
	COALESCE(sub."IdMoneda",'') AS "IdMonedaInstrumentoSubyacente",				
	COALESCE(sub."IdEmisor",'') AS "IdEmisorInstrumentoSubyacente",
	COALESCE(instr."CorteMinimo",1) AS "CorteMinimo",
	corr."IdCorredorCorresponsal" AS "IdCorredoraCorresponsal",
	CASE WHEN subprt."IdInstrumento" IS NULL THEN 'N' ELSE 'S' END AS "InstrumentoPermiteVentaCorta",
	lib."IdEstado" AS "IdEstadoLibroOrdenes",
	liq."PlazoEnDias" AS "DiasLiquidacionContado",
	
	tipoopeins."Nemotecnico" AS "NemotecnicoRelacionado",
	tipoopeins."IdInstrumento" AS "IdInstrumentoRelacionado",

	tipoope."IdTipoOperacionRelacionada",
	bol."EsExtranjera" AS "BolsaEsExtranjera",
	sub."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente"
FROM "BSCL.Orden.BD::ORD.CrearTransacciones_Proceso" tran
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on tran."IdInstrumento" = inst."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoins on tipoins."IdTipoInstrumento" = inst."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" sub on tran."IdInstrumentoSubyacente" = sub."IdInstrumento"
LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" lib on tran."IdLibroOrdenes" = lib."IdLibroOrdenes"
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" bol on bol."IdBolsa" = tran."IdBolsa"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipoope on tran."IdTipoOperacion" = tipoope."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" corr on corr."IdCorredorCorresponsal" = tran."IdCorredoraCorresponsal"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = tran."IdInstrumento" and instr."IdTipoReajuste" = tran."IdTipoReajuste"
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" subprt on tran."IdInstrumento" = subprt."IdInstrumento" and tran."IdBolsa" = subprt."IdBolsa" and subprt."IdTipoInstrumento"  IN ('PRT','PRF')
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" liq ON liq."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacionContado"

LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  tipooperel on tran."IdTipoOperacion" = tipooperel."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoopeins on tipoopeins."IdTipoOperacion" = tipooperel."IdTipoOperacionRelacionada" 
	and tipoopeins."IdTipoInstrumento" = inst."IdTipoInstrumento"
;



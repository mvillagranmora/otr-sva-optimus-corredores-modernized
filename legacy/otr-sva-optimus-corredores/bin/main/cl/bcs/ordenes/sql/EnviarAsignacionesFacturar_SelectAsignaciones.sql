SELECT TRAN."IdClasificacionTipoInstrumento",
ASIG."EsAPlazo",
"CantidadAsignada",
TRAN."IdTipoInstrumento",
TRAN."FechaEmision",
"EsCuentaPropia",
TRAN."EsFungible",
TRAN."IndicadorOrdenDirecta",
TRAN."EsVentaCorta",
TRAN."FechaLiquidacion",
ORD."FechaOperacion" AS "FechaOperacionOrden",
ASIG."FolioCartera",
TRAN."Folio" AS "FolioTransaccion",
ORD."Glosa" AS "GlosaOrden",
ASIG."IdAsignacion",
"IdAsignacionRef",
TRAN."IdBolsa",
ORD."IdBoveda",
ORD."IdCliente",
TRAN."IdCondicionDeLiquidacion",
CASE TRAN."FlujoOrigen" WHEN 'INGRESOINSTRUCCION' THEN CONCEPINT."IdConcepto" ELSE concep."IdConcepto" END AS "IdConcepto",
TRAN."IdEmisor",
TRAN."IdCorredorContraparte" AS "IdCorredoraContraparte",
"IdEmpleadoDigitador",
"IdFormaCustodia",
TRAN."IdInstrumento",
TRAN."IdInstrumentoSubyacente",
TRAN."IdMoneda" as "IdMonedaTransaccion",
"IdMonedaLiquidacionOrden",
"IdMonedaSubyacente",
ORD."IdMonedaOrden" AS "IdMonedaNegociacion",
ASIG."IdEstadoAsignacion",
ASIG."IdOrden",
ORD."IdPortafolioCliente" AS "IdPortafolio",
ORD."IdTipoCanalIngreso",
TRAN."IdTipoOperacion",
"IdTipoProducto",
TRAN."IdTipoReajuste",
ASIG."IdTransaccion",
"MontoAsignado",
"MontoAsignadoLiquidacion",
"MontoAsignadoLocal",
TRAN."Nemotecnico",
TRAN."Precio" AS "PrecioAsignacion",
TRAN."PrecioRaiz" AS "PrecioAsignacionRaiz",
ASIG."Paridad",
"ValorRescateAsignado",
ASIG."TipoCambio",
 0.0 AS "MontoAsignadoAPlazo",
0.0 AS "PrecioAPlazo",
 ASIG."MontoAsignadoInteres",
 TRAN."Reajustes" AS "ReajustesVAC",
TRAN."FechaOperacion" AS "FechaOperacionTransaccion",
ASIG."FechaParidad",
ASIG."FechaTipoCambio",
 LIQ."PlazoEnDias" AS "DiasLiquidacion",
CASE ORD."EsVentaCorta" WHEN 'S' THEN ORD."FechaVencimiento"
						ELSE TRAN."FechaVencimiento" 
END AS "FechaVencimiento",
CASE ORD."EsVentaCorta" WHEN 'S' THEN ORD."PlazoVentaCorta" 
						ELSE 0 
END AS "PlazoVentaCorta",
CASE TRAN."IdTipoInstrumento" WHEN 'SIM' THEN TRAN."Plazo" 
						ELSE 0 
END AS "PlazoSimultanea",
CASE TRAN."IdClasificacionTipoInstrumento" WHEN 'RE' THEN TRAN."Plazo" 
						ELSE 0 
END AS "PlazoReporte",
CASE TRAN."IdClasificacionTipoInstrumento" WHEN 'IF' THEN TRAN."Plazo" 
						ELSE 0 
END AS "PlazoIF",

CASE TRAN."IdClasificacionTipoInstrumento" WHEN 'RE' THEN ORD."TasaReporte" 
						ELSE 0 
END AS "TasaReporte",
CASE TRAN."IdClasificacionTipoInstrumento" WHEN 'RF' THEN TRAN."Tasa" 
											WHEN 'IF' THEN TRAN."Tasa" 
						ELSE 0 
END AS "TasaRFIF",
CASE TRAN."IdTipoInstrumento" WHEN 'SIM' THEN TRAN."Tasa" 
						ELSE 0 
END AS "TasaSimultanea",
ORD."TasaVentaCorta",
OPE."AccionOrden",
TRAN."NemotecnicoSVS",
CUS."IdCuentaCustodioPortafolio",
BANCOCUS."IdPersona" AS "IdPersonaBancoCustodio",
BANCOCUS."Codigo" AS "CodigoBancoCustodio",
BANCOLIQ."IdPersona" AS "IdPersonaBancoLiquidador",
BANCOLIQ."Codigo" AS "CodigoBancoLiquidador",
ORD."IdTipoCliente",
ORD."IdPersonaBroker",
ORD."EsBroker",
ORD."EsClienteFinal",
BROKER."Codigo" AS "CodigoBroker",
ASIG."CodigoIFRS",
ORD."MercadoSMV",
ORD."ModalidadSMV",
TRAN."IdMonedaEmision",
ASIG."IdAsignacionGrupo",
COALESCE(ORD."IdTipoLiquidacionComisionFactura",'') AS "IdTipoLiquidacionComisionFactura",
COALESCE(ORD."CodigoModoFacturacionPortafolio",'') AS "ModPrefFact",
ORD."GeneraSpot" AS "GeneraSpot"
FROM "BSCL.Orden.BD::ORD.Asignacion" ASIG
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD on ASIG."IdOrden" = ORD."IdOrden"
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRAN on ASIG."IdTransaccion" = TRAN."IdTransaccion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" liq ON TRAN."IdCondicionDeLiquidacion" = liq."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden" concep on concep."IdTipoOperacion" = TRAN."IdTipoOperacion" 
																	AND concep."Uso" = TRAN."FlujoOrigen"
																	AND concep."IdBolsa" = CASE WHEN TRAN."IdBolsa" IN ('BCS', 'BVC', 'BVM', 'MIL') 
																									AND TRAN."IdTipoOperacion" IN ('COMACN', 'VENACN') 
																									AND :localizacionSistema = 'PE' THEN TRAN."IdBolsa"
																								ELSE ''
																							END
LEFT JOIN "BSCL.Orden.BD::ORD.Instruccion" INS ON (INS."IdInstruccion" = TRAN."Folio" AND INS."IdInstruccion" IS NOT NULL)
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarConceptosOrden" CONCEPINT ON (CONCEPINT."IdTipoOperacion" =  INS."IdTipoOperacionInstruccion")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPE ON  OPE."IdTipoOperacion" = TRAN."IdTipoOperacion"
LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" CUS ON CUS."IdCuentaCustodioPortafolio" = ORD."IdCuentaCustodioPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOCUS ON CUS."IdPersonaBancoCustodio" = BANCOCUS."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BancoCustodio" BANCOLIQ ON CUS."IdPersonaBancoLiquidador" = BANCOLIQ."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BrokerExtranjero" BROKER ON ORD."IdPersonaBroker" = BROKER."IdPersona"
WHERE ASIG."IdAsignacion" in (:idsAsignacion);
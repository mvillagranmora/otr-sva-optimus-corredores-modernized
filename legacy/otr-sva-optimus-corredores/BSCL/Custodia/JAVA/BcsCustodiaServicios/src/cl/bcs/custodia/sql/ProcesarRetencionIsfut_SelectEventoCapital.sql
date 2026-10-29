SELECT 
EvCap."IdEventoCapital" 			AS "IdComprobante",
ifnull(CP."IdPortafolio",0) 		AS "IdPortafolio",
'MOVCAR'							AS "IdTipoMovimiento",
'RETISF'							AS "IdConcepto",
Concat('Cargo por Dividendo en Dinero con ISFUT, Instrumento : ',EvCap."Nemo")	AS "Descripcion",
EvCap."IdMoneda"					AS "IdMoneda",
EvCap."IdMonedaLiquidacion" 		AS "IdMonedaLiquidacion",
sum(ABS(FT."Total"))				AS "MontoComprobanteFinal",
TO_BIGINT(0) 						AS "IdEjecutivo",
'EGR'								AS "IdTipoComprobante",
EvCap."Secuencia"					AS "FolioComprobante",
EvCap."FechaHoraIngresoRegistro"	AS "FechaComprobante",
EvCap."FechaPago" 			 		AS "FechaLiquidacion",
EvCap."IdInstrumento"				AS "IdInstrumento",
EvCap."IdTipoInstrumento"			AS "IdTipoInstrumento",
EvCap."Nemo"						AS "Nemo",
'EVCA'								AS "IdTipoProducto",
'DIVDIN'							AS "IdTipoOperacion",
TO_BIGINT(0)						AS "IdSucursal",
CE."IdPersona" 						AS "IdPersonaPagoCobro"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" EvCap LEFT JOIN 
"BSCL.Custodia.BD::CUS.EventoCapitalCliente" FT ON FT."IdEventoCapital" = EvCap."IdEventoCapital" LEFT JOIN 
"BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" CE ON CE."IdEmpresa" = EvCap."IdEmpresa" LEFT JOIN 
"BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" CP ON CP."IdPersona" = CE."IdPersona" AND CP."EsPortafolioDefecto" ='S'
where EvCap."IdEventoCapital" = :IdEventoCapital
GROUP BY EvCap."IdEventoCapital",EvCap."IdMoneda",EvCap."IdMonedaLiquidacion",
EvCap."FechaPago",EvCap."IdTipoEventoCapital",EvCap."Secuencia",
EvCap."FechaHoraIngresoRegistro",EvCap."IdInstrumento",EvCap."IdTipoInstrumento",
EvCap."Nemo",CE."IdPersona",CP."IdPortafolio"
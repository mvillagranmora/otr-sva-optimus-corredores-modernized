SELECT 
1 AS "IdGenerarMovimientoCustodia",
'Cartera',
"IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransito",
"IdTipoComprobante" AS "IdTipoComprobanteBusqueda"             ,
"FolioComprobante" AS "FolioComprobanteBusqueda"              ,
"FolioCustodia"                         ,
'RETIRO' AS "AccionOperacion"           ,
MOV."IdCliente"                             ,
MOV."IdPortafolio"                          ,
"FechaOperacion"                        ,
"FechaOperacion" AS "FechaComprobante"  ,
"IdTipoOperacion" AS "IdTipoOperacion"  ,
"IdConcepto"                            ,
"IdCondicionLiquidacion"                ,
"FechaLiquidacion"                      ,
"IdTipoCanalIngreso"                    ,
"IdTipoComprobante"                     ,
"FolioComprobante"                      ,
"IdUsuario"                             ,
"IdSucursal"							,
"IdAgente"                              ,
"Glosa"                                 ,
"IdInstrumento"                         ,
"FechaVencimientoInstrumento"           ,
"IdBoveda"                              ,
"CuentaBoveda"                          ,
"IdEmisor"                              ,
"EsFungible"                            ,
"IdMonedaEmision"                       ,
"Cantidad"                              ,
COALESCE("CantidadCustodia",0) AS "CantidadCustodia"      ,
"CorteMinimo"                           ,
"ValorRescate"                          ,
"PrecioCompra"                          ,
"TasaCompra"                            ,
"MontoCompraMonedaInstrumento"          ,
"IdMonedaAdicional"                     ,
"IdOperacion"                           ,
"IdClasificacionTipoInstrumento"        
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV
LEFT JOIN (SELECT "IdCliente", "IdPortafolio","Nemotecnico", SUM("Cantidad") AS "CantidadCustodia" FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" GROUP BY "IdCliente","IdPortafolio","Nemotecnico") CUS
	ON MOV."IdCliente" = CUS."IdCliente" 
	AND MOV."IdPortafolio" = CUS."IdPortafolio" 
	AND MOV."Nemotecnico" = CUS."Nemotecnico" 
	--AND "Cantidad" <= "CantidadCUS"
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND 
ON MOV."IdPortafolio" = IND."IdEntidad" AND "IdTipoEntidad" = 'PORTAFOLIO'
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPIND 
ON IND."IdTipoIndicador" = TIPIND."IdTipoIndicador"
AND (("IdClasificacionTipoInstrumento" = 'RV' AND "CodigoTipoIndicador"= 'ORDOCUSTRV')
     OR ("IdClasificacionTipoInstrumento" IN('RF','IF') AND "CodigoTipoIndicador"= 'ORDOCUSTRF')
    )
WHERE MOV."IdEstado" = 'PEN' AND ("EsNominal" = 'S' OR "MueveCustodia" = 'N') AND IND."ValorIndicador" = 'S'
ORDER BY "IdMovimientoCarteraEnTransito"; 
SELECT 
cmsd."IdCargaMasivaDetalle",  		
cmsd."DocumentoIdentidad",	
cmsd."CodigoPortafolio",		
cmsd."ProductoAgrupador",
cmsd."TipoOperacion",		
cmsd."FechaOperacion",		
cmsd."HoraOperacion",		
cmsd."CanalIngreso",			
cmsd."MontoMonedaPrincipal",	
cmsd."TipoCambioCierre",		
cmsd."TipoCambioCosto",		
cmsd."PuntaCompra",			
cmsd."PuntaVenta",			
cmsd."FormaPagoEntregamos",	
cmsd."FormaPagoRecibimos",	
cmsd."Observacion",			
cmsd."AfectoADerivado",		
cmsd."DolarObservado",		
cmsd."CodigoComercio",		
cmsd."DetalleValidacion",	
cmsd."IdCargaMasiva",		
cmsd."IdTipoCargaMasiva",	
cmt."CodigoTipoCargaMasiva",
ins."IdInstrumento",
cmsd."IdCliente",
cmsd."IdPortafolio",
cmsd."TipoComprobanteFacturacion",
(SELECT "IdMesa" FROM "BSCL.Mesa.BD::MES.Mesa" WHERE "IdTipoProducto" = 'MEME') as "IdMesa"
FROM "BSCL.Mesa.BD::CAR.CargaMasivaSpotDetalle" cmsd
left join "BSCL.Mesa.BD::CAR.TipoCargaMasivaSpot" cmt on (cmsd."IdTipoCargaMasiva" = cmt."IdTipoCargaMasiva")
left join "BSCL.Plataforma.BD::INS.Instrumento" ins on (cmt."CodigoTipoCargaMasiva" = ins."RazonSocial")
WHERE "IdCargaMasiva" IN (:IdCargaMasiva);
SELECT
TO_BIGINT (0) AS "IdOperacion"
,CC."FolioCartera" AS "FolioOperacion"
,CC."RazonSocial"
,CC."INGMontoATasaCompraActual" AS "MontoOperacion"
,CC."INGMontoATasaCompraActual" AS "MontoOperacionMoneda"
,CC."IdMonedaInstrumento" AS "IdMoneda"
,CC."IdMonedaInstrumento" AS "IdMonedaLiquidacion"
,CC."IdMesa" AS "IdMesaOrigen"
,'' AS "IdEstado"
------
,CC."CorteMinimo"
,CC."INGMontoATasaCompraActual" 				AS "MontoCompra"
,CASE WHEN CC."Estado" = 'GARANTIA' THEN (
		SELECT MAX(GAR."INGMontoCompraActualMonedaInstrumento") FROM "BSCL.Custodia.BD::GTA.Garantia" GAR 
				INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" CGAR ON CGAR."IdGarantia" = GAR."IdGarantia"
				WHERE GAR."FolioCarteraOrigen" = CC."FolioCartera" AND GAR."IdCliente"= CC."IdCliente" AND GAR."IdPortafolio" = CC."IdPortafolio" )  
	  ELSE CC."INGMontoCompraActualMonedaInstrumento"  END	AS "ValorCompra"
,CC."INGMontoATasaMercadoActual" 				AS "MontoMercado"
,CC."INGMontoMercadoActualMonedaInstrumento" 	AS "ValorMercado"
,CC."INGMontoInteresCompraProximo" 				AS "InteresDiario"
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento" CC
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SDN
	ON SDN."IdEmpresa" = CC."IdEmpresa" AND SDN."IdCliente" = CC."IdCliente"
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
	ON EMP."IdSocioDeNegocio" = SDN."IdSocioDeNegocio"
WHERE CC."IdEmpresa" = :IdEmpresa AND CC."FechaLiquidacion"<=:Fecha AND CC."FechaOperacion"<=:Fecha AND CC."Estado" <> 'TRANSITO';
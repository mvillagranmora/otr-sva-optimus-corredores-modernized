/********************************************************** 
Proposito	: Script para obtener las carteras para vencer
autor		: JG
fecha		: 01/02/2017
***********************************************************/
SELECT
    c."IdCartera",
    c."FolioCartera",
    sub."Nemotecnico",
	c."IdTipoProducto", 
	c."IdTipoOperacion",
    c."IdClasificacionTipoInstrumento",
    c."IdTipoInstrumento",
    c."IdInstrumento",
    c."IdTipoComprobante",
    c."FolioComprobante",
    --------------
    s."IdPersona",
    s."IdCliente",
    c."IdPortafolio",
    coalesce(ip."ValorIndicador",'N') as "OperaContraCuentaInversion",
    c."IdAgente", 
	c."IdSucursal",
    -------------------
	c."FechaVencimientoOperacion",
	c."IdMonedaSistema",
	'T0' AS "IdCondicionLiquidacion",
	:Fecha AS "FechaLiquidacion",
	c."Cantidad",
	COALESCE(Sub."Nemotecnico",'') as "Nemotecnico",
	
	
	
	
    -- Precio y Monto a Plazo de obtiene de la factura a plazo, de esta forma calza el monto de la factura con lo que se env�a a cta inversion (esta logica esta alineada con SIM.ConsultarCarteraSimultanea)   
    COALESCE(MovPlazo."Precio",0) as "PrecioAPlazo",
	CASE
		WHEN c."INGCantidad" <> MovPlazo."Cantidad" THEN  c."INGCantidad" * coalesce(MovPlazo."Precio",0)
		ELSE MovPlazo."Monto"
	END  AS "MontoAPlazo",
	
	
	
	c."INGCantidad",
	c."INGTasaCompra",
	c."INGPrecioCompra",
	c."INGMontoATasaCompraActual",
	c."INGPrecioAPlazo",
	c."INGMontoAPlazo",
	c."IdOperacion",
	c."IdConcepto",
	c."IdMonedaInstrumento",
	c."INGMontoCompraActualMonedaInstrumento",
	c."IdEmisor"
	
FROM "BSCL.Custodia.BD::CUS.Cartera" c


INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = c."FolioCartera"
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" ) 

LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovNormal on 
 	MovNormal."IdEmpresa" = c."IdEmpresa" and 
 	MovNormal."IdComprobante" = c."FolioComprobante" and 
 	MovNormal."NemotecnicoSubyacente" = Sub."Nemotecnico"

LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" as MovPlazo on 
 	MovPlazo."IdEmpresa" = c."IdEmpresa" and 
 	MovPlazo."IdMovimientoComprobante" = MovNormal."IdMovimientoRelacionado" and 
 	MovPlazo."NemotecnicoSubyacente" = Sub."Nemotecnico"
 	



INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" s on s."IdCliente" = c."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"ip on ip."IdPortafolio" = c."IdPortafolio" and ip."CodigoTipoIndicador" = 'GEN_OCCINV'
WHERE c."IdEstado" = 'VIG' and  
	  c."IdTipoInstrumento" = 'SIM' and 
	  c."FechaVencimientoOperacion" > '0001-01-01' and	  
	  c."FechaVencimientoOperacion" = :Fecha and
	  c."IdEmpresa" = :IdEmpresa

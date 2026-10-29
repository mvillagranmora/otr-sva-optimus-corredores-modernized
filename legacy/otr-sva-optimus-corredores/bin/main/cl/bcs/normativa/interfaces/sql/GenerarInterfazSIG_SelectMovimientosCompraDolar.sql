 WITH NCES as (
SELECT "IdComprobanteReferencia" FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE	"IdEmpresa"					= 	:IdEmpresa
AND 	"FechaComprobante"  		=  	:FechaIng
AND 	"IdComprobanteReferencia" 	> 	0
)
SELECT 
	COM."IdComprobante"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN COM."IdTipoOperacion" = 'VENSPT' THEN 'VE  '
			WHEN COM."IdTipoOperacion" = 'COMSPT' THEN 'CO  '
			ELSE '    ' END  										AS "Producto"  
	,'MO' 							AS "Mercado"
	,TO_VARCHAR(COM."FechaComprobante", 'YYYYMMDD') 				AS "FechaInicio"
	,TO_VARCHAR(COM."FechaComprobante",'YYYYMMDD') 					AS "FechaVencimiento"
	,TO_BIGINT(1) 											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(COM."DocumentoIdentidad",1,LENGTH(COM."DocumentoIdentidad")-2),SUBSTRING(COM."DocumentoIdentidad",LENGTH(COM."DocumentoIdentidad"),1)),10,'0')								AS "RutCliente"
	,'0000000001'													AS "RutEmisor"
	,'013'				 											AS "MonedaEmisor"
	,'000' 															AS "MonedaPacto"
	,0.0 															as "Tasatransferencia"
	,0.0															AS "TasaTransaccion"
	,0.0 															AS "TasaOperacion"
	,'000'															AS "BASTAS"
	,'001' 															AS "Sucursal"
	,DET."Cantidad"													AS "MontoCompraVenta"
	,0.0					 										AS "ValorPresente"
	,0.0	 														AS "Interes"
	,0.0 															AS "Reajuste"
	,COM."MontoLiquidacion" 										AS "MontoTransaccion"
	,0.0 															AS "Comision"
	,0.0	 														AS "UtilidaPerdidaDiferenciaPrecio"
	,DET."Precio"		 											AS "PrecioCompraVenta"
	,OS."Utilidad"													AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON COM."IdComprobante"= DET."IdComprobante"
	LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OS ON DET."IdTransaccion" = OS."IdOperacionSpot"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = COM."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON DOC."IdPersona" = CLI."IdPersona"
	WHERE COM."IdEmpresa"= :IdEmpresa
	AND COM."IdClasificacionTipoInstrumento" = 'SP'
	AND COM."IdMonedaSpot" 			= 'USD'
	AND COM."IdTipoOperacion" 		IN ('VENSPT','COMSPT')
	AND COM."IdTipoComprobante" 	IN ('FAC','FAE','FEE')
	AND COM."IdEstadoComprobante" 	IN ('FAC','ENV')
	AND COM."FechaComprobante" 		= 	:FechaIng
	AND DOC."DocumentoIdentidad" 	<> '1-9'
	AND COM."IdComprobanteReferencia" NOT IN (SELECT "IdComprobanteReferencia" FROM NCES)

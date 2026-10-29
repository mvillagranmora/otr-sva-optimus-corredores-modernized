SELECT 
	MESA."IdOperacion"												AS "NumeroOperacion"
	,'00'															AS "LinDet"
	,CASE WHEN MESA."IdTipoOperacion" = 'VENTER' THEN 'VT  '
			ELSE '    ' END  										AS "Producto"  
	,DET."IdClasificacionTipoInstrumento"															AS "Mercado"
	,TO_VARCHAR(MESA."FechaOperacion",'YYYYMMDD')					AS "FechaInicio"
	,TO_VARCHAR(MESA."FechaVencimiento", 'YYYYMMDD') 				AS "FechaVencimiento"
	,MESA."PlazoEnDias" 											AS "Plazo"
	,LPAD(CONCAT(SUBSTRING(DNICLI."DocumentoIdentidad",1,LENGTH(DNICLI."DocumentoIdentidad")-2),SUBSTRING(DNICLI."DocumentoIdentidad",LENGTH(DNICLI."DocumentoIdentidad"),1)),10,'0')												AS "RutCliente"
	,LPAD(CONCAT(SUBSTRING(DNICOR."DocumentoIdentidad",1,LENGTH(DNICOR."DocumentoIdentidad")-2),SUBSTRING(DNICOR."DocumentoIdentidad",LENGTH(DNICOR."DocumentoIdentidad"),1)),10,'0')													AS "RutEmisor"
	,LPAD(MON."CodigoNumerico",3,'0') 								AS "MonedaEmisor"
	,'999' 															AS "MonedaPacto"
	, 0.0															AS "TasaTransaccion"
	,0.0 													AS "TasaOperacion"
	,CASE WHEN MESA."IdTipoReajuste" = 'CLP' THEN '030'
		ELSE '360' END 												AS "BASTAS"
	,'001' 															AS "Sucursal"
	,CAR."INGMontoCompraActualMonedaInstrumento" 									AS "MontoCompraVenta"
	,CAR."ValorRescate" 										AS "ValorPresente"
	,0.0 													AS "Interes"
	,0.0													AS "Reajuste"
	,0.0 															AS "MontoTransaccion"
	,0.0 															AS "Comision"
	,CAR."MontoMonedaPrincipal"										AS "UtilidaPerdidaDiferenciaPrecio"
	,CAR."INGPrecioCompra"													AS "PrecioCompraVenta"
	,0.0 															AS "UtilidadPerdidaOperacionCambio"
	,0.0 															AS "UtilidadPerdidaSimultanea"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS MESA
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS DET ON DET."IdOperacion" = MESA."IdOperacion"
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON DET."FolioCarteraOrigen" = CAR."FolioCartera"
	INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = DET."IdTipoReajuste"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON MESA."IdCliente" = CLI."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DNICLI ON DNICLI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS COR ON MESA."IdClienteCorredor" = COR."IdCliente"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERCOR ON COR."IdPersona" = PERCOR."IdPersona"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DNICOR ON DNICOR."IdDocumentoIdentidad" = PERCOR."IdDocumentoIdentidad"
WHERE 
	MESA."IdEmpresa" = :IdEmpresa
	AND CAR."EGRIdTipoOperacion" ='VENTER'
	--AND DET."IdClasificacionTipoInstrumento" = 'RF'
	AND DET."IdEstado"= 'VIG'
	AND MESA."FechaOperacion" = :FechaIng
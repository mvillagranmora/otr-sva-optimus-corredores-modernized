SELECT 
	'S80080' AS "CodigoAI" --Constante
	,TO_CHAR(Orden."FechaOperacionOrden",'DDMMYYYY') AS "FechaOrden"
	,TO_BIGINT(CASE WHEN COALESCE(Orden."FolioOrden",'') = '' THEN 0 ELSE Orden."FolioOrden" END) AS "FolioOrden"
	,TO_CHAR(Orden."FechaOperacionOrden",'hhmmss') AS "HoraMinutoSegundoOrden"
	,'1' AS "Mercado" --Constante
	,'1' AS "TipoCodigoCliente" --Constante
	,COALESCE(Documento."DocumentoIdentidad",'') AS "CodigoCliente"
	,CASE WHEN Orden."AccionTipoOperacion" = 'COMPRA' THEN 'C' 
		  WHEN Orden."AccionTipoOperacion" = 'VENTA'	THEN 'V'
	 END AS "TipoOperacion"
	,'20' AS "ModalidadOperacion" --Constante
	,'S' AS "FlagInscrito" --Constante
	,Orden."Instrumento" AS "CodigoValor"
	,TO_DECIMAL(Orden."CantidadOrden",26,8) AS "CantidadOrden"
	,CASE WHEN Orden."TipoPrecio" = 'MERCADO' THEN TO_DECIMAL(0,26,8) 
	 ELSE TO_DECIMAL(Orden."PrecioOrden",26,8) END AS "Precio" --Es Cero cuando el flag precio es M
	,TO_DECIMAL(Orden."MontoOrden",26,8) AS "MontoOrdenado"
	,CASE WHEN Orden."TipoPrecio" = 'MERCADO' THEN 'M' 
	 ELSE CASE 	WHEN Orden."AccionTipoOperacion" = 'COMPRA' THEN 'C'
	 			WHEN Orden."AccionTipoOperacion" = 'VENTA'  THEN 'V'
	 	   END
	 END AS "FlagPrecio"
	,COALESCE(Homologacion."ValorHomologacion",'') AS "MonedaOrdenado" --Ver como mapear Pendiente Mapeo
	,TO_DECIMAL(0,26,8) AS "PrecioStop"
FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" Orden
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente ON (Cliente."IdCliente" = ORDEN."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"  Documento ON (Cliente."IdPersona" = Documento."IdPersona" AND Documento."IdTipoDocumentoIdentidad" = 'RUT')
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" Homologacion ON (Homologacion."IdCampo" = 'Moneda' AND Homologacion."Valor" = Orden."IdMonedaOrden")
WHERE Orden."FechaOperacionOrden" >= ADD_SECONDS(:Fecha,-(60*10)) AND Orden."FechaOperacionOrden" < :Fecha AND Orden."IdEmpresa" = :IdEmpresa;
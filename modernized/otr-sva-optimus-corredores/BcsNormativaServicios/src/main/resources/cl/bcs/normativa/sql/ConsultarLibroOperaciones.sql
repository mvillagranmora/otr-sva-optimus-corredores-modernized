WITH
CTA_PROPIA AS (
  SELECT TOP 1
         CLI."IdCliente"           AS "IdClienteCtaPropia",
         CLI."DocumentoIdentidad"  AS "DocumentoIdentidadCtaPropia",
         COR."CodigoCorredora"     AS "CodigoCtaPropia",
         COD."NombreCorto"         AS "NombreCtaPropia"
  FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
          ON CLI."IdPersona" = SOC."IdPersona"
  INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP
          ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
  INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COD
          ON COD."IdPersona" = SOC."IdPersona"
  INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" COR
          ON COD."IdCorredora" = COR."IdCorredora"
  WHERE EMP."IdEmpresa" = :p_IdEmpresa
    AND CLI."IdEstado"  = 'ACT'
),
LIBRO_BASE AS (
	  WITH
	v_tablaFecha AS (
	    SELECT
	        :p_FechaDesde  AS "FECHADESDE",   -- NVARCHAR(10) 'YYYY-MM-DD' o NULL
	        :p_FechaHasta  AS "FECHAHASTA",   -- NVARCHAR(10) 'YYYY-MM-DD'
	        'MOVIMIENTO' AS "CLASEFECHA"
	    FROM DUMMY
	),
	v_tablaProductos AS (
	    SELECT
	        NPRO."IdTipoProducto",
	        NPRO."IdClasificacionTipoInstrumento",
	        NPRO."IdTipoInstrumento",
	        NPRO."IdTipoOperacion",
	        NPRO."IdNormativa",
	        NPRO."IdTipoDocumento",
	        NPRO."Uso",
	        CASE WHEN NPRO."HIdTipoProducto" = '' THEN NPRO."IdTipoProducto" ELSE NPRO."HIdTipoProducto" END AS "HIdTipoProducto",
	        CASE WHEN NPRO."HIdClasificacionTipoInstrumento" = '' THEN NPRO."IdClasificacionTipoInstrumento" ELSE NPRO."HIdClasificacionTipoInstrumento" END AS "HIdClasificacionTipoInstrumento",
	        CASE WHEN NPRO."HIdTipoInstrumento" = '' THEN NPRO."IdTipoInstrumento" ELSE NPRO."HIdTipoInstrumento" END AS "HIdTipoInstrumento",
	        CASE WHEN NPRO."HIdTipoOperacion" = '' THEN NPRO."IdTipoOperacion" ELSE NPRO."HIdTipoOperacion" END AS "HIdTipoOperacion",
	        NPRO."IncluirVentaCorta"
	    FROM "BSCL.Normativa.BD::NOR.Producto" NPRO
	    WHERE NPRO."IdNormativa" = 'NCG380Ope'
	),
	
	/* =========================
	   FACTURACION
	   ========================= */
	facturacion AS (
	    SELECT 
		    ---CAMPOS COMUNES
		    COALESCE(OPE."FechaIngresoRegistro",'') AS "FechaCreacion",
		    COALESCE(
		        TO_VARCHAR(OPE."FechaTransaccion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'),
		        '1900-01-01 00:00:00'
		    ) AS "FechaMovimiento",
		    COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') AS "FolioTransaccion",
		    COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') AS "IdOrden",
		    COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') AS "IdComprobante",
		    COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
		    COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
		    COALESCE(OPE."IdCliente",0) AS "IdCliente",
		    COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
		    COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
		    COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
		    COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
		    COALESCE(OPE."Cantidad",0) AS "Cantidad",
		    COALESCE(OPE."Precio",0) AS "Precio",
		    COALESCE(OPE."MontoMovimientoComprobante",0) AS "Monto",
		    COALESCE(OPE."Tasa",0) AS "Tasa",
		    COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
		    COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
		    COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
		    COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
		    
		    --(Compra/Venta)
		    COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
		    COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
		    COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
		    COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
		    COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
		    COALESCE(OPE."TotalCompra",0) AS "TotalCompra",
		    COALESCE(OPE."TotalVenta",0) AS "TotalVenta",
		    
		    ---ESPECIFICOS: FACTURACION
		    COALESCE(OPE."IdTransaccion",0) AS "IdTransaccion",
		    COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') AS "FechaTransaccion",
		    COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
		    COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
		    COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
		    COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
		    COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
		    COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
		    COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
		    COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
		    COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
		
		    COALESCE(OPE."Comision",0) AS "Comision",
		    COALESCE(OPE."Derecho",0) AS "Derecho",
		    COALESCE(OPE."Gasto",0) AS "Gasto",
		    COALESCE(OPE."Impuesto",0) AS "Impuesto",
		
		    COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
		    COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
		    COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
		    COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
		    COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
		    COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
		    COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
		    COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
		    COALESCE(OPE."Custodia",'') AS "Custodia",
		    COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
		    COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
		    COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
		    COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
		    COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
		    COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
		    COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
		    COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
		    COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
		    COALESCE(OPE."Canal",'') AS "Canal",
		    COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
		    COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
		    COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
		    COALESCE(OPE."EsFungible", '') AS "EsFungible",
		    COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
		    OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
		    OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor",
		
		    ---ESPECIFICOS: MESA RF
		    '' AS "TipoOperacionSecundaria",
		    0 AS "ValorRescateInicial",
		    ---ESPECIFICOS: SPOT
		    '' AS "IdMonedaSecundaria",
		
		    NPRO."IdNormativa",
		    NPRO."IdTipoDocumento",
		    NPRO."HIdTipoProducto",
		    NPRO."HIdClasificacionTipoInstrumento",
		    NPRO."HIdTipoInstrumento",
		    NPRO."HIdTipoOperacion",
		    NPRO."Uso",
		    OPE."EsEnRueda" AS "EsEnRueda",
		    OPE."EsVentaCorta" AS "EsVentaCorta",
		
		    ---ESPECIFICOS: FORWARD
		    0 AS "ParidadCierre",
		    0 AS "PlazoVencimiento",
		    OPE."DocumentoIdentidad",
		    OPE."NombreCompleto",
		    'FAC' AS "IdModulo",
		    '' AS "FechaIngresoRegistro",
		    0 AS "IdOperacionOrigenReasignacion",
		    COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
		    COALESCE(CAR."INGCantidad",0) AS "INGCantidad",
		    OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
		    COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
		    COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
		    'N' AS "EsVencimientoPacto",
		    COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
		    --Campos nuevo mesa RF
		    '' AS "IdEstadoOperacion",
		    0 AS "ValorRescateInicialReserva",
		    '' AS "IdEmisorReserva",
		    '' AS "IdClasificacionTipoInstrumentoReserva",
	        0 AS "CantidadInicialReserva",
	        0 AS "MontoLiquidacionOperacionReserva",
	        0 AS "EgrMontoValorizadoReserva",
	        0 AS "IngMontoValorizadoReserva",
	        0 AS "TasaReserva",
	        0 AS "INGMontoAPlazoReserva",
	        0 AS "IdOperacionRelacionada",
			0 AS "IdCarteraOrigen"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
		
		INNER JOIN v_tablaProductos NPRO
		    ON (
		        (OPE."IdTipoProducto"= NPRO."IdTipoProducto" OR OPE."IdTipoProducto"='')
		        AND (OPE."IdClasificacionTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='')
		        AND (OPE."IdTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='')
		        AND (
					  OPE."IdTipoOperacion" = NPRO."IdTipoOperacion"
					  OR NPRO."IdTipoOperacion" = ''
					  OR (
					      OPE."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO')
					      AND OPE."IdTipoOperacionRelacionada" = NPRO."IdTipoOperacion"
					      AND NOT EXISTS (
					        SELECT 1
					        FROM v_tablaProductos NPRO2
					        WHERE NPRO2."IdTipoProducto" = NPRO."IdTipoProducto"
					          AND NPRO2."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento"
					          AND NPRO2."IdTipoInstrumento" = NPRO."IdTipoInstrumento"
					          AND NPRO2."IdTipoOperacion" = OPE."IdTipoOperacion"
					      )
					  )
					)
		    )
		    AND OPE."EsVentaCorta" = CASE WHEN NPRO."IncluirVentaCorta" = 'N' THEN 'N' ELSE OPE."EsVentaCorta" END
		
		INNER JOIN v_tablaFecha PFECH
		    ON (
		        (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
		        OR
		        (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
		    )
		
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante
		    ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
		
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		    ON  OPE."IdTipoInstrumento" = 'SIM'
		    AND OPE."IdComprobante" = CAR."EGRFolioComprobante"
		    AND OPE."IdTipoComprobante" = CAR."EGRIdTipoComprobante"
		    AND OPE."IdTipoComprobante" = 'NCR'	
		WHERE OPE."IdTipoInstrumento" != 'SPT'
	    UNION ALL
		   SELECT 
		    ---CAMPOS COMUNES
		    COALESCE(OPE."FechaIngresoRegistro",'') AS "FechaCreacion",
		    COALESCE(
		        TO_VARCHAR(OPE."FechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR(OPE."FechaIngresoRegistro", 'HH24:MI:SS'),
		        '1900-01-01 00:00:00'
		    ) AS "FechaMovimiento",
		    COALESCE(CAST(OPE."FolioTransaccion" AS NVARCHAR(40)),'') AS "FolioTransaccion",
		    COALESCE(CAST(OPE."IdOrden" AS NVARCHAR(40)),'') AS "IdOrden",
		    COALESCE(CAST(OPE."IdComprobante" AS NVARCHAR(40)),'0') AS "IdComprobante",
		    COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
		    COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
		    COALESCE(OPE."IdCliente",0) AS "IdCliente",
		    COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
		    COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
		    COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
		    COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
		    COALESCE(OPE."Cantidad",0) AS "Cantidad",
		    COALESCE(OPE."Precio",0) AS "Precio",
		    COALESCE(OPE."MontoMovimientoComprobante",0) AS "Monto",
		    COALESCE(OPE."Tasa",0) AS "Tasa",
		    COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
		    COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
		    COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
		    COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
		
		    --(Compra/Venta)
		    COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
		    COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
		    COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
		    COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
		    COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
		    COALESCE(OPE."TotalCompra",0) AS "TotalCompra",
		    COALESCE(OPE."TotalVenta",0) AS "TotalVenta",
		
		    ---ESPECIFICOS: FACTURACION
		    COALESCE(OPE."IdTransaccion",0) AS "IdTransaccion",
		    COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') AS "FechaTransaccion",
		    COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
		    COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
		    COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
		    COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
		    COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
		    COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
		    COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
		    COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
		    COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
		
		    COALESCE(OPE."Comision",0) AS "Comision",
		    COALESCE(OPE."Derecho",0) AS "Derecho",
		    COALESCE(OPE."Gasto",0) AS "Gasto",
		    COALESCE(OPE."Impuesto",0) AS "Impuesto",
		
		    COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
		    COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
		    COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
		    COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
		    COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
		    COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
		    COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
		    COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
		    COALESCE(OPE."Custodia",'') AS "Custodia",
		    COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
		    COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
		    COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
		    COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
		    COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
		    COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
		    COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
		    COALESCE(CAST(OPE."DescripcionOperacion" AS NVARCHAR(40)),'') AS "DescripcionOperacion",
		    COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
		    COALESCE(OPE."Canal",'') AS "Canal",
		    COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
		    COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
		    COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
		    COALESCE(OPE."EsFungible", '') AS "EsFungible",
		    COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
		    OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
		    OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor",
		
		    ---ESPECIFICOS: MESA RF
		    '' AS "TipoOperacionSecundaria",
		    0 AS "ValorRescateInicial",
		    ---ESPECIFICOS: SPOT
		    '' AS "IdMonedaSecundaria",
		
		    NPRO."IdNormativa",
		    NPRO."IdTipoDocumento",
		    NPRO."HIdTipoProducto",
		    NPRO."HIdClasificacionTipoInstrumento",
		    NPRO."HIdTipoInstrumento",
		    NPRO."HIdTipoOperacion",
		    NPRO."Uso",
		    OPE."EsEnRueda" AS "EsEnRueda",
		    OPE."EsVentaCorta" AS "EsVentaCorta",
		
		    ---ESPECIFICOS: FORWARD
		    0 AS "ParidadCierre",
		    0 AS "PlazoVencimiento",
		    OPE."DocumentoIdentidad",
		    OPE."NombreCompleto",
		    'FAC' AS "IdModulo",
		    '' AS "FechaIngresoRegistro",
		    0 AS "IdOperacionOrigenReasignacion",
		    COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
		    0 AS "INGCantidad",
		    OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
		    COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
		    COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
		    'N' AS "EsVencimientoPacto",
		    COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
		    --Campos nuevo mesa RF
		    '' AS "IdEstadoOperacion",
		    0 AS "ValorRescateInicialReserva",
		    '' AS "IdEmisorReserva",
		    '' AS "IdClasificacionTipoInstrumentoReserva",
	        0 AS "CantidadInicialReserva",
	        0 AS "MontoLiquidacionOperacionReserva",
	        0 AS "EgrMontoValorizadoReserva",
	        0 AS "IngMontoValorizadoReserva",
	        0 AS "TasaReserva",
	        0 AS "INGMontoAPlazoReserva",
	        0 AS "IdOperacionRelacionada",
			0 AS "IdCarteraOrigen"
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante
		    ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
		INNER JOIN v_tablaProductos NPRO
		    ON (
		        (OPE."IdTipoProducto" = NPRO."IdTipoProducto" OR OPE."IdTipoProducto" = '')
		        AND (OPE."IdClasificacionTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento" = '')
		        AND (OPE."IdTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento" = '')
		        AND (OPE."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion" = '')
		    )
		INNER JOIN v_tablaFecha PFECH
		    ON (
		        (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
		        OR
		        (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
		    )
		WHERE OPE."EsVentaCorta" = 'S'
		UNION ALL
		-- VENCIMIENTO DE SIMULTANEAS 
			SELECT 
			    COALESCE(CARSIM."FechaIngresoRegistro", '') AS "FechaCreacion",
			    CASE 
			        WHEN CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD')
			            THEN COALESCE(
			                TO_VARCHAR(CARSIM."EGRFechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR(CARSIM."FechaIngresoRegistro", 'HH24:MI:SS'),
			                '1900-01-01 00:00:00'
			            )
			        ELSE COALESCE(
			                TO_VARCHAR(CARSIM."FechaOperacion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR(CARSIM."FechaIngresoRegistro", 'HH24:MI:SS'),
			                '1900-01-01 00:00:00'
			            )
			    END AS "FechaMovimiento",
			    COALESCE(CARSIM."FolioExternoReferencia",'') AS "FolioTransaccion",
			    ''  AS "IdOrden",
			    '0' AS "IdComprobante",
			    COALESCE(CARSIM."IdOperacion",0) AS "IdOperacion",
			
			    CASE 
			        WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'COMSIM' THEN 'VESIMC'
			        WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'VENSIM' THEN 'VESIMV'
			    END AS "IdTipoOperacion",
			
			    COALESCE(CARSIM."IdCliente",0) AS "IdCliente",
			    COALESCE(CARSIM."IdPortafolio",0) AS "IdPortafolio",
			    COALESCE(CARSIM."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
			    COALESCE(CARSIM."IdEmisor",'') AS "IdEmisor",
			    COALESCE(SUB."Nemotecnico",'') AS "Nemotecnico",
			    COALESCE(CARSIM."INGCantidad",0.0) AS "Cantidad",
			    COALESCE(CARSIM."PrecioAPlazo",0.0) AS "Precio",
			    COALESCE(CARSIM."MontoAPlazo",0.0) AS "Monto",
			    0.0 AS "Tasa",
			    COALESCE(CARSIM."IdMonedaInstrumento",'') AS "IdMoneda",
			    COALESCE(CARSIM."IdInstrumento",0) AS "IdInstrumento",
			    COALESCE(SUB."IdTipoInstrumento",'') AS "IdTipoInstrumento",
			    COALESCE(CARSIM."IdTipoProducto",'') AS "IdTipoProducto",
			
			    '' AS "AccionOperacion",
			    CASE WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'COMSIM' THEN COALESCE(CARSIM."INGMontoAPlazo", 0.0) ELSE 0.0 END AS "MontoCompra",
			    CASE WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'VENSIM' THEN COALESCE(CARSIM."INGMontoAPlazo", 0.0) ELSE 0.0 END AS "MontoVenta",
			    COALESCE(CARSIM."TasaCompra",0.0) AS "TasaCompra",
			    0.0 AS "TasaVenta",
			    0.0 AS "TotalCompra",
			    0.0 AS "TotalVenta",
			
			    0 AS "IdTransaccion",
			    '0001-01-01 00:00:00' AS "FechaTransaccion",
			    '' AS "RelacionInstrumento",
			    0 AS "IdContraparte",
			    0 AS "IdOperadorContraparte",
			    0 AS "IdOperadorCorredor",
			    COALESCE(CARSIM."IdTipoComprobante",'') AS "IdTipoComprobante",
			    COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			    COALESCE(CARSIM."FolioComprobante",0) AS "FolioComprobante",
			    0.0 AS "ValorPresenteCompraInst",
			    COALESCE(CARSIM."FechaVencimientoOperacion",'0001-01-01 00:00:00') AS "FechaVencimiento",
			
			    0.0 AS "Comision",
			    0.0 AS "Derecho",
			    0.0 AS "Gasto",
			    0.0 AS "Impuesto",
			
			    COALESCE(CARSIM."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			    0 AS "IdDocumentoRelacion",
			    '0001-01-01' AS "FechaVencimientoPacto",
			    COALESCE(CARSIM."PrecioAPlazo",0.0) AS "PrecioPlazo",
			    0.0 AS "TasaPlazo",
			    '' AS "IdMonedaPlazo",
			    0.0 AS "TotalCompraVencimiento",
			    0.0 AS "TotalVentaVencimiento",
			    COALESCE(CARSIM."EGRIdTipoComprobante",'') AS "Custodia",
			    COALESCE(CARSIM."IdBolsa",'') AS "IdBolsa",
			    COALESCE(CARSIM."IdBoveda",'') AS "IdBoveda",
			    0.0 AS "CantidadGarantia",
			    COALESCE(CARSIM."NemoSVS",'') AS "NemoSVS",
			    0.0 AS "ValorPresente",
			    0.0 AS "UtilidadPerdida",
			    '0001-01-01 00:00:00' AS "FechaOrigenPacto",
			    COALESCE(LEFT(CARSIM."EGRGlosa", 100), '') AS "DescripcionOperacion",
			    '' AS "IdEstadoFactura",
			    COALESCE(CARSIM."IdTipoCanalIngreso",'') AS "Canal",
			    0.0 AS "ValorIVA",
			
			    0 AS "FolioComprobanteAsociado",   
			
			    '' AS "ModalidadEjecucion",
			    COALESCE(CAST(CARSIM."EsFungible" AS CHAR), 'N') AS "EsFungible",
			    0 AS "IdMovimientoRelacionado",
			    '' AS "CodigoOperadorContraparte",
			    '' AS "CodigoOperadorCorredor",
			
			    ''  AS "TipoOperacionSecundaria",
			    0.0 AS "ValorRescateInicial",
			
			    COALESCE(CARSIM."IdMonedaSecundaria",'') AS "IdMonedaSecundaria",
			
			    NPRO."IdNormativa",
			    NPRO."IdTipoDocumento",
			    NPRO."HIdTipoProducto",
			    NPRO."HIdClasificacionTipoInstrumento",
			    NPRO."HIdTipoInstrumento",
			    NPRO."HIdTipoOperacion",
			    NPRO."Uso",
			    '' AS "EsEnRueda",
			    '' AS "EsVentaCorta",
			
			    0.0 AS "ParidadCierre",
			    0   AS "PlazoVencimiento",
			    CSDN."DocumentoIdentidad",
			    CSDN."NombreCompleto",
			    'FAC' AS "IdModulo",
			    ''    AS "FechaIngresoRegistro",
			    0     AS "IdOperacionOrigenReasignacion",
			    ''    AS "IdSistemaBursatil",
			    COALESCE(CARSIM."INGCantidad", 0.0) AS "INGCantidad",
			    COALESCE(CARSIM."Nemotecnico", '')  AS "InstrumentoNegociacion",
			    0 AS "IdComprobanteReferencia",
			    0 AS "IdComprobanteOperacion",
			    'N' AS "EsVencimientoPacto",
			
			    COALESCE(CARSIM."IdCartera",0) AS "SecuenciaMovimiento",
			    --Campos nuevo mesa RF
			    '' AS "IdEstadoOperacion",
			    0 AS "ValorRescateInicialReserva",
			    '' AS "IdEmisorReserva",
			    '' AS "IdClasificacionTipoInstrumentoReserva",
		        0 AS "CantidadInicialReserva",
		        0 AS "MontoLiquidacionOperacionReserva",
		        0 AS "EgrMontoValorizadoReserva",
		        0 AS "IngMontoValorizadoReserva",
		        0 AS "TasaReserva",
		        0 AS "INGMontoAPlazoReserva",
		        0 AS "IdOperacionRelacionada",
				0 AS "IdCarteraOrigen"			
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARSIM
			LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante
			    ON tipocomprobante."IdTipoComprobante" = CARSIM."IdTipoComprobante"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CSDN
			    ON CSDN."IdCliente" = CARSIM."IdCliente"
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" AS ultSub
			    ON ultSub."FolioCarteraRelacionada" = CARSIM."FolioCartera"
			INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
			    ON SUB."IdSubyacente" = ultSub."IdSubyacente"
			
			INNER JOIN v_tablaProductos NPRO
			    ON (
			        (CARSIM."IdTipoProducto" = NPRO."IdTipoProducto" OR CARSIM."IdTipoProducto" = '')
			        AND (CARSIM."IdClasificacionTipoInstrumento" = (CASE WHEN CARSIM."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento" = '')
			        AND (CARSIM."IdTipoInstrumento" = (CASE WHEN CARSIM."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento" = '')
			        AND (CARSIM."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion" = ''
			             OR (CARSIM."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO') AND CARSIM."IdTipoInstrumento" <> 'SIM'))
			    )
			
			INNER JOIN v_tablaFecha PFECH
			    ON (
			        (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(CARSIM."FechaVencimientoOperacion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
			        OR
			        (PFECH."FECHADESDE" IS NULL AND TO_DATE(CARSIM."FechaVencimientoOperacion") <= TO_DATE(PFECH."FECHAHASTA"))
			    )
			
			WHERE CARSIM."IdTipoInstrumento" = 'SIM'
			  AND CARSIM."IdEstado" = 'VCD'
		UNION ALL
			SELECT 
			    ---CAMPOS COMUNES
			    COALESCE(OPE."FechaIngresoRegistro",'') AS "FechaCreacion",
			    COALESCE(CMO."FechaMovimiento", CMOREF."FechaMovimiento", OPE."FechaMovimiento", '') AS "FechaMovimiento",
			    COALESCE(CAST(OPE."FolioTransaccion"AS NVARCHAR(40)),'') AS "FolioTransaccion",
			    COALESCE(CAST(OPE."IdOrden"AS NVARCHAR(40)),'') AS "IdOrden",
			    COALESCE(CAST(OPE."IdComprobante"AS NVARCHAR(40)),'0') AS "IdComprobante",
			    COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
			    COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
			    COALESCE(OPE."IdCliente",0) AS "IdCliente",
			    COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
			    COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
			    COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
			    COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
			    COALESCE(OPE."Cantidad",0) AS "Cantidad",
			    COALESCE(OPE."Precio",0) AS "Precio",
			    COALESCE(OPE."MontoMovimientoComprobante",0) AS "Monto",
			    COALESCE(OPE."Tasa",0) AS "Tasa",
			    COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
			    COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
			    COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
			    COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
			
			    --(Compra/Venta)
			    COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
			    COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
			    COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
			    COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
			    COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
			    COALESCE(OPE."TotalCompra",0) AS "TotalCompra",
			    COALESCE(OPE."TotalVenta",0) AS "TotalVenta",
			
			    ---ESPECIFICOS: FACTURACION
			    COALESCE(OPE."IdTransaccion",0) AS "IdTransaccion",
			    COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') AS "FechaTransaccion",
			    COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			    COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			    COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			    COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			    COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			    COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			    COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			    COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			    COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
			
			    COALESCE(OPE."Comision",0) AS "Comision",
			    COALESCE(OPE."Derecho",0) AS "Derecho",
			    COALESCE(OPE."Gasto",0) AS "Gasto",
			    COALESCE(OPE."Impuesto",0) AS "Impuesto",
			
			    COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			    COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			    COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			    COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			    COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			    COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			    COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			    COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			    COALESCE(OPE."Custodia",'') AS "Custodia",
			    COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
			    COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
			    COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			    COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			    COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			    COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			    COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			    COALESCE(CAST(OPE."DescripcionOperacion"AS NVARCHAR(40)),'') AS "DescripcionOperacion",
			    COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			    COALESCE(OPE."Canal",'') AS "Canal",
			    COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			    COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			    COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			    COALESCE(OPE."EsFungible", '') AS "EsFungible",
			    COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			    OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			    OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor",
			
			    '' AS "TipoOperacionSecundaria",
			    0 AS "ValorRescateInicial",
			    '' AS "IdMonedaSecundaria",
			
			    NPRO."IdNormativa",
			    NPRO."IdTipoDocumento",
			    NPRO."HIdTipoProducto",
			    NPRO."HIdClasificacionTipoInstrumento",
			    NPRO."HIdTipoInstrumento",
			    NPRO."HIdTipoOperacion",
			    NPRO."Uso",
			    OPE."EsEnRueda" AS "EsEnRueda",
			    OPE."EsVentaCorta" AS "EsVentaCorta",
			
			    0 AS "ParidadCierre",
			    0 AS "PlazoVencimiento",
			    OPE."DocumentoIdentidad",
			    OPE."NombreCompleto",
			    'FAC' AS "IdModulo",
			    '' AS "FechaIngresoRegistro",
			    0 AS "IdOperacionOrigenReasignacion",
			    COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			    0 AS "INGCantidad",
			    '' AS "InstrumentoNegociacion",
			    COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			    COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			    'N' AS "EsVencimientoPacto",
			    COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			    --Campos nuevo mesa RF
			    '' AS "IdEstadoOperacion",
			    0 AS "ValorRescateInicialReserva",
			    '' AS "IdEmisorReserva",
			    '' AS "IdClasificacionTipoInstrumentoReserva",
		        0 AS "CantidadInicialReserva",
		        0 AS "MontoLiquidacionOperacionReserva",
		        0 AS "EgrMontoValorizadoReserva",
		        0 AS "IngMontoValorizadoReserva",
		        0 AS "TasaReserva",
		        0 AS "INGMontoAPlazoReserva",
		        0 AS "IdOperacionRelacionada",
				0 AS "IdCarteraOrigen"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
			INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" AS CMO
			    ON CMO."IdTransaccion" = OPE."IdTransaccion"
			LEFT JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" AS CMOREF
			    ON CMOREF."IdComprobanteReferencia" = OPE."IdComprobante"
			   AND CMOREF."IdTransaccion" = OPE."IdTransaccion"
			   AND CMOREF."FolioComprobante" > 0
			LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante
			    ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
			INNER JOIN v_tablaProductos NPRO
			    ON (
			        (OPE."IdTipoProducto"= NPRO."IdTipoProducto" OR OPE."IdTipoProducto"='')
			        AND (OPE."IdClasificacionTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='')
			        AND (OPE."IdTipoInstrumento" = (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='')
			        AND (
					  OPE."IdTipoOperacion" = NPRO."IdTipoOperacion"
					  OR NPRO."IdTipoOperacion" = ''
					  OR (
					      OPE."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO')
					      AND OPE."IdTipoOperacionRelacionada" = NPRO."IdTipoOperacion"
					      AND NOT EXISTS (
					        SELECT 1
					        FROM v_tablaProductos NPRO2
					        WHERE NPRO2."IdTipoProducto" = NPRO."IdTipoProducto"
					          AND NPRO2."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento"
					          AND NPRO2."IdTipoInstrumento" = NPRO."IdTipoInstrumento"
					          AND NPRO2."IdTipoOperacion" = OPE."IdTipoOperacion"
					      )
					  )
					)
			    )
			    AND OPE."EsVentaCorta" = CASE WHEN NPRO."IncluirVentaCorta" = 'N' THEN 'N' ELSE OPE."EsVentaCorta" END
			INNER JOIN v_tablaFecha PFECH
			    ON (
			        (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
			        OR
			        (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
			    )
			WHERE OPE."IdTipoInstrumento" = 'SPT'  
	),
	/* =========================
	   MESA RF
	   ========================= */
	mesa_rf AS (
	    SELECT
	        ---CAMPOS COMUNES
	        OPE."FechaCreacion" AS "FechaCreacion",
	        COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
	        COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') AS "FolioTransaccion",
	        '' AS "IdOrden",
	        '' AS "IdComprobante",
	        COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
	        COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
	        COALESCE(OPE."IdCliente",0) AS "IdCliente",
	        COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
	        COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	        COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
	        COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
	        COALESCE(OPE."Cantidad",0) AS "Cantidad",
	        COALESCE(OPE."Precio",0) AS "Precio",
	        COALESCE(OPE."MontoValorizado",0) AS "Monto",
	        COALESCE(OPE."Tasa",0) AS "Tasa",
	        COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
	        COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
	        COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
	        COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
	        COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
	        COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
	        COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
	        COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
	        COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
	        0 AS "TotalCompra",
	        0 AS "TotalVenta",
	        0 AS "IdTransaccion",
	        COALESCE(OPE."FechaCreacion", '0001-01-01 00:00:00') AS "FechaTransaccion",
	        '' AS "RelacionInstrumento",
	        0 AS "IdContraparte",
	        COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
	        COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
	        '' AS "IdTipoComprobante",
	        '' AS "TipoComprobante",
	        COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
	        COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
	        COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
	        0 AS "Comision",
	        0 AS "Derecho",
	        0 AS "Gasto",
	        0 AS "Impuesto",
	        COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
	        0 AS "IdDocumentoRelacion",
	        IFNULL(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
	        COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
	        COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
	        COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
	        COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
	        COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
	        COALESCE(OPE."Custodia",'') AS "Custodia",
	        COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
	        COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
	        COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
	        COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
	        COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
	        COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
	        COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
	        COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
	        CASE WHEN OPE."IdEstado" IN ('ANT','LIQ') AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC','ANTPAC') THEN 'VIG'
	             ELSE COALESCE(OPE."IdEstado",'??') END AS "IdEstadoFactura",
	        COALESCE(OPE."Canal",'') AS "Canal",
	        COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
	        COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
	        '' AS "ModalidadEjecucion",
	        COALESCE(OPE."EsFungible", '') AS "EsFungible",
	        0 AS "IdMovimientoRelacionado",
	        '' AS "CodigoOperadorContraparte",
	        '' AS "CodigoOperadorCorredor",
	        COALESCE(OPE."TipoOperacionSecundaria", '') AS "TipoOperacionSecundaria",
	        COALESCE(OPE."ValorRescateInicial", 0) AS "ValorRescateInicial",
	        '' AS "IdMonedaSecundaria",
	        NPRO."IdNormativa",
	        NPRO."IdTipoDocumento",
	        NPRO."HIdTipoProducto",
	        NPRO."HIdClasificacionTipoInstrumento",
	        NPRO."HIdTipoInstrumento",
	        NPRO."HIdTipoOperacion",
	        NPRO."Uso",
	        'N' AS "EsEnRueda",
	        'N' AS "EsVentaCorta",
	        0 AS "ParidadCierre",
	        0 AS "PlazoVencimiento",
	        OPE."DocumentoIdentidad",
	        OPE."NombreCompleto",
	        'MRF' AS "IdModulo",
	        '' AS "FechaIngresoRegistro",
	        OPE."IdOperacionOrigenReasignacion" AS "IdOperacionOrigenReasignacion",
	        '' AS "IdSistemaBursatil",
	        0 AS "INGCantidad",
	        '' AS "InstrumentoNegociacion",
	        0 AS "IdComprobanteReferencia",
	        0 AS "IdComprobanteOperacion",
	        'N' AS "EsVencimientoPacto",
	        0 AS "SecuenciaMovimiento",
		    --Campos nuevo mesa RF
		    OPE."IdEstadoOperacion",
		    OPE."ValorRescateInicialReserva",
		    OPE."IdEmisorReserva",
		    OPE."IdClasificacionTipoInstrumentoReserva",
	        OPE."CantidadInicialReserva",
	        OPE."MontoLiquidacionOperacionReserva",
	        OPE."EgrMontoValorizadoReserva",
	        OPE."IngMontoValorizadoReserva",
	        OPE."TasaReserva",
	        OPE."INGMontoAPlazoReserva",
	        OPE."IdOperacionRelacionada",
			OPE."IdCarteraOrigen"	
	    FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones" OPE
	    INNER JOIN v_tablaProductos NPRO
	        ON (
	            (OPE."IdTipoProducto"= NPRO."IdTipoProducto")
	            AND (OPE."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='')
	            AND (OPE."IdTipoInstrumento" = NPRO."IdTipoInstrumento" OR NPRO."IdTipoInstrumento"='')
	            AND (OPE."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion"='')
	        )
	    INNER JOIN v_tablaFecha PFECH
	        ON (
	            (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
	            OR
	            (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
	        )
	),
	
	/* =========================
	   FORWARD
	   ========================= */
	forward AS (
	    SELECT
	        ---CAMPOS COMUNES
	        OPE."FechaCreacion",
	        OPE."FechaMovimiento" AS "FechaMovimiento",
	        COALESCE(CAST(OPE."IdOperacion"AS  NVARCHAR(40)),'') AS "FolioTransaccion",
	        '' AS "IdOrden",
	        '' AS "IdComprobante",
	        COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
	        COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
	        COALESCE(OPE."IdCliente",0) AS "IdCliente",
	        COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
	        COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	        '' AS "IdEmisor",
	        COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
	        COALESCE(OPE."INGCantidad",0) AS "Cantidad",
	        COALESCE(OPE."TipoCambioCierre",0) AS "Precio",
	        COALESCE(OPE."INGMontoMonedaSecundaria",0) AS "Monto",
	        0 AS "Tasa",
	        COALESCE(OPE."IdMonedaPrincipal",'') AS "IdMoneda",
	        COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
	        COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
	        COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
	        COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
	        COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
	        COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
	        0 AS "TasaCompra",
	        0 AS "TasaVenta",
	        0 AS "TotalCompra",
	        0 AS "TotalVenta",
	        0 AS "IdTransaccion",
	        "FechaCreacion" AS "FechaTransaccion",
	        '' AS "RelacionInstrumento",
	        0 AS "IdContraparte",
	        0 AS "IdOperadorContraparte",
	        0 AS "IdOperadorCorredor",
	        '' AS "IdTipoComprobante",
	        '' AS "TipoComprobante",
	        COALESCE(OPE."IdOperacion",0) AS "FolioComprobante",
	        0 AS "ValorPresenteCompraInst",
	        OPE."FechaVencimiento" AS "FechaVencimiento",
	        0 AS "Comision",
	        0 AS "Derecho",
	        0 AS "Gasto",
	        0 AS "Impuesto",
	        '' AS "IdCondicionLiquidacion",
	        0 AS "IdDocumentoRelacion",
	        '' AS "FechaVencimientoPacto",
	        0 AS "PrecioPlazo",
	        0 AS "TasaPlazo",
	        '' AS "IdMonedaPlazo",
	        0 AS "TotalCompraVencimiento",
	        0 AS "TotalVentaVencimiento",
	        COALESCE(OPE."Custodia",'') AS "Custodia",
	        'BCS' AS "IdBolsa",
	        '' AS "IdBoveda",
	        0 AS "CantidadGarantia",
	        '' AS "NemoSVS",
	        0 AS "ValorPresente",
	        0 AS "UtilidadPerdida",
	        '' AS "FechaOrigenPacto",
	        COALESCE(CAST(OPE."TipoOperacion" AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
	        OPE."IdEstado" AS "IdEstadoFactura",
	        COALESCE(OPE."IdTipoCanalIngreso",'') AS "Canal",
	        0 AS "ValorIVA",
	        CASE WHEN 'NCG380Ope' = 'NCG380Operc' AND OPE."TipoOperacion" IN ('COMPRA FORWARD','VENTA FORWARD') THEN OPE."FolioOperacion"
	             ELSE 0 END AS "FolioComprobanteAsociado",
	        '' AS "ModalidadEjecucion",
	        '' AS "EsFungible",
	        0 AS "IdMovimientoRelacionado",
	        '' AS "CodigoOperadorContraparte",
	        '' AS "CodigoOperadorCorredor",
	        '' AS "TipoOperacionSecundaria",
	        0 AS "ValorRescateInicial",
	        COALESCE(OPE."IdMonedaSecundaria",'') AS "IdMonedaSecundaria",
	        NPRO."IdNormativa",
	        NPRO."IdTipoDocumento",
	        NPRO."HIdTipoProducto",
	        NPRO."HIdClasificacionTipoInstrumento",
	        NPRO."HIdTipoInstrumento",
	        NPRO."HIdTipoOperacion",
	        NPRO."Uso",
	        'N' AS "EsEnRueda",
	        'N' AS "EsVentaCorta",
	        OPE."ParidadCierre" AS "ParidadCierre",
	        CASE WHEN "FechaVencimiento" > TO_DATE('0001-01-01','YYYY-MM-DD')
	             THEN DAYS_BETWEEN("FechaOperacion", "FechaVencimiento")
	             ELSE 0 END AS "PlazoVencimiento",
	        OPE."DocumentoIdentidad",
	        OPE."NombreCompleto",
	        'MFW' AS "IdModulo",
	        '' AS "FechaIngresoRegistro",
	        0 AS "IdOperacionOrigenReasignacion",
	        '' AS "IdSistemaBursatil",
	        0 AS "INGCantidad",
	        '' AS "InstrumentoNegociacion",
	        0 AS "IdComprobanteReferencia",
	        0 AS "IdComprobanteOperacion",
	        'N' AS "EsVencimientoPacto",
	        0 AS "SecuenciaMovimiento",
		    --Campos nuevo mesa RF
		    '' AS "IdEstadoOperacion",
		    0 AS "ValorRescateInicialReserva",
		    '' AS "IdEmisorReserva",
		    '' AS "IdClasificacionTipoInstrumentoReserva",
	        0 AS "CantidadInicialReserva",
	        0 AS "MontoLiquidacionOperacionReserva",
	        0 AS "EgrMontoValorizadoReserva",
	        0 AS "IngMontoValorizadoReserva",
	        0 AS "TasaReserva",
	        0 AS "INGMontoAPlazoReserva",
	        0 AS "IdOperacionRelacionada",
			0 AS "IdCarteraOrigen"	        
	    FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones" as OPE
	    INNER JOIN v_tablaProductos NPRO
	        ON (
	            (OPE."IdTipoProducto"= NPRO."IdTipoProducto")
	            AND (OPE."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='')
	            AND (OPE."IdTipoInstrumento" = NPRO."IdTipoInstrumento" OR NPRO."IdTipoInstrumento"='')
	            AND (OPE."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion"='')
	        )
	    INNER JOIN v_tablaFecha PFECH
	        ON (
	            (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
	            OR
	            (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
	        )
	    WHERE OPE."IdEstadoCartera" NOT IN ('ANU','MOD')
	    AND OPE."IdTipoProducto" = 'MEFW'
	),
	
	/* =========================
	   FONDO MUTUO
	   ========================= */
	fondo_mutuo AS (
	    SELECT
	        OPE."FechaCreacion" AS "FechaCreacion",
	        COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
	        COALESCE(CAST(OPE."FolioContrato" AS  NVARCHAR(40)),'') AS "FolioTransaccion",
	        '' AS "IdOrden",
	        '' AS "IdComprobante",
	        COALESCE(OPE."IdSolicitudAporteRescate",0) AS "IdOperacion",
	        COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
	        COALESCE(OPE."IdCliente",0) AS "IdCliente",
	        COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
	        COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	        COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
	        COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
	        COALESCE(OPE."Cantidad",0) AS "Cantidad",
	        COALESCE(OPE."Precio",0) AS "Precio",
	        COALESCE(OPE."Monto",0) AS "Monto",
	        0 AS "Tasa",
	        COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
	        COALESCE(OPE."IdInstrumento",0) AS "IdInstrumento",
	        COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
	        COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
	        COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
	        COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
	        COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
	        0 AS "TasaCompra",
	        0 AS "TasaVenta",
	        0 AS "TotalCompra",
	        0 AS "TotalVenta",
	        0 AS "IdTransaccion",
	        COALESCE(OPE."FechaCreacion",'0001-01-01 00:00:00') AS "FechaTransaccion",
	        '' AS "RelacionInstrumento",
	        0 AS "IdContraparte",
	        0 AS "IdOperadorContraparte",
	        0 AS "IdOperadorCorredor",
	        '' AS "IdTipoComprobante",
	        '' AS "TipoComprobante",
	        COALESCE(OPE."IdSolicitudAporteRescate",0) AS "FolioComprobante",
	        0 AS "ValorPresenteCompraInst",
	        '' AS "FechaVencimiento",
	        0 AS "Comision",
	        0 AS "Derecho",
	        0 AS "Gasto",
	        0 AS "Impuesto",
	        '' AS "IdCondicionLiquidacion",
	        0 AS "IdDocumentoRelacion",
	        '' AS "FechaVencimientoPacto",
	        0 AS "PrecioPlazo",
	        0 AS "TasaPlazo",
	        '' AS "IdMonedaPlazo",
	        0 AS "TotalCompraVencimiento",
	        0 AS "TotalVentaVencimiento",
	        '' AS "Custodia",
	        COALESCE(OPE."Bolsa",'') AS "IdBolsa",
	        '' AS "IdBoveda",
	        0 AS "CantidadGarantia",
	        '' AS "NemoSVS",
	        0 AS "ValorPresente",
	        0 AS "UtilidadPerdida",
	        '' AS "FechaOrigenPacto",
	        OPE."TipoOperacion" AS "DescripcionOperacion",
	        CASE WHEN OPE."IdEstado" = 'CRT' THEN 'VIG' ELSE OPE."IdEstado" END AS "IdEstadoFactura",
	        OPE."Canal" AS "Canal",
	        0 AS "ValorIVA",
	        0 AS "FolioComprobanteAsociado",
	        '' AS "ModalidadEjecucion",
	        '' AS "EsFungible",
	        0 AS "IdMovimientoRelacionado",
	        '' AS "CodigoOperadorContraparte",
	        '' AS "CodigoOperadorCorredor",
	        '' AS "TipoOperacionSecundaria",
	        0 AS "ValorRescateInicial",
	        '' AS "IdMonedaSecundaria",
	        NPRO."IdNormativa",
	        NPRO."IdTipoDocumento",
	        NPRO."HIdTipoProducto",
	        NPRO."HIdClasificacionTipoInstrumento",
	        NPRO."HIdTipoInstrumento",
	        NPRO."HIdTipoOperacion",
	        NPRO."Uso",
	        'N' AS "EsEnRueda",
	        'N' AS "EsVentaCorta",
	        0 AS "ParidadCierre",
	        0 AS "PlazoVencimiento",
	        OPE."DocumentoIdentidad",
	        OPE."NombreCompleto",
	        'FMM' AS "IdModulo",
	        '' AS "FechaIngresoRegistro",
	        0 AS "IdOperacionOrigenReasignacion",
	        '' AS "IdSistemaBursatil",
	        0 AS "INGCantidad",
	        '' AS "InstrumentoNegociacion",
	        0 AS "IdComprobanteReferencia",
	        0 AS "IdComprobanteOperacion",
	        'N' AS "EsVencimientoPacto",
	        0 AS "SecuenciaMovimiento",
       		--Campos nuevo mesa RF
		    '' AS "IdEstadoOperacion",
		    0 AS "ValorRescateInicialReserva",
		    '' AS "IdEmisorReserva",
		    '' AS "IdClasificacionTipoInstrumentoReserva",
	        0 AS "CantidadInicialReserva",
	        0 AS "MontoLiquidacionOperacionReserva",
	        0 AS "EgrMontoValorizadoReserva",
	        0 AS "IngMontoValorizadoReserva",
	        0 AS "TasaReserva",
	        0 AS "INGMontoAPlazoReserva",
	        0 AS "IdOperacionRelacionada",
			0 AS "IdCarteraOrigen"
	    FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOperaciones" OPE
	    INNER JOIN v_tablaProductos NPRO
	        ON (
	            (OPE."IdTipoProducto"= NPRO."IdTipoProducto")
	            AND (OPE."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='')
	            AND (OPE."IdTipoInstrumento" = NPRO."IdTipoInstrumento" OR NPRO."IdTipoInstrumento"='')
	            AND (OPE."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion"='')
	        )
	    INNER JOIN v_tablaFecha PFECH
	        ON (
	            (PFECH."FECHADESDE" IS NOT NULL AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
	            OR
	            (PFECH."FECHADESDE" IS NULL AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
	        )
	),
	/* =========================
	 EVENTO CAPITAL
	 ========================= */
	evento_capital AS (
	    SELECT
			COALESCE(OPE."FechaIngresoRegistro",EVCLI."FechaHoraIngresoRegistro",'')	AS "FechaCreacion",
			COALESCE(TO_VARCHAR( OPE."FechaTransaccion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'),TO_VARCHAR(EVCLI."FechaPago", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (EVCLI."FechaHoraIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') AS "FolioTransaccion",
			COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",TEC."IdTipoOperacion",'') AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente", SOC."IdCliente", 0) AS "IdCliente",
			COALESCE(OPE."IdPortafolio",POR."IdPortafolio",0) AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",EVCLI."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
			COALESCE(OPE."IdEmisor",INS."IdEmisor",'') AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",INS."Nemotecnico",'') AS "Nemotecnico",
			CASE 
				WHEN EVCLI."IdTipoEventoCapital" = 'RC' THEN
					EVCLI."CantidadDerecho"
				ELSE COALESCE(OPE."Cantidad",EVCLI."CantidadNueva", 0)
			END AS "Cantidad",
			COALESCE(OPE."Precio",EVCLI."Precio",0) AS "Precio",
			COALESCE(OPE."MontoMovimientoComprobante",EVCLI."Total",0) AS "Monto",
			COALESCE(OPE."Tasa",EV."TasaEfectiva",0) AS "Tasa",
			COALESCE(OPE."IdMoneda",EV."IdMoneda",'') AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",INS."IdInstrumento",0) AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",EVCLI."IdTipoInstrumento",'') AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
			COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
			COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
			COALESCE(OPE."TotalCompra",0) AS "TotalCompra",
			COALESCE(OPE."TotalVenta",0) AS "TotalVenta",
			COALESCE(OPE."IdTransaccion",0) AS "IdTransaccion",
			COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') AS "FechaTransaccion",
			COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
			COALESCE(OPE."Comision",0) AS "Comision",
			COALESCE(OPE."Derecho",0) AS "Derecho",
			COALESCE(OPE."Gasto",0) AS "Gasto",
			COALESCE(OPE."Impuesto",0) AS "Impuesto",
			COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			COALESCE(OPE."IdBolsa",EVCLI."IdBolsa" ,'') AS "IdBolsa",
			COALESCE(OPE."IdBoveda",EVCLI."IdBoveda" ,'') AS "IdBoveda",
			COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),CAST(TEC."Nombre" AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			COALESCE(OPE."EsFungible",'') AS "EsFungible",
			COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor", 
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			OPE."EsEnRueda" 	AS "EsEnRueda",
			OPE."EsVentaCorta" 	AS "EsVentaCorta",
			0 AS "ParidadCierre",
			0 AS "PlazoVencimiento",
			COALESCE(OPE."DocumentoIdentidad", EVCLI."DocumentoIdentidad", '') AS "DocumentoIdentidad",
			COALESCE(OPE."NombreCompleto", EVCLI."NombreCliente" , '') AS "NombreCompleto",
			'EVC' AS "IdModulo",
			'' AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			CASE 
				WHEN EVCLI."IdTipoEventoCapital" = 'RC' THEN
					EVCLI."CantidadDerecho"
				ELSE COALESCE(OPE."Cantidad",EVCLI."CantidadNueva", 0)
			END AS "INGCantidad",
			OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
			COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			--Campos nuevo mesa RF
		    '' AS "IdEstadoOperacion",
		    0 AS "ValorRescateInicialReserva",
		    '' AS "IdEmisorReserva",
		    '' AS "IdClasificacionTipoInstrumentoReserva",
	        0 AS "CantidadInicialReserva",
	        0 AS "MontoLiquidacionOperacionReserva",
	        0 AS "EgrMontoValorizadoReserva",
	        0 AS "IngMontoValorizadoReserva",
	        0 AS "TasaReserva",
	        0 AS "INGMontoAPlazoReserva",
	        0 AS "IdOperacionRelacionada",
			0 AS "IdCarteraOrigen"
		FROM
			"BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVCLI
		INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEC ON TEC."IdTipoEventoCapital" = EVCLI."IdTipoEventoCapital" 
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = EVCLI."IdPortafolio" 
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = EVCLI."IdInstrumento" 
		INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EV ON EV."IdEventoCapital" = EVCLI."IdEventoCapital" 
		INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEV ON TEV."IdTipoEventoCapital" = EV."IdTipoEventoCapital" 
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EVCLI."IdSocioNegocio" 
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = SOC."IdPersona" 
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SDF ON SDF."IdInstrumento" = INS."IdInstrumento" 
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE 
			ON OPE."IdComprobante" = EVCLI."IdComprobanteFac" 
			AND OPE."Cantidad" = EVCLI."CantidadNueva"
			AND OPE."IdMovimientoComprobante" = EVCLI."IdMovimientoComprobanteFac"
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
		LEFT JOIN v_tablaProductos NPRO ON (
			(NPRO."IdTipoProducto" = EVCLI."IdTipoEventoCapital")
			AND (EVCLI."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento")
			AND (EVCLI."IdTipoInstrumento" = NPRO."IdTipoInstrumento")
		)
		INNER JOIN v_tablaFecha PFECH ON (
			(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(EVCLI."FechaPago") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))
			OR
			(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(EVCLI."FechaPago") <= TO_DATE(PFECH."FECHAHASTA"))
		)
		WHERE (SOC."IdCliente" = :p_IdCliente OR :p_IdCliente IS NULL)
		AND   (EVCLI."IdClasificacionTipoInstrumento" = :p_IdClasificacion OR :p_IdClasificacion IS NULL)
		AND   (EVCLI."IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento IS NULL)
		AND EV."IdTipoEventoCapital" IN ('EL', 'DA')
		AND EVCLI."IdEstado" = 'CER'
		AND :p_MostrarEVC = 'S'
	)
	SELECT * FROM facturacion
	UNION ALL SELECT * FROM mesa_rf
	UNION ALL SELECT * FROM forward
	UNION ALL SELECT * FROM fondo_mutuo
	UNION ALL SELECT * FROM evento_capital

),
LIBRO_ENRIQ AS (
  SELECT
    OPE."FechaMovimiento",
    OPE."FolioTransaccion",
    OPE."IdOrden",
    CASE WHEN OPE."IdTipoInstrumento" = 'PAC'
         THEN CAST(OPE."IdOperacion" AS NVARCHAR(40))
         ELSE OPE."IdComprobante"
    END AS "IdComprobante",
    OPE."IdOperacion",
    OPE."IdCliente",
    PER."DocumentoIdentidad" AS "RutCliente",
    COALESCE(NULLIF(TRIM(POR."Alias"), ''), PER."NombreCompleto") AS "NombreCliente",
    OPE."IdPortafolio",
    POR."CodigoPortafolio",
    CASE WHEN AREA."Descripcion" IS NULL THEN 'POR DEFINIR' ELSE AREA."Descripcion" END AS "AreaCliente",
    OPE."IdClasificacionTipoInstrumento",
    OPE."IdTipoInstrumento",
    CASE WHEN REA."IdEstado" = 'ANT' AND OPE."IdTipoInstrumento" = 'PAC' THEN 'ANTPAC'
         ELSE OPE."IdTipoOperacion"
    END AS "IdTipoOperacion",
	COALESCE(ClasInst."Nombre",'') 					AS "ClasificacionTipoInstrumento",
	COALESCE(TipoInstrumento."Descripcion",'') 		AS "TipoInstrumento",
	CASE WHEN  REA."IdEstado" =  'ANT' AND OPE."IdTipoInstrumento" = 'PAC' THEN 'ANTICIPO PACTOS'
		ELSE COALESCE(TipoOperacion."Nombre",'') 
    END AS "TipoOperacion",	
	OPE."AccionOperacion",
	(CASE COALESCE(RELNOR."RELACION",'NOVINCULADO') 
			WHEN 'NOVINCULADO' THEN 'NO'
			WHEN 'NOINFORMAR'  THEN 'NO'
			ELSE 'SI'
	END ) AS "Relacion",
				COALESCE(RELNOR."RELACION",'NOVINCULADO') AS "CodigoTipoRelacion",
				COALESCE(RELNOR."NombreValorIndicador",'NO VINCULADO') AS "TipoRelacion",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."IdEmisorReserva"
					ELSE OPE."IdEmisor" 
				END AS "IdEmisor",
				CASE WHEN OPE."IdTipoInstrumento" = 'SPT' THEN 
							CASE COM."IdMonedaSpot"
								WHEN 'BRL' THEN 'REAL'
								WHEN 'CAD' THEN 'DOLAR'
								WHEN 'USD' THEN 'DOLAR'
								WHEN 'PEN' THEN 'SOL'
								WHEN 'AUD' THEN 'DOLAR'
								WHEN 'RMB' THEN 'RENMINBI'
								WHEN 'DKK' THEN 'CORONA'
								WHEN 'JPY' THEN 'YEN'
								WHEN 'CHF' THEN 'FRANCO'
								WHEN 'NOK' THEN 'CORONA'
								WHEN 'NZD' THEN 'DOLAR'
								WHEN 'GBP' THEN 'LIBRA'
								WHEN 'SEK' THEN 'CORONA'
								WHEN 'HKD' THEN 'DOLAR'
								WHEN 'MXN' THEN 'PESO'
								WHEN 'EUR' THEN 'EURO'
								WHEN 'CLP' THEN 'DOLAR'
								WHEN 'COP' THEN 'PESO'
								WHEN 'CUP' THEN 'PESO'
								WHEN 'UF'  THEN 'UNIDAD'
								WHEN 'DO'  THEN 'DOLAR'			
							ELSE 'DOLAR' END
			    WHEN OPE."IdTipoInstrumento" = 'PAC' THEN
			    	CASE
						WHEN OPE."NemoSVS" != '' OR OPE."NemoSVS" IS NOT NULL  THEN OPE."NemoSVS"					-- se hizo cambio
					ELSE OPE."Nemotecnico" END 
				ELSE OPE."Nemotecnico" END AS "Nemotecnico",
				CASE WHEN OPE."IdTipoInstrumento" = 'PAC' THEN 
					CASE WHEN OPE."IdClasificacionTipoInstrumentoReserva" = 'IF'THEN OPE."ValorRescateInicialReserva" 
						ELSE OPE."CantidadInicialReserva"
					END
					ELSE OPE."Cantidad" 
				END AS "Cantidad",
				OPE."Precio" AS "Precio",
				OPE."Tasa" AS "Tasa",
				OPE."IdMoneda" AS "IdMoneda",
				OPE."IdMonedaSecundaria" AS "IdMonedaSecundaria",
				---ESPECIFICOS: FACTURACION
				OPE."TasaCompra" AS "TasaCompra",
				OPE."RelacionInstrumento" AS "RelacionInstrumento",
				OPE."IdOperadorContraparte" AS "IdOperadorContraparte",
				OPE."IdOperadorCorredor" AS "IdOperadorCorredor",
				OPE."IdTipoComprobante" AS "IdTipoComprobante",
				TPCOM."Nombre" AS "NombreTipoComprobante",
				OPE."FolioComprobante" AS "FolioComprobante",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."MontoLiquidacionOperacionReserva"
				    ELSE OPE."ValorPresenteCompraInst"
				END  AS "ValorPresenteCompraInst",
				OPE."FechaVencimiento" AS "FechaVencimiento",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."EgrMontoValorizadoReserva"
				    ELSE OPE."MontoCompra" 
				END AS "MontoCompra",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."IngMontoValorizadoReserva"
				    ELSE OPE."MontoVenta" 
		    	END AS "MontoVenta",
				OPE."Comision" AS "Comision",
				OPE."Derecho" AS "Derecho",
				OPE."Gasto" AS "CargoFijo",
				OPE."Impuesto" AS "IVA",
				OPE."TotalCompra",
				OPE."TotalVenta",
				OPE."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
				OPE."IdDocumentoRelacion" AS "IdDocumentoRelacion",
				OPE."FechaVencimientoPacto" AS "FechaVencimientoPacto",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."TasaReserva"
				    ELSE OPE."PrecioPlazo" 
				END AS "PrecioPlazo",
				OPE."TasaPlazo" AS "TasaPlazo",
				OPE."IdMonedaPlazo" AS "IdMonedaPlazo",
				OPE."TotalCompraVencimiento" AS "TotalCompraVencimiento",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."INGMontoAPlazoReserva"
				    ELSE OPE."TotalVentaVencimiento" 
				END AS "TotalVentaVencimiento",
				OPE."Custodia" AS "Custodia",
				OPE."IdBoveda" AS "IdBoveda",
				OPE."CantidadGarantia" AS "CantidadGarantia",
				OPE."NemoSVS" AS "NemoSVS",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."TasaReserva"
				    ELSE OPE."TasaVenta" 
				END  AS "TasaVenta",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN OPE."MontoLiquidacionOperacionReserva"
				    ELSE OPE."ValorPresente" 
				END AS "ValorPresente",
				OPE."UtilidadPerdida" AS "UtilidadPerdida",
				OPE."FechaOrigenPacto" AS "FechaOrigenPacto",
				OPE."IdEstadoFactura" AS "IdEstadoFactura",
				OPE."Canal" AS "Canal",
				OPE."ValorIVA" AS "ValorIVA",
				OPE."IdComprobanteReferencia" AS "IdComprobanteReferencia",
				OPE."FolioComprobanteAsociado" AS "FolioComprobanteAsociado",

    COALESCE(CNC."NombreCorto", CP."NombreCtaPropia")              AS "NombreComprador",
    ''                                                             AS "NombreVendedor",
    COALESCE(CNC."DocumentoIdentidad", CP."DocumentoIdentidadCtaPropia") AS "RutComprador",
    ''                                                             AS "RutVendedor",

    OPE."IdBolsa",
    CASE
      WHEN OPE."IdCliente" = CP."IdClienteCtaPropia" THEN '1'
      ELSE CASE
             WHEN COALESCE(RELNOR."RELACION",'NOVINCULADO')='NOVINCULADO' THEN '2'
             WHEN COALESCE(RELNOR."RELACION",'NOVINCULADO')<>'NOVINCULADO'
                  AND COALESCE(IND."ValorIndicador",'N')='N' THEN '3'
             WHEN COALESCE(RELNOR."RELACION",'NOVINCULADO')<>'NOVINCULADO'
                  AND COALESCE(IND."ValorIndicador",'N')='S' THEN '4'
           END
    END AS "Proceso",
	  			OPE."EsVentaCorta" AS "EsVentaCorta",
	  			CASE OPE."EsVentaCorta" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '' END as "EsVentaCortaUI",
	  			CASE OPE."IdModulo" WHEN 'FAC' THEN COALESCE(CAST( OPE."FolioComprobante" AS  NVARCHAR(40)),'')
	  								WHEN 'MSP' THEN COALESCE(CAST( OPE."FolioComprobante" AS  NVARCHAR(40)),'')
	  								 ELSE '' END AS "FolioDTE",
	  								 
			    OPE."ParidadCierre",
			    OPE."PlazoVencimiento",
			    REA."IdOperacionRelacionada",
			    REA."IdOperacionOrigenReasignacion",
				OPE."InstrumentoNegociacion"
  FROM LIBRO_BASE OPE
  CROSS JOIN CTA_PROPIA CP
  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
          ON OPE."IdPortafolio" = POR."IdPortafolio"
  INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
          ON CLI."IdCliente" = OPE."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
         ON PER."IdPersona" = CLI."IdPersona" AND PER."TipoDocumentoIdentidad" = 'R'
  LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" TPCOM ON OPE."IdTipoComprobante" = TPCOM."IdTipoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON OPE."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = OPE."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = OPE."IdClasificacionTipoInstrumento"
	---CONTRAPARTE CORREDOR
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS AREA ON (AREA."Codigo" = POR."AreaCliente")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"		  CNC ON (CNC."IdCorredora"=OPE."IdContraparte")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
				ON ( IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
					AND IND."IdCliente" =OPE."IdCliente")
 	LEFT OUTER JOIN ---RELACIONADO
 			(SELECT COALESCE(REL."CodigoValorIndicador" ,'NOVINCULADO') AS "RELACION",REL."IdCliente",COALESCE(REL."NombreValorIndicador",'NO VINCULADO') AS "NombreValorIndicador"
     			 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
				--LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" RELHOM
	     		--	ON ((CAST (REL."CodigoValorIndicador" AS  nvarchar(100))= RELHOM."ValorOrigen" AND RELHOM."Tipo"='RELACION' AND RELHOM."Esquema"=v_Esquema AND RELHOM."Version"=v_Version))
			 	 WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
 		) RELNOR ON (RELNOR."IdCliente" =OPE."IdCliente")
 	LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS REA ON OPE."IdOperacion" = REA."IdOperacion"
 	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COM ON CASE WHEN OPE."IdComprobante" = '' THEN 0 ELSE OPE."IdComprobante" END = COM."IdComprobante"
   ---PACTOS
    --LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON REA."IdOperacion" = RES."IdOperacion" AND OPE."IdTipoInstrumento" = 'PAC' 
    --LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARR ON   carr."IdCartera" = OPE."IdCarteraOrigen"
   where (OPE."IdClasificacionTipoInstrumento" = :p_IdClasificacion OR :p_IdClasificacion IS NULL)
		AND (OPE."IdTipoInstrumento" = :p_IdTipoInstrumento OR :p_IdTipoInstrumento IS NULL)
		AND (OPE."IdMoneda" = :p_IdMoneda OR :p_IdMoneda IS NULL)
		AND (OPE."IdBolsa" = :p_IdBolsa OR :p_IdBolsa IS NULL)
		AND (OPE."IdCliente" = :p_IdCliente OR :p_IdCliente IS NULL)
		AND (OPE."IdInstrumento" = :p_IdInstrumento OR :p_IdInstrumento IS NULL)
		--* MSH, IMPCREDOPT-2527
		AND ( ( OPE."IdEstadoFactura" IN ('VIG','ANU', 'LIQ', 'ENV', 'FAC', 'RGN', 'EMI') AND OPE."IdTipoProducto" IN ('MERF', 'MEME') ) OR (OPE."IdTipoProducto" NOT IN ('MERF', 'MEME') AND OPE."IdEstadoFactura"<>'ANU' ) )
		--AND OPE."IdTipoOperacion"  = 'VENACN'--p_IdTipoOperacion
		AND (
			:p_MostrarPactos = 'S' OR
			(
				:p_MostrarPactos = 'N' AND OPE."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC', 'ANTPAC','VCDPAC')
			)
		)
		AND  ((OPE."IdTipoInstrumento" <> 'PAC') OR (OPE."IdTipoInstrumento" = 'PAC' AND  OPE."IdEmisor"='')) 
	    AND (( OPE."IdTipoOperacion" IN ( 'VENPAC','COMPAC') AND REA."IdEstado" NOT IN ('ANT','LIQ')) 
              OR (OPE."IdTipoOperacion" IN ( 'ANTPAC','VCDPAC') AND REA."IdEstado"  IN ('ANT','LIQ'))
              OR (OPE."IdTipoOperacion" NOT IN ( 'ANTPAC','VCDPAC')) )
	    AND (OPE."IdTipoOperacion"  = :p_IdTipOperacion OR :p_IdTipOperacion IS NULL)	

	ORDER BY OPE."FechaMovimiento"

   )
SELECT *
FROM LIBRO_ENRIQ

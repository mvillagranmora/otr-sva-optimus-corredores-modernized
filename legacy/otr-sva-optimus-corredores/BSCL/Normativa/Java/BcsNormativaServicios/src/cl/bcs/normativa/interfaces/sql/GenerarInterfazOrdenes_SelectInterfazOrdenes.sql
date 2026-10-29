SELECT 
	 'D' AS "TipoRegistro"
	,'S80080' AS "CodigoAI" --Constante
	,TO_CHAR(ORDEN."FechaOperacionOrden",'DDMMYYYY') AS "FechaOrden"
	,TO_BIGINT(CASE WHEN COALESCE(ORDEN."FolioOrden",'') = '' THEN 0 ELSE ORDEN."FolioOrden" END) AS "FolioOrden"
	,TO_CHAR(ORDEN."FechaOperacionOrden",'hhmmss') AS "HoraMinutoSegundoOrden"
	,'' AS "Mercado" --Pendiente
	,'1' AS "TipoCodigoCliente"
	,COALESCE(DOCUMENTO_RUT."DocumentoIdentidad",'') AS "CodigoCliente"
	,CASE WHEN ORDEN."AccionTipoOperacion" = 'COMPRA' THEN 'C' 
		  WHEN ORDEN."AccionTipoOperacion" = 'VENTA'	THEN 'V'
	 END AS "TipoOperacion"
	,'' AS "ModalidadOperacion" --Pendiente
	,COALESCE(INSTRUMENTO."EstaInscrito",'') AS "FlagInscrito"
	,ORDEN."Instrumento" AS "CodigoValor"
	,TO_DECIMAL(ORDEN."CantidadOrden",26,8) AS "CantidadOrden"
	,CASE WHEN ORDEN."TipoPrecio" = 'MERCADO' THEN 'M' 
	 ELSE CASE 	WHEN ORDEN."AccionTipoOperacion" = 'COMPRA' THEN 'C'
	 			WHEN ORDEN."AccionTipoOperacion" = 'VENTA'  THEN 'V'
	 	   END
	 END AS "FlagPrecio"
	,CASE WHEN ORDEN."TipoPrecio" = 'MERCADO' THEN TO_DECIMAL(0,26,8) 
	 ELSE TO_DECIMAL(ORDEN."PrecioOrden",26,8) END AS "Precio" --Es Cero cuando el flag precio es M
	,TO_DECIMAL(ORDEN."MontoOrden",26,8) AS "MontoOrdenado"
	,CASE WHEN ORDEN."FechaVigencia" = '0001-01-01' THEN 0 ELSE ABS(DAYS_BETWEEN(ORDEN."FechaLiquidacion",ORDEN."FechaVigencia")) END AS "VigenciaOrden"
	,CASE WHEN ORDEN."AccionTipoOperacion" = 'VENTA' THEN TO_DECIMAL(ORDEN."CantidadOrden",26,8) ELSE TO_DECIMAL(0,26,8) END AS "CantidadValoresDisponiblesOrdenesVenta"
	,CASE WHEN ORDEN."AccionTipoOperacion" = 'COMPRA' THEN TO_DECIMAL(ORDEN."MontoOrden",26,8) ELSE TO_DECIMAL(0,26,8) END AS "DineroDisponibleOrdenesCompra"
	,COALESCE(HOMOLOGACION_MONEDA."ValorHomologacion",'') AS "MonedaOrdenado"
	,'' AS "FlagEstadoOrden" --Pendiente
	,0 AS "NumeroOrdenModificada" --Pendiente
	,'' AS "PaisMercadoIntegrado" --Pendiente
	,CASE WHEN COALESCE(ORDEN."CodigoClaseOrden",'') = 'FAXCONFIRMACIONPOSTERIOR' THEN 'P' ELSE 'N' END AS "MedioInicialRecepcionOrden"
	,'' AS "SustentoCambio"
	,TO_DECIMAL(0,26,8) AS "PrecioStop" --Pendiente
	,COALESCE(DIRECCION_FISICA."CodigoDivisionPais",'') AS "UbigeoOficina"
	,COALESCE(PERSONA_AUTORIZADO."DocumentoIdentidad",'0') AS "PrimerRepresentante" 
	,'0' AS "SegundoRepresentante" 
	,'0' AS "TercerRepresentante" 
	,0 AS "CorrelativoCorrecciones" --Pendiente
FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS ORDEN
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE ON (CLIENTE."IdCliente" = ORDEN."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_RUT ON (CLIENTE."IdPersona" = DOCUMENTO_RUT."IdPersona" AND DOCUMENTO_RUT."IdTipoDocumentoIdentidad" = 'RUT')
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INSTRUMENTO ON (INSTRUMENTO."IdInstrumento" = ORDEN."IdInstrumento") 
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_MONEDA ON (HOMOLOGACION_MONEDA."IdCampo" = 'Moneda' AND HOMOLOGACION_MONEDA."Valor" = ORDEN."IdMonedaOrden")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMPLEADO ON (EMPLEADO."IdEmpleado" = ORDEN."IdEmpleadoDigitador")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA_EMPLEADO ON (PERSONA_EMPLEADO."IdPersona" = EMPLEADO."IdPersona")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIRECCION_FISICA ON (DIRECCION_FISICA."IdDireccionFisica" = PERSONA_EMPLEADO."IdDireccionFisicaPrincipal")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA_AUTORIZADO ON (PERSONA_AUTORIZADO."IdPersona" = ORDEN."IdPersonaAutorizadoDarOrdenes")
WHERE (TO_CHAR(ORDEN."FechaOperacionOrden",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD'))
AND (:EsTodosCliente = 'S' OR ORDEN."IdCliente" = :IdCliente)
AND (:EsTodosInstrumentos = 'S' OR ORDEN."IdInstrumento" = :IdInstrumento)
AND ORDEN."IdEmpresa" = :IdEmpresa;
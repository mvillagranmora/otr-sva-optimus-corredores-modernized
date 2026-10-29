SELECT
	CMD."IdCargaMasiva",
	CMD."IdCargaMasivaDetalle",
	CMD."IdConcepto",
	CMD."IdTipoOperacion",
	CMD."DocumentoIdentidad",
	CMD."IdPortafolio",
	CMD."CodigoPortafolio",
	POR."IdCliente",
	CMD."IdTipoComprobante",
	CMD."IdCondicionDeLiquidacion",
	CDL."PlazoEnDias",      -- Días plazo para liquidar, según la condición de liquidación
	CMD."IdMoneda",
	CMD."Monto",
	CMD."Glosa",
	CMD."Referencia",
	CMD."TipoReferencia",
	CMD."IdMedioDePago",
	CMD."IdModalidadDePago",
	CMP."CodigoArchivo",	    -- Ej. CARGA_MOVIMIENTOS_FACTURACION
	CMP."IdEmpleadoDigitador",  -- viene del complemento
	CMP."IdBolsa",				-- viene del complemento
	CMP."IdBoveda",				-- viene del complemento
	IND."ValorIndicador" AS "OperaCuentaInversion",
	CMD."TipoComprobanteDestino", -- NC
	CMD."FolioComprobante",		 -- NC
	COALESCE(TCO."AfectoImpuesto",'N') AS "AfectoImpuesto"   -- De acuerda al tipo de comprobante facturacion, se indica si debe generar cobros(IVA)
FROM "BSCL.Facturacion.BD::GBL.ProcesarCargaMasiva_Proceso" AS CMP
INNER JOIN "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMP."IdCargaMasiva")		
INNER JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" AS ConfFac ON ConfFac."CodigoParametro" = 'NOTASCREDITOPARCIAL' AND ConfFac."Valor" = 'N'
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL ON (CDL."IdCondicionDeLiquidacion" = CMD."IdCondicionDeLiquidacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON (IND."IdPortafolio" = CMD."IdPortafolio" AND IND."CodigoTipoIndicador"  = 'GEN_OCCINV')
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TCO ON (TCO."IdTipoComprobante" = CMD."IdTipoComprobante")
WHERE CMP."CodigoArchivo"='CARGA_NOTA_DE_CREDITO' AND CMP."IdCargaMasiva" IN (:IdCargaMasiva);
 
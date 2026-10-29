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
	COALESCE(TCO."AfectoImpuesto",'N') AS "AfectoImpuesto",   -- De acuerda al tipo de comprobante facturacion, se indica si debe generar cobros(IVA)
	CP."Contrapartida",
	CMD."FechaLiquidacion"
FROM "BSCL.Facturacion.BD::GBL.ProcesarCargaMasiva_Proceso" AS CMP
INNER JOIN "BSCL.Facturacion.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = CMP."IdCargaMasiva")		
LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = CMD."IdPortafolio")
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CDL ON (CDL."IdCondicionDeLiquidacion" = CMD."IdCondicionDeLiquidacion")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON (IND."IdPortafolio" = CMD."IdPortafolio" AND IND."CodigoTipoIndicador"  = 'GEN_OCCINV')
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TCO ON (TCO."IdTipoComprobante" = CMD."IdTipoComprobante")
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CP ON CP."IdConcepto" = CMD."IdConcepto"
WHERE CMP."CodigoArchivo"='CARGA_MOVIMIENTOS_OTROS_CONCEPTOS' AND CMP."IdCargaMasiva" IN (:IdCargaMasiva);
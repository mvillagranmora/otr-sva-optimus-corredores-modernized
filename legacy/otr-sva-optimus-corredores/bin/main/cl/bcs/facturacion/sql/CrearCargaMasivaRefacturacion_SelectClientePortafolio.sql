
SELECT 
	CLI."IdPortafolio", 
	CLI."CodigoPortafolio", 
	CLI."NombreCompleto",
	CLI."DocumentoIdentidad",
	CLI."IdCliente",
	IND1."ValorIndicador" AS "PagoCobroCuentaInversion",	
	IND2."ValorIndicador" AS "OperaCustodiaRV",
	IND3."ValorIndicador" AS "OperaCustodiaRFIF"
	
FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS  CLI
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  AS IND1 ON (IND1."IdPortafolio" = CLI."IdPortafolio" AND IND1."CodigoTipoIndicador"='GEN_OCCINV')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  AS IND2 ON (IND2."IdPortafolio" = CLI."IdPortafolio" AND IND2."CodigoTipoIndicador"='ORDOCUSTRV')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  AS IND3 ON (IND3."IdPortafolio" = CLI."IdPortafolio" AND IND3."CodigoTipoIndicador"='ORDOCUSTRF')
WHERE CLI."DocumentoIdentidad" IN(:DocumentoIdentidad) AND CLI."IdPortafolio" IN (:IdPortafolio); 


WITH PAGOS AS (
    SELECT 
		SOR."PortafolioDerivados"					AS "IdPortafolio",
		CASE 
			WHEN SUM(SOR."PerdidaGanancia") > 0 
				THEN 'LIDERE' 
				ELSE 'LIDEPA' 
			END 									AS "IdTipoOperacion",
		SUM(SOR."PerdidaGanancia")					AS  "Monto",
		SOR."Fecha"									AS  "Fecha",
		SOR."FechaVencimiento"						AS  "FechaVencimiento",
		SOR."NumeroReferencia"						AS  "NumeroReferencia"
    FROM 		"BSCL.Custodia.BD::CUS.SorteoContratos"	SOR
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 	AS CLI
	ON SOR."PortafolioDerivados" = CLI."IdPortafolio"
	
	INNER JOIN ((SELECT    
				"IdPortafolio",
        		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion"
			FROM (	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
    				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio") 
			WHERE "IdPortafolio" IN ("IdPortafolio")
			GROUP BY "IdPortafolio")
		)	 AS CTA 
		ON (CTA."IdPortafolio" = CLI."IdPortafolio")
	

    WHERE "Fecha" = :FechaSistema
    AND CTA."OperaCuentaInversion" = 'N'
    GROUP BY SOR."PortafolioDerivados", SOR."Fecha", SOR."FechaVencimiento", SOR."NumeroReferencia"
)


    SELECT 
		SOR."IdPortafolio"								AS "IdPortafolio",	
    	PAG."IdMedioDePago"    							AS "IdMedioDePago",
    	MOP."IdModalidadDePago"							AS "IdModalidadPago",
    	SOR."Monto"										AS  "Monto",
    	PAG."IdCuentaBancaria"							AS  "IdCuentaBancaria",
    	PAG."NombreBancoCuentaBancaria"					AS  "NombreBanco",
    	PAG."NumeroCuentaBancaria"						AS  "NumeroCuentaBancaria",
    	PAG."IdTipoCuentaBancaria"						AS  "IdTipoCuentaBancaria",
    	PAG."NombreTipoCuentaBancaria"					AS  "NombreTipoCuentaBancaria",
    	PAG."IdTipoTarjetaBancaria"						AS  "IdTipoTarjetaBancaria",
    	PAG."NumeroTarjetaBancaria"						AS  "NumeroTarjetaBancaria",    					
    	TO_VARCHAR(SOR."FechaVencimiento",'YYYY-MM-DD') AS 	 "FechaVencimiento",
    	SOR."NumeroReferencia"							AS   "NumeroReferencia"
    FROM 		PAGOS																SOR
    INNER JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"		PAG
    ON (SOR."IdPortafolio" = PAG."IdPortafolio" AND SOR."IdTipoOperacion" = PAG."IdTipoOperacion")
    INNER JOIN   "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"			MOP
    ON (MOP."IdMedioDePago" = PAG."IdMedioDePago")
    

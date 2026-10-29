
WITH FPAGOS AS (
    SELECT 
    	FUT."IdCliente"															AS "IdCliente",
		FUT."IdPortafolio"														AS "IdPortafolio",
	CASE 
		WHEN :TipoVencimiento = 'D' THEN 
			(FUT."MontoATasaMercadoActual" - FUT."MontoATasaMercadoAnterior")	
		ELSE 
			(FUT."MontoATasaMercadoActual" - FUT."PrecioSubyacente") -- PENDIENTE P*Q*LP
		END																		AS "Monto",			
		FUT."FechaOperacion"													AS "Fecha",
		FUT."FechaVencimientoOperacion"											AS "FechaVencimiento",
		FUT."NumeroCargoAbono"													AS  "NumeroReferencia",	
		:TipoVencimiento														AS "TipoVencimiento"	
FROM	
	"BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos"		FUT
WHERE 
 
	((FUT."FechaVencimientoOperacion" > :FechaSistema AND 'D' = :TipoVencimiento ) OR
	 (FUT."FechaVencimientoOperacion" = :FechaSistema AND 'V'= :TipoVencimiento ))		

	 )

    SELECT 
		FPA."IdPortafolio"								AS "IdPortafolio",	
    	PAG."IdMedioDePago"    							AS "IdMedioDePago",
    	MOP."IdModalidadDePago"							AS "IdModalidadPago",
    	FPA."Monto"										AS  "Monto",
    	PAG."IdCuentaBancaria"							AS  "IdCuentaBancaria",
    	PAG."NombreBancoCuentaBancaria"					AS  "NombreBanco",
    	PAG."NumeroCuentaBancaria"						AS  "NumeroCuentaBancaria",
    	PAG."IdTipoCuentaBancaria"						AS  "IdTipoCuentaBancaria",
    	PAG."NombreTipoCuentaBancaria"					AS  "NombreTipoCuentaBancaria",
    	PAG."IdTipoTarjetaBancaria"						AS  "IdTipoTarjetaBancaria",
    	PAG."NumeroTarjetaBancaria"						AS  "NumeroTarjetaBancaria",    					
    	TO_VARCHAR(FPA."FechaVencimiento",'YYYY-MM-DD') AS 	 "FechaVencimiento",
    	FPA."NumeroReferencia"							AS   "NumeroReferencia",
		CASE 
			WHEN "Monto"  > 0  
			THEN 	'LIFURE' 
			ELSE 	'LIFUPA'
		END 											AS 	"IdTipoOperacion"
    	
    	
    FROM 		FPAGOS																FPA
    INNER JOIN 	"BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"		PAG
    ON (FPA."IdPortafolio" = PAG."IdPortafolio" AND PAG."IdTipoOperacion" = 		CASE 
																						WHEN "Monto"  > 0  
																						THEN 	'LIFURE' 
																						ELSE 	'LIFUPA'
																					END)
    INNER JOIN   "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"			MOP
    ON (MOP."IdMedioDePago" = PAG."IdMedioDePago")
    
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente" = FPA."IdCliente")
	INNER JOIN ((SELECT    
				"IdPortafolio",
        		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion"
			FROM (	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
    				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio") 
			WHERE "IdPortafolio" IN ("IdPortafolio")
			GROUP BY "IdPortafolio")
		)	 AS CTA 
		ON (CTA."IdPortafolio" = CLI."IdPortafolioDefecto")--CLI."IdPortafolio")
WHERE
	CTA."OperaCuentaInversion" = 'N'
WITH TESORERIA AS (
	Select  
	FUT."NumeroCargoAbono"													AS "NumeroReferencia",
	FUT."IdPortafolio"														AS "IdPortafolio",
	CASE 
		WHEN :TipoVencimiento = 'D' THEN 
			(FUT."MontoATasaMercadoActual" - FUT."MontoATasaMercadoAnterior")	
		ELSE 
			(FUT."MontoATasaMercadoActual" - FUT."PrecioSubyacente") -- PENDIENTE P*Q*LP
		END																	AS "Monto",

	FUT."FechaLiquidacionOperacion"											AS "FechaLiquidacion",
	FUT."FechaComprobante"													AS "FechaComprobante",
	:TipoVencimiento														AS "TipoVencimiento"	
	
FROM	
	"BSCL.Custodia.BD.Vistas::DER.ConsultarCarteraFuturos"		FUT
WHERE 
	((FUT."FechaVencimientoOperacion" > :FechaSistema AND 'D' = :TipoVencimiento ) OR
	 (FUT."FechaVencimientoOperacion" = :FechaSistema AND 'V'= :TipoVencimiento ))
)


SELECT 
	TES."NumeroReferencia"										AS "NumeroReferencia",
	CASE 
		WHEN TES."Monto"  > 0
			THEN 'MOVING'
			ELSE 'MOVEGR'
		END 													AS "IdTipoMovimiento",
 
	TES."IdPortafolio"											AS 	"IdPortafolio", 
	CLI."IdPersona"												AS "IdPersona",
	TES."Monto",
	TES."FechaLiquidacion",
	TES."FechaComprobante",
	CASE
		WHEN TES."TipoVencimiento" = 'D' THEN 
		CASE
			WHEN TES."Monto"  > 0 
				THEN 	'ABLFDI' 
				ELSE 	'CALFDI'
			END	
		WHEN TES."TipoVencimiento" = 'V' THEN
		CASE 
			WHEN TES."Monto"  > 0 
				THEN 	'ABLFDI' 
				ELSE 	'CALFDI'
			END	
		END							AS "IdConcepto",
	CASE 
		WHEN TES."Monto"  > 0  
			THEN 	'LIFURE' 
			ELSE 	'LIFUPA'
	END 							AS "IdTipoOperacion",	
	CASE 
			WHEN SOC."IdCliente"  		> 0 	THEN 'CLI'
			WHEN SOC."IdEmpleado"		> 0 	THEN 'EMP'
			WHEN SOC."IdContraparte"	> 0 	THEN 'CNT'
			WHEN SOC."IdProveedor"   	> 0 	THEN 'PRO'
			ELSE ''
	END															AS "IdTipoPersona"	

FROM TESORERIA															AS TES
INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 	AS CLI
		ON TES."IdPortafolio" = CLI."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SOC 
	ON (SOC."IdCliente" = CLI."IdCliente")
INNER JOIN ((SELECT    
				"IdPortafolio",
        		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion"
			FROM (	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
    				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio") 
			WHERE "IdPortafolio" IN ("IdPortafolio")
			GROUP BY "IdPortafolio")
		)	 AS CTA 
		ON (CTA."IdPortafolio" = CLI."IdPortafolio")
WHERE 
	CTA."OperaCuentaInversion" = 'N'		




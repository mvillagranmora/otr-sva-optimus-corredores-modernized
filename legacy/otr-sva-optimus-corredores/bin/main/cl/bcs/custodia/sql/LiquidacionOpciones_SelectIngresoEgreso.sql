WITH CTE AS (
	Select  
	SOR."NumeroReferencia"													AS "NumeroReferencia",		
	CASE 
		WHEN SOR."PerdidaGanancia"  > 0 
			THEN 'MOVING'
			ELSE 'MOVEGR'
		END 																AS "IdTipoMovimiento",
		
	CLI."IdPortafolio"														AS "IdPortafolio",
	CLI."IdPersona"															AS "IdPersona",
	ABS(SOR."PerdidaGanancia")												AS "Monto",
	TO_VARCHAR(SOR."FechaVencimiento",'YYYY-MM-DD')							AS "FechaLiquidacion",
	TO_VARCHAR(SOR."Fecha",'YYYY-MM-DD')									AS "FechaComprobante",

	CASE
	WHEN SOR."SaldoEjercicio" <> 0 THEN 		
			CASE 
				WHEN SOR."PerdidaGanancia"  > 0 THEN 	'ABEJ' 
			ELSE 										'CAEJ'
		END 
	WHEN SOR."SaldoSorteo" <> 0 THEN
			CASE 
				WHEN SOR."PerdidaGanancia"  > 0 THEN 	'ABSO' 
			ELSE 										'CASO'
		END	
	END																			AS "IdConcepto",
	CASE 
		WHEN SOR."PerdidaGanancia"  > 0 THEN 	'LIDERE' 
		ELSE 									'LIDEPA'
	END 																		AS "IdTipoOperacion",
	CASE 
			WHEN SOC."IdCliente"  		> 0 	THEN 'CLI'
			WHEN SOC."IdEmpleado"		> 0 	THEN 'EMP'
			WHEN SOC."IdContraparte"	> 0 	THEN 'CNT'
			WHEN SOC."IdProveedor"   	> 0 	THEN 'PRO'
			ELSE ''
	END																			AS "IdTipoPersona"	
	
	


FROM "BSCL.Custodia.BD::CUS.SorteoContratos"								AS SOR
	INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 	AS CLI
		ON SOR."PortafolioDerivados" = CLI."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"   AS SOC 
	ON (SOC."IdCliente" = CLI."IdCliente")
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 		AS INST
		ON (INST."Nemotecnico" = SOR."Serie")
		
INNER JOIN ((SELECT    
				"IdPortafolio",
        		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion"
			FROM (	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
    				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio") 
			WHERE "IdPortafolio" IN ("IdPortafolio")
			GROUP BY "IdPortafolio")
		)	 AS CTA 
		ON (CTA."IdPortafolio" = CLI."IdPortafolio")

		
WHERE	SOR."Fecha" = :FechaSistema
  AND SOR."PerdidaGanancia" <> 0
  AND CTA."OperaCuentaInversion" = 'N'
)

SELECT 
	"NumeroReferencia",  
	"IdTipoMovimiento", 
	"IdPortafolio", 
	"IdPersona",
	SUM("Monto")			AS "Monto",
	"FechaLiquidacion",
	"FechaComprobante",
	"IdConcepto",
	"IdTipoOperacion",
	"IdTipoPersona"

FROM CTE
GROUP BY "NumeroReferencia",  
	"IdTipoMovimiento", 
	"IdPortafolio", 
	"IdPersona",
	"FechaLiquidacion",
	"FechaComprobante",
	"IdConcepto",
	"IdTipoOperacion",
	"IdTipoPersona"


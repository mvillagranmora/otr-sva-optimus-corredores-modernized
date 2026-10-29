-- Actualizacio de estado y otros para Deposito cuando entidad es 'TES.DepositoDetalle'
UPDATE DEP
SET DEP."IdEstado"			= CASE WHEN :TipoEjecucion = 'LIQ' AND "DocumentoSinLiquidar" > 0  THEN 'LIP'
   								   WHEN :TipoEjecucion = 'LIQ' AND "DocumentoSinLiquidar" = 0  THEN 'LIQ'				   								   
   								   WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" > 0 THEN DEP."IdEstado"
   								   WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN 'ANU'
	   						  END,
	   						  
	   DEP."Monto"			= CASE WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" > 0 THEN (DEP."Monto" - I."MontoAnulado")
	   							   WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN (I."MontoTotal")
	   							   ELSE DEP."Monto"
	   						  END,
	   							   
	   DEP."IdConcepto"				= CASE WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN :IdConcepto END,
   	   DEP."Descripcion"			= CASE WHEN :TipoEjecucion = 'ANU' AND I."NominaDetSinAnular" = 0 THEN :Descripcion END,
   	   DEP."IdUsuarioLiquidador" 	= CASE WHEN :TipoEjecucion = 'LIQ' THEN :IdUsuario ELSE DEP."IdUsuarioLiquidador" END,
	   DEP."FechaHoraLiquidacion" = CASE WHEN :TipoEjecucion = 'LIQ' 
	   								   THEN TO_DATE(:FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7') 
	   								   ELSE DEP."FechaHoraLiquidacion" 
	   							   END	
FROM "BSCL.Tesoreria.BD::TES.Deposito" AS DEP
INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD ON DD."IdDeposito" = DEP."IdDeposito"	
INNER JOIN (SELECT D."IdDeposito",
					SUM(CFP."Monto") AS "MontoTotal",
 					SUM(CASE WHEN DD."IdEstado" = 'ANU' THEN CFP."Monto" ELSE 0 END) AS "MontoAnulado",
					(COUNT(DD."IdDepositoDetalle") - SUM( CASE WHEN DD."IdEstado" = 'ANU' THEN 1 ELSE 0 END )) as "NominaDetSinAnular",
					(COUNT(DD."IdDepositoDetalle") - SUM( CASE WHEN DD."IdEstado" IN ('LIQ','ANU') THEN 1 ELSE 0 END )) as "DocumentoSinLiquidar"
			   FROM "BSCL.Tesoreria.BD::TES.Deposito" as D
			  INNER JOIN "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD ON DD."IdDeposito" = D."IdDeposito"
			  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
			  GROUP BY D."IdDeposito"
 			) as I
 		 ON I."IdDeposito" = DEP."IdDeposito"
 
WHERE DD."IdDepositoDetalle" = :Id
AND :Entidad = 'TES.DepositoDetalle';





--solicitudes monto ingreso egreso partidas
SELECT DISTINCT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
ConsultarComprobanteDetalle."IdComprobanteDetalle" 	AS "IdMovimiento",
		CASE ConsultarComprobanteDetalle."IdTipoMovimiento" WHEN 'MOVING' 
											THEN 
												CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD_MONTO_INGRESO' 
												ELSE 'SOLICITUD_MONTO_TRASPASO_INGRESO'
												END
											ELSE CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD_MONTO_EGRESO' 
												ELSE 'SOLICITUD_MONTO_TRASPASO_EGRESO'
												END
											END 			AS "IdEvento", 
		CASE ConsultarComprobanteDetalle."IdTipoMovimiento" WHEN 'MOVING' 
											THEN 
												CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD MONTO INGRESO' 
												ELSE 'SOLICITUD MONTO TRASPASO INGRESO'
												END
											ELSE CASE WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD MONTO EGRESO' 
												 ELSE 'SOLICITUD MONTO TRASPASO EGRESO'
												 END
											END			AS "Nombre", 
		ConsultarComprobanteDetalle."Monto"	 				AS "MontoEvento", 
		Comprobante."IdMoneda" 								AS "CodigoMoneda"
FROM "BSCL.Tesoreria.BD::TES.Comprobante"					AS	Comprobante
INNER JOIN (
  SELECT comp.* FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" comp WHERE comp."IdComprobante" IN 
  
  (SELECT x."IdComprobanteAsociado" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" x
WHERE x."IdComprobante" IN (  SELECT z."IdComprobante" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" z
  WHERE z."IdConcepto" IN ('PAREGR' ,'PARING')  and z."IdEstado" = 'LIQ') ))   AS	Comprobante2 
  ON  Comprobante2."IdComprobante" = Comprobante."IdComprobante" 
 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago 
      ON  (	ComprobanteFormaDePago."IdComprobante" 	= Comprobante."IdComprobante" 
     
      AND	((ComprobanteFormaDePago."IdEstado"  IN ('LIQ', 'GIR') AND :LocSistema = 'CL')  
      OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU')	AND :LocSistema = 'PE')))
  
  
  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"   ConsultarComprobanteDetalle
		ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante" 
		AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ','ING')
WHERE Comprobante."IdEmpresa" = :IdEmpresa AND
      ((Comprobante."IdEstado" IN ('LIQ', 'GIR') AND :LocSistema = 'CL') 
      OR (Comprobante."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')) AND
	  Comprobante."FechaLiquidacion" = :FechaContable
UNION ALL 


SELECT DISTINCT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
SUB."IdComprobanteFormaDePago"*-1	AS "IdMovimiento",
CASE SUB."IdTipoMovimiento" WHEN 'MOVING' THEN 
     CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_INGRESO' 
	  ELSE 'DOCUMENTO_MONTO_TRASPASO_INGRESO'
	  END
	  ELSE CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_EGRESO' 
	  ELSE 'DOCUMENTO_MONTO_TRASPASO_EGRESO'
	  END
	  END AS "IdEvento", 
CASE SUB."IdTipoMovimiento" WHEN 'MOVING' 
	 THEN 
     CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO INGRESO' 
     ELSE 'DOCUMENTO MONTO TRASPASO INGRESO'
	 END
	 ELSE CASE WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO EGRESO' 
	 ELSE 'DOCUMENTO MONTO TRASPASO EGRESO'
	 END END	AS "Nombre", 
SUB."Monto"	 AS "MontoEvento", 
SUB."IdMoneda" AS "CodigoMoneda"		
FROM(
     SELECT	DISTINCT 
     Comprobante."IdEstado",
     ComprobanteFormaDePago."FechaIngresoRegistro",
     Comprobante."IdEmpresa" ,
    ComprobanteFormaDePago."IdComprobanteFormaDePago" AS "IdComprobanteFormaDePago",
     Comprobante."IdTipoMovimiento",
     ComprobanteFormaDePago."Monto",
     Comprobante."IdMoneda" ,
     Comprobante."ComprobanteNeteado",
     CAST((	CASE WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' THEN ADD_DAYS (TO_DATE (Comprobante."FechaLiquidacion",'YYYY-MM-DD'),ComprobanteFormaDePago."DiasRetencion")	
     ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"	 END) AS DATE) AS "FechaLiquidacion"
   
     FROM "BSCL.Tesoreria.BD::TES.Comprobante" 	AS Comprobante
     INNER JOIN (
  SELECT comp.* FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" comp WHERE comp."IdComprobante" IN 
  
  (SELECT x."IdComprobanteAsociado" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" x
WHERE x."IdComprobante" IN (  SELECT z."IdComprobante" FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" z
  WHERE z."IdConcepto" IN ('PAREGR' ,'PARING')  and z."IdEstado" = 'LIQ') ))   AS	Comprobante2   ON  (Comprobante2."IdComprobante" = Comprobante."IdComprobante" )

  INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago 
      ON  (	ComprobanteFormaDePago."IdComprobante" 	= Comprobante2."IdComprobante"     
      AND	((ComprobanteFormaDePago."IdEstado" IN ('LIQ','GIR') AND :LocSistema = 'CL')  
      OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU')	AND :LocSistema = 'PE')))
      INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"   ConsultarComprobanteDetalle
		ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante" 
		AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ','ING')
      
		)SUB	
WHERE SUB."IdEmpresa" = :IdEmpresa 			
AND    ((SUB."IdEstado" IN ('LIQ','GIR') AND :LocSistema = 'CL') OR (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')) 
AND CAST(SUB."FechaLiquidacion" AS DATE) = :FechaContable 
-- obtiene los cobros de COMISION y DERECHO por Convenios y Contratos de Clientes
 WITH ConceptoFactura AS (
 	SELECT "IdConcepto"
 		  ,"Nombre" AS "NombreConcepto"
 	  FROM "BSCL.Plataforma.BD::PLA.Concepto"
 	 WHERE "IdConcepto" = 'COMMEN'
 ),
 OtrosConceptosFactura AS (
SELECT C."IdConcepto"
 		  ,C."Nombre" AS "NombreConcepto"
 		  ,"IdContratoComision"
 	  FROM "BSCL.Plataforma.BD::PLA.Concepto" C
 	  INNER JOIN "BSCL.Plataforma.BD::COM.DetalleContratoComision"  DET ON DET."IdConcepto" = C."IdConcepto" 
 )
-- CONVENIO
-- Para el caso de Convenio calcula los Montos de Derechos y Comision en la vista
SELECT "IdCliente"
      ,"NombreCliente"
	  ,"DocumentoIdentidad"
	  ,"IdPortafolio"
	  ,"CodigoPortafolio"
	  ,"IdMoneda"
	  ,"ModPrefFact"
	  ,"ModoFacturacion"
	  ,"IdClasificacionTipoInstrumento"
	  ,"IdContratoComision"
	  ,"IdComprobante"
	  ,"NombreConvenio" 					AS "NombreConvenioContrato"
	  ,'CONV' AS "IdFormatoCobro"
	  ,''									AS "IdConceptoComprobante"
	  ,''									AS "NombreConceptoComprobante"
	  ,CONC."IdConcepto" 					AS "IdConceptoFacturacion"
	  ,CONC."NombreConcepto"				AS "NombreConceptoFacturacion"
	  ,"MontoLiquidacion"					AS "TotalMontoLiquidacion"
	  ,"MontoDerecho"
	  ,"MontoComision"
	  ,0									AS "IdInstrumento"
	  /*,COALESCE(SUM("MontoLiquidacion"),0)	AS "TotalMontoLiquidacion"
	  ,COALESCE(SUM("MontoDerecho"),0) + 
	   COALESCE(SUM("MontoComision"),0)		AS "Monto"
	  ,COALESCE(SUM("MontoDerecho"),0)		AS "MontoDerecho"
	  ,COALESCE(SUM("MontoComision"),0)		AS "MontoComision"*/
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" FACT
 LEFT JOIN ConceptoFactura AS CONC ON 1=1
WHERE "IdComprobante" 	IN (:IdComprobanteContrato)
  AND "IdEmpresa" 		= :IdEmpresa
/*GROUP BY "IdCliente"
       	 ,"NombreCliente"
	  	 ,"DocumentoIdentidad"
	  	 ,"IdPortafolio"
	  	 ,"CodigoPortafolio"
	  	 ,"IdMoneda"
	  	 ,"ModPrefFact"
	  	 ,"ModoFacturacion"
		 ,"IdClasificacionTipoInstrumento"
	     ,"IdContratoComision"
	     ,"NombreConvenio"
		 ,CONC."IdConcepto"
		 ,CONC."NombreConcepto"
		 ,"MontoLiquidacion"*/
 UNION ALL
-- CONTRATO
--COMPROBANTES CON CONTRATOS AGRUPADO POR PORTAFOLIO
-- Para el caso de Contratos se calcula los Montos de Derechos y Comision en el servicio
-- CrearVistaPreliminarFacturacionCobroMensual.java dado a la complejidad del calculo
-- El calculo se basa en el total del MontoLiquidacion de los comprobantes
SELECT FACT."IdCliente"
	  ,FACT."NombreCliente"
	  ,FACT."DocumentoIdentidad"
	  ,FACT."IdPortafolio"
	  ,FACT."CodigoPortafolio"
	  ,FACT."IdMoneda"
	  ,FACT."ModPrefFact"
	  ,FACT."ModoFacturacion"
	  ,FACT."IdClasificacionTipoInstrumento"
	  ,FACT."IdContratoComision"
	  ,FACT."IdComprobante"
	  ,FACT."NombreConvenioContrato"	
	  ,FACT."IdFormatoCobro"
	  ,FACT."IdConcepto"			AS "IdConceptoComprobante"
	  ,FACT."NombreConcepto"		AS "NombreConceptoComprobante"
	  ,CASE WHEN OC."IdConcepto" IS NOT NULL THEN OC."IdConcepto" ELSE CONC."IdConcepto" END	AS "IdConceptoFacturacion"
	  ,CASE WHEN OC."IdConcepto" IS NOT NULL THEN OC."NombreConcepto" ELSE CONC."NombreConcepto" END	AS "NombreConceptoFacturacion"
	  ,FACT."MontoLiquidacion"		AS "TotalMontoLiquidacion"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoDerecho"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoComision"
	  ,COMP."IdInstrumento"			AS "IdInstrumento"
	  /*,SUM(FACT."MontoLiquidacion") AS "TotalMontoLiquidacion"
	  ,CAST(0 AS DECIMAL(26,8))		AS "Monto"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoDerecho"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoComision"*/	  
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorPortafolio" AS FACT
 INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP ON COMP."IdComprobante" = FACT."IdComprobante"
 LEFT JOIN ConceptoFactura AS CONC 	ON 1=1
 LEFT JOIN OtrosConceptosFactura AS OC ON OC."IdContratoComision" = FACT."IdContratoComision" 
WHERE FACT."IdComprobante" 				IN (:IdComprobanteContrato)
  AND FACT."IdEmpresa" 					= :IdEmpresa
/*GROUP BY FACT."IdCliente"
	    ,FACT."NombreCliente"
	    ,FACT."DocumentoIdentidad"
    	,FACT."IdPortafolio"
	    ,FACT."CodigoPortafolio"
	    ,FACT."IdMoneda"
	    ,FACT."ModPrefFact"
	    ,FACT."ModoFacturacion"
	    ,FACT."IdClasificacionTipoInstrumento"
	    ,FACT."IdContratoComision"
	    ,FACT."NombreConvenioContrato"	  
	    ,FACT."IdConcepto"
	    ,FACT."NombreConcepto"
	    ,CONC."IdConcepto"
	    ,CONC."NombreConcepto"
	    */	    
 UNION ALL	    
-- CONTRATO
--COMPROBANTES CON CONTRATOS AGRUPADO POR RUT (Se agrupa por IdPortafolio del contrato)
-- Para el caso de Contratos se calcula los Montos de Derechos y Comision en el servicio
-- CrearVistaPreliminarFacturacionCobroMensual.java dado a la complejidad del calculo
-- El calculo se basa en el total del MontoLiquidacion de los comprobantes
SELECT FACT."IdCliente"
	  ,FACT."NombreCliente"
	  ,FACT."DocumentoIdentidad"
	  ,CONT."IdPortafolio"
	  ,POR."CodigoPortafolio"
	  ,FACT."IdMoneda"
	  ,FACT."ModPrefFact"
	  ,FACT."ModoFacturacion"
	  ,FACT."IdClasificacionTipoInstrumento"
	  ,FACT."IdContratoComision"
	  ,FACT."IdComprobante"
	  ,FACT."NombreConvenioContrato"
	  ,FACT."IdFormatoCobro"
	  ,FACT."IdConcepto"			AS "IdConceptoComprobante"
	  ,FACT."NombreConcepto"		AS "NombreConceptoComprobante"
	  ,CASE WHEN OC."IdConcepto" IS NOT NULL THEN OC."IdConcepto" ELSE CONC."IdConcepto" END	AS "IdConceptoFacturacion"
	  ,CASE WHEN OC."IdConcepto" IS NOT NULL THEN OC."NombreConcepto" ELSE CONC."NombreConcepto" END	AS "NombreConceptoFacturacion"
	  ,FACT."MontoLiquidacion"		AS "TotalMontoLiquidacion"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoDerecho"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoComision"
	  ,COMP."IdInstrumento"			AS "IdInstrumento"
	  /*,SUM(FACT."MontoLiquidacion") AS "TotalMontoLiquidacion"
	  ,CAST(0 AS DECIMAL(26,8))		AS "Monto"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoDerecho"
	  ,CAST(0 AS DECIMAL(26,8))		AS "MontoComision"	 */ 
 FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroContratoPorRut" AS FACT
 INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP ON COMP."IdComprobante" = FACT."IdComprobante"
 LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" CONT ON CONT."IdContratoComision" = FACT."IdContratoComision" 
 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CONT."IdPortafolio"
 LEFT JOIN ConceptoFactura AS CONC 	ON 1=1
 LEFT JOIN OtrosConceptosFactura AS OC ON OC."IdContratoComision" = FACT."IdContratoComision" 
WHERE FACT."IdComprobante" 				IN (:IdComprobanteContrato)
  AND FACT."IdEmpresa" 					= :IdEmpresa
/*GROUP BY FACT."IdCliente"
	    ,FACT."NombreCliente"
	    ,FACT."DocumentoIdentidad"
    	,FACT."IdPortafolio"
	    ,FACT."CodigoPortafolio"
	    ,FACT."IdMoneda"
	    ,FACT."ModPrefFact"
	    ,FACT."ModoFacturacion"
	    ,FACT."IdClasificacionTipoInstrumento"
	    ,FACT."IdContratoComision"
	    ,FACT."NombreConvenioContrato"	  
	    ,FACT."IdConcepto"
	    ,FACT."NombreConcepto" 
	    ,CONC."IdConcepto"
	    ,CONC."NombreConcepto"	*/	    
ORDER BY "NombreCliente"
        ,"ModoFacturacion"      
        ,"CodigoPortafolio"
/*
123569	PROJO	333-6	34991	2 TESTING	CLP	MODPREFCONT	CONTRATO	RV	93	CONTRATO ACCIONES NACIONALES	CMACNA	COMPRA ACCIONES NACIONALES	COMMEN	COMISION MENSUAL	3999965.00000000	0.00000000	0.00000000	0.00000000
123569	PROJO	333-6	27499	1	CLP	MODPREFCONT	CONTRATO	RV	93	CONTRATO ACCIONES NACIONALES	VNACNA	VENTA ACCIONES NACIONALES	COMMEN	COMISION MENSUAL	484140.00000000	0.00000000	0.00000000	0.00000000
123569	PROJO	333-6	27499	1	CLP	MODPREFCONT	CONTRATO	RV	93	CONTRATO ACCIONES NACIONALES	CMACNA	COMPRA ACCIONES NACIONALES	COMMEN	COMISION MENSUAL	4936291.00000000	0.00000000	0.00000000	0.00000000        
*/
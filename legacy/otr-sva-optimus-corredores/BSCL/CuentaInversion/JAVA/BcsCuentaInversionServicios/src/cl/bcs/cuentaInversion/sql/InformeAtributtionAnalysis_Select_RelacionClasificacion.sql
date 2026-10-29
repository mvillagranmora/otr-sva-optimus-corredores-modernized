WITH v_div AS (
		       
			 ---------------DIVIDENDOS--------------           
	         SELECT  
	         	CARAB."IdInstrumento",
			    INST."Nemotecnico",
			    sum(CARAB."Monto") AS "MontoDiv",
			    CARAB."IdTipoOperacion" AS "TipoOperacion"
	         FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARTS
	          LEFT  JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINVER ON (CTAINVER."IdPortafolio" = CARTS."IdPortafolio" AND CTAINVER."IdMoneda" = CARTS."IdMonedaInstrumento")
	          LEFT  JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CARAB ON ((CARAB."IdCuentaInversion" = CTAINVER."IdCuentaInversion" /*AND CTAINVER."IdMoneda" = CARAB."IdMonedaOperacion"*/) 
	          AND (CARAB."FechaLiquidacionOperacion" BETWEEN :p_FechaDesde AND :p_FechaHasta ))
   	          LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"          AS INST ON  (CARAB."IdInstrumento"=INST."IdInstrumento")
	         WHERE  CARAB."IdTipoOperacion" = 'DIVDIN'  AND CARTS."IdCliente" IN (:p_IdCliente)          
	         GROUP BY 
	         	CARAB."IdInstrumento",
	            INST."Nemotecnico",
         	    CARAB."IdTipoOperacion"        
),
v_cup AS (
		       
			 ---------------CUPONES--------------           
	         SELECT  
	         	CARAB."IdInstrumento",
			    INST."Nemotecnico",
			    sum(CARAB."Monto") AS "MontoCup",
			    CARAB."IdTipoOperacion" AS "TipoOperacion"
	         FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARTS
	          LEFT  JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINVER ON (CTAINVER."IdPortafolio" = CARTS."IdPortafolio" AND CTAINVER."IdMoneda" = CARTS."IdMonedaInstrumento")
	          LEFT  JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CARAB ON ((CARAB."IdCuentaInversion" = CTAINVER."IdCuentaInversion" /*AND CTAINVER."IdMoneda" = CARAB."IdMonedaOperacion"*/) 
	          AND (CARAB."FechaLiquidacionOperacion" BETWEEN :p_FechaDesde AND :p_FechaHasta ))
   	          LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"          AS INST ON  (CARAB."IdInstrumento"=INST."IdInstrumento")
	         WHERE  CARAB."IdTipoOperacion" = 'CORCUP'  AND CARTS."IdCliente" IN (:p_IdCliente)       
	         GROUP BY 
	         	CARAB."IdInstrumento",
	            INST."Nemotecnico",
         	    CARAB."IdTipoOperacion"
),

v_MontoCierre AS (
			 ---------------BUSCA MONTO CIERRE ANTERIOR--------------

		SELECT t."IdInstrumento", t."Nemotecnico", t."FechaIngresoRegistro", t."INGMontoMercadoActualMonedaInstrumento" AS "MontoCierreAnterior"
		FROM (
		      SELECT "Nemotecnico", MAX("FechaIngresoRegistro")  AS MaxTime 
		      FROM "BSCL.Custodia.BD::CUS.Cartera"
		      WHERE "IdCliente" IN (:p_IdCliente) 
		        AND "FechaOperacion" = :p_FechaAnterior  
		      GROUP BY "Nemotecnico"
		) r
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" t
		ON t."Nemotecnico" = r."Nemotecnico" AND t."FechaIngresoRegistro" = r.MaxTime
),
v_MontoInicio AS (
			 ---------------BUSCA MONTO INICIO PERIODO --------------

		SELECT t."IdInstrumento", t."Nemotecnico", t."FechaIngresoRegistro", t."INGMontoMercadoActualMonedaInstrumento" AS "MontoInicioPeriodo"
		FROM (
		      SELECT "Nemotecnico", MAX("FechaIngresoRegistro")  AS MaxTime 
		      FROM "BSCL.Custodia.BD::CUS.Cartera"
		      WHERE "IdCliente" IN (:p_IdCliente) 
		        --AND "FechaOperacion" = '2019-05-29' 
		      GROUP BY "Nemotecnico"
		) r
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" t
		ON t."Nemotecnico" = r."Nemotecnico" AND t."FechaIngresoRegistro" = r.MaxTime
)

SELECT 
      (CASE WHEN CART."IdClasificacionTipoInstrumento" = 'IF' THEN CART."NemoSVS" ELSE  CART."Nemotecnico" END) AS "Nemotecnico",	 
       CART."IdPortafolio",
	   CART."FechaOperacion",
	   CART."INGFechaOperacion",	   
	   (CASE WHEN CART."IdTipoReajuste" <> '' THEN CART."IdTipoReajuste" ELSE  CART."IdMonedaInstrumento" END) AS "MonedaReajuste",
	   TipOpe."Nombre",
	   TipOpe."Accion" AS "INGAccion",
	   COALESCE(TipOpe2."Accion",'') AS "EGRAccion",
	   CART."INGIdTipoOperacion",
	   CART."EGRIdTipoOperacion",
	   CART."IdCliente",
	   CART."FechaVencimientoOperacion",
	   CART."INGMontoATasaMercadoActual" AS "MontoCompra", --COMPRA
	   TipOpe."AccionOrden",
	   COALESCE(DIV."MontoDiv",NULL,0) AS "MontoDiv",
	   COALESCE(CUP."MontoCup",NULL,0) AS "MontoCup",
	   COALESCE(MC."MontoCierreAnterior",NULL,0) AS "MontoCierreAnterior",
	    COALESCE(MINI."MontoInicioPeriodo",NULL,0)AS "MontoInicioPeriodo",
	   CART."IdClasificacionTipoInstrumento",
	   CART."IdInstrumento",
	   CART."IdTipoInstrumento",
	   CART."INGCantidad",
	   CART."INGPrecioCompra" AS "INGPrecio",
	   CART."INGMontoMercadoActualMonedaInstrumento" AS "ValorInstrumento",
	   CART."EGRPrecio",
	   CART."EGRMonto" AS "ValorInstrumentoEGR",
	   CART."IdEstado",
	   TipOpe."EsSimultanea"    AS "EsSimultanea",
	   TipOpe."EsVentaCorta"    AS "EsVentaCorta",
	   TipOpe."EsAnticipo"      AS "EsAnticipo",
	   TipOpe."EsVencimiento"   AS "EsVencimiento",
	   TEMP."Mercado",
	   TEMP."SubMercado",
	   CART."EGRFechaLiquidacion"
	    
	    
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CART
INNER JOIN "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos" AS TEMP ON CART."IdInstrumento" = TEMP."IdInstrumento"
LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipOpe ON (CART."INGIdTipoOperacion"  = TipOpe."IdTipoOperacion")
LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipOpe2 ON (CART."EGRIdTipoOperacion"  = TipOpe2."IdTipoOperacion")
LEFT  JOIN v_div AS DIV ON  (DIV."IdInstrumento" = CART."IdInstrumento")
LEFT  JOIN v_cup AS CUP ON  (CUP."IdInstrumento" = CART."IdInstrumento")
LEFT  JOIN v_MontoCierre AS MC ON  (MC."IdInstrumento" = CART."IdInstrumento")
LEFT  JOIN v_MontoInicio AS MINI ON (MINI."IdInstrumento" = CART."IdInstrumento")
WHERE  	(CART."IdCliente" IN (:p_IdCliente)) 
	AND (CART."IdPortafolio" IN (:p_IdPortafolio))
  	AND ((TipOpe."AccionOrden" ='COMPRA' OR TipOpe."AccionOrden" ='VENTA' OR TipOpe."AccionOrden" IS NULL OR TipOpe."AccionOrden" =''))
    AND (CART."INGFechaOperacion" BETWEEN :p_FechaDesde AND :p_FechaHasta)
	GROUP BY
		 CART."NemoSVS",
		 CART."Nemotecnico",
	     CART."IdPortafolio",
		 CART."FechaOperacion",
  	     CART."INGFechaOperacion",   	     
   	     CART."IdTipoReajuste",
   	     CART."IdMonedaInstrumento",
	     TipOpe."Nombre",
		 TipOpe."Accion",
	   	 TipOpe2."Accion",
		 TipOpe."EsSimultanea",
		 TipOpe."EsVentaCorta",
		 TipOpe."EsAnticipo",
		 TipOpe."EsVencimiento",
		 CART."INGIdTipoOperacion",
		 CART."EGRIdTipoOperacion",
		 CART."IdCliente",
  	     CART."MontoCompraMonedaInstrumento",
		 TipOpe."AccionOrden",
		 CART."IdClasificacionTipoInstrumento",	 
		 TEMP."Mercado",
		 TEMP."SubMercado",
		 CART."INGMontoMercadoActualMonedaInstrumento",
		 DIV."MontoDiv",
		 CUP."MontoCup",
		 MC."MontoCierreAnterior",
		 MINI."MontoInicioPeriodo",
		 CART."IdEstado",
		 CART."IdInstrumento",
		 CART."IdTipoInstrumento",
		 CART."INGCantidad",
		 CART."INGPrecioCompra",
		 CART."INGMontoATasaMercadoActual",
		 CART."EGRPrecio",
		 CART."EGRMonto",
		 CART."FechaVencimientoOperacion",
		 CART."EGRFechaLiquidacion"
ORDER BY CART."Nemotecnico" asc,CART."IdPortafolio" asc 
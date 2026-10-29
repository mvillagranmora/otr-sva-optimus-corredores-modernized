SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::CuadraturaComprobantesCuentaInversion";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::CuadraturaComprobantesCuentaInversion" AS


SELECT   COM."IdComprobante"
		,COM."IdEmpresa"
		,COM."IdTipoMovimiento"
		,TMOV."Nombre" AS "NombreTipoMovimiento"
		,COM."IdConcepto"
		,CPT."Nombre" AS "NombreConcepto"
		,COM."IdMoneda"
		,COM."Monto"
		,COM."TitularPago"
		,COM."IdPersona"
		,COM."NombrePersona"
		,COM."IdPersonaTitularPago"
		,COM."NombrePersonaTitularPago"
		,COM."IdPortafolioTitularPago"
		,PTF."CodigoPortafolio" ||' '|| PTF."Proposito" as "NombrePortafolioTitularPago"
		,COM."FechaLiquidacion"
		,COM."IdCaja"
		,CJ."Nombre" AS "NombreCaja"
		,COM."IdEstado"	
		,EST."Nombre" AS "NombreEstado"
		
		,CTA."IdTipoMovimiento" AS "IdTipoMovimientoCTA"
		,CTA."NombreTipoMovimiento" AS "NombreTipoMovimientoCTA" 
		,CTA."IdCargoAbonoMovimiento"
		,CTA."IdEstado" AS "IdEstadoCTA"
		,CTA."NombreEstado" AS "NombreEstadoCTA"
		,CTA."Cargo"
		,CTA."Abono"
		
		,CASE WHEN (COM."Monto" - CTA."Monto") = 0 THEN 'OK' ELSE 'NOK' END as "ExisteDiferenciaEnMonto"		
  FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM  
 INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS TMOV 
 		 ON (TMOV."IdModulo" = 'TES' and  TMOV."IdTipoMovimiento" = COM."IdTipoMovimiento")
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
  		 ON COM."IdEstado" = EST."IdEstado"
  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PTF 
  		 ON PTF."IdPortafolio" = COM."IdPortafolioTitularPago"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CPT 
  		 ON (CPT."IdConcepto" = COM."IdConcepto")
  LEFT JOIN "BSCL.Tesoreria.BD::TES.Caja" AS CJ 
  		 ON CJ."IdCaja" = COM."IdCaja"  
  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaTesoreria" AS CTA 
  		 ON (CTA."IdTipoComprobante" = COM."IdTipoMovimiento" and CTA."IdComprobante" = COM."IdComprobante")
 WHERE COM."IdEstado" IN ('LIQ','LIP')
   AND (CASE WHEN COM."TitularPago" = 'S' THEN COM."IdTipoPersona" ELSE COM."IdTipoPersonaTitularPago" END) = 'CLI'
   
WITH READ ONLY;

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::CuadraturaComprobantesCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

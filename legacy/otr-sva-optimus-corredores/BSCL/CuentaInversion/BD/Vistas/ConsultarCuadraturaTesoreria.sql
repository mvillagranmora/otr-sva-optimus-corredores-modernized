SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaTesoreria";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaTesoreria" AS


SELECT	
		 CAMOV."IdCargoAbonoMovimiento" ,
		 CAMOV."FechaSistema" ,
		 CAMOV."FechaIngresoRegistro" ,
	
		 CAMOV."IdEjecutivo",
		 CAMOV."IdSucursal",
		 CAMOV."IdTipoMovimiento" ,
		 TMOV."Tipo" AS "TipoMovimiento" ,
		 TMOV."Nombre" AS "NombreTipoMovimiento" ,
		 CAMOV."IdConcepto" ,
		 CAMOV."Descripcion" ,
		 CAMOV."IdCondicionDeLiquidacion" ,		
		 CAMOV."FechaEfectivaMovimiento" ,
		 CAMOV."Monto",
		 CASE WHEN CAMOV."IdTipoMovimiento" = 'MOVCAR' THEN CAMOV."Monto" ELSE 0 END "Cargo",
		 CASE WHEN CAMOV."IdTipoMovimiento" = 'MOVABN' THEN CAMOV."Monto" ELSE 0 END "Abono",

		 CASE WHEN CAMOV."IdTipoComprobante" = 'ING' THEN 'MOVING' ELSE 'MOVEGR' END AS "IdTipoComprobante",
		 TCTES."Nombre" AS "NombreTipoComprobante",
		 CAMOV."IdComprobante",
		 CAMOV."FolioComprobante",
		 CAMOV."IdComprobanteDetalle" ,
		 CAMOV."IdComprobanteFormaDePago" ,		 
		 CAMOV."IdTipoMovimientoAsociado" ,
		 CAMOV."IdMovimientoAsociado" ,		 
		 COALESCE(CAMOV."FechaComprobante",'0001-01-01') AS "FechaComprobante",		 
		 CAMOV."IdPersonaPagoCobro" ,
		 CAMOV."IdEstado",
		 ECTA."Nombre" as "NombreEstado"
   FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAMOV
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TMOV ON  TMOV."IdTipoMovimiento" = CAMOV."IdTipoMovimiento"      
   LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ECTA ON (ECTA."IdEstado" = CAMOV."IdEstado")
   LEFT OUTER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarTipoComprobante" AS TCTES ON TCTES."IdTipoComprobante" = CAMOV."IdTipoComprobante"
  WHERE CAMOV."IdTipoComprobante" IN ('ING','EGR')
WITH READ ONLY;
  
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCuadraturaTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
  

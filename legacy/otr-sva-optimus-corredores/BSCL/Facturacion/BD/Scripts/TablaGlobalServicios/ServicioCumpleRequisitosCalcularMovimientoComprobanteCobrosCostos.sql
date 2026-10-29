/** No considerar como ejemplo debido a que se tuvo que realizar cambios en el IdCobro porque es nvarchar y normalmente 
    el Id es BIGINT **/
    
    
SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_IN_Cobro" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_IN_Cobro"
    ( "IdCobro" NVARCHAR(40), 
      "MontoCobro" DECIMAL(26,8), 
      "MontoCobroNuevo" DECIMAL(26,8), 
      "PorcentajeCobroNuevo" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS; 
    
   
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados"
    ( "IdCobro" NVARCHAR(40)) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
    ( "Id" BIGINT, 
      "Codigo" nvarchar(100),
      "Descripcion" nvarchar(520)   
    ) ON COMMIT DELETE ROWS;  
	
commit;

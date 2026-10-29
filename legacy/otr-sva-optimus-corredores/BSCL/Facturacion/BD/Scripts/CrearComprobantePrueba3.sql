SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba3";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba3"
    (                                                    
      OUT v_MovimientosBloqueados table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)),   
      OUT v_MovimientosFacturados table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)), 
      OUT v_MovimientoAAgruparPorSecuencias table ("IdMovimientoComprobante" BIGINT, "SecuenciaAgrupacion" BIGINT),
      OUT v_MovimientosABloquearConProblemas table ("IdMovimientoComprobante" BIGINT, "IdEstadoFacturacion" NVARCHAR(3)),
      
      OUT v_IdMovimientoComprobante    table ("IdMovimientoComprobante" BIGINT),
      OUT v_ComprobanteGenerado        table ("IdComprobante" BIGINT),
      
      OUT p_out_CargoAbonoGenerado     table ("IdCargoAbonoMovimiento" BIGINT),  
      
      --Salida Sp Ingreso Manual de Titulo RV
      OUT p_out_FolioCartera      table ("FolioCartera" BIGINT),
      OUT p_out_FolioCarteraSub   table ("FolioCarteraSub" BIGINT), 
      OUT p_out_FolioCarteraRet   table ("FolioCarteraRet" BIGINT), 
      OUT p_out_IdSubyacente      table ("FolioSubyacente" BIGINT),
      OUT p_out_IdSubyacenteRet   table ("FolioSubyacenteRet" BIGINT),
      OUT p_out_IdGarantia        table ("FolioGarantia" BIGINT),
      OUT p_out_IdGarantiaRetiro  table ("FolioGarantiaRet" BIGINT),
      
      --Tesoreria
      OUT p_out_IngEgrGenerado        table ("IdMovIngEgr" BIGINT),
        
      OUT p_out_AdvertenciasContador int,
      OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
      OUT p_out_ErroresContador int,   
      OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
	  OUT p_out_MensajeRegla         NVARCHAR(520)
    )     
     
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA  
AS
BEGIN
     Declare xx table ("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdMovimientoComprobante" BIGINT,
                       "IdTipoComprobante" NVARCHAR(3));  
                       
                       
         
   

  -- Guz testing!!!!              
    xx =    Select 10066 as "IdPortafolio", 10059 as "IdCliente", 740 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 750 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 538 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 819 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 820 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 821 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 822 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy UNION ALL
			Select 10066 as "IdPortafolio", 10059 as "IdCliente", 909 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy;
    
    	
    --Select 10066 as "IdPortafolio", 10059 as "IdCliente", 741 as "IdMovimientoComprobante", 'FAE' as  "IdTipoComprobante" from dummy; 
        -- (10007, 10023, 292, 'FAC')
        
         /*Select 10066 as "IdPortafolio", 10059 as "IdCliente", 750 as "IdMovimientoComprobante", 'FAE' as  "IdTipoComprobante" from dummy 
         Select 10066 as "IdPortafolio", 10059 as "IdCliente", 744 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy 
		 Select 10007 as "IdPortafolio", 10023 as "IdCliente", 292 as "IdMovimientoComprobante", 'FAC' as "IdTipoComprobante" from dummy; 
         Select 10066 as "IdPortafolio", 10059 as "IdCliente", 538 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy 
         Select 10066 as "IdPortafolio", 10059 as "IdCliente", 819 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy 
         Select 10066 as "IdPortafolio", 10059 as "IdCliente", 820 as "IdMovimientoComprobante", 'NCR' as "IdTipoComprobante" from dummy union all


		 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 198 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 223 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 224 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 231 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 235 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 239 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL
    	 Select 10003 as "IdPortafolio", 10023 as "IdCliente", 232 as "IdMovimientoComprobante", 'FAE' as "IdTipoComprobante" from dummy UNION ALL*/
    /*insert into #xx2 values (10066, 10059, 741, 'FAE');
	insert into #xx2 values (10066, 10059, 750, 'FAE');
	insert into #xx2 values (10066, 10059, 744, 'FAE');
	insert into #xx2 values (10066, 10059, 538, 'FAE');
	insert into #xx2 values (10066, 10059, 819, 'NCR');
	insert into #xx2 values (10066, 10059, 820, 'NCR');
	insert into #xx2 values (10066, 10059, 821, 'NCR');
	insert into #xx2 values (10066, 10059, 822, 'NCR');
	insert into #xx2 values (10066, 10059, 909, 'NCR');
insert into #xx2 values (10003, 10023, 198, 'FAE');
insert into #xx2 values (10003, 10023, 223, 'FAE');
insert into #xx2 values (10003, 10023, 224, 'FAE');
insert into #xx2 values (10003, 10023, 231, 'FAE');
insert into #xx2 values (10003, 10023, 232, 'FAE');
insert into #xx2 values (10003, 10023, 235, 'FAE');
insert into #xx2 values (10003, 10023, 239, 'FAE');
insert into #xx2 values (10007, 10023, 288, 'FAC');
insert into #xx2 values (10007, 10023, 285, 'FAC');
insert into #xx2 values (10007, 10023, 292, 'FAC');




*/
    

 select * from :xx;          
/* Call "BSCL"."BSCL.Facturacion.BD.Servicios::CrearComprobante"(1,1,'FAC',  
      'N','N', 
      'xGeneralPrendido', :xx,     
      v_MovimientosBloqueados, v_MovimientosFacturados, v_MovimientoAAgruparPorSecuencias, v_MovimientosABloquearConProblemas,
      v_IdMovimientoComprobante, v_ComprobanteGenerado, 
      p_out_CargoAbonoGenerado,       
      p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
      p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro, 
      p_out_IngEgrGenerado,
      p_out_AdvertenciasContador, p_out_AdvertenciasDetalle, p_out_ErroresContador, p_out_ErroresDetalle);*/
      Call "BSCL"."BSCL.Facturacion.BD.Servicios::CrearComprobantePrueba4"(1,1,'FAC',  
      'N','N', 
      'xGeneralPrendido', :xx,     
      v_MovimientosBloqueados, v_MovimientosFacturados, v_MovimientoAAgruparPorSecuencias, v_MovimientosABloquearConProblemas,
      v_IdMovimientoComprobante, v_ComprobanteGenerado, 
      p_out_CargoAbonoGenerado,       
      p_out_FolioCartera, p_out_FolioCarteraSub, p_out_FolioCarteraRet, 
      p_out_IdSubyacente, p_out_IdSubyacenteRet, p_out_IdGarantia, p_out_IdGarantiaRetiro, 
      p_out_IngEgrGenerado,
      p_out_AdvertenciasContador, p_out_AdvertenciasDetalle, p_out_ErroresContador, p_out_ErroresDetalle);
END;
 

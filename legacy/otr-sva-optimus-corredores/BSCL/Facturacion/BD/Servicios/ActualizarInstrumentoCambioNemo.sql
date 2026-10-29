SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ActualizarInstrumentoCambioNemo";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ActualizarInstrumentoCambioNemo"
  (
    OUT p_out_InformacionContador  	 INT,
    OUT p_out_ErroresContador        INT
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA
AS
BEGIN

    p_out_InformacionContador  := 0;
    
    BEGIN --Logica
	    --------------------
		--Declare Variables
		-------------------
	    DECLARE v_CantInstrumento, v_CantMvtoCmbte, v_CantMvtoCmbteDet, v_CantCmbteDet BIGINT := 0;
	    
	    
	    --------------------
		--Negocio Aqui
		--------------------	
	    v_IdInstrumentoActualizar = Select Distinct ConsMvtoCmbte."IdMovimientoComprobante", ConsMvtoCmteDet."IdComprobanteDetalle",
	                                       ConsMvtoCmbte."IdEmpresa", ConsMvtoCmbte."IdClasificacionTipoInstrumento", 
	                                       ConsMvtoCmbte."IdTipoInstrumento", ConsMvtoCmbte."IdInstrumento", Instr."IdInstrumentoNuevo" 
	                                From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" As ConsMvtoCmbte
	                                  Inner Join "BSCL.Facturacion.BD::GBL.ActualizarInstrumentoCambioNemo_IN_Instrumento" As Instr 
	                                     On Instr."IdEmpresa"  = ConsMvtoCmbte."IdEmpresa"
	                                    And Instr."IdClasificacionTipoInstrumento" = ConsMvtoCmbte."IdClasificacionTipoInstrumento"
	                                    And Instr."IdTipoInstrumento" = ConsMvtoCmbte."IdTipoInstrumento"
	                                    And Instr."IdInstrumento" = ConsMvtoCmbte."IdInstrumento"
	                                  Left Join "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle" As ConsMvtoCmteDet
	                                     On ConsMvtoCmteDet."IdMovimientoComprobante" = ConsMvtoCmbte."IdMovimientoComprobante";
	                                    
	    /*select 'v_IdInstrumentoActualizar', *
	    from :v_IdInstrumentoActualizar;*/
	    
	    Select Count("IdMovimientoComprobante")
	    Into v_CantInstrumento 
	    From :v_IdInstrumentoActualizar;
	    
	            
	    If v_CantInstrumento > 0 Then   
	    
	       /* Actualiza Movimiento Comprobante */       
	       Update MvtoCmbte
	       Set MvtoCmbte."IdInstrumentoNuevo" = InsAct."IdInstrumentoNuevo"
	       From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
	         Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdMovimientoComprobante" =  MvtoCmbte."IdMovimientoComprobante";
	            
	       Select Count(MvtoCmbte."IdMovimientoComprobante")
	       Into v_CantMvtoCmbte 
           From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
             Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdMovimientoComprobante" =  MvtoCmbte."IdMovimientoComprobante"
	       Where MvtoCmbte."IdInstrumentoNuevo" = 0; 
	       
	       
	       
	       /* Actualiza Movimiento Comprobante Detalle */       
	       Update MvtoCmbteDet
	       Set MvtoCmbteDet."IdInstrumentoNuevo" = InsAct."IdInstrumentoNuevo"
	       From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" As MvtoCmbteDet
	         Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdMovimientoComprobante" =  MvtoCmbteDet."IdMovimientoComprobante";
	            
	       Select Count(MvtoCmbteDet."IdMovimientoComprobante")
	       Into v_CantMvtoCmbteDet 
           From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" As MvtoCmbteDet
	         Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdMovimientoComprobante" =  MvtoCmbteDet."IdMovimientoComprobante"
	       Where MvtoCmbteDet."IdInstrumentoNuevo" = 0;     
	            
	       
	       /* Actualiza Comprobante Detalle */       
	       Update CmbteDet
	       Set CmbteDet."IdInstrumentoNuevo" = InsAct."IdInstrumentoNuevo"
	       From "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet
	         Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdComprobanteDetalle" =  CmbteDet."IdComprobanteDetalle";  
	            
	       Select Count(CmbteDet."IdComprobanteDetalle")
	       Into v_CantCmbteDet 
           From "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet
	         Inner Join :v_IdInstrumentoActualizar As InsAct
	            On InsAct."IdComprobanteDetalle" =  CmbteDet."IdComprobanteDetalle"
	       Where CmbteDet."IdInstrumentoNuevo" = 0;             
	          
	    End If; 
	    
	    
	    /* Acumula contador */	    
	    p_out_InformacionContador := v_CantInstrumento;
	    
	    p_out_ErroresContador = v_CantMvtoCmbte + v_CantMvtoCmbteDet + v_CantCmbteDet; 

	        
    END; --Logica    
END;

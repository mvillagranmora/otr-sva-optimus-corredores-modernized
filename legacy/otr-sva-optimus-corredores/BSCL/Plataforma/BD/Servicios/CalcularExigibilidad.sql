SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CalcularExigibilidad";
GO
CREATE PROCEDURE
-- 
--  Proposito: crea el servicio de exigibilidad
--  Autor: - EAR
--  Fecha Creacion: 20/07/2015
--  el servicio permite la insercion en la tabla de exigibilidad de garantia desde el mantenedor 
-- de exigibilidad de garantia de los porcentaje de exigibilidad para los tipos instrumentos

"BSCL.Plataforma.BD.Servicios::CalcularExigibilidad" (
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
    
    IN p_TableExigibilidadSimulacion    	TABLE("IdInterno" BIGINT,"IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"Cantidad" DECIMAL(26,8),"Precio" DECIMAL(26,8),"Monto" DECIMAL(26,8),"IdBolsa" NVARCHAR(3)),
    IN p_IdTipoInstrumento 					NVARCHAR(3),
    IN p_IdPortafolio 						BIGINT, 
    IN p_IdBolsa 							NVARCHAR(3),
    IN p_IdInstrumento                      BIGINT,
    IN p_IdMoneda                           NVARCHAR(3) DEFAULT '',
    
	OUT p_out_TableExigibilidadSimulacion   TABLE("IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"Cantidad" DECIMAL(26,8),"Precio" DECIMAL(26,8),"Monto" DECIMAL(26,8),"IdBolsa" NVARCHAR(3),"MontoExigibilidadEntidad" Decimal(26,8),"MontoExigibilidadCorredora" Decimal(26,8),"PorcentajeExigibilidadEntidad" Decimal(26,8),"PorcentajeExigibilidadCorredora" Decimal(26,8)) DEFAULT EMPTY,
	OUT p_out_TableExigibilidadPortafolio   TABLE("MontoExigibilidadEntidad" DECIMAL(26,8),"MontoExigibilidadCorredora" DECIMAL(26,8)) DEFAULT EMPTY, 
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520)) DEFAULT EMPTY
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE v_IdMonedaLocal nvarchar(520):='';
	DECLARE v_FechaSistema nvarchar(520):='';
	DECLARE v_Quantity BIGINT :=0;
	DECLARE v_IdTrabajoParidad BIGINT;
	-------------------------
    -- VARIABLES DEL SISTEMA 
    -------------------------

    DECLARE v_resultadoRegla NVARCHAR(3); 
    	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		--validacion invalidante configuraci?n b?sica del sistema
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle,:v_resultadoRegla);
	    IF :v_resultadoRegla = 'RECH' THEN 
	    	p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_out_ErroresDetalle;
	        SIGNAL SALIDA_CONTROLADA;
	    END IF; 	
        
        -- recupero la moneda del sistema
        -- en caso que no la encuentre en la tabla configuracion arroja un error

        select "Valor" INTO v_IdMonedaLocal from "BSCL.Plataforma.BD::PLA.Configuracion"
       	where "IdEmpresa" = p_IdEmpresa and 
        "CodigoParametro" =  'PLA.MonedaSistema';
        
        --moneda paramentro 
        IF (p_IdMoneda != NULL OR p_IdMoneda != '') THEN
            v_IdMonedaLocal :=  p_IdMoneda;
        END IF;
        
        select "Valor" INTO  v_FechaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
       	where "IdEmpresa" = p_IdEmpresa and 
        "CodigoParametro" =  'PLA.FechaSistema';
         
		SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
		
		INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
		SELECT :v_IdTrabajoParidad,"IdInterno" AS "IdInterno",:v_FechaSistema as "Fecha",INS."IdMoneda" as "IdMonedaOrigen",:v_IdMonedaLocal as "IdMonedaDestino",0 as "ValorCambioLocalModificado",
											SIMU."Monto" as "MontoOrigen",'PLATAFORMA' AS "IdUso"
				from :p_TableExigibilidadSimulacion AS SIMU
                inner join "BSCL.Plataforma.BD::INS.Instrumento" as INS
                ON INS."IdInstrumento" = SIMU."IdInstrumento";
		
		call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
			p_IdEmpresa => :p_IdEmpresa,
			p_IdTrabajo => :v_IdTrabajoParidad
		);
         
		v_tablasalida = select v_FechaSistema as "Fecha"
                               ,INS."IdMoneda" as "MonedaOrigen"
                               ,SIMU."Monto" as "MontoOrigen"
                               ,v_IdMonedaLocal as "MonedaDestino"
                               ,0 as "TipoCambio"
                               ,'USD' as "MonedaCambio"
                               ,'CENTRAL' as "OrigenTipoCambio"
                               ,SIMU."IdInstrumento" as "IdInstrumento"
                               ,SIMU."IdTipoInstrumento" as "IdTipoInstrumento"
                               ,INS."IdTipoInstrumento" as "IdTipoInstrumentoSubyacente"
                               ,SIMU."Cantidad" as "Cantidad"
                               ,SIMU."Precio" as "Precio"
                               ,SIMU."Monto" as "Monto"
                               ,SIMU."IdBolsa" as "IdBolsa"
                               ,OUTT."MontoLocal" as "MontoLocal"
                          from :p_TableExigibilidadSimulacion AS SIMU
                          inner join "BSCL.Plataforma.BD::INS.Instrumento" as INS on INS."IdInstrumento" = SIMU."IdInstrumento"
                          inner join "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" OUTT ON OUTT."IdTrabajo" = :v_IdTrabajoParidad AND OUTT."IdInterno" = SIMU."IdInterno";  
                                   
	        p_out_TableExigibilidadSimulacion = select 
											    COALESCE(tablesimulacion."IdTipoInstrumento",'') as "IdTipoInstrumento"
											   ,COALESCE(tablesimulacion."IdInstrumento",0) as "IdInstrumento" 
											   ,COALESCE(tablesimulacion."Cantidad",0) as "Cantidad"
											   ,COALESCE(tablesimulacion."Precio",0) as "Precio"
											   ,COALESCE(tablesimulacion."Monto",0) as "Monto"
											   ,COALESCE(tablesimulacion."IdBolsa",'') as "IdBolsa"
											   --No uso monto local por que ya viene transformado, esto tambien aplica a Chile
											   --,COALESCE(tablesimulacion."MontoLocal",0) * ((COALESCE(ExigibilidadGarantia."ExigibilidadEntidad",100,ExigibilidadGarantia."ExigibilidadEntidad")) /100) as "MontoExigibilidadEntidad"
											   --,COALESCE(tablesimulacion."MontoLocal",0) * ((COALESCE(ExigibilidadGarantia."ExigibilidadCorredora",100,ExigibilidadGarantia."ExigibilidadCorredora"))/100) as "MontoExigibilidadCorredora"
											   ,COALESCE(tablesimulacion."Monto",0) * ((COALESCE(ExigibilidadGarantia."ExigibilidadEntidad",100,ExigibilidadGarantia."ExigibilidadEntidad")) /100) as "MontoExigibilidadEntidad"
											   ,COALESCE(tablesimulacion."Monto",0) * ((COALESCE(ExigibilidadGarantia."ExigibilidadCorredora",100,ExigibilidadGarantia."ExigibilidadCorredora"))/100) as "MontoExigibilidadCorredora"
											   ,COALESCE(ExigibilidadGarantia."ExigibilidadEntidad",0,ExigibilidadGarantia."ExigibilidadEntidad") as "PorcentajeExigibilidadEntidad" 
											   ,COALESCE(ExigibilidadGarantia."ExigibilidadCorredora",0,ExigibilidadGarantia."ExigibilidadCorredora") as "PorcentajeExigibilidadCorredora"  
											   from :v_tablasalida as tablesimulacion
											   inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS instrumento on instrumento."IdInstrumento" =  tablesimulacion."IdInstrumento"
	       									   left join  "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GrupoTipoInstrumento on GrupoTipoInstrumento."CodigoClasificacion" = 'FUTUROS' and GrupoTipoInstrumento."IdTipoInstrumento" = instrumento."IdTipoInstrumento"
	     									   left join "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" as ExigibilidadGarantia 
		     									   on ExigibilidadGarantia."IdBolsa" = tablesimulacion."IdBolsa"
		                                           and ExigibilidadGarantia."IdTipoInstrumento" = tablesimulacion."IdTipoInstrumento"
		                                           and ExigibilidadGarantia."IdTipoInstrumentoSubyacente" = tablesimulacion."IdTipoInstrumentoSubyacente"
	                                               and ExigibilidadGarantia."ClasificacionRiesgo" = CASE WHEN IFNULL(GrupoTipoInstrumento."CodigoClasificacion",'') = '' THEN instrumento."ValorClasificacionRiesgo" ELSE '' END
	                                               and ExigibilidadGarantia."IdEntidadClasificadoraRiesgo" = CASE WHEN IFNULL(GrupoTipoInstrumento."CodigoClasificacion",'') = '' THEN instrumento."IdEntidadClasificadoraRiesgo" ELSE '' END
		                                           and ExigibilidadGarantia."IdTipoClasificacionRiesgo" = IFNULL(instrumento."IdTipoClasificacionRiesgo",'')
		                                           and ExigibilidadGarantia."IdInstrumento" = p_IdInstrumento;
	     
	     
	    -- busco todas la ordenes de simulatanea vigentes
        -- todas aquellas cuya fecha vigencia sea mayor o igual a la fecha de
        -- sistema or su fecha vigencia sea '0001-01-01' lo que corresponde a una orden permanente
        -- partimos con simultaneas
        -- debo revisarlos productos 

       portafolio_ordenes = select  ConsultarOrden."IdOrden" 
							        ,ConsultarOrden."IdInstrumentoSubyacente" -- debido a que es una simultanea se debe elegir el instrumento asociado
							        ,ConsultarOrden."CantidadDisponibleOrden"
							        ,ConsultarOrden."PrecioMercado"
							        ,ConsultarOrden."MontoOrden" as "Monto"
							        from "BSCL.Orden.BD.Vistas::ConsultarOrden" as ConsultarOrden
							        where ConsultarOrden."IdPortafolio"         = p_IdPortafolio  
							        and   ConsultarOrden."IdBolsa"              = p_IdBolsa
							        and   ConsultarOrden."IdTipoInstrumento"    = p_IdTipoInstrumento
							        and   ConsultarOrden."IdInstrumento"        = p_IdInstrumento  
							        and   ConsultarOrden."IdEstadoOrden"  in ('ING','OFE')
							        and  (ConsultarOrden."FechaVigencia" >= v_FechaSistema or ConsultarOrden."FechaVigencia" = '0001-01-01');
       

      -- al monto de la ordenes vigentes y ofertadas le restamos las facturas que esten facturadas a la fecha de sistema
      -- nota en caso de nota de credito se deben sumar se debe revizar mas a futuro
	  v_Quantity := 0;
	  select Count("IdInstrumentoSubyacente") INTO v_Quantity FROM :portafolio_ordenes;
	  if v_Quantity > 0  then 
	   	  portafolio_ordenes2 =  select    ordenes_portafolio."IdOrden" 
										  ,ordenes_portafolio."IdInstrumentoSubyacente"  -- debido a que es una simultanea se debe elegir el instrumento asociado
										  ,ordenes_portafolio."CantidadDisponibleOrden"
										  ,ordenes_portafolio."PrecioMercado"
										  ,ordenes_portafolio."Monto"--- coalesce(facturacion."Monto",0) 
										  ,rig."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo"
										  ,rig."ValorClasificacionRiesgo" as "ClasificacionRiesgo"
										  ,instrumento."IdTipoInstrumento"  as "IdTipoInstrumentoInstrumentoAsociado"
										  ,instrumento."Nemotecnico"  as "NemoTecnicoInstrumentoAsociado"
										  ,instrumento."IdMoneda"  as "IdMonedaInstrumentoAsociado"
							  from :portafolio_ordenes as ordenes_portafolio
							  inner join "BSCL.Plataforma.BD::INS.Instrumento" as instrumento on instrumento."IdInstrumento" = ordenes_portafolio."IdInstrumentoSubyacente"
							  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" tipcl ON instrumento."IdTipoInstrumento" = tipcl."IdTipoInstrumento"
							  LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" rig ON instrumento."IdInstrumento" = rig."IdInstrumento" AND rig."IdEntidadClasificadoraRiesgo" = tipcl."IdEntidadClasificadoraRiesgo"
							  
							  left join (select "IdOrden" as "IdOrden",coalesce(sum("Monto"),0) as "Monto"
										 from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" as ConsultarMovimientoComprobanteRV 
											where ConsultarMovimientoComprobanteRV."IdBolsa" = p_IdBolsa
												and ConsultarMovimientoComprobanteRV."IdEstadoFacturacion" = 'FAC'
								             	and ConsultarMovimientoComprobanteRV."IdPortafolio" = p_IdPortafolio
												and ConsultarMovimientoComprobanteRV."FechaSistema" <= v_FechaSistema
												group by "IdOrden"
										)as facturacion
							 on facturacion."IdOrden" = ordenes_portafolio."IdOrden";
				   
	  else
	     portafolio_ordenes2 = select 0 as "IdOrden"
	                                 ,0 as "IdInstrumentoSubyacente"
	                                 ,0 as "CantidadDisponibleOrden"
	                                 ,0 as "PrecioMercado"
	                                 ,0 as "Monto"
	                                 ,'' as "IdEntidadClasificadoraRiesgo"
	                                 ,'' as "ClasificacionRiesgo"
	                                 ,'' as "IdTipoInstrumentoInstrumentoAsociado"   
	                                 ,'' as "NemoTecnicoInstrumentoAsociado"
	                                 ,'' as "IdMonedaInstrumentoAsociado"
	                                 from dummy;
	  end if;
      
      -- recuperamos la informacion de la cartera vigente

      custodia_vig = select sum(ConsultarCarteraSubyacente."INGMontoATasaMercadoActual") as "Monto"
                            ,ConsultarCarteraSubyacente."IdInstrumento" as "IdInstrumento"
							,rig."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo"
							,rig."ValorClasificacionRiesgo" as "ClasificacionRiesgo"
                            ,instrumento."IdTipoInstrumento" as "IdTipoInstrumento"
                            ,instrumento."Nemotecnico" as "Nemotecnico"
                            ,instrumento."IdMoneda"  as "IdMoneda"
                          from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" as ConsultarCarteraSaldos
--                          inner join "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteSaldos"  as ConsultarCarteraSubyacente 
                          inner join "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica"  as ConsultarCarteraSubyacente 
                          on (ConsultarCarteraSubyacente."FolioCarteraRelacionada" = ConsultarCarteraSaldos."FolioCartera"
                          	and ConsultarCarteraSubyacente."IdEstado" = 'VIG')
                          inner join "BSCL.Plataforma.BD::INS.Instrumento" as instrumento 
                          on instrumento."IdInstrumento" = ConsultarCarteraSubyacente."IdInstrumento"
                          LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" tipcl ON instrumento."IdTipoInstrumento" = tipcl."IdTipoInstrumento"
                          LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" rig ON instrumento."IdInstrumento" = rig."IdInstrumento" AND rig."IdEntidadClasificadoraRiesgo" = tipcl."IdEntidadClasificadoraRiesgo"
                              
                          where ConsultarCarteraSaldos."IdBolsa" = p_IdBolsa
                            and ConsultarCarteraSaldos."IdPortafolio" = p_IdPortafolio
                            and ConsultarCarteraSaldos."IdTipoInstrumento" = p_IdTipoInstrumento
                            and ConsultarCarteraSaldos."IdInstrumento"  = p_IdInstrumento 
                          group by ConsultarCarteraSubyacente."IdInstrumento"
                          ,rig."IdEntidadClasificadoraRiesgo"
                          ,rig."ValorClasificacionRiesgo"
                          ,instrumento."IdTipoInstrumento"
                          ,instrumento."Nemotecnico"
                          ,instrumento."IdMoneda"; 
                                      
      v_Quantity := 0;                                 
      select Count("IdInstrumento") INTO v_Quantity FROM :custodia_vig;
	  if v_Quantity = 0  then 
	       custodia_vig = select 0 as "Monto" 
	                           , 0 as "IdInstrumento"
	                           ,'' as "IdEntidadClasificadoraRiesgo"
	                           ,'' as "ClasificacionRiesgo"
	                           ,'' as "IdTipoInstrumento"	
			                   ,'' as "Nemotecnico"
			                   ,'' as "IdMoneda"                
			                   from dummy;
	   end if;  
	   
		
        -- uno todas la tablas para calcular su exigibilidad en caso de no tener la exigibilidad sera de un 100%
       tabla_exigibilidad = select  ordenes."IdInstrumentoSubyacente" as "IdInstrumento"
					                ,coalesce(sum(ordenes."Monto"),0) as "Monto"
					                ,ordenes."IdTipoInstrumentoInstrumentoAsociado" as "IdTipoInstrumento"
									,ordenes."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo"
									,ordenes."ClasificacionRiesgo" as "ClasificacionRiesgo"
					                ,ordenes."NemoTecnicoInstrumentoAsociado" as "Nemotecnico"
					                ,ordenes."IdMonedaInstrumentoAsociado" as "IdMoneda"
					                ,'S' as "MultiplicaPorParidad"
					         from :portafolio_ordenes2 as ordenes
					         group by ordenes."IdInstrumentoSubyacente"
					         ,ordenes."IdTipoInstrumentoInstrumentoAsociado"
					         ,ordenes."NemoTecnicoInstrumentoAsociado"
					         ,ordenes."IdMonedaInstrumentoAsociado"
					         ,ordenes."IdEntidadClasificadoraRiesgo"
					         ,ordenes."ClasificacionRiesgo"
					         union all
					         select "IdInstrumento"
					               ,"Monto"
					               ,"IdTipoInstrumento"
								   ,"IdEntidadClasificadoraRiesgo"
								   ,"ClasificacionRiesgo"
					               ,"Nemotecnico"
					               ,"IdMoneda"
					               ,'N' as "MultiplicaPorParidad"
					         from :custodia_vig;

		-- agrupamos la tabla de exigibilidad
        -- si la tabla exigibilidad tienen registros

         v_Quantity := 0;
         select Count(*) INTO v_Quantity FROM :tabla_exigibilidad;
	     if v_Quantity > 0  then   
		        tabla_exigibilidad_agrupada = select row_number() over (order by "IdInstrumento") AS "IdInterno", 
		        									"IdInstrumento"
							               			,sum("Monto") as "Monto"
							               			,"IdTipoInstrumento"
								   					,"IdEntidadClasificadoraRiesgo"
								   					,"ClasificacionRiesgo"
							               			,"Nemotecnico"
							               			,"IdMoneda"
							               			,"MultiplicaPorParidad"
							               	 from :tabla_exigibilidad
							               	 group by "IdInstrumento"
							               	 ,"IdTipoInstrumento"
											 ,"IdEntidadClasificadoraRiesgo"
								   			 ,"ClasificacionRiesgo"
							               	 ,"Nemotecnico"
							               	 ,"IdMoneda"
							               	 ,"MultiplicaPorParidad";
	    else
	           tabla_exigibilidad_agrupada = select 0 as "IdInterno"
	           										,0 as "IdInstrumento"
	                                              	, 0 as "Monto"
	                                              ,'' as "IdTipoInstrumento"
								   				  ,'' as "IdEntidadClasificadoraRiesgo"
								   				  ,'' as "ClasificacionRiesgo"
			                  					  ,'' as "Nemotecnico"
			                  					  ,'' as "IdMoneda"
			                  					  ,'N' as "MultiplicaPorParidad"
			                 				 from dummy; 
	    end if; 
	   
	   
	   SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
		
		INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
		SELECT :v_IdTrabajoParidad,"IdInterno" AS "IdInterno",:v_FechaSistema as "Fecha",EXI."IdMoneda" as "IdMonedaOrigen",:v_IdMonedaLocal as "IdMonedaDestino",0 as "ValorCambioLocalModificado",
											EXI."Monto" as "MontoOrigen",'PLATAFORMA' AS "IdUso"
				from :tabla_exigibilidad_agrupada AS EXI;
		
		call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
			p_IdEmpresa => :p_IdEmpresa,
			p_IdTrabajo => :v_IdTrabajoParidad
		);
	   

		
		v_tablasalida2 = select v_FechaSistema as "Fecha"
                       ,SIMU."IdMoneda" as "MonedaOrigen"
                       ,SIMU."Monto" as "MontoOrigen"
                       ,v_IdMonedaLocal as "MonedaDestino"
                       ,0 as "TipoCambio"
                       ,'USD' as "MonedaCambio"
                       ,'CENTRAL' as "OrigenTipoCambio"
                       ,SIMU."IdInstrumento" as "IdInstrumento"
                       ,SIMU."IdTipoInstrumento" as "IdTipoInstrumentoSubyacente"
                       ,OUTT."MontoLocal" as "MontoLocal"
  					   ,INS."ValorClasificacionRiesgo" as "ClasificacionRiesgo" 
                       ,INS."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo"
                       ,INS."IdTipoClasificacionRiesgo" as "IdTipoClasificacionRiesgo"
                       ,SIMU."Nemotecnico" as "Nemotecnico"
                  from :tabla_exigibilidad_agrupada AS SIMU
                  inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS INS on INS."IdInstrumento" = SIMU."IdInstrumento"
                  inner join "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" OUTT on OUTT."IdTrabajo" = :v_IdTrabajoParidad  AND OUTT."IdInterno" = SIMU."IdInterno"; 

	    -- cruzo los resultados con la tabla de exigibilidad
        p_out_TableExigibilidadPortafolio=  select 
									        		 coalesce(sum(tabla_exigibilidad_agrupada."MontoLocal" * COALESCE(ExigibilidadGarantia."ExigibilidadEntidad",100,ExigibilidadGarantia."ExigibilidadEntidad")/100),0) as "MontoExigibilidadEntidad"
									        		,coalesce(sum(tabla_exigibilidad_agrupada."MontoLocal" * COALESCE(ExigibilidadGarantia."ExigibilidadCorredora",100,ExigibilidadGarantia."ExigibilidadEntidad")/100),0) as "MontoExigibilidadCorredora"
									        from :v_tablasalida2 as tabla_exigibilidad_agrupada
										    left join  "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GrupoTipoInstrumento on GrupoTipoInstrumento."CodigoClasificacion" = 'FUTUROS'
										                                                             and GrupoTipoInstrumento."IdTipoInstrumento" = tabla_exigibilidad_agrupada."IdTipoInstrumentoSubyacente"
										    inner join "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" as ExigibilidadGarantia on ExigibilidadGarantia."IdBolsa" = p_IdBolsa
										                                                             and ExigibilidadGarantia."IdTipoInstrumento" = p_IdTipoInstrumento 
										                                                             and ExigibilidadGarantia."IdTipoInstrumentoSubyacente" = tabla_exigibilidad_agrupada."IdTipoInstrumentoSubyacente"
										                                                             and ExigibilidadGarantia."ClasificacionRiesgo" =  CASE WHEN IFNULL(GrupoTipoInstrumento."IdTipoInstrumento",'') ='' THEN tabla_exigibilidad_agrupada."ClasificacionRiesgo" ELSE '' END 
										                                                             and ExigibilidadGarantia."IdEntidadClasificadoraRiesgo" = CASE WHEN IFNULL(GrupoTipoInstrumento."IdTipoInstrumento",'') = '' THEN tabla_exigibilidad_agrupada."IdEntidadClasificadoraRiesgo" ELSE '' END 
	                                                                                         		 and ExigibilidadGarantia."IdTipoClasificacionRiesgo" = tabla_exigibilidad_agrupada."IdTipoClasificacionRiesgo"
	                                                                                         		 and ExigibilidadGarantia."Nemo" = CASE WHEN IFNULL(GrupoTipoInstrumento."IdTipoInstrumento",'') = '' THEN ExigibilidadGarantia."Nemo" ELSE tabla_exigibilidad_agrupada."Nemotecnico" END;
         v_Quantity := 0;
         select Count("MontoExigibilidadEntidad") INTO v_Quantity FROM :p_out_TableExigibilidadPortafolio;
         if v_Quantity = 0 then 
            p_out_TableExigibilidadPortafolio = Select 0 AS "MontoExigibilidadEntidad" , 0  AS "MontoExigibilidadCorredora"
                                                from dummy;
         end if; 

	END;	
END; 


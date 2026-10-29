SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarDataPruebaTransaccionDesdeSebra";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: OCT 
--  modificacion EAR 

"BSCL.Homologacion.BD.Servicios::CargarDataPruebaTransaccionDesdeSebra"(
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdCarga                   BIGINT,         --ID de la carga
	IN  p_Modulo                    NVARCHAR(3),    -- modulo 
	IN  p_Modo                      NVARCHAR(1),     --"R" reproceso borra todos lo cargado y vuelve hacer la carga desde cero "N" nueva carga
	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
	IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
	-- Parametros salida del servicio publico
	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
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
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
	
	
	-----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	   
	-- declaracion variables
	    DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	    DECLARE v_total_mov_cargadas DECIMAL(26,8):=0; 
	    DECLARE v_Quantity BIGINT:=0;
		DECLARE v_Ciclo BIGINT := 1;
		DECLARE v_FechaSistemaCreacion DATE;
		DECLARE v_IdTipoOperacion NVARCHAR(6);
		DECLARE v_IdCondicionDeLiquidacion NVARCHAR(2);
		DECLARE v_IdTransaccion BIGINT;
		DECLARE v_P_OUT_FECHA_LIQUIDACION NVARCHAR(10);
		DECLARE ARR_FechaSistemaCreacion     DATE ARRAY;
		DECLARE ARR_IdTipoOperacion          NVARCHAR ARRAY;
		DECLARE ARR_IdCondicionDeLiquidacion NVARCHAR ARRAY;
		DECLARE ARR_IdTransaccion            BIGINT ARRAY;
		DECLARE v_i BIGINT;
		DECLARE v_FechaSistema NVARCHAR(10);

		----------------------------------------------------
		-- Declaracion de Tabla
	    ----------------------------------------------------
     
		 CREATE LOCAL TEMPORARY TABLE #t_InsertarTransaccion(
		 "IdTransaccion" 						BIGINT,
		 "Folio" 								NVARCHAR(40),
		 "FechaSistemaCreacion" 				DATE,
		 "HoraSistemaCreacion" 					TIME,
		 "FechaOperacion" 						SECONDDATE,
		 "FechaLiquidacion" 					DATE,
		 "FechaEmision" 						DATE,
		 "FechaVencimiento" 					DATE,
		 "FechaIngresoOferta" 					TIMESTAMP,
		 "FechaIngresoRegistro" 				TIMESTAMP,
		 "IdSistemaBursatil" 					BIGINT,
		 "EsOD" 								NVARCHAR(1),
		 "EsCuentaPropia" 						NVARCHAR(1),
		 "EsFungible" 							NVARCHAR(1),
		 "EsFueraRueda" 						NVARCHAR(1),
		 "EsAPlazo" 							NVARCHAR(1),
		 "IdOperadorContraparte" 				BIGINT,
		 "IdOperadorCorredor" 					BIGINT,
		 "IdEmisor" 							NVARCHAR(40),
		 "Cantidad" 							DECIMAL(26,8),
		 "Monto" 								DECIMAL(26,8),
		 "Precio" 								DECIMAL(26,8),
		 "Tasa" 								DECIMAL(26,8),
		 "Nemotecnico" 							NVARCHAR(40),
		 "NemotecnicoSVS" 						NVARCHAR(40),
		 "ValorRescate" 						DECIMAL(26,8),
		 "ValorCaptacion" 						DECIMAL(26,8),
		 "IdEstado" 							NVARCHAR(3),
		 "IdTransaccionPlazo" 					BIGINT,
		 "IdTransaccionOd"	 					BIGINT,
		 "IdTipoInstrumento" 					NVARCHAR(3),
		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3),
		 "IdTipoReajuste" 						NVARCHAR(2),
		 "IdEmpleado" 							BIGINT,
		 "IdTipoOperacion" 						NVARCHAR(6),
		 "IdCondicionDeLiquidacion" 			NVARCHAR(2),
		 "IdMoneda" 							NVARCHAR(3),
		 "IdMonedaLiquidacion"					NVARCHAR(3),
		 "IdCorredor" 							BIGINT,
		 "IdEmpleadoReserva" 					BIGINT,
		 "IdEmpresa" 							BIGINT,
		 "IdBolsa" 								NVARCHAR(3),
		 "IdInstrumento" 						BIGINT,
		 "NemotecnicoSubyacente"				NVARCHAR(40),
		 "IdInstrumentoSubyacente" 				BIGINT,
		 "FolioCartera"							BIGINT,
		 "IdMonedaSubyacente"					NVARCHAR(3),
		 "EsVentaCorta"							NVARCHAR(1),
		 "Plazo"								BIGINT,
		 "FechaParidad" 						DATE,
		 "ValorParidad" 						DECIMAL(26,8),
		 "FechaTipoCambio" 						DATE,
		 "ValorTipoCambio"						DECIMAL(26,8),
		 "BloqueoAsignacionAutomatica"		    NVARCHAR(1),
		 "IdCarga"                              BIGINT,
		 "MERCADO_GES"							NVARCHAR(6),
		 "FEC_TRANS_GES"						TIMESTAMP,
		 "FOLIO_TRANS_GES"						NVARCHAR(40),
		 "IND_BOLSA_GES"						NVARCHAR(40),
		 "TIPO_OPERAC_GES"						NVARCHAR(40),
		 "FOLIO_SIMUL_GES"						NVARCHAR(40),
		 "IND_SIMUL_GES"						NVARCHAR(40),
		 "IND_OD_GES"							NVARCHAR(40)
		);
	  	  
	  	-- recupero fecha sistema
		 CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);  
	  	  	
        -- dependiendo del modo podemos borrar la informacion anterior cargada
        IF p_Modo = 'R' THEN -- reprocesar
         DELETE FROM "BSCL.Orden.BD::ORD.Transaccion"
         WHERE "IdCarga" = p_IdCarga; 
        END IF;
         
		--CARGAR TRANSACCION RENTA VARIABLE
        -- nos reservaremos los idtransaccion para poder trabajar en las tablas temporales
       	  insert into #t_InsertarTransaccion
			("IdTransaccion","Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion"
			,"FechaEmision","FechaVencimiento","FechaIngresoRegistro","IdSistemaBursatil","EsOD"
			,"EsCuentaPropia","EsFungible","EsFueraRueda","EsAPlazo","IdOperadorContraparte","IdOperadorCorredor"
			,"IdEmisor","Cantidad","Monto","Precio","Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate"
			,"ValorCaptacion","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento"
			,"IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredor","IdEmpleadoReserva"
			,"IdEmpresa","IdBolsa","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","FolioCartera"
			,"FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio","IdMonedaLiquidacion","IdMonedaSubyacente"
			,"EsVentaCorta","Plazo","FechaIngresoOferta","BloqueoAsignacionAutomatica","IdCarga"
            ,"MERCADO_GES","FEC_TRANS_GES","FOLIO_TRANS_GES","IND_BOLSA_GES","TIPO_OPERAC_GES","FOLIO_SIMUL_GES"
			,"IND_SIMUL_GES","IND_OD_GES"
			)
         	SELECT
			"BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL,--IdTransaccion;
			TRIM(GES."FOLIO_TRANS"),--Folio;
			v_FechaSistema,--FechaSistemaCreacion;
			SUBSTRING (GES."FEC_TRANS", 12, 8),--HoraSistemaCreacion;
			CONCAT (v_FechaSistema, CONCAT(' ',SUBSTRING (GES."FEC_TRANS", 12, 8))),--FechaOperacion;
			CASE WHEN GES."IND_LIQUID" = 'TP' THEN v_FechaSistema ELSE '0001-01-01' END,--FechaLiquidacion;
			'0001-01-01',--FechaEmision;
			CASE WHEN GES."IND_LIQUID" = 'TP' THEN ADD_YEARS(TO_DATE (v_FechaSistema, 'YYYY-MM-DD'), 1) else '0001-01-01' end,--FechaVencimiento;
			v_FechaSistema, --FechaIngresoRegistro;
	        0,--IdSistemaBursatil;		
			CASE WHEN GES."IND_OD" = 'OD' THEN 'S'
			     ELSE 'N'
			END,--EsOD;
	        GES."IND_CTA_PRO",--EsCuentaPropia;
			'N',--EsFungible;
			'N',--EsFueraRueda;
	        CASE WHEN GES."IND_LIQUID" ='TP' THEN 'S'
			     ELSE 'N'
			END,--"EsAPlazo"
	        CASE WHEN GES."IND_OD" = 'OD' THEN GES."COD_OPE_EXT"
				 ELSE 0
			END,-- IdOperadorContraparte;
			CASE WHEN GES."IND_OD" = 'OD' THEN GES."OPE_INT"
				 ELSE 0
			END,-- IdOperadorCorredor;
			0,--IdEmisor;
			GES."CANTIDAD",--Cantidad;
			GES."MONTO",--Monto;
			GES."PRECIO",--Precio;
			0,--Tasa;
			GES."NEMO",--Nemotecnico;
			'',--NemotecnicoSVS;
			0,--ValorRescate;
			0,--ValorCaptacion;
			'ING',--IdEstado;
			0,--IdTransaccionPlazo
	        0,--"IdTransaccionOd"
			CASE WHEN GES."IND_SIMUL" = 'TS' THEN 'SIM'
			ELSE i."IdTipoInstrumento"
			END , --IdTipoInstrumento;
			i."IdClasificacionTipoInstrumento",--IdClasificacionTipoInstrumento;
			'',--IdTipoReajuste;
			1,--IdEmpleado;
			CASE WHEN GES."TIPO_OPERAC" = 'C' AND  GES."IND_SIMUL" = 'TS' THEN 'COMSIM'
			     WHEN GES."TIPO_OPERAC" = 'V' AND  GES."IND_SIMUL" = 'TS' THEN 'VENSIM'
			     WHEN GES."TIPO_OPERAC" = 'C' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
			     WHEN GES."TIPO_OPERAC" = 'V' AND  Homologacion."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND GES."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
			     WHEN GES."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',Homologacion."ValorDestino")
			     WHEN GES."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',Homologacion."ValorDestino")
			END ,--IdTipoOperacion
			CASE WHEN GES."IND_LIQUID" = 'PH' THEN 'T0'
			     WHEN GES."IND_LIQUID" = 'PM' THEN 'T1'
			     WHEN GES."IND_LIQUID" = 'CN' THEN 'T2'
			     WHEN GES."IND_LIQUID" = 'T3' THEN 'T3'
			     ELSE 'T0'
			END,--IdCondicionDeLiquidacion;
			GES."MONEDA",--IdMoneda;
			GES."COD_CORR_CONTRA",--IdCorredor;
			0,--IdEmpleadoReserva;
			p_IdEmpresa,--IdEmpresa;
			GES."IND_BOLSA",--IdBolsa;
			CASE WHEN GES."IND_SIMUL" = 'TS' THEN i2."IdInstrumento" ELSE i."IdInstrumento" END,--IdInstrumento
	        CASE WHEN GES."IND_SIMUL" = 'TS' THEN i."Nemotecnico" ELSE '' END, --"NemotecnicoSubyacente",
	        CASE WHEN GES."IND_SIMUL" = 'TS' THEN i."IdInstrumento" ELSE 0 END,--"IdInstrumentoSubyacente",
	        0 AS "FolioCartera",
	        v_FechaSistema, --"fechaParidad"
	        GES."PARIDAD", -- "Valorparidad"
			'0001-01-01', -- "fechaTipoCambio"
	        0 ,-- valortipocambio 
			0 ,--"IdMonedaLiquidacion" 
	        CASE WHEN GES."IND_SIMUL" = 'TS' THEN i."IdMoneda" ELSE '' END,--"IdMonedaSubyacente"
	        CASE WHEN GES."IND_SIMUL" = 'VC' THEN 'S' ELSE 'N' END,-- EsVentaCorta
			CASE WHEN GES."IND_LIQUID" ='TP' THEN DAYS_BETWEEN(SUBSTRING(TO_VARCHAR(GES."FEC_TRANS"),1,10),GES."FEC_VCTO_TP") 
			     ELSE 0
			END,-- "Plazo"
	        '0001-01-01', --"FechaIngresoOferta"
	        CASE WHEN GES."IND_RESERVADO" = 'S' THEN 'S' ELSE 'N' END,--"BloqueaAsignacionAutomatica" dependera si el palo esta reservado
			p_IdCarga, -- identificador de la carga por migracion gestion
	        ges."MERCADO" AS "MERCADO_GES",
	        CONCAT (v_FechaSistema, CONCAT(' ',SUBSTRING (GES."FEC_TRANS", 12, 8))) AS "FEC_TRANS_GES",
	        ges."FOLIO_TRANS" AS "FOLIO_TRANS_GES",
	        ges."IND_BOLSA" AS "IND_BOLSA_GES",
	        ges."TIPO_OPERAC" AS "TIPO_OPERAC_GES",
	        ges."FOLIO_SIMUL" AS "FOLIO_SIMUL_GES",
	        ges."IND_SIMUL" AS "IND_SIMUL_GES",
	        ges."IND_OD" AS "IND_OD_GES"
			from "BSCL_VIRTUAL"."GES_TBPLTBRV" as ges 
			inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as i 
				on i."Nemotecnico" = GES."NEMO"
			left join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as i2
				 on i2."IdTipoInstrumento" = case when GES."IND_SIMUL" = 'TS' 
			   								 then 'SIM'
											 else ''
											 end
			and i2."Nemotecnico"  = CASE WHEN GES."TIPO_OPERAC" = 'C' THEN 'SIMULTÁNEA COMPRA'
			                    	 ELSE 'SIMULTÁNEA VENTA'
			                    	 END
			left join "BSCL.Homologacion.BD::HOM.Homologacion" as Homologacion
		  		on Homologacion."TipoValor" = 'TipoIntrumento'
		  		and Homologacion."Entidad"  = 'FACTURA'
		  		and Homologacion."ValorOrigen" = GES."MERCADO"
		  	left join "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
		  		on GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
		  		and GRUPO_INS."IdTipoInstrumento" = Homologacion."ValorDestino"
		  	WHERE CONCAT (SUBSTRING(ges."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (ges."FEC_TRANS", 12, 8))) >= p_FechaInicial
		  	  AND CONCAT (SUBSTRING(ges."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (ges."FEC_TRANS", 12, 8))) < ADD_DAYS (TO_DATE (p_FechaFinal, 'YYYY-MM-DD'), 1)
			order by "FOLIO_TRANS";


    	   --------------------------------------------------
	       -- ahora se hara la relacion de los palos 
	       -- primero las operaciones de simultanea
		   --------------------------------------------------

	       update TRA set TRA."IdTransaccionPlazo" = TRA2."IdTransaccion"
	                     ,TRA."IdTransaccionOd" = 0
	       from #t_InsertarTransaccion as TRA
	       		,#t_InsertarTransaccion as TRA2
	       where TRA."IND_SIMUL_GES" = 'TS'
	       and   TRA."IND_OD_GES" = ''
	       and   TRA."FOLIO_SIMUL_GES" <> ''
	       and   TRA."MERCADO_GES" = TRA2."MERCADO_GES"
	       and   TRA."FEC_TRANS_GES" = TRA2."FEC_TRANS_GES"
	       and   TRA."FOLIO_TRANS_GES" = TRA2."FOLIO_SIMUL_GES"
	       and   TRA."IND_BOLSA_GES" = TRA2."IND_BOLSA_GES"
	       and   TRA."TIPO_OPERAC_GES" = CASE WHEN TRA2."TIPO_OPERAC_GES" = 'V' then 'C' else 'V' end;

	       -------------------------------
	       -- ahora las transacciones od 
	       -------------------------------       
	       update TRA set TRA."IdTransaccionOd" = TRA2."IdTransaccion"
	                     ,TRA."IdTransaccionPlazo" = 0
	       from #t_InsertarTransaccion as TRA
	           ,#t_InsertarTransaccion as TRA2
	       where TRA."IND_SIMUL_GES" = ''
	       and   TRA."IND_OD_GES" = 'OD'
	       and   TRA."FOLIO_SIMUL_GES" = ''
	       and   TRA."MERCADO_GES" = TRA2."MERCADO_GES"
	       and   TRA."FEC_TRANS_GES" = TRA2."FEC_TRANS_GES"
	       and   TRA."FOLIO_TRANS_GES" = TRA2."FOLIO_TRANS_GES"
	       and   TRA."IND_BOLSA_GES" = TRA2."IND_BOLSA_GES"
	       and   TRA."TIPO_OPERAC_GES" = CASE WHEN TRA2."TIPO_OPERAC_GES" = 'V' then 'C' else 'V' end;      
	
	       -------------------------------------
	       -- transacciones SIMULTANEAS OD
	       -------------------------------------
	       
		   update TRA set TRA."IdTransaccionPlazo" = TRA2."IdTransaccion"
	                     ,TRA."IdTransaccionOd" = TRA2."IdTransaccion"
	       from #t_InsertarTransaccion as TRA
	       		,#t_InsertarTransaccion as TRA2
	       where TRA."IND_SIMUL_GES" = 'TS'
	       and   TRA."IND_OD_GES" = 'OD'
	       and   TRA."FOLIO_SIMUL_GES" <> ''
	       and   TRA."MERCADO_GES" = TRA2."MERCADO_GES"
	       and   TRA."FEC_TRANS_GES" = TRA2."FEC_TRANS_GES"
	       and   TRA."FOLIO_TRANS_GES" = TRA2."FOLIO_SIMUL_GES"
	       and   TRA."IND_BOLSA_GES" = TRA2."IND_BOLSA_GES"
	       and   TRA."TIPO_OPERAC_GES" = CASE WHEN TRA2."TIPO_OPERAC_GES" = 'V' then 'C' else 'V' end;

     	SELECT COUNT("IdTransaccion") INTO v_Quantity
		FROM #t_InsertarTransaccion;
		
 
   
		IF (:v_Quantity > 0) THEN 

			insert into "BSCL.Orden.BD::ORD.Transaccion"
			("IdTransaccion","Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion"
				,"FechaEmision","FechaVencimiento","FechaIngresoRegistro","IdSistemaBursatil","EsOD"
				,"EsCuentaPropia","EsFungible","EsFueraRueda","EsAPlazo","IdOperadorContraparte","IdOperadorCorredor"
				,"IdEmisor","Cantidad","Monto","Precio","Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate"
				,"ValorCaptacion","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento"
				,"IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredor","IdEmpleadoReserva"
				,"IdEmpresa","IdBolsa","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","FolioCartera"
				,"FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio","IdMonedaLiquidacion","IdMonedaSubyacente"
				,"EsVentaCorta","Plazo","FechaIngresoOferta","BloqueoAsignacionAutomatica","IdCarga"
				)
			SELECT
			"IdTransaccion","Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion"
			,"FechaEmision","FechaVencimiento","FechaIngresoRegistro","IdSistemaBursatil","EsOD"
			,"EsCuentaPropia","EsFungible","EsFueraRueda","EsAPlazo","IdOperadorContraparte","IdOperadorCorredor"
			,"IdEmisor","Cantidad","Monto","Precio","Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate"
			,"ValorCaptacion","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento"
			,"IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredor","IdEmpleadoReserva"
			,"IdEmpresa","IdBolsa","IdInstrumento","NemotecnicoSubyacente","IdInstrumentoSubyacente","FolioCartera"
			,"FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio","IdMonedaLiquidacion","IdMonedaSubyacente"
			,"EsVentaCorta","Plazo","FechaIngresoOferta","BloqueoAsignacionAutomatica","IdCarga"
			from #t_InsertarTransaccion; 
			
  

			--ACTUALIZAR FECHA DE LIQUIDACION
			ref_transaccion = SELECT DISTINCT "IdTransaccion","FechaSistemaCreacion", "IdTipoOperacion", "IdCondicionDeLiquidacion"
			                  FROM "BSCL.Orden.BD::ORD.Transaccion"
			                  where "IdCarga" = p_IdCarga
			                   and "EsAPlazo" = 'N';

			ARR_IdTipoOperacion          := ARRAY_AGG(:ref_transaccion."IdTipoOperacion" ORDER BY "IdTransaccion" DESC);
			ARR_IdCondicionDeLiquidacion := ARRAY_AGG(:ref_transaccion."IdCondicionDeLiquidacion" ORDER BY "IdTransaccion" DESC);
	        ARR_IdTransaccion			 := ARRAY_AGG(:ref_transaccion."IdTransaccion" ORDER BY "IdTransaccion" DESC);
	        
			FOR v_i IN 1 .. CARDINALITY(:ARR_FechaSistemaCreacion) DO 
				v_FechaSistemaCreacion := :ARR_FechaSistemaCreacion[:v_i];
				v_IdTipoOperacion := :ARR_IdTipoOperacion[:v_i];
				v_IdCondicionDeLiquidacion := :ARR_IdCondicionDeLiquidacion[:v_i];
				v_IdTransaccion:= :ARR_IdTransaccion[:v_i];
				
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion"(:v_FechaSistema,
																			   :v_IdTipoOperacion,
																			   :v_IdCondicionDeLiquidacion,
																			    1,
																			     v_P_OUT_FECHA_LIQUIDACION);
																			     
				UPDATE "BSCL.Orden.BD::ORD.Transaccion" SET "FechaLiquidacion" = v_P_OUT_FECHA_LIQUIDACION
				WHERE "IdTransaccion" = v_IdTransaccion;																		  			
			END FOR;
		END IF;
       
		--------------------
		--FIN SP 
		--------------------
	END;

	drop table #t_InsertarTransaccion;	
END;

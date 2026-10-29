SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionSPTOptimus";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de solicitudes FWD
--  FWD
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarOperacionSPTOptimus"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_Personas			   table ("RUT_CLI"   NVARCHAR(11), "SEC_RUT_CLI" NVARCHAR(1)),    -- rut del clientes sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
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
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		----------------------------------------------------- 
	    -- declaracion variables 
	   DECLARE v_Total_Operaciones_spt BIGINT:=0;
	   DECLARE v_Total_spt_Cargados BIGINT :=0;
	   DECLARE v_Quantity  BIGINT :=0;
	   
	   select COUNT(1) INTO v_Quantity FROM :p_Personas;
	   
       IF v_Quantity > 0 THEN 
        
           SELECT count(*) into v_Total_Operaciones_spt
           FROM "BSCL.Homologacion.BD::HOM.OperacionSpot" as HOM
				INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RutCliente" 
				  AND PER."SEC_RUT_CLI" = HOM."CuentaCliente"
				LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OPE
				  ON OPE."IdOperacionSpot" = HOM."IdOperacionSpot"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND OPE."IdOperacionSpot"  IS NULL;
  
			INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot"
			( 
					"IdOperacionSpot","IdEmpresa","IdCliente","IdPortafolio","IdInstrumento","IdTipoOperacion","FechaOperacion","FechaLiquidacion","IdMonedaPrincipal"
				,"IdMonedaSecundaria","IdMonedaLocal","IdMonedaEquivalente","MontoMonedaPrincipal","MontoMonedaPrincipalEquivalente","MontoMonedaSecundaria","TipoCambioCierre"
				,"TipoCambioCierreForward","ParidadCierre","ParidadCierreForward","TipoCambioCosto","ParidadCosto","MontoEquivalente","MontoRentabilidad","MontoRentabilidadCanal"
				,"ResultadoMonedaLocalPuntas","ResultadoMonedaLocalBidAsk","ResultadoMonedaLocalDO","ResultadoMonedaLocalTCSpot","ResultadoMonedaLocalTCCierreMesa","ResultadoMonedaLocalConsolidado"
				,"Margen","MargenBidAsk","Utilidad","PuntaCompra","PuntaVenta","PuntaBid","PuntaAsk","ParidadBid","ParidadAsk","ValorDolarObservado","ValorTCSpot","SolicitudTipoCambio"
				,"IdEvento","IdMesa","IdModulo","IdTipoDolar","TipoComprobanteFacturacion","IdComprobanteFacturacion","IdComprobanteTesoreriaIngreso","IdComprobanteTesoreriaEgreso"
				,"IdAgente","BenOur","EquivalenteConGasto","TipoCambioCierreConGasto","Observacion","CodigoComercio","FolioPlanilla","FolioInterfaz","OperacionFicticia"
				,"IdTipoCanalIngreso","IdDatatec","Estado","IdUsuario","IdUsuarioModifica","FechaModifica","FechaReproceso","IdCarga"			)      
			SELECT
				 HOM."IdOperacionSpot",HOM."IdEmpresa",HOM."IdCliente",HOM."IdPortafolio",HOM."IdInstrumento",HOM."IdTipoOperacion",HOM."FechaOperacion",HOM."FechaLiquidacion",HOM."IdMonedaPrincipal"
				,HOM."IdMonedaSecundaria",HOM."IdMonedaLocal",HOM."IdMonedaEquivalente",HOM."MontoMonedaPrincipal",HOM."MontoMonedaPrincipalEquivalente",HOM."MontoMonedaSecundaria",HOM."TipoCambioCierre"
				,HOM."TipoCambioCierreForward",HOM."ParidadCierre",HOM."ParidadCierreForward",HOM."TipoCambioCosto",HOM."ParidadCosto",HOM."MontoEquivalente",HOM."MontoRentabilidad",HOM."MontoRentabilidadCanal"
				,HOM."ResultadoMonedaLocalPuntas",HOM."ResultadoMonedaLocalBidAsk",HOM."ResultadoMonedaLocalDO",HOM."ResultadoMonedaLocalTCSpot",HOM."ResultadoMonedaLocalTCCierreMesa",HOM."ResultadoMonedaLocalConsolidado"
				,HOM."Margen",HOM."MargenBidAsk",HOM."Utilidad",HOM."PuntaCompra",HOM."PuntaVenta",HOM."PuntaBid",HOM."PuntaAsk",HOM."ParidadBid",HOM."ParidadAsk",HOM."ValorDolarObservado",HOM."ValorTCSpot",HOM."SolicitudTipoCambio"
				,HOM."IdEvento",HOM."IdMesa",HOM."IdModulo",HOM."IdTipoDolar",HOM."TipoComprobanteFacturacion",HOM."IdComprobanteFacturacion",HOM."IdComprobanteTesoreriaIngreso",HOM."IdComprobanteTesoreriaEgreso"
				,HOM."IdAgente",HOM."BenOur",HOM."EquivalenteConGasto",HOM."TipoCambioCierreConGasto",HOM."Observacion",HOM."CodigoComercio",HOM."FolioPlanilla",HOM."FolioInterfaz",HOM."OperacionFicticia"
				,HOM."IdTipoCanalIngreso",HOM."IdDatatec",HOM."Estado",HOM."IdUsuario",HOM."IdUsuarioModifica",HOM."FechaModifica",HOM."FechaReproceso",HOM."IdCarga"
			FROM "BSCL"."BSCL.Homologacion.BD::HOM.OperacionSpot" as HOM
			INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RutCliente" 
				  AND PER."SEC_RUT_CLI" = HOM."CuentaCliente"
            LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" as OPE
            	   ON OPE."IdOperacionSpot" = HOM."IdOperacionSpot"
             WHERE OPE."IdOperacionSpot" IS NULL
              AND HOM."IdCarga" = p_IdCarga;
              
		ELSE
           SELECT count(*) into v_Total_Operaciones_spt
           FROM "BSCL.Homologacion.BD::HOM.OperacionSpot" as HOM
				LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OPE
				  ON OPE."IdOperacionSpot" = HOM."IdOperacionSpot"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND OPE."IdOperacionSpot"  IS NULL;

           INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot"
			( 
					"IdOperacionSpot","IdEmpresa","IdCliente","IdPortafolio","IdInstrumento","IdTipoOperacion","FechaOperacion","FechaLiquidacion","IdMonedaPrincipal"
				,"IdMonedaSecundaria","IdMonedaLocal","IdMonedaEquivalente","MontoMonedaPrincipal","MontoMonedaPrincipalEquivalente","MontoMonedaSecundaria","TipoCambioCierre"
				,"TipoCambioCierreForward","ParidadCierre","ParidadCierreForward","TipoCambioCosto","ParidadCosto","MontoEquivalente","MontoRentabilidad","MontoRentabilidadCanal"
				,"ResultadoMonedaLocalPuntas","ResultadoMonedaLocalBidAsk","ResultadoMonedaLocalDO","ResultadoMonedaLocalTCSpot","ResultadoMonedaLocalTCCierreMesa","ResultadoMonedaLocalConsolidado"
				,"Margen","MargenBidAsk","Utilidad","PuntaCompra","PuntaVenta","PuntaBid","PuntaAsk","ParidadBid","ParidadAsk","ValorDolarObservado","ValorTCSpot","SolicitudTipoCambio"
				,"IdEvento","IdMesa","IdModulo","IdTipoDolar","TipoComprobanteFacturacion","IdComprobanteFacturacion","IdComprobanteTesoreriaIngreso","IdComprobanteTesoreriaEgreso"
				,"IdAgente","BenOur","EquivalenteConGasto","TipoCambioCierreConGasto","Observacion","CodigoComercio","FolioPlanilla","FolioInterfaz","OperacionFicticia"
				,"IdTipoCanalIngreso","IdDatatec","Estado","IdUsuario","IdUsuarioModifica","FechaModifica","FechaReproceso","IdCarga"			)      
			SELECT
				HOM."IdOperacionSpot",HOM."IdEmpresa",HOM."IdCliente",HOM."IdPortafolio",HOM."IdInstrumento",HOM."IdTipoOperacion",HOM."FechaOperacion",HOM."FechaLiquidacion",HOM."IdMonedaPrincipal"
				,HOM."IdMonedaSecundaria",HOM."IdMonedaLocal",HOM."IdMonedaEquivalente",HOM."MontoMonedaPrincipal",HOM."MontoMonedaPrincipalEquivalente",HOM."MontoMonedaSecundaria",HOM."TipoCambioCierre"
				,HOM."TipoCambioCierreForward",HOM."ParidadCierre",HOM."ParidadCierreForward",HOM."TipoCambioCosto",HOM."ParidadCosto",HOM."MontoEquivalente",HOM."MontoRentabilidad",HOM."MontoRentabilidadCanal"
				,HOM."ResultadoMonedaLocalPuntas",HOM."ResultadoMonedaLocalBidAsk",HOM."ResultadoMonedaLocalDO",HOM."ResultadoMonedaLocalTCSpot",HOM."ResultadoMonedaLocalTCCierreMesa",HOM."ResultadoMonedaLocalConsolidado"
				,HOM."Margen",HOM."MargenBidAsk",HOM."Utilidad",HOM."PuntaCompra",HOM."PuntaVenta",HOM."PuntaBid",HOM."PuntaAsk",HOM."ParidadBid",HOM."ParidadAsk",HOM."ValorDolarObservado",HOM."ValorTCSpot",HOM."SolicitudTipoCambio"
				,HOM."IdEvento",HOM."IdMesa",HOM."IdModulo",HOM."IdTipoDolar",HOM."TipoComprobanteFacturacion",HOM."IdComprobanteFacturacion",HOM."IdComprobanteTesoreriaIngreso",HOM."IdComprobanteTesoreriaEgreso"
				,HOM."IdAgente",HOM."BenOur",HOM."EquivalenteConGasto",HOM."TipoCambioCierreConGasto",HOM."Observacion",HOM."CodigoComercio",HOM."FolioPlanilla",HOM."FolioInterfaz",HOM."OperacionFicticia"
				,HOM."IdTipoCanalIngreso",HOM."IdDatatec",HOM."Estado",HOM."IdUsuario",HOM."IdUsuarioModifica",HOM."FechaModifica",HOM."FechaReproceso",HOM."IdCarga"
             FROM "BSCL"."BSCL.Homologacion.BD::HOM.OperacionSpot" as HOM
             LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" as OPE
            	   ON OPE."IdOperacionSpot" = HOM."IdOperacionSpot"
             WHERE OPE."IdOperacionSpot" IS NULL
              AND HOM."IdCarga" = p_IdCarga;
		END IF;
		
		-- contamos los cargados
	        select count(*) into v_Total_spt_Cargados
	        from "BSCL.Mesa.BD::SPT.OperacionSpot"
	        where "IdCarga" = p_IdCarga;

		 
        -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	    p_ResumenDatosProcesados =  select 'CARGAOPERACIONESFWD' as "CodigoProceso" 
	   			  					      ,'FWD' as "Modulo" 
	   			 						  ,v_Total_Operaciones_spt as "TotalRegistrosSebra"
	   			  						  ,v_Total_spt_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    union all
       							    select * from :p_ResumenDatosProcesados;

          IF v_Total_Operaciones_spt <> v_Total_spt_Cargados THEN 
			 p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_FWD' as "Codigo",'falla en la carga fwd' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   	  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

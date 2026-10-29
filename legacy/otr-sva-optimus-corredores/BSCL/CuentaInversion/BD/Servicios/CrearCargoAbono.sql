SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono";

CREATE PROCEDURE   
-- 
--  Proposito: Crear Movimiento de Cargo o Abono
--  Autor: NRM - BCS
--  Fecha Creacion: 01/07/2015 
--  Numero Servicio: 7
--
-------------------MODIFICACIONES-------------------------------------------------------
--  Proposito: Optmizar SP incorporando tablas globales para tablas de entrada y salida
--  Autor: NRM - BCS
--  Fecha Modificacion: 26/12/2016
--  
"BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (	
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3),
    IN p_simularOperacion				NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN p_UsaReglasNegocio				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		
    IN p_FechaSistema              		NVARCHAR(40),
    IN p_EsIngresoManual				NVARCHAR(1)		DEFAULT 'N',
    
	OUT p_out_InformacionContador  		INT,
	OUT p_out_AdvertenciasContador		INT,
	OUT p_out_ErroresContador			INT
	
	-- ENTRADAS
	-- BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar
	
	-- SALIDAS
	-- BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado
	-- BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle
	-- BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle
	-- BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA  CONDITION;	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		IF (NOT IS_EMPTY("BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes")) THEN
			
			v_Mensajes = SELECT "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2" FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes";
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Mensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle"							
			SELECT TCM."NumeroCargoAbono", TCM."IdCargoAbonoMovimiento",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'OK';
			
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle"
			("NumeroCargoAbono","Codigo","RequiereAutorizacion","Descripcion")
			SELECT TCM."NumeroCargoAbono",EM."Codigo",TCM."Resultado" as "RequiereAutorizacion",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" IN ('ADVE','RAUT');
						
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle"
			SELECT TCM."NumeroCargoAbono",EM."Codigo",EM."Descripcion" 
			  FROM :v_erroroutmsg EM 
			 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes" TCM 
			 		 ON TCM."IdInterno" = EM."IdInterno" AND TCM."Codigo" = EM."Codigo" WHERE TCM."Resultado" = 'RECH';
		END IF;
		
		SELECT count(1) into p_out_InformacionContador FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
		SELECT count(1) into p_out_ErroresContador FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";	
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" 
			SELECT 0 as "NumeroCargoAbono",v_errorExt AS "Codigo",::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			
			SELECT count(1) into p_out_ErroresContador FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";								
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado";
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle";
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle";
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle";
	DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes";
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_CargoAbonoAGenerar 		= SELECT  ROW_NUMBER() over (ORDER BY (SELECT 1 from dummy)) as "IdInterno"
										,"NumeroCargoAbono"
									    ,"IdPortafolio"
									    ,"IdMoneda"
									    ,"IdTipoMovimiento"
									    ,"IdConcepto"
									    ,"Descripcion"
									    ,"IdCondicionDeLiquidacion"
									    ,"FechaCondicionDeLiquidacion"
									    ,"Monto"
										,"IdEjecutivo"
									 	,"IdSucursal"
									    ,"IdTipoProducto"
									    ,"IdTipoOperacion"
									    ,"IdOperacion"
									    ,"FechaOperacion"
									    ,"FechaLiquidacionOperacion"
									    ,"FechaVencimientoOperacion"
									    ,"IdMonedaOperacion"
									    ,"MontoOperacion"
									    ,"TipoCambioOperacion"
									    ,"IdContraparte"
									    ,"IdTipoComprobante"
										,"IdComprobante"
										,"FolioComprobante"
										,"IdComprobanteDetalle"
										,"IdComprobanteFormaDePago"
										,"IdTipoMovimientoAsociado"
									 	,"IdMovimientoAsociado"
									    ,"FechaComprobante"
									    ,"IdPersonaPagoCobro"
									    ,"IdInstrumento"
									    ,"IdTipoInstrumento"
									    ,"Nemo"
									    ,"Cantidad"
									    ,"EsIndicadorBilateral"
									    ,"EsCargoAbonoPorTraspaso"
										,"IdPortafolioTraspaso"
										,"GeneroContraparteEnTesoreria"
										,"CertificadoAutomatico"
										,'' as "RequiereAutorizacion"
									FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar";
																		
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		DECLARE v_resultadoValidacion 	NVARCHAR(40);
		
		IF p_EsIngresoManual = 'S' THEN
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoValidacion);
			IF :v_resultadoValidacion = 'RECH' THEN
				INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes"
				SELECT 0 as "IdInterno",0 as "NumeroCargoAbono",0 as "IdCargoAbonoMovimiento","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2",'RECH' as "Resultado" FROM :v_Errores;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
		
	  	IF IS_EMPTY(:v_CargoAbonoAGenerar) THEN
	  		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",0 as "NumeroCargoAbono",0 as "IdCargoAbonoMovimiento",'NO_VIENEN_MOVIMIENTOS' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
	  	END IF;
		
		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_IN_CargoAbonoAGenerar" (
				"IdInterno","NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion","Monto",										    
				"IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion",
				"TipoCambioOperacion","IdContraparte","IdTipoComprobante","IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado",
 				"FechaComprobante","IdPersonaPagoCobro","IdInstrumento","IdTipoInstrumento","Cantidad","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso","GeneroContraparteEnTesoreria",
				"CertificadoAutomatico","RequiereAutorizacion"
		)
		SELECT  "IdInterno","NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion","Monto",										    
				"IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion","MontoOperacion",
				"TipoCambioOperacion","IdContraparte","IdTipoComprobante","IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado",
 				"FechaComprobante","IdPersonaPagoCobro","IdInstrumento","IdTipoInstrumento","Cantidad","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso","GeneroContraparteEnTesoreria",
				"CertificadoAutomatico","RequiereAutorizacion"
		FROM :v_CargoAbonoAGenerar;		
		CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoCrearCargoAbono" (
			 p_IdEmpresa			=> :p_IdEmpresa			 
			,p_IdModulo 			=> :p_IdModulo			 
			,p_FechaSistema			=> :p_FechaSistema
			,p_EsIngresoManual		=> :p_EsIngresoManual
		);
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes"
		SELECT R."IdInterno",CBG."NumeroCargoAbono",0 as "IdCargoAbonoMovimiento",R."Codigo",R."CodigoDato1",R."ValorDato1",R."CodigoDato2",R."ValorDato2",R."Resultado" 
		  FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado" as R
		 INNER JOIN :v_CargoAbonoAGenerar as CBG ON CBG."IdInterno" = R."IdInterno"
		 WHERE "Resultado" <> 'OK';
		
		--Se eliminan los registros rechazados
		v_CargoAbonoAGenerar =
	 	SELECT CAB."IdInterno",CAB."NumeroCargoAbono",CAB."IdPortafolio",CAB."IdMoneda",CAB."IdTipoMovimiento",CAB."IdConcepto",CAB."Descripcion",CAB."IdCondicionDeLiquidacion",
			   CAB."FechaCondicionDeLiquidacion",CAB."Monto",CAB."IdEjecutivo",CAB."IdSucursal",CAB."IdTipoProducto",CAB."IdTipoOperacion",CAB."IdOperacion",
			   CAB."FechaOperacion",CAB."FechaLiquidacionOperacion",CAB."FechaVencimientoOperacion",CAB."IdMonedaOperacion",CAB."MontoOperacion",CAB."TipoCambioOperacion",
			   CAB."IdContraparte",CAB."IdTipoComprobante",CAB."IdComprobante",CAB."FolioComprobante",CAB."IdComprobanteDetalle",CAB."IdComprobanteFormaDePago",
			   CAB."IdTipoMovimientoAsociado",CAB."IdMovimientoAsociado",CAB."FechaComprobante",CAB."IdPersonaPagoCobro",CAB."IdInstrumento",CAB."IdTipoInstrumento",
			   CAB."Nemo",CAB."Cantidad",CAB."EsIndicadorBilateral",CAB."EsCargoAbonoPorTraspaso",CAB."IdPortafolioTraspaso",CAB."GeneroContraparteEnTesoreria",
			   CAB."CertificadoAutomatico",CASE WHEN RA."RAUT" > 0 THEN 'S' ELSE 'N' END as "RequiereAutorizacion" 
	 	  FROM :v_CargoAbonoAGenerar as CAB
	 	 INNER JOIN (SELECT "IdInterno", SUM(CASE WHEN "Resultado" = 'RAUT' THEN 1 ELSE 0 END) as "RAUT"
		  			   FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado" GROUP BY "IdInterno"
	 	 			) as RA
	 	 		 ON RA."IdInterno" = CAB."IdInterno"
	 	 INNER JOIN (SELECT DISTINCT "IdInterno" FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado" WHERE "Resultado" = 'OK'
		 			 EXCEPT 
		 			 SELECT DISTINCT "IdInterno" FROM "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoCrearCargoAbono_OUT_Resultado" WHERE "Resultado" = 'RECH') as R
		 		 ON R."IdInterno" = CAB."IdInterno";	
	END; 
	END IF;
	
	--------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE v_IdEjecucion BIGINT;
		DECLARE v_FechaHoraGeneracion NVARCHAR(40);
		DECLARE v_ErroresContador, v_AdvertenciasContador	BIGINT;
		DECLARE v_IdCargoMovimiento BIGINT;
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
		DECLARE v_CargoAbono			TABLE ("IdInterno" BIGINT,"NumeroCargoAbono" BIGINT,"IdCargoAbonoMovimiento" BIGINT,"FechaSistema" NVARCHAR(10),"IdModulo" NVARCHAR(3),"IdTipoMovimiento" NVARCHAR(6),
				 					   "EsIngresoManual" NVARCHAR(1),"IdCuentaInversion" BIGINT,"NombreCompleto" NVARCHAR(300),"IdConcepto" NVARCHAR(6),"Descripcion" NVARCHAR(520),
				 					   "IdCondicionDeLiquidacion" NVARCHAR(2),"FechaCondicionDeLiquidacion" NVARCHAR(10),"Monto" DECIMAL(26,8),"Saldo" DECIMAL(26,8),"SaldoPorFechaMovimiento" DECIMAL(26,8),
				 					   "SaldoLibroInventario" DECIMAL(26,8),"EsCertificadoAutomatico" NVARCHAR(1),"FechaCertificacion" NVARCHAR(40),"IdEjecutivo" BIGINT,"IdSucursal" BIGINT,
				 					   "IdTipoComprobante" NVARCHAR(3),"IdComprobante" BIGINT,"FolioComprobante" NVARCHAR(40),"IdComprobanteDetalle" BIGINT,"IdComprobanteFormaDePago"	BIGINT,
				 					   "IdTipoMovimientoAsociado" NVARCHAR(6),"IdMovimientoAsociado" BIGINT,"FechaComprobante" NVARCHAR(40),"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),
				 					   "IdOperacion" BIGINT,"FechaOperacion" NVARCHAR(40),"FechaLiquidacionOperacion" DATE,"FechaVencimientoOperacion" DATE,"IdMonedaOperacion" NVARCHAR(3),
				 					   "MontoOperacion" DECIMAL(26,8),"TipoCambioOperacion" DECIMAL(26,8),"IdInstrumento" BIGINT,"IdTipoInstrumento" NVARCHAR(3),"Nemo" NVARCHAR(40),"Cantidad" DECIMAL(26,8),
				 					   "EsCargoAbonoPorTraspaso" NVARCHAR(1),"EsIndicadorBilateral" NVARCHAR(1),"GeneroContraparteEnTesoreria" NVARCHAR(1),"IdContraparte" BIGINT,
				 					   "IdPersonaPagoCobro" BIGINT,"FechaIngresoRegistro" NVARCHAR(40),"IdUsuario" BIGINT,"IdEmpresa" BIGINT,"IdEstado" NVARCHAR(3));
			
		DECLARE v_CargoAbonoTraspaso	TABLE ("IdInterno" BIGINT,"NumeroCargoAbono" BIGINT,"IdCargoAbonoMovimiento" BIGINT,"FechaSistema" NVARCHAR(10),"IdModulo" NVARCHAR(3),"IdTipoMovimiento" NVARCHAR(6),
				 					   "EsIngresoManual" NVARCHAR(1),"IdCuentaInversion" BIGINT,"NombreCompleto" NVARCHAR(300),"IdConcepto" NVARCHAR(6),"Descripcion" NVARCHAR(520),
				 					   "IdCondicionDeLiquidacion" NVARCHAR(2),"FechaCondicionDeLiquidacion" NVARCHAR(10),"Monto" DECIMAL(26,8),"Saldo" DECIMAL(26,8),"SaldoPorFechaMovimiento" DECIMAL(26,8),
				 					   "SaldoLibroInventario" DECIMAL(26,8),"EsCertificadoAutomatico" NVARCHAR(1),"FechaCertificacion" NVARCHAR(40),"IdEjecutivo" BIGINT,"IdSucursal" BIGINT,
				 					   "IdTipoComprobante" NVARCHAR(3),"IdComprobante" BIGINT,"FolioComprobante" NVARCHAR(40),"IdComprobanteDetalle" BIGINT,"IdComprobanteFormaDePago"	BIGINT,
				 					   "IdTipoMovimientoAsociado" NVARCHAR(6),"IdMovimientoAsociado" BIGINT,"FechaComprobante" NVARCHAR(40),"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),
				 					   "IdOperacion" BIGINT,"FechaOperacion" NVARCHAR(40),"FechaLiquidacionOperacion" DATE,"FechaVencimientoOperacion" DATE,"IdMonedaOperacion" NVARCHAR(3),
				 					   "MontoOperacion" DECIMAL(26,8),"TipoCambioOperacion" DECIMAL(26,8),"IdInstrumento" BIGINT,"IdTipoInstrumento" NVARCHAR(3),"Nemo" NVARCHAR(40),"Cantidad" DECIMAL(26,8),
				 					   "EsCargoAbonoPorTraspaso" NVARCHAR(1),"EsIndicadorBilateral" NVARCHAR(1),"GeneroContraparteEnTesoreria" NVARCHAR(1),"IdContraparte" BIGINT,
				 					   "IdPersonaPagoCobro" BIGINT,"FechaIngresoRegistro" NVARCHAR(40),"IdUsuario" BIGINT,"IdEmpresa" BIGINT,"IdEstado" NVARCHAR(3));		
		
 		DECLARE v_TabCuentaInversion 	TABLE("IdCuentaInversion" BIGINT);
 		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN		
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes"
			SELECT 0 as "IdInterno",0 as "NumeroCargoAbono",0 as "IdCargoAbonoMovimiento",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2",'RECH' as "Resultado" FROM DUMMY;			
			SIGNAL SALIDA_CONTROLADA;
		END;
		

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa 			=> :p_IdEmpresa,
			p_out_FechaHoraSistema	=> :v_FechaHoraGeneracion
		);

		v_CargoAbono = 
		 SELECT  CBG."IdInterno"
		 		,CBG."NumeroCargoAbono"
				,"BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".NEXTVAL as "IdCargoAbonoMovimiento"
				,CAST(p_FechaSistema as NVARCHAR(10)) as "FechaSistema"
				,p_IdModulo as "IdModulo"
				,CBG."IdTipoMovimiento"
				,p_EsIngresoManual as "EsIngresoManual"
				,CI."IdCuentaInversion"
			    ,CAST(CLI."NombreCompleto" as NVARCHAR(300)) as "NombreCompleto"
			    ,CBG."IdConcepto"
			    ,COALESCE(CBG."Descripcion",'') as "Descripcion"
			    ,CBG."IdCondicionDeLiquidacion"
			    ,CBG."FechaCondicionDeLiquidacion"
			    ,CBG."Monto"
			    ,0 as "Saldo"
			    ,0 as "SaldoPorFechaMovimiento"
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN 0 ELSE CBG."Monto" END as "SaldoLibroInventario"			    
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN 'N' ELSE 'S' END as "EsCertificadoAutomatico"
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN '0001-01-01' ELSE CAST(:v_FechaHoraGeneracion as TIMESTAMP) END as "FechaCertificacion"
			    ,CBG."IdEjecutivo"
			    ,CBG."IdSucursal"
				,COALESCE(CBG."IdTipoComprobante",'') as "IdTipoComprobante"
				,COALESCE(CBG."IdComprobante",0) as "IdComprobante"
				,COALESCE(CBG."FolioComprobante",'') as "FolioComprobante"
				,COALESCE(CBG."IdComprobanteDetalle",0) as "IdComprobanteDetalle"
				,COALESCE(CBG."IdComprobanteFormaDePago",0) as "IdComprobanteFormaDePago"
				,COALESCE(CBG."IdTipoMovimientoAsociado",'') as "IdTipoMovimientoAsociado"
				,COALESCE(CBG."IdMovimientoAsociado",0) as "IdMovimientoAsociado"	
	            ,COALESCE(CBG."FechaComprobante",'0001-01-01') as "FechaComprobante"
	            ,COALESCE(CBG."IdTipoProducto",'') as "IdTipoProducto"
				,COALESCE(CBG."IdTipoOperacion",'') as "IdTipoOperacion"
				,COALESCE(CBG."IdOperacion",0) as "IdOperacion"
				,COALESCE(CBG."FechaOperacion",'0001-01-01') as "FechaOperacion"
				,COALESCE(CBG."FechaLiquidacionOperacion",'0001-01-01') as "FechaLiquidacionOperacion"
				,COALESCE(CBG."FechaVencimientoOperacion",'0001-01-01') as "FechaVencimientoOperacion"
				,COALESCE(CBG."IdMonedaOperacion",'') as "IdMonedaOperacion"
				,COALESCE(CBG."MontoOperacion",0) as "MontoOperacion"
				,COALESCE(CBG."TipoCambioOperacion",0) as "TipoCambioOperacion"
				,COALESCE(CBG."IdInstrumento",0) as "IdInstrumento"
				,COALESCE(CBG."IdTipoInstrumento",'') as "IdTipoInstrumento"
				,COALESCE(CBG."Nemo",'') as "Nemo"
				,COALESCE(CBG."Cantidad",0) as "Cantidad"
				,CBG."EsCargoAbonoPorTraspaso"				
				,CBG."EsIndicadorBilateral"
				,CBG."GeneroContraparteEnTesoreria"
				,COALESCE(CBG."IdContraparte",0) as "IdContraparte"
				,COALESCE(CBG."IdPersonaPagoCobro",0) as "IdPersonaPagoCobro"												
				,CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro"
				,p_IdUsuario as "IdUsuario"
				,p_IdEmpresa as "IdEmpresa"
				,CASE WHEN CBG."CertificadoAutomatico" = 'N' AND CBG."RequiereAutorizacion" = 'N' THEN 'PEN'
					  WHEN CBG."RequiereAutorizacion" = 'S' THEN 'PEN'
					  ELSE 'CRT' 
				  END as "IdEstado"
		   FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI		 		 
		  INNER JOIN :v_CargoAbonoAGenerar AS CBG 
		 		  ON CI."IdEmpresa"	  	= :p_IdEmpresa
		 		 AND CI."IdMoneda"     	= CBG."IdMoneda"
 	       		 AND CI."IdPortafolio" 	= CBG."IdPortafolio"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
		 		  ON CLI."IdCliente" = CI."IdCliente";
		 
		v_CargoAbonoTraspaso =  
		 SELECT  CBG."IdInterno"
		 		,CBG."NumeroCargoAbono"
				,"BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".NEXTVAL as "IdCargoAbonoMovimiento"
				,CAST(p_FechaSistema as NVARCHAR(10)) as "FechaSistema"
				,p_IdModulo as "IdModulo"
				,CASE WHEN CBG."IdTipoMovimiento" = 'MOVCAR' THEN 'MOVABN'
					  WHEN CBG."IdTipoMovimiento" = 'MOVABN' THEN 'MOVCAR' 
				  END as "IdTipoMovimiento"
				,p_EsIngresoManual as "EsIngresoManual"
				,CI."IdCuentaInversion"
			    ,CAST(CLI."NombreCompleto" as NVARCHAR(300)) as "NombreCompleto"
			    ,CBG."IdConcepto"
			    ,COALESCE(CBG."Descripcion",'') as "Descripcion"
			    ,CBG."IdCondicionDeLiquidacion"
			    ,CBG."FechaCondicionDeLiquidacion"
			    ,CBG."Monto"
			    ,0 as "Saldo"
			    ,0 as "SaldoPorFechaMovimiento"
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN 0 ELSE CBG."Monto" END as "SaldoLibroInventario"
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN 'N' ELSE 'S' END as "EsCertificadoAutomatico"
			    ,CASE WHEN p_EsIngresoManual = 'S' THEN '0001-01-01' ELSE CAST(:v_FechaHoraGeneracion as TIMESTAMP) END as "FechaCertificacion"
			    ,CBG."IdEjecutivo"
			    ,CBG."IdSucursal"
				,COALESCE(CBG."IdTipoComprobante",'') as "IdTipoComprobante"
				,COALESCE(CBG."IdComprobante",0) as "IdComprobante"
				,COALESCE(CBG."FolioComprobante",'') as "FolioComprobante"
				,COALESCE(CBG."IdComprobanteDetalle",0) as "IdComprobanteDetalle"
				,COALESCE(CBG."IdComprobanteFormaDePago",0) as "IdComprobanteFormaDePago"				
				,COALESCE(CBG."IdTipoMovimientoAsociado",'') as "IdTipoMovimientoAsociado"
				,COALESCE(CBG."IdMovimientoAsociado",0) as "IdMovimientoAsociado"				
	            ,COALESCE(CBG."FechaComprobante",'0001-01-01') as "FechaComprobante"
	            ,COALESCE(CBG."IdTipoProducto",'') as "IdTipoProducto"
				,COALESCE(CBG."IdTipoOperacion",'') as "IdTipoOperacion"
				,COALESCE(CBG."IdOperacion",0) as "IdOperacion"
				,COALESCE(CBG."FechaOperacion",'0001-01-01') as "FechaOperacion"
				,COALESCE(CBG."FechaLiquidacionOperacion",'0001-01-01') as "FechaLiquidacionOperacion"
				,COALESCE(CBG."FechaVencimientoOperacion",'0001-01-01') as "FechaVencimientoOperacion"
				,COALESCE(CBG."IdMonedaOperacion",'') as "IdMonedaOperacion"
				,COALESCE(CBG."MontoOperacion",0) as "MontoOperacion"
				,COALESCE(CBG."TipoCambioOperacion",0) as "TipoCambioOperacion"
				,COALESCE(CBG."IdInstrumento",0) as "IdInstrumento"
				,COALESCE(CBG."IdTipoInstrumento",'') as "IdTipoInstrumento"
				,COALESCE(CBG."Nemo",'') as "Nemo"
				,COALESCE(CBG."Cantidad",0) as "Cantidad"
				,CBG."EsCargoAbonoPorTraspaso"				
				,CBG."EsIndicadorBilateral"
				,CBG."GeneroContraparteEnTesoreria"
				,COALESCE(CBG."IdContraparte",0) as "IdContraparte"
				,COALESCE(CBG."IdPersonaPagoCobro",0) as "IdPersonaPagoCobro"												
				,CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro"
				,p_IdUsuario as "IdUsuario"
				,p_IdEmpresa as "IdEmpresa"
				,CASE WHEN CBG."CertificadoAutomatico" = 'N' AND CBG."RequiereAutorizacion" = 'N' THEN 'PEN'
					  WHEN CBG."RequiereAutorizacion" = 'S' THEN 'PEN'
					  ELSE 'CRT' 
				  END as "IdEstado"
		   FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI
		  INNER JOIN :v_CargoAbonoAGenerar AS CBG
		 		  ON CI."IdEmpresa"	  	= :p_IdEmpresa
		 		 AND CI."IdMoneda"     	= CBG."IdMoneda"
 	       		 AND CI."IdPortafolio" 	= CBG."IdPortafolioTraspaso"
		  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
		 		  ON CLI."IdCliente" = CI."IdCliente";
		
		
		v_CargoAbonoMovimiento =	
		 SELECT CA.*, COALESCE(CAT."IdCargoAbonoMovimiento",0) as "IdCargoAbonoRelacionadoTraspaso"
		   FROM :v_CargoAbono as CA
		   LEFT JOIN :v_CargoAbonoTraspaso as CAT ON CAT."IdInterno" = CA."IdInterno"
		  UNION ALL	
		 SELECT CAT.*, COALESCE(CA."IdCargoAbonoMovimiento",0) as "IdCargoAbonoRelacionadoTraspaso"
		   FROM :v_CargoAbonoTraspaso as CAT
		   LEFT JOIN :v_CargoAbono as CA ON CA."IdInterno" = CAT."IdInterno";
		
		
		IF (:p_simularOperacion = 'N') THEN
			--1) Insertar Registros
			INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"(
				"IdCargoAbonoMovimiento", 			
		        "FechaSistema",
		        "IdModulo",
				"IdTipoMovimiento",	
				"EsIngresoManual",								
				"IdCuentaInversion",
				"NombreCliente",
				"IdConcepto",
				"Descripcion",
				"IdCondicionDeLiquidacion",	
				"FechaEfectivaMovimiento",
				"Monto",
				"Saldo",
				"SaldoPorFechaMovimiento",
				"SaldoLibroInventario",
				"EsCertificadoAutomatico",
				"FechaCertificacion",			
				"IdEjecutivo",
		 		"IdSucursal",
		        "IdTipoComprobante",
				"IdComprobante",
				"FolioComprobante",
				"IdComprobanteDetalle",
				"IdComprobanteFormaDePago",
				"IdTipoMovimientoAsociado",
	 			"IdMovimientoAsociado",
		        "FechaComprobante",
		        "IdTipoProducto",
				"IdTipoOperacion",
				"IdOperacion",
				"FechaOperacion",
				"FechaLiquidacionOperacion",
				"FechaVencimientoOperacion", -----nuevo
				"IdMonedaOperacion",
				"MontoOperacion",	-----nuevo
				"TipoCambioOperacion",
				"IdInstrumento",
				"IdTipoInstrumento",
				"Nemo",
				"Cantidad",
				"EsCargoAbonoPorTraspaso",
				"IdCargoAbonoRelacionadoTraspaso",
				"EsIndicadorBilateral",
				"GeneroContraparteEnTesoreria",
				"IdContraparte",
				"IdPersonaPagoCobro",
				"FechaIngresoRegistro",
				"IdUsuario",
				"IdEmpresa",
				"IdEstado"
			)
			 SELECT	"IdCargoAbonoMovimiento"
					,"FechaSistema"
					,"IdModulo"
					,"IdTipoMovimiento"
					,"EsIngresoManual"
					,"IdCuentaInversion"
				    ,"NombreCompleto"
				    ,"IdConcepto"
				    ,"Descripcion"
				    ,"IdCondicionDeLiquidacion"
				    ,"FechaCondicionDeLiquidacion"
				    ,"Monto"
				    ,"Saldo"
				    ,"SaldoPorFechaMovimiento"
				    ,"SaldoLibroInventario"
				    ,"EsCertificadoAutomatico"
				    ,"FechaCertificacion"
				    ,"IdEjecutivo"
		 			,"IdSucursal"	 			
					,"IdTipoComprobante"
					,"IdComprobante"
					,"FolioComprobante"
					,"IdComprobanteDetalle"
					,"IdComprobanteFormaDePago"
					,"IdTipoMovimientoAsociado"
					,"IdMovimientoAsociado"
		            ,"FechaComprobante"
		            ,"IdTipoProducto"
					,"IdTipoOperacion"
					,"IdOperacion"
					,"FechaOperacion"
					,"FechaLiquidacionOperacion"
					,"FechaVencimientoOperacion"
					,"IdMonedaOperacion"
					,"MontoOperacion"
					,"TipoCambioOperacion"
					,"IdInstrumento"
					,"IdTipoInstrumento"
					,"Nemo"
					,"Cantidad"
					,"EsCargoAbonoPorTraspaso"
					,"IdCargoAbonoRelacionadoTraspaso"	
					,"EsIndicadorBilateral"
					,'N'
					,"IdContraparte"
					,"IdPersonaPagoCobro"
					,"FechaIngresoRegistro"
					,"IdUsuario"
					,"IdEmpresa"
					,"IdEstado"
			  FROM :v_CargoAbonoMovimiento;
			
			--2) Actualizar saldos

			v_TabCuentaInversion = SELECT "IdCuentaInversion" FROM :v_CargoAbonoMovimiento WHERE "IdEstado" = 'CRT' GROUP BY "IdCuentaInversion";			
			
			CALL "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbono" (
				p_IdEmpresa			=> :p_IdEmpresa,
			    p_IdUsuario			=> :p_IdUsuario,
				p_FechaMovimiento	=> :p_FechaSistema,
				p_CuentaInversion	=> :v_TabCuentaInversion
			);			
		END IF;

		
		--4) Retornar informacion de salida
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado"
		 SELECT "NumeroCargoAbono","IdCargoAbonoMovimiento","IdTipoMovimiento"
		   FROM :v_CargoAbonoMovimiento;
		
				
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_SRV_TablaCodigosMensajes"
		SELECT  "IdInterno"
			   ,"NumeroCargoAbono"
			   ,"IdCargoAbonoMovimiento"
			   ,CASE WHEN "IdTipoMovimiento" = 'MOVCAR' AND "IdEstado" = 'CRT' THEN 'UI_CARGO_CREADO'
			  		 WHEN "IdTipoMovimiento" = 'MOVCAR' AND "IdEstado" = 'PEN' THEN 'UI_CARGO_CREADO_PENDIENTE_CERTIFICACION'
			   		 WHEN "IdTipoMovimiento" = 'MOVABN' AND "IdEstado" = 'CRT' THEN 'UI_ABONO_CREADO'
			   		 WHEN "IdTipoMovimiento" = 'MOVABN' AND "IdEstado" = 'PEN' THEN 'UI_ABONO_CREADO_PENDIENTE_CERTIFICACION'
			   		 ELSE '' 
			   	 END as "Codigo"
			   ,'IdCargoAbonoMovimiento' as "CodigoDato1"
			   ,CAST("IdCargoAbonoMovimiento" as NVARCHAR(520)) as "ValorDato1"
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2"
			   ,CASE WHEN "IdEstado" = 'CRT' THEN 'OK' WHEN "IdEstado" = 'PEN' THEN 'ADVE' END as "Resultado"
		  FROM :v_CargoAbonoMovimiento;
		 
		 DELETE FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar";
		  
		SIGNAL SALIDA_CONTROLADA;
		
		select "IdCargoAbonoMovimiento" into v_IdCargoMovimiento from  :v_CargoAbonoMovimiento;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => :v_fechaHoraSistema);
		        
		v_DetalleLog = 	SELECT 'INGRESO' AS "SubRegistro", 
		        		'IdCargoAbonoMovimiento' AS "CampoRegistro", 
		        		'' AS "ValorAntiguo", 
		        		:v_IdCargoMovimiento AS "ValorNuevo" FROM dummy;
		        		

		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'CTA' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'INGRESOCARGOABONO'
					,p_IdRegistro => :v_IdCargoMovimiento
					,p_DetalleLog => :v_DetalleLog  --hacia arriba obligatorios
					,p_NombreProceso => 'BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono'
					,p_EstadoProceso => ''
					,p_Cantidad => 0
					,p_Nemo => '');
		
	END;
END;
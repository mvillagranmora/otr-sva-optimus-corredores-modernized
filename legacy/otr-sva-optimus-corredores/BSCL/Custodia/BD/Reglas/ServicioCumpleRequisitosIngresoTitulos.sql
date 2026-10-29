SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosIngresoTitulos";

CREATE PROCEDURE
-- 
--  Proposito: Validar los requisitos para la generación de comprobantes de tesoreria
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
--     
"BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosIngresoTitulos"(
	-- TABLAS ENTRADA
	--	"BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Entrada_DatosValidar"

	-- TABLAS SALIDA
	--	"BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Salida_MovimientosRech"
	--	"BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Salida_ErroresDetalle"

	
	-- SalidAS para mensajes de errores
	OUT p_out_resultadoRegla    NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
													--ADVE = 	 								  	   
                                                   	--RECH=Alguna de lAS reglAS detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 

	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	
/*
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);
		
		INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Salida_ErroresDetalle"
		SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
		
	END; 
*/	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_TablaCodigosMensajes = SELECT 1 AS "IdInterno", 'DATA_NO_EXISTE' AS "Codigo", '' as "CodigoDato1", '' as "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;		
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_erroroutmsg);
		
		INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosAnticipoSimultanea_Salida_ErroresDetalle" ("Codigo","Descripcion")
		SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	
	BEGIN
		DECLARE v_Obligatorios			NVARCHAR(250);
		DECLARE v_Indicadores			NVARCHAR(250);
		DECLARE v_countTableFormaPago	BIGINT;
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
										DEFAULT SELECT 0 AS "IdInterno", '' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",'' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
										
		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato" NVARCHAR(100), 
											   "ValorDato" NVARCHAR(520), "IdGenerarMovimientoCustodia" BIGINT, "Entidad" NVARCHAR(250))
										DEFAULT SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 
										  0 AS "IdGenerarMovimientoCustodia",'' as "Entidad" FROM dummy WHERE (1=2);
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		
/*			if p_UsaReglasNegocio = 'S' then
			
				IF p_Cantidad <= 0 THEN
						p_out_ErroresDetalle =  select 'CANTIDAD_MENOR_IGUAL_A_CERO' as "Codigo",'La cantidad especificada debe ser mayor a cero.' as "Descripcion" from dummy UNION ALL
												select * from :p_out_ErroresDetalle; 
				END IF;

			
			
				CALL "BSCL.Plataforma.BD.Reglas::InstrumentoExiiste" (p_IdInstrumento, 0, v_MiExiste, v_MensajeRegla );
				IF v_MiExiste = 'N' THEN
					p_out_ErroresDetalle =  select 'INSTRUMENTO_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle; 
				END IF;
				
--			IF p_IdMoneda <> '' then
--				v_IdMoneda := p_IdMoneda;
--				
--				CALL "BSCL.Plataforma.BD.Reglas::MonedaExiiste" (v_IdMoneda, 0, v_MiExiste, v_MensajeRegla);
--				IF v_MiExiste = 'N' THEN
--					p_out_ErroresDetalle =  select 'MONEDA_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
--											select * from :p_out_ErroresDetalle; 
--				END IF;
--			else
--				SELECT top 1 IFNULL("IdMoneda",'') as "IdMoneda" INTO v_IdMoneda from "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
--				WHERE "IdInstrumento" = :p_IdInstrumento;
--			end if;
				
	
				CALL "BSCL.Custodia.BD.Reglas::ConceptoExiiste" (p_IdConcepto, 0,  v_MiExiste, v_MensajeRegla);
				IF v_MiExiste = 'N' THEN
					p_out_ErroresDetalle =  select 'CONCEPTO_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle; 
				END IF;
	
				CALL "BSCL.Plataforma.BD.Reglas::TipoOperacionExiiste" (p_IdTipoOperacion, 0, v_MiExiste, v_MensajeRegla);
				IF v_MiExiste = 'N' THEN
					p_out_ErroresDetalle =  select 'TIPOOPERACION_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle; 
				END IF;
				
				-- Rescata la accion segun el Tipo Operacion --
	
				
--			CALL "BSCL.Plataforma.BD.Reglas::ProductoExiiste" (p_IdProducto, 0, v_MiExiste, v_MensajeRegla );
--			IF v_MiExiste = 'N' THEN
--				p_out_ErroresDetalle =  select 'PRODUCTO_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
--										select * from :p_out_ErroresDetalle; 
--			END IF;
				
	
				 
				IF p_IdBoveda <> '' THEN 
					CALL "BSCL.Plataforma.BD.Reglas::BovedaExiiste" (p_IdBoveda, 0, v_MiExiste, v_MensajeRegla);
					IF v_MiExiste = 'N' THEN
						p_out_ErroresDetalle =  select 'BOVEDA_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
												select * from :p_out_ErroresDetalle; 
					END IF;
				END IF;
				
				IF p_IdCondicionLiquidacion <> '' THEN
					CALL "BSCL.Plataforma.BD.Reglas::CondicionDeLiquidacionExiiste" (p_IdCondicionLiquidacion, 0, v_MiExiste, v_MensajeRegla);
					IF v_MiExiste = 'N' THEN
						p_out_ErroresDetalle =  select 'CONDICIONDELIQUIDACION_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
												select * from :p_out_ErroresDetalle; 
					END IF;
				END IF;
				
				IF p_IdBolsa <> '' THEN
					CALL "BSCL.Plataforma.BD.Reglas::BolsaExiiste" (p_IdBolsa, 0, v_MiExiste, v_MensajeRegla);
					IF v_MiExiste = 'N' THEN
						p_out_ErroresDetalle =  select 'BOLSA_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
												select * from :p_out_ErroresDetalle; 
					END IF;
				END IF;
				 
				 
				IF p_FolioCartera > 0 AND v_IndicadorIngEgr = 'RETIRO' THEN
SELECT IFNULL(MAX("IdCartera"),0) 
	INTO p_IdCartera
    FROM "BSCL.Custodia.BD::CUS.Cartera" Cart
    WHERE
		Cart."IdEmpresa" = p_IdEmpresa AND
		Cart."IdCliente" = p_IdCliente AND
		Cart."IdPortafolio" = p_IdPortafolio AND
		"FolioCartera" = :p_FolioCartera;

--					CALL "BSCL.Custodia.BD.Reglas::CarteraExisteBuscaPorClienteFolio" (p_IdEmpresa, p_IdCliente, p_IdPortafolio, p_FolioCartera, 0, v_IdCarteraBuscado, v_MiExiste, v_MensajeRegla);
					IF v_MiExiste <> 'S' THEN
						p_out_ErroresDetalle =  select 'FOLIOCARTERA_NOEXISTE' as "Codigo",:v_MensajeRegla as "Descripcion" from dummy UNION ALL
							select * from :p_out_ErroresDetalle; 
					END IF;
				END IF;
			
			end if;
*/



		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_Obligatorios := 'IdClienteIngreso, IdPortafolioIngreso, IdClienteRetiro, IdPortafolioRetiro, IdConcepto, IdTipoOperacion, FechaOperacion, FechaLiquidacion, IdBoveda, IdCondicionLiquidacion, IdInstrumento, Nemotecnico, IdTipoInstrumento, IdClasificacionTipoInstrumento, IdMonedaInstrumento, Cantidad, Monto';
		v_Indicadores  := 'Bilateral, GeneraComprobante, AgruparEnComprobante, TitularPago';
		
		v_TablaRequisitos =
		SELECT 	 1 AS "Prioridad"
		       	,CASE 
		       		WHEN COUNT("IdGenerarMovimientoCustodia") < 1 THEN 0 
		       		ELSE 1 
				 END AS "Cumple"
		       	,'NO_VIENEN_MOVIMIENTOS' AS "CodigoError"
		       	,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0 as "IdGenerarMovimientoCustodia"
		       	,'' as "Entidad"
		  FROM "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Entrada_DatosValidar"
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdGenerarMovimientoCustodia","Entidad" FROM :v_TablaRequisitos;
	
	
		v_TablaRequisitos =
		SELECT   1 as "Prioridad"
		       	,CASE 
		       		WHEN 
		       			 (COALESCE("IdClienteIngreso",0) = 0 and COALESCE("IdClienteRetiro",0) = 0)   or
		       			 (COALESCE("IdPortafolioIngreso",0) = 0 and COALESCE("IdPortafolioRetiro",0) = 0) or
		       			 LENGTH("IdConcepto") = 0 or 
		       			 LENGTH("IdTipoOperacion") = 0 or 
		       			 COALESCE("FechaOperacion",'0001-01-01') = '0001-01-01' or 
		       			 COALESCE("FechaLiquidacion",'0001-01-01') = '0001-01-01' or 
		       			 LENGTH("IdBoveda") = 0 or 
		       			 LENGTH("IdCondicionLiquidacion") = 0 or 
		       			 COALESCE("IdInstrumento",0) = 0 or
		       			 LENGTH("Nemotecnico") = 0 or
		       			 LENGTH("IdTipoInstrumento") = 0 or
		       			 LENGTH("IdClasificacionTipoInstrumento") = 0 or
		       			 LENGTH("IdMonedaInstrumento") = 0 or 
		       			 COALESCE("Cantidad",0) = 0 or 
		       			 COALESCE("MontoCompra",0) = 0 
					THEN 0 
		       		ELSE 1 
				 END as "Cumple"
		       	,'PARAMETROS_OBLIGATORIOS' as "CodigoError"
		       	,'CodigoParametros' as "CodigoDato"
		       	,v_Obligatorios as "ValorDato"
		       	,"IdGenerarMovimientoCustodia"
		       	,'IngresoTitulos' as "Entidad"
		  FROM "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Entrada_DatosValidar"
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdGenerarMovimientoCustodia","Entidad" FROM :v_TablaRequisitos;
	
/*
		v_TablaRequisitos =
		SELECT   1 as "Prioridad"
		       	,CASE 
		       		WHEN LENGTH("Bilateral") = 0 or 
		       			 LENGTH("GeneraComprobante") = 0 or 
		       			 LENGTH("AgruparEnComprobante") = 0 or 
		       			 LENGTH("TitularPago") = 0
		       		THEN 0 
		       		ELSE 1 
				 END as "Cumple"
		       	,'INDICADORES_OBLIGATORIOS' as "CodigoError"
		       	,'CodigoParametros' as "CodigoDato"
		       	,v_Indicadores as "ValorDato"
		       	,"NumeroIngresoEgreso"
		       	,'IngresoEgresoGenerar' as "Entidad"
		  FROM :p_IngresoEgresoGenerar
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","NumeroIngresoEgreso","Entidad" FROM :v_TablaRequisitos;
*/
	
		
		--*********************************************************************************************
		--***************************************  NEGOCIO  *******************************************
		--*********************************************************************************************	
		--Validar datos obligatorios
		v_TablaRequisitos =
		SELECT 	 2 as "Prioridad"
				,CASE WHEN COALESCE(TM."IdConcepto",'') = '' THEN 0
				 	  WHEN COALESCE(Mon1."IdMoneda",'') = '' THEN 0
				 	  WHEN COALESCE(Mon2."IdMoneda",'') = '' THEN 0
				 	  WHEN Mon1."IdEstado" = 'INA' THEN 0
				 	  WHEN Mon2."IdEstado" = 'INA' THEN 0
				 	  WHEN COALESCE(cli."IdCliente",0) = 0 THEN 0
				 	  WHEN cli."IdEstado" = 'INA' THEN 0
				 	  WHEN COALESCE(por."IdPortafolio",0) = 0 THEN 0
				 	  WHEN por."IdEstado" = 'INA' THEN 0
				 	  WHEN COALESCE(bov."IdBoveda",'') = '' THEN 0
				 	  WHEN COALESCE(cond."IdCondicionDeLiquidacion",'') = '' THEN 0
				 	  WHEN COALESCE(ins."IdInstrumento",0) = 0 THEN 0
				 	  WHEN IEG."MontoCompra" = 0 THEN 0
				 	  WHEN IEG."Cantidad" = 0 THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN COALESCE(TM."IdConcepto",'') = '' THEN 'CONCEPTO_INVALIDO'
				 	  WHEN COALESCE(Mon1."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'
				 	  WHEN COALESCE(Mon2."IdMoneda",'') = '' THEN 'MONEDA_NO_EXISTE'
				 	  WHEN Mon1."IdEstado" = 'INA' THEN 'MONEDA_NO_ACTIVA'
				 	  WHEN Mon2."IdEstado" = 'INA' THEN 'MONEDA_NO_ACTIVA'
				 	  WHEN COALESCE(cli."IdCliente",0) = 0 THEN 'CLIENTE_NO_EXISTE'
				 	  WHEN cli."IdEstado" = 'INA' THEN 'CLIENTE_NO_ACTIVO'
				 	  WHEN COALESCE(por."IdPortafolio",0) = 0 THEN 'PORTAFOLIO_NO_EXISTE'
				 	  WHEN por."IdEstado" = 'INA' THEN 'PORTAFOLIO_NO_ACTIVO'
				 	  WHEN COALESCE(bov."IdBoveda",'') = ''THEN 'BOVEDA_NO_EXISTE'
				 	  WHEN COALESCE(cond."IdCondicionDeLiquidacion",'') = '' THEN 'CONDICION_DE_LIQUIDACION_NO_EXISTE'
				 	  WHEN COALESCE(ins."IdInstrumento",0) = 0 THEN 'INSTRUMENTO_NO_EXISTE'
				 	  WHEN IEG."MontoCompra" = 0 THEN 'MONTO_MENOR_IGUAL_A_CERO'
				 	  WHEN IEG."Cantidad" = 0 THEN 'CANTIDAD_MENOR_IGUAL_A_CERO'
					  ELSE  ''
				 END AS "CodigoError"
		       	,'' as "CodigoDato"
		       	,'' as "ValorDato"
		       	,IEG."IdGenerarMovimientoCustodia"
		       	,'IngresoTitulos' as "Entidad"
		  FROM "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Entrada_DatosValidar" as IEG
		  LEFT JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia" as TM ON TM."IdConcepto" = IEG."IdConcepto" and TM."IdTipoOperacion" = IEG."IdTipoOperacion"
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON Mon1."IdMoneda" = IEG."IdMonedaInstrumento"
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON Mon2."IdMoneda" = IEG."IdMonedaSistema"
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as cli ON cli."IdCliente" = case when coalesce(IEG."IdClienteRetiro",0) > 0 then  IEG."IdClienteRetiro" else IEG."IdClienteIngreso" end
		  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as por ON por."IdPortafolio" = case when coalesce(IEG."IdPortafolioRetiro",0) > 0 then IEG."IdPortafolioRetiro" else IEG."IdPortafolioIngreso" end
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda" as bov ON bov."IdBoveda" = IEG."IdBoveda"
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" as cond ON 
		  						cond."IdCondicionDeLiquidacion" = IEG."IdCondicionLiquidacion" and 
		  						cond."IdTipoOperacion" = IEG."IdTipoOperacion"
		  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as ins ON ins."IdInstrumento" = IEG."IdInstrumento"
		  
		  

		  

		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdGenerarMovimientoCustodia","Entidad" FROM :v_TablaRequisitos;	

		

/*	
		
		--validar los parametros referenciales
		v_TablaRequisitos =
		SELECT   2 as "Prioridad"
				,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN 0
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN 0
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN 0
					  ELSE  1
				 END AS "Cumple"
				,CASE WHEN (LENGTH(IEG."IdTipoProducto") > 0 AND COALESCE(TPr."IdTipoProducto",'') = '') THEN 'TIPO_PRODUCTO_NO_EXISTE'
					  WHEN (LENGTH(IEG."IdTipoOperacion") > 0 AND COALESCE(TOpe."IdTipoOperacion",'') = '') THEN 'TIPO_OPERACION_NO_EXISTE'
					  WHEN (LENGTH(IEG."IdCondicionLiquidacion") > 0 AND COALESCE(Cond."IdCondicionDeLiquidacion",'') = '') THEN 'CONDICION_DE_LIQUIDACION_NO_EXISTE'
					  ELSE  ''
				 END AS "CodigoError"
				,'' as "CodigoDato"
		       	,'' as "ValorDato"
		       	,IEG."NumeroIngresoEgreso"
		       	,'IngresoEgresoGenerar' as "Entidad"
		  FROM :p_IngresoEgresoGenerar as IEG
		  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TPr ON TPr."IdTipoProducto" = IEG."IdTipoProducto"
		  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TOpe ON TOpe."IdTipoOperacion" = IEG."IdTipoOperacion"
		  LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as Cond ON Cond."IdCondicionDeLiquidacion" = IEG."IdCondicionLiquidacion"
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","NumeroIngresoEgreso","Entidad" FROM :v_TablaRequisitos;
	*/



		--*********************************************************************************************
		--********************************* ANALISIS DE DATOS *****************************************
		--*********************************************************************************************

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	    	   
	    INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Salida_MovimientosRech"
	    SELECT DISTINCT "IdGenerarMovimientoCustodia"
	      FROM :v_TablaRequisitos
	     WHERE "Prioridad" > 1
	       AND "Cumple" = 0;
	    		
	    v_TablaCodigosMensajes =
		SELECT 1 AS "IdInterno"	
			   ,"CodigoError" AS "Codigo"
			   ,"CodigoDato" AS "CodigoDato1"
			   ,"ValorDato" AS "ValorDato1" 
			   ,'' as "CodigoDato2"
			   ,'' as "ValorDato2"
		  FROM :v_TablaRequisitos
		 WHERE "Cumple" = 0
		 ORDER By "Prioridad" DESC;
		 
		 CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalle);
		 
		
		INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosIngresoTitulos_Salida_ErroresDetalle"
		SELECT ED."Codigo",
			   ED."Descripcion"
		  FROM :v_out_ErroresDetalle as ED
		 INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		 WHERE "Cumple" = 0;
    END;	
END;
SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCronologica";
GO
CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 14/03/2016
--  
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCronologica" (
	-- Parametros entrada comunes en todos los servicios privados
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	-- Parametros entrada del servicio privado
	IN	p_IdTipo			NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion REFACTURACIONuracion REASIGNACIONnacion (REASIGNACION,REFACTURACION))
	IN	p_Origen					NVARCHAR(40),--origen de la llamamda a REFACTURACIONurar
	IN  p_Transacciones				TABLE ("IdMovimiento" 	BIGINT --movimiento que origino la asignacion por REFACTURACIONuracion comp, movtocomp,asig,transac
										, "IdTransaccion"	BIGINT
										, "Cantidad" DECIMAL (26,8)
										, "Monto" DECIMAL (26,8)
										, "ValorRescate" DECIMAL (26,8)),
	IN  p_Ordenes					TABLE ("IdOrden"		BIGINT),
	-- Parametros salida del servicio privado
	OUT p_out_SimAsignacion			TABLE (	"IdInterno"				BIGINT
											,"IdMovimiento"			BIGINT
											,"FechaSimulacion"		TIMESTAMP
											,"FechaIngresoRegistro"	TIMESTAMP
											,"IdTransaccion"		BIGINT
											,"FolioTransaccion"		NVARCHAR(40)
											,"FechaTransaccion"		DATE
											,"FolioCartera"			BIGINT
											,"IdPortafolio"			BIGINT
											,"CodigoPortafolio"		NVARCHAR(40)
											,"IdCliente"			BIGINT
											,"IdLibroOrdenes"		BIGINT
											,"IdInstrumento"		BIGINT
											,"Instrumento"			NVARCHAR(40)
											,"IdTipoInstrumento"	NVARCHAR(3)
											,"IdMonedaInstrumento"	NVARCHAR(3)
											,"IdOrden"				BIGINT
											,"NombreCompleto"		NVARCHAR(100)
											,"DocumentoIdentidad"	NVARCHAR(40)
											,"CantidadAsignacion"	DECIMAL(26,8)
											,"ValorRescateAsignacion"DECIMAL(26,8)
											,"PorcentajeAsignacion"	DECIMAL(26,8)
											,"PrecioSimulacion"		DECIMAL(26,8)
											
											,"IdTipoProducto"		NVARCHAR(4)
											,"IdTipoOperacion"		NVARCHAR(6)
											,"IdTipoCanalIngreso"	NVARCHAR(3)
											,"IdBolsa"	NVARCHAR(3))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
		
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE	v_FechaSistemaCreacion TIMESTAMP;
		DECLARE v_countTransacciones, v_countTranBloq, v_countTranRes,	   v_countOrdenes, v_countOrdBloq, v_countOrdRes,
				v_countPortafolios,   v_CountTipoProg, v_TotalPortafolios, v_countTransaccionesOrden, v_cantReg, v_i, v_CantTotalOrden BIGINT := 0;
				
		DECLARE v_Id, v_IdPortafolio, v_IdGrupo, v_IdTransaccion, v_IdOrden, v_CantidadDisponibleOrden, v_CantidadDisponibleTransaccion,
				v_CantidadAsignar,    v_CantidadAsignada, v_IdInstrumento, v_CantidadAsignarAux BIGINT := 0;
				
		DECLARE v_IdTipoOperacion		NVARCHAR(6);
		DECLARE v_asignacionCronologicaObligatoria    NVARCHAR(520);
		
		CREATE LOCAL TEMPORARY TABLE #infoListaAsignar (
			"Id"									BIGINT,
			"IdMovimiento"							BIGINT,
			"IdOrden"								BIGINT,
			"IdBolsaOrden"							NVARCHAR(3),
			"IdTipoOperacionOrden"					NVARCHAR(6),	
			"IdCondicionDeLiquidacionOrden"			NVARCHAR(2),
			"PrecioOrden"							DECIMAL(26,8),	
			"CantidadAsignadaOrden"					DECIMAL(26,8),	
			"FechaOperacionOrden"					TIMESTAMP,		
			"IdLibroOrdenesOrden"					BIGINT,
			"IdTipoPrecioOrden"						NVARCHAR(1),	
			"IdInstrumentoOrden"					BIGINT,
			"InstrumentoOrden"						NVARCHAR(40),	
			"IdTipoInstrumentoOrden"				NVARCHAR(3),
			"IdMonedaInstrumentoOrden"				NVARCHAR(3),	
			"IdPortafolio"							BIGINT,			
			"CodigoPortafolio"						NVARCHAR(40),
			"IdCliente"								BIGINT,			
			"NombreCompleto"						NVARCHAR(100),
			"DocumentoIdentidad"					NVARCHAR(40),
			"IdTransaccion"							BIGINT,			
			"FolioTransaccion"						NVARCHAR(40),
			"FechaTransaccion"						DATE,
			"IdInstrumentoTransaccion"				BIGINT,			
			"IdTipoOperacionTransaccion"			NVARCHAR(6),	
			"PrecioTransaccion"						DECIMAL(26,8),
			"IdCondicionDeLiquidacionTransaccion"	NVARCHAR(2),
			"FechaOperacionTransaccion"				TIMESTAMP,		
			"CantidadDisponibleOrden"				BIGINT,
			"CantidadDisponibleTransaccion"			BIGINT,			
			"CantidadAsignar"						BIGINT,
			"CantidadAsignada" 						DECIMAL(26,8),	
			"PrecioAsignacion"						DECIMAL(26,8),
			"IdTipoProducto" 						NVARCHAR(4),	
			"IdTipoCanalIngreso"					NVARCHAR(3)
			);
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		-- obtener variable que determina si se permite asignar transacciones independiente de su cronologia. http://jira.bcs.cl/browse/OPTIMUSPE-386
		SELECT "Valor" INTO v_asignacionCronologicaObligatoria
		FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" 
		WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'AsignacionCronologicaObligatoria';

		IF :v_asignacionCronologicaObligatoria IS NULL THEN
		  v_asignacionCronologicaObligatoria := 'S';
		END IF;
		---------- ALGORITMO DE ASIGNACION ----------

			---------- Consulta las Transacciones ----------
			v_infoTransaciones = SELECT p_TRA."IdMovimiento",
									tra."IdEmpresa",			 	tra."IdTransaccion",				tra."FolioTransaccion", 
									tra."FechaSistemaCreacion",		tra."HoraSistemaCreacion",			tra."FechaOperacion",
									tra."FechaOperacionTransaccion",tra."FechaLiquidacion",
									tra."IdEmisor",				 	tra."EsVentaCorta",					tra."FechaVencimiento",
									tra."IdSistemaBursatil",	 	tra."IndicadorOrdenDirecta",		tra."EsCuentaPropia",
									tra."EsFungible",			 	tra."EsAPlazo", 
									tra."CodigoOperadorContraparte", tra."CodigoOperadorCorredor",			tra."CantidadTransaccion",
									tra."MontoTransaccion",		 	tra."PrecioTransaccion",			tra."Tasa",
									tra."Plazo",				 	tra."IdInstrumento",				tra."IdTipoReajuste",
									tra."Instrumento",			 	tra."IdMonedaInstrumento",			tra."IdInstrumentoSubyacente",
									tra."InstrumentoSubyacente",	tra."IdTipoInstrumentoSubyacente",	tra."NombreTipoInstrumentoSubyacente",
									tra."IdClasificacionTipoInstrumentoSubyacente",	tra."NombreClasificacionTipoInstrumentoSubyacente",
									tra."IdMonedaInstrumentoSubyacente",			tra."IdTipoInstrumento",
									tra."NombreTipoInstrumento",					tra."IdClasificacionTipoInstrumento",
									tra."NombreClasificacionTipoInstrumento",		tra."ValorRescateTransaccion",
									tra."IdEstadoTransaccion",						tra."EstadoTransaccion", 
									tra."IdTransaccionPlazo",		tra."IdTransaccionOd",				tra."IdEmpleado",
									tra."IdTipoOperacion", 			tra."NombreTipoOperacion",			tra."AccionOrden",
									tra."IdTipoProducto", 			tra."IdCondicionDeLiquidacion",		tra."NombreCondicionDeLiquidacion",	
									tra."FechaParidad",					tra."ValorParidad",
									tra."FechaTipoCambio",			tra."ValorTipoCambio",				tra."IdCorredorContraparte",
									tra."IdUsuarioReserva",			tra."FechaIngresoOferta",			tra."IdBolsa",
									tra."IdLibroOrdenes", 			tra."NombreBolsa"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" ELSE tra."CantidadDisponibleTransaccion" END AS "CantidadDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."CantidadAsignadaTransaccion" END AS "CantidadAsignadaTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" ELSE tra."MontoDisponibleTransaccion" END AS "MontoDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."MontoAsignadoTransaccion" END AS "MontoAsignadoTransaccion"
									,tra."CorteMinimo"
								 FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS tra
								 INNER JOIN :p_Transacciones p_TRA ON (tra."IdTransaccion" = p_TRA."IdTransaccion")
								 ORDER BY tra."FechaOperacionTransaccion" asc;
			
			--select 'v_infoTransaciones',* from :v_infoTransaciones;
			
			---------- Consulta las Ordenes ----------
			v_infoOrdenes =	SELECT
								ord."IdOrden",								ord."FechaRecepcion",			ord."FechaIngresoRegistro", 
								ord."FechaIngresoRegistroCorta",			ord."FechaSolicitudAnulacion",	ord."FechaAnulacion", 
								ord."FechaOperacionOrden",					ord."FechaVigencia",			ord."FechaVencimiento", 
								ord."FechaLiquidacion",			ord."FechaAceptacion", 
								ord."FechaAutorizacion",					ord."IdTipoVigenciaOrden",		ord."GlosaOrden", 
								ord."IdEstadoOrden",						ord."EstadoOrden",				ord."CantidadOrden", 
								ord."IdFormaCustodia", 
								ord."PorcentajeVisibilidad",				ord."ValorRescate",				ord."IdTipoReajuste",
								ord."PrecioOrden",							ord."PrecioMercado",			ord."Tasa", 
								ord."Plazo",					ord."EsVentaCorta",
								ord."IndicadorFacturacionInmediata",		ord."IdTipoPrecio",				ord."TipoPrecio", 
								ord."ValorCaptacion",			ord."IdCondicionDeLiquidacion", 
								ord."NombreCondicionDeLiquidacion",			ord."IdTipoOperacion",			ord."IdCliente", 
								ord."RazonSocial",							ord."NombreCompleto",			ord."DocumentoIdentidad", 
								ord."IdPortafolio",					ord."CodigoPortafolio",			ord."IdGrupoCobro", 
								ord."IdTipoCanalIngreso",					ord."NombreCanal",				ord."IdLibroOrdenes", 
								ord."IdEmisor",								ord."IdEmpresa",				ord."IdBolsa", 
								ord."NombreBolsa",							ord."IdInstrumento",			ord."Instrumento", 
								ord."IdTipoInstrumento",					ord."NombreTipoInstrumento",
								ord."IdClasificacionTipoInstrumento",		ord."NombreClasificacionTipoInstrumento",
								ord."IdInstrumentoSubyacente",				ord."InstrumentoSubyacente",
								ord."IdTipoInstrumentoSubyacente",			ord."NombreTipoInstrumentoSubyacente",
								ord."IdClasificacionTipoInstrumentoSubyacente", ord."NombreClasificacionTipoInstrumentoSubyacente", 
								ord."IdEstadoAsignacion",
								ord."EstadoOrdenAsignacion",				ord."Tamano",					ord."IdMonedaInstrumento",
								ord."IdMonedaOrden",						ord."IdMonedaLiquidacionOrden",
								ord."NombreTipoOperacion",					ord."AccionOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN (ord."CantidadDisponibleOrden" + ord."CantidadAsignadaOrden") ELSE ord."CantidadDisponibleOrden" END AS "CantidadDisponibleOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE ord."CantidadAsignadaOrden" END AS "CantidadAsignadaOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN (ord."MontoDisponibleOrden" + ord."MontoAsignadoOrden") ELSE ord."MontoDisponibleOrden" END AS "MontoDisponibleOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE ord."MontoAsignadoOrden" END AS "MontoAsignadoOrden"
								,ord."SecOrdenMigracion",					ord."TipoCambio"
							FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS ord
							INNER JOIN :p_Ordenes  AS p_ord ON (ord."IdOrden" = p_ord."IdOrden")
							ORDER BY "FechaOperacionOrden" asc;
			
			--select * from :v_infoTransaciones;
			--select 'v_infoOrdenes',* from :v_infoOrdenes;



			
			---------- Consulta las Transacciones-Ordenes ----------
			v_infoTranOrd =	SELECT tran."IdMovimiento"
								 , ord."IdOrden"							as "IdOrden"
								 , ord."IdBolsa"							as "IdBolsaOrden"
								 , ord."IdTipoOperacion"					as "IdTipoOperacionOrden"
								 , ord."IdCondicionDeLiquidacion"			as "IdCondicionDeLiquidacionOrden"
								 , ord."PrecioOrden"						as "PrecioOrden"
								 , ord."CantidadAsignadaOrden"				as "CantidadAsignadaOrden"
								 , ord."FechaOperacionOrden"				as "FechaOperacionOrden"
								 , ord."IdLibroOrdenes"						as "IdLibroOrdenesOrden"
								 , ord."IdTipoPrecio"						as "IdTipoPrecioOrden"
								 , ord."IdInstrumento" 						as "IdInstrumentoOrden"
								 , ord."Instrumento"						as "InstrumentoOrden"
								 , ord."IdTipoInstrumento"					as "IdTipoInstrumentoOrden"
								 , ord."IdMonedaInstrumento"				as "IdMonedaInstrumentoOrden"
								 , ord."IdPortafolio"				as "IdPortafolio"
								 , ord."CodigoPortafolio"					as "CodigoPortafolio"
								 , ord."NombreCompleto"						as "NombreCompleto"
								 , ord."DocumentoIdentidad"					as "DocumentoIdentidad"
								 , ord."IdCliente"							as "IdCliente"
								 , tran."IdTransaccion"						as "IdTransaccion"
								 , tran."FolioTransaccion"					as "FolioTransaccion"
								 , tran."FechaSistemaCreacion"				as "FechaTransaccion"
								 , tran."IdInstrumento"						as "IdInstrumentoTransaccion"
								 , tran."IdTipoOperacion"					as "IdTipoOperacionTransaccion"
								 , tran."PrecioTransaccion"					as "PrecioTransaccion"
								 , tran."IdCondicionDeLiquidacion"			as "IdCondicionDeLiquidacionTransaccion"
								 , tran."FechaOperacionTransaccion"			as "FechaOperacionTransaccion"
								 , ord."CantidadDisponibleOrden"			as "CantidadDisponibleOrden"
								 , tran."CantidadDisponibleTransaccion"		as "CantidadDisponibleTransaccion"
								 , 0 										as "CantidadAsignar"
								 , tran."PrecioTransaccion"					as "PrecioAsignacion"
								 , 0										as "CantidadAsignada"
								 , tran."IdTipoProducto"
								 , ord."IdTipoCanalIngreso"
							FROM :v_infoTransaciones AS tran 
							INNER JOIN :v_infoOrdenes AS ord ON (
								tran."IdClasificacionTipoInstrumento"	= ord."IdClasificacionTipoInstrumento" and
								tran."IdLibroOrdenes" = ord."IdLibroOrdenes" and
								tran."IdTipoInstrumento" 				= ord."IdTipoInstrumento" and
								tran."IdInstrumento"					= ord."IdInstrumento" and
								(tran."IdBolsa"							= ord."IdBolsa" OR ord."IdBolsa" = '0') and
								tran."IdTipoOperacion"					= ord."IdTipoOperacion" and
								tran."IdCondicionDeLiquidacion"			= ord."IdCondicionDeLiquidacion" and
								tran."EsVentaCorta"						= ord."EsVentaCorta" and
								tran."FechaOperacionTransaccion"		>= CASE WHEN v_asignacionCronologicaObligatoria = 'S' THEN ord."FechaOperacionOrden" ELSE tran."FechaOperacionTransaccion" END and
								((tran."IdTipoOperacion"  = 'COMSIM' and /*tran."MontoDisponibleTransaccion"	<= ord."MontoDisponibleOrden" and */ tran."Tasa" >= ord."Tasa" and tran."Plazo" <= ord."Plazo")
								 or (tran."IdTipoOperacion"  = 'VENSIM' and tran."CantidadDisponibleTransaccion" = ord."CantidadDisponibleOrden" and tran."Tasa" <= ord."Tasa" and tran."Plazo" >= ord."Plazo" and tran."IdInstrumentoSubyacente" = ord."IdInstrumentoSubyacente")
								 or (tran."IdTipoOperacion" != 'COMSIM' and tran."IdTipoOperacion" != 'VENSIM' and 
								 		(ord."IdTipoPrecio" <> 'L'
								 			or (ord."IdTipoPrecio" = 'L' and substring(ord."IdTipoOperacion",0,3) = 'COM' and ord."PrecioOrden" >= tran."PrecioTransaccion")
								 			or (ord."IdTipoPrecio" = 'L' and substring(ord."IdTipoOperacion",0,3) = 'VEN' and ord."PrecioOrden" <= tran."PrecioTransaccion")
								 		)
								 	)
								));
			--select 'A',*,"IdTransaccion","IdOrden" from :v_infoTranOrd;
			--select 'v_infoTranOrd',* from :v_infoTranOrd;
			
			---------- Consulta la cantidad de Ordenes ----------
			--v_cantOrdenes = SELECT "IdOrden","CantidadDisponibleOrden" FROM :v_infoTranOrd GROUP BY "IdOrden", "CantidadDisponibleOrden";
			v_cantOrdenes = SELECT "IdTransaccion", SUM("CantidadDisponibleOrden") AS "CantTotalOrden" FROM :v_infoTranOrd GROUP BY "IdTransaccion";
			
			--SELECT SUM("CantidadDisponibleOrden") INTO v_CantTotalOrden FROM :v_cantOrdenes;
			
			--select 'B',* from :v_cantOrdenes;
			--select v_CantTotalOrden as "v_CantTotalOrden" from dummy;
			
			
			---------- Genera lista a Asignar ----------
			v_infoListaAsignar = SELECT "IdMovimiento",
									"IdOrden",					  
									"IdBolsaOrden",
									"IdTipoOperacionOrden",		 
									"IdCondicionDeLiquidacionOrden",		 
									"PrecioOrden",
									"CantidadAsignadaOrden",
									"FechaOperacionOrden",		  
									"IdLibroOrdenesOrden",				
									"IdTipoPrecioOrden",
									"IdInstrumentoOrden",		  
									"InstrumentoOrden",					 
									"IdTipoInstrumentoOrden",
									"IdMonedaInstrumentoOrden",	  
									"IdPortafolio",
									"CodigoPortafolio",			  
									"IdCliente",							 
									"NombreCompleto",
									"DocumentoIdentidad",
									ord."IdTransaccion",		  
									"FolioTransaccion",
									"FechaTransaccion",
									"IdInstrumentoTransaccion",
									"IdTipoOperacionTransaccion",
									"PrecioTransaccion",
									"IdCondicionDeLiquidacionTransaccion", 
									"FechaOperacionTransaccion",
									"CantidadDisponibleOrden",	  
									"CantidadDisponibleTransaccion",
									--cantidad = (long) (cantOrden * (cantTran/cantTotalOrd));
									(case 
										when "CantTotalOrden" > 0 then --when v_CantTotalOrden > 0 then
											(case
												when "CantidadDisponibleTransaccion" >= "CantidadDisponibleOrden" then "CantidadDisponibleOrden"
												else "CantidadDisponibleTransaccion"
											end)
										else 0
									end) AS "CantidadAsignar", 0 AS "CantidadAsignada", "PrecioAsignacion","IdTipoProducto",ord."IdTipoCanalIngreso"
								 FROM :v_infoTranOrd AS ord INNER JOIN :v_cantOrdenes AS cant ON (ord."IdTransaccion" = cant."IdTransaccion")
								 ORDER BY "FechaOperacionTransaccion","FechaOperacionOrden";
			
			
			---------- Se llena tabla temporal infoListaAsignar. Agregar Id para cada registro----------
			INSERT INTO #infoListaAsignar
			SELECT row_number() OVER (order by "FechaOperacionTransaccion","FechaOperacionOrden") as "Id",* FROM :v_infoListaAsignar;
			select 'v_infoListaAsignar', * from #infoListaAsignar;
			
			
			---------- Cantidad de registros de tabla temporal infoListaAsignar ----------
			SELECT COUNT(*) INTO v_cantReg FROM #infoListaAsignar;
			
			
			---------- Update a tabla temporal infoListaAsignar ----------
			FOR v_i IN 1 .. v_cantReg DO
				SELECT "CantidadAsignar" INTO v_CantidadAsignar FROM #infoListaAsignar WHERE "Id" = v_i;
				
				IF (v_CantidadAsignar > 0) THEN
					SELECT (case
								when "CantidadDisponibleOrden" <= "CantidadDisponibleTransaccion" and "CantidadDisponibleOrden" <= "CantidadAsignar" then
									 "CantidadDisponibleOrden"
								when "CantidadDisponibleTransaccion" <= "CantidadDisponibleOrden" and "CantidadDisponibleTransaccion" <= "CantidadAsignar" then
									 "CantidadDisponibleTransaccion"
								else "CantidadAsignar"
							end) INTO v_CantidadAsignarAux
					FROM #infoListaAsignar
					WHERE "Id" = v_i;
					
					SELECT "Id"
 						,"IdPortafolio"
 						,"IdOrden"
						,"IdTransaccion"
						,"IdInstrumentoTransaccion"
						,"IdTipoOperacionTransaccion"
						,"CantidadDisponibleOrden"		- v_CantidadAsignarAux AS "CantidadDisponibleOrden"
						,"CantidadDisponibleTransaccion"- v_CantidadAsignarAux AS "CantidadDisponibleTransaccion"
						,"CantidadAsignar"				- v_CantidadAsignarAux AS "CantidadAsignar"
						,v_CantidadAsignarAux 								   AS "CantidadAsignada"
					INTO v_Id, v_IdPortafolio, v_IdOrden, v_IdTransaccion, v_IdInstrumento , v_IdTipoOperacion, v_CantidadDisponibleOrden, v_CantidadDisponibleTransaccion, v_CantidadAsignar, v_CantidadAsignada
					FROM #infoListaAsignar
					WHERE "Id" = v_i;
					
					--select v_Id,v_IdPortafolio,v_IdOrden,v_IdTransaccion,v_CantidadDisponibleOrden,v_CantidadDisponibleTransaccion,v_CantidadAsignar,v_CantidadAsignada from dummy;
					
					UPDATE #infoListaAsignar
					SET "CantidadDisponibleOrden"		= "CantidadDisponibleOrden"		  - v_CantidadAsignarAux,
						"CantidadDisponibleTransaccion" = "CantidadDisponibleTransaccion" - v_CantidadAsignarAux,
						"CantidadAsignar"				= "CantidadAsignar"				  - v_CantidadAsignarAux,
						"CantidadAsignada"				= v_CantidadAsignarAux
					WHERE "Id" = v_i;
					
					
					--transaccion relacionadas
					UPDATE #infoListaAsignar
					SET "CantidadDisponibleOrden" = "CantidadDisponibleOrden" - v_CantidadAsignada
						--,"MontoDisponibleTransaccion"
						/*,"CantidadAsignar" = (case
												when "IdPortafolio" = v_IdPortafolio then "CantidadAsignar" - v_CantidadAsignada
												else "CantidadAsignar" end )*/
					WHERE "IdOrden" = v_IdOrden
					  AND "Id" > v_i;
					
					
					UPDATE #infoListaAsignar
					SET "CantidadDisponibleTransaccion" = (case
															when "IdTransaccion" = v_IdTransaccion then "CantidadDisponibleTransaccion" - v_CantidadAsignada
															else "CantidadDisponibleTransaccion" end)
						--,"MontoDisponibleTransaccion"
						/*,"CantidadAsignar" = (case
												when "IdPortafolio" = v_IdPortafolio then "CantidadAsignar" - v_CantidadAsignada
												else "CantidadAsignar" end )*/
					WHERE "IdTransaccion" = v_IdTransaccion
					  AND "Id" > v_i;
					
				END IF;
				--select *,v_i from #infoListaAsignar;
			END FOR;
			
			p_out_SimAsignacion = SELECT "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL as "IdInterno" 
										,"IdMovimiento"				as "IdMovimiento"
										,v_FechaSistemaCreacion 	as "FechaSimulacion"
										,CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro"
										,"IdTransaccion"			as "IdTransaccion"
										,"FolioTransaccion"			as "FolioTransaccion"
										,"FechaTransaccion"			as "FechaTransaccion"
										,0							as "FolioCartera"
										,"IdPortafolio"				as "IdPortafolio"
										,"CodigoPortafolio"			as "CodigoPortafolio"
										,"IdCliente"				as "IdCliente"
										,"IdLibroOrdenesOrden"		as "IdLibroOrdenes"
										,"IdInstrumentoOrden"		as "IdInstrumento"
										,"InstrumentoOrden"			as "Instrumento"
										,"IdTipoInstrumentoOrden"	as "IdTipoInstrumento"
										,"IdMonedaInstrumentoOrden"	as "IdMonedaInstrumento"
										,"IdOrden"					as "IdOrden"
										,"NombreCompleto"			as "NombreCompleto"
										,"DocumentoIdentidad"		as "DocumentoIdentidad"
										,"CantidadAsignada"			as "CantidadAsignacion"
										,cast(0 as DECIMAL(26,8))	as "ValorRescateAsignacion"
										,cast(0 as DECIMAL(26,8))	as "PorcentajeAsignacion"
										,"PrecioAsignacion"			as "PrecioSimulacion"
										
										,"IdTipoProducto"
										,"IdTipoOperacionOrden" as "IdTipoOperacion"
										,"IdTipoCanalIngreso"
										,"IdBolsaOrden" as "IdBolsa"
										
								  FROM #infoListaAsignar 
								  WHERE "CantidadAsignada" > 0
								  ORDER BY "Id";
			
			DROP TABLE #infoListaAsignar;
END;
GO

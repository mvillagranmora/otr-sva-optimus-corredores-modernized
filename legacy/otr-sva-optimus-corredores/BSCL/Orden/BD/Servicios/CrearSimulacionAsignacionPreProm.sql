SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPreProm";
GO
CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: PROJO - BCS
--  Fecha Creacion: 08/02/2015
--  FAlta calculo Pre PROm
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPreProm" (
	-- Parametros entrada comunes en todos los servicios privados
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	-- Parametros entrada del servicio privado
	IN	p_IdTipo			NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion refacturacion (REFACT))
	IN	p_Origen					NVARCHAR(40),--origen de la llamamda a refacturar
	IN  p_Transacciones				TABLE ("IdMovimiento" 	BIGINT --movimiento que origino la asignacion por refacturacion comp, movtocomp,asig,transac
										, "IdTransaccion"	BIGINT
										, "Cantidad" DECIMAL (26,8)
										, "Monto" DECIMAL (26,8)
										, "ValorRescate" DECIMAL (26,8)),
	IN  p_Portafolios				TABLE ("IdPortafolio"	BIGINT, "Cantidad" DECIMAL (26,8), "Porcentaje" DECIMAL (26,8)),

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
											,"CodigoPortafolio"		NVARCHAR(100)
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
											,"IdBolsa"				NVARCHAR(3))
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
		DECLARE v_cantReg,v_i	BIGINT := 0;
		DECLARE v_CantidadAsignar,v_Id,v_IdPortafolio,v_IdGrupo,v_IdTransaccion
				,v_CantidadDisponibleGrupo,v_CantidadDisponibleTransaccion
				,v_CantidadAsignada,v_IdInstrumento	BIGINT := 0;
		DECLARE v_IdTipoOperacion	NVARCHAR(6);
		DECLARE v_IdMonedaInstrumento NVARCHAR(3);
		DECLARE v_PrecioPromedio DECIMAL(26,8);
		CREATE LOCAL TEMPORARY TABLE #infoListaAsignar ("Id"						BIGINT
												,"IdMovimiento"						BIGINT
												,"IdPortafolio"						BIGINT
												,"CodigoPortafolio"					NVARCHAR(40)
												,"IdCliente"						BIGINT
												,"Cantidad"							BIGINT
												,"Porcentaje"						DECIMAL(26,8)
												,"IdGrupo"							BIGINT
												,"IdInstrumento"					BIGINT
												,"IdTipoOperacion"					NVARCHAR(6)
												,"IdMonedaInstrumento"				NVARCHAR(3)
												,"PrecioTransaccion"				DECIMAL(26,8)
												,"Tasa"								DECIMAL(26,8)
												,"IdCondicionDeLiquidacion"			NVARCHAR(2)
												,"CantTotalDisponible"				BIGINT
												,"FechaOperacion"					TIMESTAMP
												,"NombreCompleto"					NVARCHAR(100)
												,"DocumentoIdentidad"				NVARCHAR(40)
												--,"IdGrupo"
												,"Instrumento"						NVARCHAR(40)
												,"IdTipoInstrumento"				NVARCHAR(3)
												,"IdInstrumentoGrupo"				BIGINT
												,"FechaOperacionTransaccionGrupo"	TIMESTAMP
												,"IdTipoOperacionGrupo"				NVARCHAR(6)
												,"IdMonedaInstrumentoGrupo"			NVARCHAR(3)
												,"PrecioTransaccionGrupo"			DECIMAL(26,8)
												,"TasaGrupo"						DECIMAL(26,8)
												,"IdCondicionDeLiquidacionGrupo"	NVARCHAR(2)
												,"IdInstrumentoTransaccion"			BIGINT
												,"IdTipoOperacionTransaccion"		NVARCHAR(6)
												,"IdMonedaInstrumentoTransaccion"	NVARCHAR(3)
												,"PrecioTransaccionTransaccion"		DECIMAL(26,8)
												,"TasaTransaccion"					DECIMAL(26,8)
												,"IdCondicionDeLiquidacionTransaccion"NVARCHAR(2)
												,"IdTransaccion"					BIGINT
												,"FolioTransaccion"					NVARCHAR(40)
												,"FechaTransaccion"					DATE
												,"IdLibroOrdenesTransaccion"		BIGINT
												,"CantidadDisponibleGrupo"			DECIMAL(26,8)
												,"CantidadDisponibleTransaccion"	DECIMAL(26,8)
												--,"MontoDisponibleTransaccion"
												,"CantidadAsignar"					DECIMAL(26,8)	 
												,"CantidadAsignada"					DECIMAL(26,8)
												,"PrecioPromedio"					DECIMAL(26,8)
												,"IdTipoProducto"					NVARCHAR(4)
												,"IdTipoCanalIngreso"				NVARCHAR(3)
												,"IdBolsa"							NVARCHAR(3)
												);
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		--Algoritmo de asignacion
			
			--Consulto las transacciones
			v_infoTransaciones = 	SELECT 	p_TRA."IdMovimiento"
										, tra."IdEmpresa"
										, tra."IdTransaccion"
										, tra."FolioTransaccion"
										, tra."FechaSistemaCreacion"
										, tra."HoraSistemaCreacion"
										, tra."FechaOperacion"
										, tra."FechaOperacionTransaccion"
										, tra."FechaLiquidacion"
										, tra."FechaEmision"
										, tra."IdEmisor"
										, tra."EsVentaCorta"
										, tra."FechaVencimiento"
										, tra."IdSistemaBursatil"
										, tra."IndicadorOrdenDirecta"
										, tra."EsCuentaPropia"
										, tra."EsFungible"
										, tra."EsAPlazo"
										, tra."CodigoOperadorContraparte"
										, tra."CodigoOperadorCorredor"
										, tra."CantidadTransaccion"
										, tra."MontoTransaccion"
										, tra."PrecioTransaccion"
										, tra."Tasa"
										, tra."Plazo"
										, tra."IdInstrumento"
										, tra."IdTipoReajuste"
										, tra."Instrumento"
										, tra."IdMonedaInstrumento"
										, tra."IdInstrumentoSubyacente"
										, tra."InstrumentoSubyacente"
										, tra."IdTipoInstrumentoSubyacente"
										, tra."NombreTipoInstrumentoSubyacente"
										, tra."IdClasificacionTipoInstrumentoSubyacente"
										, tra."NombreClasificacionTipoInstrumentoSubyacente"
										, tra."IdMonedaInstrumentoSubyacente"
										, tra."IdTipoInstrumento"
										, tra."NombreTipoInstrumento"
										, tra."IdClasificacionTipoInstrumento"
										, tra."NombreClasificacionTipoInstrumento"
										, tra."ValorRescateTransaccion"
										
										, tra."IdEstadoTransaccion"
										, tra."EstadoTransaccion"
										, tra."IdTransaccionPlazo"
										, tra."IdTransaccionOd"
										, tra."IdEmpleado"
										, tra."IdTipoOperacion"
										, tra."NombreTipoOperacion"
										, tra."AccionOrden"
										, tra."IdTipoProducto"
										, tra."IdCondicionDeLiquidacion"
										, tra."NombreCondicionDeLiquidacion"
										, tra."FechaParidad"
										, tra."ValorParidad"
										, tra."FechaTipoCambio"
										, tra."ValorTipoCambio"
										, tra."IdCorredorContraparte"
										, tra."IdUsuarioReserva"
										, tra."FechaIngresoOferta"
										, tra."IdBolsa"
										, tra."IdLibroOrdenes"
										, tra."NombreBolsa"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" ELSE tra."CantidadDisponibleTransaccion" END AS "CantidadDisponibleTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."CantidadAsignadaTransaccion" END AS "CantidadAsignadaTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" ELSE tra."MontoDisponibleTransaccion" END AS "MontoDisponibleTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."MontoAsignadoTransaccion" END AS "MontoAsignadoTransaccion"
										, tra."CorteMinimo" 
								FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS tra INNER JOIN :p_Transacciones p_TRA ON (tra."IdTransaccion" = p_TRA."IdTransaccion")
								ORDER BY tra."FechaOperacionTransaccion";
								
			--agrupo las transacciones que correspondes al mismo precio, condicion de liq, mismo instrumento, tipo operacion, tasa, Moneda Instrumemtno
			v_infoTransacionesGrupo =	SELECT  row_number() OVER (order by CURRENT_UTCTIMESTAMP) as "IdGrupo"
												,"IdInstrumento"
												,"IdTipoOperacion"
												,"IdMonedaInstrumento"
												,"PrecioTransaccion"
												,"Tasa"
												,"IdCondicionDeLiquidacion"
												,"FechaOperacion"
												,"IdTipoProducto"
												,SUM("CantidadDisponibleTransaccion") AS "CantTotalDisponible"
												--,CAST(0 AS BIGINT)  AS "CantTotalAsignada"
										FROM :v_infoTransaciones
										GROUP BY "IdInstrumento","IdTipoOperacion","IdMonedaInstrumento","PrecioTransaccion","Tasa","IdCondicionDeLiquidacion","FechaOperacion","IdTipoProducto";
			
			
			v_infoPortafolios = SELECT  por."IdPortafolio"
										,p_por."Cantidad"
										,p_por."Porcentaje"
										,por."CodigoPortafolio"
										,por."DescripcionPortafolio"
										,por."Proposito"
										,cli."IdCliente"
										,cli."NombreCompleto"
										,cli."DocumentoIdentidad"
										,cli."Tipo"
										,por."IdEstado"
										,por."Estado"
										,por."FechaBloqueo"
										,por."ComentarioBloqueo"
										,por."IdTipoBloqueo"
										,por."IdPerfilComercialBaseOrigen"
										,por."IdPerfilProductos"
										,por."IdPerfilProductosExtra"
										,por."IdPerfilCobros"
										,por."IdPerfilCobrosExtra"
										,por."IdPerfilLimites"
										,por."IdPerfilCondicionesOperativas"
										--,por."IdPerfilFacturacion"
										--,por."IdPerfilFormasPago"
										,por."IdEstadoAutorizacion"
										,por."FechaIngreso"
										,por."FechaCreacion"
										,por."FechaModificacion"
								FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as por INNER JOIN :p_Portafolios p_por ON (por."IdPortafolio" = p_por."IdPortafolio")
																							 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (por."IdCliente" = cli."IdCliente");
			
			
			
			
			/*select * from :v_infoTransacionesGrupo;
			select * from :v_infoTransaciones;
			select * from :v_infoPortafolios;*/
			
			v_infoTranGrupos = select 	grupo."IdGrupo"
										,grupo."IdInstrumento" 						as "IdInstrumentoGrupo"
										,grupo."IdTipoOperacion" 					as "IdTipoOperacionGrupo"
										,grupo."IdMonedaInstrumento" 				as "IdMonedaInstrumentoGrupo"
										,grupo."PrecioTransaccion" 					as "PrecioTransaccionGrupo"
										,grupo."Tasa"								as "TasaGrupo"
										,grupo."IdCondicionDeLiquidacion" 			as "IdCondicionDeLiquidacionGrupo"
										,grupo."CantTotalDisponible"				as "CantidadDisponibleGrupo"
										,grupo."FechaOperacion"						as "FechaOperacionTransaccionGrupo"
										,tran."IdInstrumento"						as "IdInstrumentoTransaccion"
										,tran."Instrumento"							as "InstrumentoTransaccion"
										,tran."IdTipoInstrumento"					as "IdTipoInstrumentoTransaccion"
										,tran."IdTipoOperacion"						as "IdTipoOperacionTransaccion"
										,tran."IdMonedaInstrumento"					as "IdMonedaInstrumentoTransaccion"
										,tran."PrecioTransaccion"					as "PrecioTransaccionTransaccion"
										,tran."Tasa"								as "TasaTransaccion"
										,tran."IdCondicionDeLiquidacion"			as "IdCondicionDeLiquidacionTransaccion"
										,tran."IdTransaccion"						as "IdTransaccion"
										,tran."IdLibroOrdenes"						as "IdLibroOrdenesTransaccion"
										,tran."CantidadDisponibleTransaccion"		as "CantidadDisponibleTransaccion"
										,tran."FolioTransaccion"					as "FolioTransaccion"
										,tran."FechaSistemaCreacion"				as "FechaTransaccion"
										,tran."IdMovimiento"
										,tran."IdTipoProducto"
										,tran."IdBolsa"	
										--,tran."MontoDisponibleTransaccion"			as "MontoDisponibleTransaccion"
									 
								from :v_infoTransacionesGrupo grupo inner join :v_infoTransaciones tran 
										on (grupo."IdInstrumento" = tran."IdInstrumento" and grupo."IdTipoOperacion" = tran."IdTipoOperacion" 
											and grupo."IdMonedaInstrumento" = tran."IdMonedaInstrumento" and grupo."PrecioTransaccion" = tran."PrecioTransaccion"
											and grupo."Tasa" = tran."Tasa" and grupo."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacion" and TO_DATE(TO_VARCHAR(grupo."FechaOperacion",'YYYY-MM-DD')) = TO_DATE(TO_VARCHAR(tran."FechaOperacion",'YYYY-MM-DD')))
								Order by grupo."IdGrupo",grupo."FechaOperacion";

			v_infoPortGrupo = 	select 	por."IdPortafolio"
										,por."CodigoPortafolio"
										,por."IdCliente"
										,por."NombreCompleto"
										,por."DocumentoIdentidad"
										,por."Cantidad"
										,por."Porcentaje"
										,tra."IdGrupo"
										,tra."IdInstrumento"
										,tra."IdTipoOperacion"
										,tra."IdMonedaInstrumento"
										,tra."PrecioTransaccion"
										,tra."Tasa"
										,tra."IdCondicionDeLiquidacion"
										,tra."CantTotalDisponible"
										,tra."FechaOperacion"
								from :v_infoPortafolios por, :v_infoTransacionesGrupo tra;
			
			
			
 			v_infoListaAsig = select  	tra."IdMovimiento"
 										,por."IdPortafolio"
 										,por."CodigoPortafolio"
 										,por."IdCliente"
										,por."Cantidad"
										,por."Porcentaje"
										,por."IdGrupo"
										,por."IdInstrumento"
										,por."IdTipoOperacion"
										,por."IdMonedaInstrumento"
										,por."PrecioTransaccion"
										,por."Tasa"
										,por."IdCondicionDeLiquidacion"
										,por."CantTotalDisponible"
										,por."FechaOperacion"
										--,tra."IdGrupo"
										,por."NombreCompleto"
										,por."DocumentoIdentidad"
										,tra."InstrumentoTransaccion" as "Instrumento"
										,tra."IdTipoInstrumentoTransaccion" as "IdTipoInstrumento"
										,tra."IdInstrumentoGrupo"
										,tra."FechaOperacionTransaccionGrupo"
										,tra."IdTipoOperacionGrupo"
										,tra."IdMonedaInstrumentoGrupo"
										,tra."PrecioTransaccionGrupo"
										,tra."TasaGrupo"
										,tra."IdCondicionDeLiquidacionGrupo"
										,tra."IdInstrumentoTransaccion"
										,tra."IdTipoOperacionTransaccion"
										,tra."IdMonedaInstrumentoTransaccion"
										,tra."PrecioTransaccionTransaccion"
										,tra."TasaTransaccion"
										,tra."IdCondicionDeLiquidacionTransaccion"
										,tra."IdTransaccion"
										,tra."FolioTransaccion"
										,tra."FechaTransaccion"
										,tra."IdLibroOrdenesTransaccion"
										,tra."CantidadDisponibleGrupo"
										,tra."CantidadDisponibleTransaccion"
										--,tra."MontoDisponibleTransaccion"
										,(tra."CantidadDisponibleGrupo" * (por."Porcentaje" / 100)) as "CantidadAsignar"									 
										,0 as "CantidadAsignada"
										
										,tra."IdTipoProducto"
										,'TRA'  as "IdTipoCanalIngreso"
										,tra."IdBolsa"
					 			from :v_infoPortGrupo por inner join :v_infoTranGrupos tra 
					 					on (por."IdGrupo" = tra."IdGrupo")
					 			order by por."IdGrupo" asc, por."Porcentaje" desc;
					 			
					 			
			INSERT INTO #infoListaAsignar  ("Id","IdMovimiento","IdPortafolio","CodigoPortafolio","IdCliente","Cantidad","Porcentaje","IdGrupo","IdInstrumento","IdTipoOperacion"
												,"IdMonedaInstrumento","PrecioTransaccion","Tasa","IdCondicionDeLiquidacion","CantTotalDisponible","FechaOperacion","NombreCompleto","DocumentoIdentidad","Instrumento"
												,"IdTipoInstrumento","IdInstrumentoGrupo","FechaOperacionTransaccionGrupo","IdTipoOperacionGrupo","IdMonedaInstrumentoGrupo","PrecioTransaccionGrupo"
												,"TasaGrupo","IdCondicionDeLiquidacionGrupo","IdInstrumentoTransaccion","IdTipoOperacionTransaccion","IdMonedaInstrumentoTransaccion"
												,"PrecioTransaccionTransaccion","TasaTransaccion","IdCondicionDeLiquidacionTransaccion","IdTransaccion","FolioTransaccion","FechaTransaccion","IdLibroOrdenesTransaccion"
												,"CantidadDisponibleGrupo","CantidadDisponibleTransaccion","CantidadAsignar","CantidadAsignada","PrecioPromedio","IdTipoProducto","IdTipoCanalIngreso","IdBolsa"
												)
			SELECT row_number() OVER (order by CURRENT_UTCTIMESTAMP)
				as "Id",asig."IdMovimiento",asig."IdPortafolio",asig."CodigoPortafolio",asig."IdCliente",asig."Cantidad",asig."Porcentaje",asig."IdGrupo",asig."IdInstrumento",asig."IdTipoOperacion",
				asig."IdMonedaInstrumento",	asig."PrecioTransaccion",asig."Tasa",asig."IdCondicionDeLiquidacion",asig."CantTotalDisponible",asig."FechaOperacion",asig."NombreCompleto",asig."DocumentoIdentidad",asig."Instrumento",
				asig."IdTipoInstrumento",asig."IdInstrumentoGrupo",asig."FechaOperacionTransaccionGrupo",asig."IdTipoOperacionGrupo",asig."IdMonedaInstrumentoGrupo",asig."PrecioTransaccionGrupo",
				asig."TasaGrupo",asig."IdCondicionDeLiquidacionGrupo",asig."IdInstrumentoTransaccion",asig."IdTipoOperacionTransaccion",asig."IdMonedaInstrumentoTransaccion",
				asig."PrecioTransaccionTransaccion",asig."TasaTransaccion",asig."IdCondicionDeLiquidacionTransaccion",asig."IdTransaccion",asig."FolioTransaccion",asig."FechaTransaccion",asig."IdLibroOrdenesTransaccion",
				asig."CantidadDisponibleGrupo",asig."CantidadDisponibleTransaccion",asig."CantidadAsignar",asig."CantidadAsignada",0 as "PrecioPromedio",asig."IdTipoProducto",asig."IdTipoCanalIngreso",asig."IdBolsa"
			from :v_infoListaAsig asig;

			SELECT COUNT(1) 
			INTO v_cantReg
			FROM #infoListaAsignar;--FROM :v_infoListaAsignar;
			
			FOR v_i IN 1 .. v_cantReg DO
				
				select 	"CantidadAsignar"									 
				into v_CantidadAsignar
				from #infoListaAsignar
				where "Id" = v_i;
				
				
				if (v_CantidadAsignar > 0) then	
					select 	"Id"
 						,"IdPortafolio"
						,"IdGrupo"
						,"IdTransaccion"
						,"IdInstrumentoTransaccion"
						,"IdTipoOperacionTransaccion"
						,"IdMonedaInstrumentoTransaccion"
						,"CantidadDisponibleGrupo" - (case 
														when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
															"CantidadDisponibleTransaccion"
														else "CantidadAsignar"
													 end) as "CantidadDisponibleGrupo"
						,"CantidadDisponibleTransaccion" - (case 
																when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
																	"CantidadDisponibleTransaccion"
																else "CantidadAsignar"
															end) as "CantidadDisponibleTransaccion"
						,"CantidadAsignar" - (case 
												when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
													"CantidadDisponibleTransaccion"
												else "CantidadAsignar"
											 end) as "CantidadAsignar"									 
						,(case 
							when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
								"CantidadDisponibleTransaccion"
							else "CantidadAsignar"
						 end) as "CantidadAsignada"
					into v_Id,v_IdPortafolio,v_IdGrupo,v_IdTransaccion,v_IdInstrumento ,v_IdTipoOperacion,v_IdMonedaInstrumento,v_CantidadDisponibleGrupo,v_CantidadDisponibleTransaccion,v_CantidadAsignar,v_CantidadAsignada
					from #infoListaAsignar
					where "Id" = v_i;
					
					UPDATE #infoListaAsignar SET "CantidadDisponibleGrupo" = "CantidadDisponibleGrupo" - (case 
																				when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
																					"CantidadDisponibleTransaccion"
																				else "CantidadAsignar"
																			 end)
												,"CantidadDisponibleTransaccion" = "CantidadDisponibleTransaccion" - (case 
																						when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
																							"CantidadDisponibleTransaccion"
																						else "CantidadAsignar"
																					end)
												--,"MontoDisponibleTransaccion"
												,"CantidadAsignar"  = "CantidadAsignar" - (case 
																		when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
																			"CantidadDisponibleTransaccion"
																		else "CantidadAsignar"
																	 end)
												,"CantidadAsignada" = (	case 
																			when "CantidadAsignar" >= "CantidadDisponibleTransaccion" then
																				"CantidadDisponibleTransaccion"
																			else "CantidadAsignar"
																		 end)
					WHERE "Id" = v_i;
					
					--transaccion relacionadas
					UPDATE #infoListaAsignar SET "CantidadDisponibleGrupo" = "CantidadDisponibleGrupo" - v_CantidadAsignada
												,  "CantidadDisponibleTransaccion" = (case 
																						when "IdTransaccion" = v_IdTransaccion then 
																								"CantidadDisponibleTransaccion" - v_CantidadAsignada  
																							else "CantidadDisponibleTransaccion"
																						 end)
												,"CantidadAsignar" = (case 
																		when "IdPortafolio" = v_IdPortafolio then 
																			"CantidadAsignar" - v_CantidadAsignada
																		else "CantidadAsignar"
																	 end )
					where "IdGrupo" = v_IdGrupo and "Id" > v_i;
					
					select sum(("CantidadDisponibleTransaccion"+"CantidadAsignada") * "PrecioTransaccion") / sum("CantidadDisponibleTransaccion"+"CantidadAsignada")
					into v_PrecioPromedio
					FROM #infoListaAsignar
					WHERE "IdInstrumentoTransaccion" = v_IdInstrumento and "IdTipoOperacionTransaccion" = v_IdTipoOperacion and "IdMonedaInstrumentoTransaccion" = v_IdMonedaInstrumento;
					
					UPDATE #infoListaAsignar SET "PrecioPromedio" = :v_PrecioPromedio
					WHERE "IdInstrumentoTransaccion" = v_IdInstrumento and "IdTipoOperacionTransaccion" = v_IdTipoOperacion and "IdMonedaInstrumentoTransaccion" = v_IdMonedaInstrumento;

				end if;
				
			END FOR;
			
			
			p_out_SimAsignacion = 	SELECT 	"BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL	as	"IdInterno"
											,"IdMovimiento"				as "IdMovimiento"
											,v_FechaSistemaCreacion 	as "FechaSimulacion"
											,CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro"
											,"IdTransaccion"			as "IdTransaccion"
											,"FolioTransaccion"			as "FolioTransaccion"
											,"FechaTransaccion"			as "FechaTransaccion"
											, 0							as "FolioCartera"
											,"IdPortafolio"				as "IdPortafolio"
											,"CodigoPortafolio"			as "CodigoPortafolio"
											,"IdCliente"				as "IdCliente"
											,"IdLibroOrdenesTransaccion"as "IdLibroOrdenes"
											,"IdInstrumento"			as "IdInstrumento"
											,"Instrumento"				as "Instrumento"
											,"IdTipoInstrumento"		as "IdTipoInstrumento"
											,"IdMonedaInstrumento"		as "IdMonedaInstrumento"
											,cast(0 as BIGINT)			as "IdOrden"
											,"NombreCompleto"			as "NombreCompleto"
											,"DocumentoIdentidad"		as "DocumentoIdentidad"
											,"CantidadAsignada"			as "CantidadAsignacion"
											,cast(0 as DECIMAL(26,8))	as "ValorRescateAsignacion"
											,"Porcentaje"				as "PorcentajeAsignacion"
											,"PrecioPromedio"			as "PrecioSimulacion"
											
											,"IdTipoProducto"
											,"IdTipoOperacionTransaccion" as "IdTipoOperacion"
											,"IdTipoCanalIngreso"
											,"IdBolsa"
									FROM #infoListaAsignar
									ORDER BY "Id";
			
			DROP TABLE #infoListaAsignar;
			
END;
go
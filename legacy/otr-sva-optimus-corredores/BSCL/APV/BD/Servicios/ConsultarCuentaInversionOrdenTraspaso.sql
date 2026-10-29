SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.APV.BD.Servicios::ConsultarCuentaInversionOrdenTraspaso";
GO
CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de una cuentas de inversion pero por IdCliente (se replica SP BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversion)  
--  Autor: PRONET IT 
--  Fecha Creacion: 28/12/2017  
-- 
"BSCL.APV.BD.Servicios::ConsultarCuentaInversionOrdenTraspaso" ( 
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,
    IN  p_IdUsuario                     BIGINT, 
    IN  p_IdModulo                      NVARCHAR(3),        
	IN  p_UsaReglasNegocio	 		    NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
        
    -- Parametros entrada del servicio publico
    IN  p_IdTraspaso					BIGINT,
    IN  p_TipoAgrupador					NVARCHAR(2), -- PL: Plan Inversion, PR: Producto, RE: Regimen, PO: Portafolio  
	IN  p_CuentaInversionAConsultar		TABLE	("IdEjecutivo"			BIGINT,
												 "IdCliente"			BIGINT,
											  	 "IdPortafolio"			BIGINT,
											  	 "IdMoneda"				NVARCHAR(3)),
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),	
    IN  p_EstadoTraspaso				NVARCHAR(10) DEFAULT '',
    -- Parametros salida del servicio publico                                            	 
	OUT p_out_SaldoPorFechaMovimiento	TABLE	("CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"				BIGINT,
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "NombrePlan"				NVARCHAR(10),
                                              	 "IdPlanTipoInversion"		BIGINT,
	                							 "IdProductoAgrupador"		BIGINT,
	                							 "Producto"					NVARCHAR(40),
	                							 "IdRegimenTributario"		BIGINT,
	                							 "Regimen"					NVARCHAR(50),
	                							 "NombreAgrupador"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazo"				DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "IdEstado"					NVARCHAR(3), --Tabla con proyección de Saldo
		                                      	 "Instrumento"				NVARCHAR(40),
		                                      	 "CantidadCuotas"			DECIMAL(26,8),
		                                      	 "ValorCuota"				DECIMAL(26,8),
		                                      	 "Porcentaje"				BIGINT,
		                                      	 "UFTraspaso"				DECIMAL(26,8)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	v_out_SaldoPorFechaMovimiento = SELECT '' AS "CodigoCuentaInversion"--	NVARCHAR(40), 
                                      	 , 0 AS "IdCliente"               --	BIGINT,
                                      	 , '' AS "NombreCliente"			--NVARCHAR(300),
                                      	 , '' AS "DocumentoIdentidad"		--NVARCHAR(40),
                                      	 , 0 AS "IdPortafolio"			--	BIGINT,
                                      	 , '' AS "NombrePortafolio"		--	NVARCHAR(100),
                                      	 , '' AS "NombrePlan"				--NVARCHAR(10),
                                      	 , 0 AS "IdPlanTipoInversion"		--BIGINT,
            							 , 0 AS "IdProductoAgrupador"		--BIGINT,
            							 , '' AS "Producto"				--	NVARCHAR(40),
            							 , 0 AS "IdRegimenTributario"		--BIGINT,
            							 , '' AS "Regimen"				--	NVARCHAR(50),
            							 , '' AS "NombreAgrupador"		--	NVARCHAR(100),
                                      	 , '' AS "FechaSaldo" 			--	NVARCHAR(10),
                                      	 , '' AS "IdMoneda"               -- 	NVARCHAR(3),
                                      	 , '' AS "CodigoMoneda"			--	NVARCHAR(40),
                                      	 , '' AS "NombreMoneda"			--	NVARCHAR(100),
                                      	 , 0 AS "SaldoDisponible"         -- DECIMAL(26,8),
                                      	 , 0 AS "SaldoEnGarantia"			--DECIMAL(26,8),
                                      	 , 0 AS "SaldoAPlazo"				--DECIMAL(26,8),
                                      	 , 0 AS "SaldoRetiro"				--DECIMAL(26,8),
                                      	 , '' AS  "IdEstado"				--	NVARCHAR(3),
                                      	 , '' AS "Instrumento"				--NVARCHAR(40),
                                      	 , 0 AS "CantidadCuotas"			--DECIMAL(26,8),
                                      	 , 0 AS "ValorCuota"				--DECIMAL(26,8),
                                      	 , 0 AS "Porcentaje"				--BIGINT,
                                      	 , 0 AS "UFTraspaso"					--DECIMAL(26,8)
                                     FROM DUMMY WHERE 1=2;
	
	CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente" ( 
		-- Parametros entrada comunes en todos los servicios publicos
	    p_IdEmpresa						=> :p_IdEmpresa                     
	    ,p_IdUsuario                     => :p_IdUsuario 
	    ,p_IdModulo                      => :p_IdModulo
		,p_UsaReglasNegocio	 		     => 'N'
	    -- Parametros entrada del servicio publico
	    ,p_TipoAgrupador				 => 'PO' --PO:Portafolio  		     
		,p_CuentaInversionAConsultar	 => :p_CuentaInversionAConsultar
		,p_FechaDesde  					 => :p_FechaDesde
	    ,p_FechaHasta  					 => :p_FechaHasta
	    -- Parametros salida del servicio publico           	 
		,p_out_SaldoPorFechaMovimiento	 => :v_out_SaldoPorFechaMovimiento 
		-- Salidas para mensajes de advertencias y errores
		,p_out_InformacionContador  	 => :p_out_InformacionContador
		,p_out_InformacionDetalle   	 => :p_out_InformacionDetalle
		,p_out_AdvertenciasContador      => :p_out_AdvertenciasContador
		,p_out_AdvertenciasDetalle       => :p_out_AdvertenciasDetalle	
		,p_out_ErroresContador           => :p_out_ErroresContador
		,p_out_ErroresDetalle            => :p_out_ErroresDetalle
	); 	

	IF (p_EstadoTraspaso = '')
		THEN 
			p_out_SaldoPorFechaMovimiento = 
				SELECT * 
				FROM (	SELECT 
							 CuentaInversionRV."CodigoCuentaInversion" 
				            ,CuentaInversionRV."IdCliente"            
				            ,CuentaInversionRV."NombreCliente"			
				            ,CuentaInversionRV."DocumentoIdentidad"		
				            ,OrdenTraspasoRV."IdPortafolio"			
				            ,CuentaInversionRV."NombrePortafolio"		
				            ,CuentaInversionRV."NombrePlan"				
				            ,CuentaInversionRV."IdPlanTipoInversion"	
				            ,CuentaInversionRV."IdProductoAgrupador"	
				            ,CuentaInversionRV."Producto"				
				            ,CuentaInversionRV."IdRegimenTributario"	
				            ,OrdenTraspasoRV."Regimen"				
				            ,CuentaInversionRV."NombreAgrupador"		
				            ,CuentaInversionRV."FechaSaldo" 			
				            ,CuentaInversionRV."IdMoneda"               
				            ,CuentaInversionRV."CodigoMoneda"			
				            ,CuentaInversionRV."NombreMoneda"			
				            ,OrdenTraspasoRV."MontoOrden" AS "SaldoDisponible"   
				            ,CuentaInversionRV."SaldoEnGarantia"		
				            ,CuentaInversionRV."SaldoAPlazo"			
				            ,CuentaInversionRV."SaldoRetiro"			
				            ,CuentaInversionRV."IdEstado"					 
				            ,OrdenTraspasoRV."Instrumento"
				            ,OrdenTraspasoRV."CantidadCuotas"
				            ,OrdenTraspasoRV."ValorCuota"
				            ,CuentaInversionRV."Porcentaje"
				            ,CuentaInversionRV."UFTraspaso"
						FROM :v_out_SaldoPorFechaMovimiento AS CuentaInversionRV
						INNER JOIN "BSCL.APV.BD.Vistas::TRA.ConsultarOrdenesTraspaso" AS OrdenTraspasoRV
							ON OrdenTraspasoRV."IdPortafolio" = CuentaInversionRV."IdPortafolio"
						INNER JOIN "BSCL.Orden.BD::ORD.Orden" AS OrdenRV
							ON OrdenRV."IdOrden" = OrdenTraspasoRV."IdSolicitudAporteRescate"
						INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ASG
							ON OrdenTraspasoRV."IdSolicitudAporteRescate" = ASG."IdOrden"
						INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MC
							ON ASG."IdAsignacion" = "MC"."IdAsignacion"
							AND MC."IdEstadoFacturacion" = 'FAC'
						WHERE OrdenTraspasoRV."IdTraspaso" = :p_IdTraspaso	
						UNION ALL
						SELECT 
							 CuentaInversion."CodigoCuentaInversion" 
				        	,CuentaInversion."IdCliente"            
				            ,CuentaInversion."NombreCliente"			
				            ,CuentaInversion."DocumentoIdentidad"		
				            ,OrdenTraspaso."IdPortafolio"			
				            ,CuentaInversion."NombrePortafolio"		
				            ,CuentaInversion."NombrePlan"				
				            ,CuentaInversion."IdPlanTipoInversion"	
				            ,CuentaInversion."IdProductoAgrupador"	
				            ,CuentaInversion."Producto"				
				            ,CuentaInversion."IdRegimenTributario"	
				            ,OrdenTraspaso."Regimen"				
				            ,CuentaInversion."NombreAgrupador"		
				            ,CuentaInversion."FechaSaldo" 			
				            ,CuentaInversion."IdMoneda"               
				            ,CuentaInversion."CodigoMoneda"			
				            ,CuentaInversion."NombreMoneda"			
				            ,OrdenTraspaso."MontoOrdenFFMM" AS "SaldoDisponible"
				            ,CuentaInversion."SaldoEnGarantia"		
				            ,CuentaInversion."SaldoAPlazo"			
				            ,CuentaInversion."SaldoRetiro"			
				            ,CuentaInversion."IdEstado"					 
				            ,OrdenTraspaso."Instrumento"
				            ,OrdenTraspaso."CantidadCuotas"
				            ,OrdenTraspaso."ValorCuota"			
				        	,CuentaInversion."Porcentaje"
				            ,CuentaInversion."UFTraspaso"
						FROM :v_out_SaldoPorFechaMovimiento AS CuentaInversion
						INNER JOIN "BSCL.APV.BD.Vistas::TRA.ConsultarOrdenesTraspaso" AS OrdenTraspaso
							ON OrdenTraspaso."IdPortafolio" = CuentaInversion."IdPortafolio"
							AND OrdenTraspaso."Estado" IN ('EJE','CRT')
						WHERE OrdenTraspaso."IdTraspaso" = :p_IdTraspaso
			);
		ELSE 
			p_out_SaldoPorFechaMovimiento = 
				SELECT * FROM (					
							SELECT 					
							'' 													AS "CodigoCuentaInversion"
                          	, 0 													AS "IdCliente"             
                          	, '' 													AS "NombreCliente"			
                          	, '' 													AS "DocumentoIdentidad"	
                          	, 0 													AS "IdPortafolio"			
                          	, '' 													AS "NombrePortafolio"		
                          	, '' 													AS "NombrePlan"				
                          	, 0 													AS "IdPlanTipoInversion"		
							, 0														AS "IdProductoAgrupador"
							,PRA."NombreProductoAgrupador"							AS "Producto"
							, 0 													AS "IdRegimenTributario"
							,POR."CodigoPortafolio"									AS "Regimen"
							, '' 													AS "NombreAgrupador"		
                          	, '' 													AS "FechaSaldo" 
                          	,INS."IdMoneda"											AS "IdMoneda"
                          	, '' 													AS "CodigoMoneda"			
                          	, '' 													AS "NombreMoneda"	
                          	,DTR."MontoTraspaso"									AS "SaldoDisponible"
                          	, 0 													AS "SaldoEnGarantia"		
                          	, 0 													AS "SaldoAPlazo"			
                          	, 0 													AS "SaldoRetiro"			
                          	, ''													AS "IdEstado"
                          	,INS."Nemotecnico"										AS "Instrumento"
                          	,CAR."Cantidad"											AS "CantidadCuotas"
							,CAR."INGPrecioPromedioCompra"							AS "ValorCuota"
							,DTR."PorcentajeTraspaso"								AS "Porcentaje"
                          	,DTR."UFTraspaso"										AS "UFTraspaso"
					
                        FROM 		"BSCL.APV.BD::TRA.Traspaso"	 			 		TRA
						INNER JOIN  "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" 		DTR ON DTR."IdTraspaso" 			= TRA."IdTraspaso"
						INNER JOIN 	"BSCL.Plataforma.BD::INS.Instrumento" 	 		INS ON INS."IdInstrumento" 			= DTR."IdInstrumento"
						INNER JOIN  "BSCL.Custodia.BD::CUS.Cartera"			 		CAR ON CAR."IdInstrumento" 			= INS."IdInstrumento" AND TRA."IdCliente" = CAR."IdCliente"
						INNER JOIN  "BSCL.Portafolio.BD::POR.Portafolio"			POR ON POR."IdPortafolio" 			= CAR."IdPortafolio"
						INNER JOIN  "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"	MAA ON MAA."IdMatrizAgrupadorAPV" 	= POR."IdMatrizAgrupadorAPV"
						INNER JOIN  "BSCL.APV.BD::APV.ProductoAgrupador"			PRA ON PRA."IdProductoAgrupador"	= MAA."IdProductoAgrupador"
						
						WHERE TRA."IdTraspaso" = :p_IdTraspaso
						AND POR."IdEstado" <> 'INA'
				);
		END IF;
END;
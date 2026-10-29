SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaMovimientosVencidosFDW";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaMovimientosVencidosFDW"
    ( 
	-- Tabla Entrada
	IN p_fechaDesde DATE,
	IN p_fechaHoy 	DATE,
	IN p_fechaHasta DATE,			   
	--    Salida   	--------------------------------------------------
   	OUT p_out_vencidos table ("IdOperacion"							BIGINT
                                ,"FechaOperacion"					DATE
								,"FechaLiquidacion"					DATE
								,"HoraOperacion"					NVARCHAR (8)
								,"NombreInstrumento"				NVARCHAR (100)	
								,"Nemotecnico"						NVARCHAR (6)
								,"IdTipoOperacion"					NVARCHAR (6)
								,"TipoOperacion"					NVARCHAR (100)
								,"RutCliente"						NVARCHAR (12)
								,"NombreCliente"					NVARCHAR (100)
								,"IdMonedaPrincipal"				NVARCHAR (3)
								,"IdMonedaSecundaria" 				NVARCHAR (3)
								,"MontoMonedaPrincipal"				DECIMAL(26,8)
								,"MontoMonedaSecundaria"			DECIMAL(26,8)
								,"ResultadoMonedaLocalPuntas"		DECIMAL(26,8)
								,"ResultadoMonedaLocalBidAsk"		DECIMAL(26,8)
								,"ResultadoMonedaLocalDO"			DECIMAL(26,8)
								,"ResultadoMonedaLocalTCSpot"		DECIMAL(26,8)
								,"ResultadoMonedaLocalTCCierreMesa"	DECIMAL(26,8)
								,"TipoCambioCierre"     			DECIMAL(26,8)   
								,"TipoCambioCosto"     				DECIMAL(26,8)   
								,"ParidadCierre"     				DECIMAL(26,8)  
								,"ParidadCosto"     				DECIMAL(26,8)  
								,"MontoEquivalente"       			DECIMAL(26,8)  
								,"Margen"  							DECIMAL(26,8) 
								,"Utilidad"							DECIMAL(26,8) 
								,"MontoDiferido"					DECIMAL(26,8) 
								,"Modalidad"						BIGINT
								,"Plazo"							BIGINT
								,"PlazoResidual" 					BIGINT
								,"FechaVencimiento" 				DATE
								,"FechaEfectiva"					DATE
								,"Agente"  							BIGINT
								,"NombreAgente"						NVARCHAR (100)
								,"OperacionFicticia"				NVARCHAR (2)
								,"CodigoUsuario"					NVARCHAR (40)
								,"Estado"							NVARCHAR (3)
								,"Mesa"								NVARCHAR (3))
    ) 

	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA 
AS   
  BEGIN			
		p_out_vencidos = SELECT  "IdOperacion"
								,"FechaOperacion"
								,"FechaLiquidacion"
								,"HoraOperacion"
								,"NombreInstrumento"
								,"Nemotecnico"
								,"IdTipoOperacion"
								,"TipoOperacion"
								,"RutCliente"
								,"NombreCliente"
								,"IdMonedaPrincipal"
								,"IdMonedaSecundaria" 
								,"MontoMonedaPrincipal"
								,"MontoMonedaSecundaria"
								,"ResultadoMonedaLocalPuntas"
								,"ResultadoMonedaLocalBidAsk"
								,"ResultadoMonedaLocalDO"
								,"ResultadoMonedaLocalTCSpot"
								,"ResultadoMonedaLocalTCCierreMesa"
								,"TipoCambioCierre" 
								,"TipoCambioCosto" 
								,"ParidadCierre"
								,"ParidadCosto"
								,"MontoEquivalente"  
								,"Margen"  
								,"Utilidad"
								,"MontoDiferido"
								,"Modalidad"
								,"Plazo"
								,"PlazoResidual" 
								,"FechaVencimiento" 
								,"FechaEfectiva"
								,"Agente"  
								,"NombreAgente"
								,"OperacionFicticia"
								,"CodigoUsuario"
								,"Estado"
								,"Mesa"
								  FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
								  	WHERE "Estado" = 'VEN'
								  	  	AND "FechaVencimiento" between :p_fechaDesde and :p_fechaHoy
								  
  UNION ALL
  	
						SELECT  "IdOperacion"
								,"FechaOperacion"
								,"FechaLiquidacion"
								,"HoraOperacion"
								,"NombreInstrumento"
								,"Nemotecnico"
								,"IdTipoOperacion"
								,"TipoOperacion"
								,"RutCliente"
								,"NombreCliente"
								,"IdMonedaPrincipal"
								,"IdMonedaSecundaria" 
								,"MontoMonedaPrincipal"
								,"MontoMonedaSecundaria"
								,"ResultadoMonedaLocalPuntas"
								,"ResultadoMonedaLocalBidAsk"
								,"ResultadoMonedaLocalDO"
								,"ResultadoMonedaLocalTCSpot"
								,"ResultadoMonedaLocalTCCierreMesa"
								,"TipoCambioCierre" 
								,"TipoCambioCosto" 
								,"ParidadCierre"
								,"ParidadCosto"
								,"MontoEquivalente"  
								,"Margen"  
								,"Utilidad"
								,"MontoDiferido"
								,"Modalidad"
								,"Plazo"
								,"PlazoResidual" 
								,"FechaVencimiento" 
								,"FechaEfectiva"
								,"Agente"  
								,"NombreAgente"
								,"OperacionFicticia"
								,"CodigoUsuario"
								,"Estado"
								,"Mesa"
								FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD"
								  	WHERE "FechaVencimiento" between :p_fechaHoy and :p_fechaHasta 
								ORDER BY "FechaVencimiento" DESC;
END;
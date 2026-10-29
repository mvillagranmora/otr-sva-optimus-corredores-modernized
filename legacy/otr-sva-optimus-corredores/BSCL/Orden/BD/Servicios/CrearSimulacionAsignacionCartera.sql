SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCartera";

CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion por cartera
--  Autor: TEAM ORDENES - BCS
--  Fecha Creacion: 08/02/2015
--  
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionCartera" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN	p_IdTipo				NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion refacturacion (REFACT))
	
	-- Parametros entrada del servicio publico
	IN	p_Origen				NVARCHAR(40),--origen de la llamamda a refacturar
	IN  p_Transacciones			TABLE (
									"IdMovimiento" 	BIGINT --movimiento que origino la asignacion por refacturacion comp, movtocomp,asig,transac
									, "IdTransaccion"	BIGINT
									, "Cantidad" DECIMAL (26,8)
									, "Monto" DECIMAL (26,8)
									, "ValorRescate" DECIMAL (26,8)
								),
	IN  p_Carteras				TABLE (
									"IdPortafolio"	BIGINT,
									"FolioCartera"	BIGINT,
									"Cantidad" 		DECIMAL (26,8)
								),

	OUT p_out_SimAsignacion		TABLE (
									"IdInterno"				BIGINT
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
											,"NombreCompleto"		NVARCHAR(40)
											,"DocumentoIdentidad"	NVARCHAR(40)
											,"CantidadAsignacion"	DECIMAL(26,8)
											,"ValorRescateAsignacion"DECIMAL(26,8)
											,"PorcentajeAsignacion"	DECIMAL(26,8)
											,"PrecioSimulacion"		DECIMAL(26,8)
											,"IdTipoProducto"		NVARCHAR(4)
											,"IdTipoOperacion"		NVARCHAR(6)
											,"IdTipoCanalIngreso"	NVARCHAR(3)
											,"IdBolsa"				NVARCHAR(3)
								)
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE	v_FechaSistema TIMESTAMP;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistema);
	
	BEGIN 
		DECLARE idTrans BIGINT ARRAY;
		DECLARE idCartera BIGINT ARRAY;
		DECLARE saldoTrans DECIMAL(26,8) ARRAY;
		DECLARE saldoCartera DECIMAL(26,8) ARRAY;
		
		DECLARE cantTransaccion BIGINT;
		DECLARE cantCartera BIGINT;
		DECLARE indexTransaccion BIGINT := 1;
		DECLARE indexCartera BIGINT := 1;
		
		DECLARE asignacionTrans BIGINT ARRAY;
		DECLARE asignacionCartera BIGINT ARRAY;
		DECLARE asignacionCantidad DECIMAL(26,8) ARRAY;
		DECLARE asignacionIndex BIGINT := 1;
		DECLARE asignacionCantidadAux DECIMAL(26,8) := 0;
		
		--idTrans = SELECT "IdTransaccion" FROM :p_Transacciones;
		idTrans  = ARRAY_AGG(:p_Transacciones."IdTransaccion" order by "IdTransaccion");
		idCartera  = ARRAY_AGG(:p_Carteras."FolioCartera" order by "FolioCartera");
		saldoTrans  = ARRAY_AGG(:p_Transacciones."Cantidad" order by "IdTransaccion");
		saldoCartera  = ARRAY_AGG(:p_Carteras."Cantidad" order by "FolioCartera");
		
		cantTransaccion = CARDINALITY(:idTrans);
		cantCartera = CARDINALITY(:idCartera);
	
		WHILE (indexTransaccion <= cantTransaccion)
		DO
			IF (indexCartera > cantCartera) THEN
				BREAK;
			END IF;
			
			WHILE (indexCartera <= cantCartera)
			DO
				IF (:saldoCartera[:indexCartera] > 0) THEN
					
					--GENERA ASIGNACION
					asignacionTrans[:asignacionIndex] = :idTrans[:indexTransaccion];
					asignacionCartera[:asignacionIndex] = :idCartera[:indexCartera];
					IF (:saldoTrans[:indexTransaccion] > :saldoCartera[:indexCartera]) THEN
						asignacionCantidadAux = :saldoCartera[:indexCartera];
					ELSE
						asignacionCantidadAux = :saldoTrans[:indexTransaccion];
					END IF;
					asignacionCantidad[:asignacionIndex] = asignacionCantidadAux;
					
					asignacionIndex = asignacionIndex + 1;
					
					saldoCartera[:indexCartera] = :saldoCartera[:indexCartera] - asignacionCantidadAux;
					saldoTrans[:indexTransaccion] = :saldoTrans[:indexTransaccion] - asignacionCantidadAux;
				
				END IF;
			
				IF (:saldoCartera[:indexCartera] = 0) THEN
					indexCartera = indexCartera + 1;
				END IF;
				
				IF (:saldoTrans[:indexTransaccion] = 0) THEN
					indexTransaccion =  indexTransaccion + 1;
					BREAK;
				END IF;
			END WHILE;
		END WHILE;
		asignaciones = UNNEST(:asignacionTrans, :asignacionCartera,:asignacionCantidad) AS ("IdTransaccion", "FolioCartera", "CantidadAsignacion");
	END;
	
	p_out_SimAsignacion = SELECT 0 as "IdInterno",0 as "IdMovimiento", v_FechaSistema as "FechaSimulacion", CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro",
			p_tran."IdTransaccion","Folio" as "FolioTransaccion",tran."FechaSistemaCreacion" as "FechaTransaccion",asig."FolioCartera",cart."IdPortafolio",
			"CodigoPortafolio",port."IdCliente","IdLibroOrdenes",
			"IdInstrumento",tran."Nemotecnico" as "Instrumento","IdTipoInstrumento",tran."IdMoneda" as "IdMonedaInstrumento",
			0 as "IdOrden","NombreCompleto","DocumentoIdentidad","CantidadAsignacion","CantidadAsignacion" as "ValorRescateAsignacion",0 as "PorcentajeAsignacion",0 as "PrecioSimulacion",
			"IdTipoProducto",tran."IdTipoOperacion",'TRA' as "IdTipoCanalIngreso","IdBolsa"
			
	FROM :asignaciones asig 
	INNER JOIN :p_Transacciones p_tran ON asig."IdTransaccion" = p_tran."IdTransaccion"
	INNER JOIN :p_Carteras cart ON asig."FolioCartera" = cart."FolioCartera"
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON p_tran."IdTransaccion" = tran."IdTransaccion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipoope ON tipoope."IdTipoOperacion" = tran."IdTipoOperacion"
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as port on cart."IdPortafolio" = port."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON port."IdCliente" = cli."IdCliente";
	
END;

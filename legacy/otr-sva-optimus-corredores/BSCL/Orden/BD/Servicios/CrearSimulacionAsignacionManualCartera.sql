SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCartera";

CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion por cartera
--  Autor: TEAM ORDENES - BCS
--  Fecha Creacion: 08/02/2015
--  
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCartera" (
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
	IN  p_Carteras				TABLE (
									"IdPortafolio"	BIGINT,
									"FolioCartera"	BIGINT,
									"Cantidad" 		DECIMAL (26,8)
								),
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
											,"NombreCompleto"		NVARCHAR(40)
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

	DECLARE	v_FechaSistema TIMESTAMP;
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistema);
	
	p_out_SimAsignacion = SELECT "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL as "IdInterno",0 as "IdMovimiento", v_FechaSistema as "FechaSimulacion", CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro",
			p_tran."IdTransaccion","Folio" as "FolioTransaccion",tran."FechaSistemaCreacion" AS "FechaTransaccion",cart."FolioCartera",cart."IdPortafolio","CodigoPortafolio",port."IdCliente","IdLibroOrdenes",
			"IdInstrumento",tran."Nemotecnico" as "Instrumento","IdTipoInstrumento",tran."IdMoneda" as "IdMonedaInstrumento",
			0 as "IdOrden","NombreCompleto","DocumentoIdentidad",0 as "CantidadAsignacion",0 as "ValorRescateAsignacion",0 as "PorcentajeAsignacion",0 as "PrecioSimulacion",
			"IdTipoProducto",tran."IdTipoOperacion",'TRA' as "IdTipoCanalIngreso","IdBolsa"
			
	FROM :p_Transacciones p_tran
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" tran ON p_tran."IdTransaccion" = tran."IdTransaccion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipoope ON tipoope."IdTipoOperacion" = tran."IdTipoOperacion"
	,
	:p_Carteras cart
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as port on cart."IdPortafolio" = port."IdPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON port."IdCliente" = cli."IdCliente";

END;

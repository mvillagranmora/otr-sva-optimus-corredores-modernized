SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCliente";

CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: PROJO - BCS
--  Fecha Creacion: 08/02/2015
--  
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManualCliente" (
	-- Parametros entrada comunes en todos los servicios privados
	IN  p_IdEmpresa					BIGINT,
	-- Parametros entrada del servicio privado
	IN	p_IdTipo			NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion REFACTURACIONuracion (REFACTURACION))
	IN	p_Origen					NVARCHAR(40),--origen de la llamamda a REFACTURACIONurar
	IN  p_Transacciones				TABLE ("IdMovimiento" 	BIGINT --movimiento que origino la asignacion por REFACTURACIONuracion comp, movtocomp,asig,transac
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
											,"ValorRescateAsignacion" DECIMAL(26,8)
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
	-----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	DECLARE	v_FechaSistemaCreacion TIMESTAMP;
	DECLARE v_cantPort	BIGINT := 0;
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
				
	--Algoritmo de asignacion
			
		--Consulto las transacciones
		v_infoTransaciones = 	SELECT	p_TRA."IdMovimiento" 
									, tra."IdTransaccion" 
									, tra."FolioTransaccion"
									, tra."FechaSistemaCreacion" AS "FechaTransaccion"
									, tra."FechaOperacionTransaccion"
									, tra."IdInstrumento" 
									, tra."Instrumento" 
									, tra."IdTipoInstrumento"  
									, tra."IdClasificacionTipoInstrumento" 
									, tra."IdMonedaInstrumento"
									, tra."IdTipoOperacion"
									, tra."IdTipoProducto"
									, tra."IdBolsa" 
									, tra."IdLibroOrdenes"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" ELSE tra."CantidadDisponibleTransaccion" END AS "CantidadDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."CantidadAsignadaTransaccion" END AS "CantidadAsignadaTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."ValorRescate" ELSE tra."ValorRescateDisponibleTransaccion" END AS "ValorRescateDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."ValorRescateAsignadoTransaccion" END AS "ValorRescateAsignadoTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" ELSE tra."MontoDisponibleTransaccion" END AS "MontoDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE tra."MontoAsignadoTransaccion" END AS "MontoAsignadoTransaccion"
									, tra."IdCarga"
									, tra."CorteMinimo"
								FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS tra INNER JOIN :p_Transacciones p_TRA ON (tra."IdTransaccion" = p_TRA."IdTransaccion")
								ORDER BY tra."FechaOperacionTransaccion";
								
		v_infoPortafolios = SELECT  por."IdPortafolio"
									,p_por."Cantidad"
									,p_por."Porcentaje"
									,por."CodigoPortafolio"
									,por."DescripcionPortafolio"
									,por."Proposito"
									,cli."IdCliente"
									,cli."NombreCompleto"
									,cli."DocumentoIdentidad"
								FROM :p_Portafolios p_por 
								INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as por ON (por."IdPortafolio" = p_por."IdPortafolio")
								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (por."IdCliente" = cli."IdCliente");
			
		p_out_SimAsignacion = 	select "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL as "IdInterno"
										,"IdMovimiento"
										,v_FechaSistemaCreacion 	as "FechaSimulacion"
										,CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro"
										,"IdTransaccion"
										,"FolioTransaccion"
										,"FechaTransaccion"
										,0							as "FolioCartera"
										,"IdPortafolio"
										,"CodigoPortafolio"
										,"IdCliente"
										,"IdLibroOrdenes"
										,"IdInstrumento"
										,"Instrumento"
										,"IdTipoInstrumento"
										,"IdMonedaInstrumento"
										,cast(0 as BIGINT)			as "IdOrden"
										,"NombreCompleto"
										,"DocumentoIdentidad"
										,0 as "CantidadAsignacion"
										,0	as "ValorRescateAsignacion"
										,0 as "PorcentajeAsignacion"
										,cast(0 as DECIMAL(26,8))	as "PrecioSimulacion"
										,"IdTipoProducto"
										,"IdTipoOperacion"
										,'TRA' as "IdTipoCanalIngreso"
										,"IdBolsa"
									from :v_infoPortafolios por, :v_infoTransaciones tra
									order by "FechaOperacionTransaccion" desc;
			
			
			
END;

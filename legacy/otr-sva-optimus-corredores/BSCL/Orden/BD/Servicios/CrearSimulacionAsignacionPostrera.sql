SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPostrera";
GO
CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: PROJO - BCS
--  Fecha Creacion: 08/02/2015
--  
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionPostrera" (
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
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
				
	--Algoritmo de asignacion
			
		--Consulto las transacciones
		v_infoTransaciones = 	SELECT	p_TRA."IdMovimiento" 
									, tra."IdTransaccion" 
									, tra."Folio" AS "FolioTransaccion"
									, tra."FechaOperacion" AS "FechaOperacionTransaccion"
									, tra."FechaSistemaCreacion" as "FechaTransaccion"
									, tra."EsFungible"
									, tra."Tasa" 
									, tra."IdInstrumento" 
									, tra."Precio"
									, ins."Nemotecnico" AS "Instrumento" 
									, tra."IdTipoInstrumento" 
									, tra."IdClasificacionTipoInstrumento" 
									, tra."IdMoneda" AS "IdMonedaInstrumento"
									, tra."IdTipoOperacion" 
									, tipoope."IdTipoProducto" 
									, tra."IdCondicionDeLiquidacion" 
									, tra."IdBolsa" 
									, tra."IdLibroOrdenes"
									, tra."Cantidad" as "CantidadTotalTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" ELSE disponible."CantidadDisponible" END AS "CantidadDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE disponible."CantidadAsignada" END AS "CantidadAsignadaTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."ValorRescate" ELSE disponible."ValorRescateDisponible" END AS "ValorRescateDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE disponible."ValorRescateAsignado" END AS "ValorRescateAsignadoTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" ELSE disponible."MontoDisponible" END AS "MontoDisponibleTransaccion"
									, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE disponible."MontoAsignado" END AS "MontoAsignadoTransaccion"
									, itr."CorteMinimo"
								FROM :p_Transacciones p_TRA 
								INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS tra ON tra."IdTransaccion" = p_TRA."IdTransaccion"
								LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS disponible ON disponible."IdTransaccion" = tra."IdTransaccion"
								LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on tra."IdInstrumento" = ins."IdInstrumento"
								LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = tra."IdTipoOperacion"
								LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on tra."IdInstrumento" = itr."IdInstrumento" and tra."IdTipoReajuste" = itr."IdTipoReajuste"
								ORDER BY tra."FechaOperacion";
								
		v_infoPortafolios = SELECT  por."IdPortafolio"
										,p_por."Cantidad"
										,p_por."Porcentaje"
										,por."CodigoPortafolio"
										,cli."IdCliente"
										,cli."NombreCompleto"
										,cli."DocumentoIdentidad"
								FROM :p_Portafolios p_por  
								LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as por ON (por."IdPortafolio" = p_por."IdPortafolio")
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as cli ON (por."IdCliente" = cli."IdCliente");
		
		select 'v_infoPortafolios',* from :v_infoPortafolios;
						
		v_infoListaAsig = 	select 	"BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL as "Id"
									,tra."IdMovimiento"
										,por."IdPortafolio"
										,por."CodigoPortafolio"
										,por."IdCliente"
										,por."NombreCompleto"
										,por."DocumentoIdentidad"
										,por."Cantidad"
										,por."Porcentaje"
										,tra."IdInstrumento"
										,tra."Instrumento"
										,tra."IdTipoInstrumento"
										,tra."IdLibroOrdenes"
										,tra."IdTransaccion"
										,tra."FolioTransaccion"
										,tra."FechaTransaccion"
										,tra."IdTipoOperacion"
										,tra."IdMonedaInstrumento"
										,tra."EsFungible"
										,tra."Tasa"
										,tra."IdCondicionDeLiquidacion"
										,tra."CantidadDisponibleTransaccion"
										,tra."FechaOperacionTransaccion"
										,tra."Precio" AS "PrecioSimulacion"
										,case 
											when tra."EsFungible" = 'S' then "CantidadDisponibleTransaccion" 
											else 
												CASE WHEN "IdClasificacionTipoInstrumento" = 'RF' THEN TO_INTEGER("CantidadDisponibleTransaccion" * (por."Porcentaje" / 100) / tra."CorteMinimo") * tra."CorteMinimo"
												ELSE (tra."CantidadDisponibleTransaccion" * (por."Porcentaje" / 100)) 
												END
											end as "CantidadAsignar"
										,case
											when tra."EsFungible" = 'S' then ("ValorRescateDisponibleTransaccion" * (por."Porcentaje" / 100) ) 
											else "ValorRescateDisponibleTransaccion" 
											end as "ValorRescateAsignar"--para fungible
										,tra."IdTipoProducto"
										,'TRA' as "IdTipoCanalIngreso"
										,tra."IdBolsa"
								from :v_infoPortafolios por, :v_infoTransaciones tra
								order by "FechaOperacionTransaccion" desc;
								
		p_out_SimAsignacion = 	SELECT 		"Id"						as "IdInterno"
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
											,"IdLibroOrdenes"			as "IdLibroOrdenes"
											,"IdInstrumento"			as "IdInstrumento"
											,"Instrumento"				as "Instrumento"
											,"IdTipoInstrumento"		as "IdTipoInstrumento"
											,"IdMonedaInstrumento"		as "IdMonedaInstrumento"
											,cast(0 as BIGINT)			as "IdOrden"
											,"NombreCompleto"			as "NombreCompleto"
											,"DocumentoIdentidad"		as "DocumentoIdentidad"
											,"CantidadAsignar"			as "CantidadAsignacion"
											,"ValorRescateAsignar"		as "ValorRescateAsignacion"
											,"Porcentaje"				as "PorcentajeAsignacion"
											,"PrecioSimulacion"			as "PrecioSimulacion"
					
											,"IdTipoProducto"
											,"IdTipoOperacion"
											,"IdTipoCanalIngreso"
											,"IdBolsa"
									FROM :v_infoListaAsig 
									--WHERE "CantidadAsignada" > 0
									ORDER BY "Id";
			
			
END;
GO
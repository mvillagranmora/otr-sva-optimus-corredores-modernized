SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManual";
GO
CREATE PROCEDURE
-- 
--  Proposito: Generar Simulacion de Asignacion
--  Autor: PROJO - BCS
--  Fecha Creacion: 08/02/2015
--  FAlta calculo Pre PROm
"BSCL.Orden.BD.Servicios::CrearSimulacionAsignacionManual" (
	-- Parametros entrada comunes en todos los servicios privados
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	-- Parametros entrada del servicio privado
	IN	p_IdTipo			NVARCHAR(40) default 'ASIG',	--Tipo operacion a ejecutar(simulacion asignacion (ASIG) o simulacion REFACTURACIONuracion (REFACTURACION))
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
		DECLARE v_countTransacciones,v_countTranBloq,v_countTranRes,v_countOrdenes,v_countOrdBloq,v_countOrdRes
				,v_countPortafolios,v_CountTipoProg,v_TotalPortafolios,v_countTransaccionesGrupo,v_cantReg,v_i,v_CantTotalOrden	BIGINT := 0;
		DECLARE v_Id,v_IdPortafolio,v_IdOrden,v_IdInstrumento,v_IdTransaccion,v_CantidadDisponibleOrden,v_CantidadDisponibleTransaccion,v_CantidadAsignar,v_CantidadAsignada BIGINT := 0;
		DECLARE v_IdTipoOperacion	NVARCHAR(6);
		DECLARE v_IdMonedaInstrumento NVARCHAR(3);
		
		DECLARE v_asignacionCronologicaObligatoria    NVARCHAR(520);
		
		SELECT "Valor" INTO v_asignacionCronologicaObligatoria
		FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" 
		WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'AsignacionCronologicaObligatoria';

		IF :v_asignacionCronologicaObligatoria IS NULL THEN
		  v_asignacionCronologicaObligatoria := 'S';
		END IF;
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
					
		--Algoritmo de asignacion
			--Consulto las transacciones
			v_infoTransaciones = 	SELECT p_TRA."IdMovimiento"
										, tra."IdTransaccion"
										, tra."Folio" AS "FolioTransaccion"
										, tra."FechaSistemaCreacion" as "FechaTransaccion"
										, TO_DATE(TO_VARCHAR(tra."FechaOperacion",'YYYY-MM-DD')) AS "FechaOperacion"
										, tra."FechaOperacion" AS "FechaOperacionTransaccion"
										, tra."IdEmisor"
										, tra."EsVentaCorta"
										, tra."Cantidad" AS "CantidadTransaccion"
										, tra."Monto" AS "MontoTransaccion"
										, tra."Precio" AS "PrecioTransaccion"
										, tra."Tasa"
										, tra."Plazo"
										, tra."IdInstrumento"
										, tra."IdTipoReajuste"
										, inst."Nemotecnico" AS "Instrumento"
										, tra."IdMoneda" AS "IdMonedaInstrumento"
										, tra."IdInstrumentoSubyacente"
										, tra."IdTipoInstrumento"
										, tra."IdClasificacionTipoInstrumento"
										, tra."IdTipoOperacion"
										, tipoope."AccionOrden" as "AccionOrden"
										, tipoope."IdTipoProducto"
										, tra."IdCondicionDeLiquidacion"
										, tra."IdBolsa"
										, tra."IdLibroOrdenes"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" ELSE disp."CantidadDisponible" END AS "CantidadDisponibleTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE disp."CantidadAsignada" END AS "CantidadAsignadaTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" ELSE disp."MontoDisponible" END AS "MontoDisponibleTransaccion"
										, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE disp."MontoAsignado" END AS "MontoAsignadoTransaccion"
										, COALESCE(itr."CorteMinimo",0) AS "CorteMinimo" 
								FROM :p_Transacciones p_TRA 
								INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS tra ON (tra."IdTransaccion" = p_TRA."IdTransaccion")
								LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst ON  ( inst."IdInstrumento" = tra."IdInstrumento" )
								LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS tipoope ON  ( tipoope."IdTipoOperacion" = tra."IdTipoOperacion" )
								LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" AS disp ON  ( disp."IdTransaccion" = tra."IdTransaccion" )
								LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" itr on tra."IdInstrumento" = itr."IdInstrumento" and tra."IdTipoReajuste" = itr."IdTipoReajuste"
								ORDER BY tra."FechaOperacion";
								
			
			
			v_infoOrdenes =	SELECT ord."IdOrden"
								, ord."FechaOperacionOrden"
								, ord."IdEstadoOrden"
								, ord."CantidadOrden"
								, ord."MontoOrden"
								, ord."ValorRescate"
								, ord."IdTipoReajuste"
								, ord."PrecioOrden"
								, ord."Tasa"
								, ord."Plazo"
								, ord."EsVentaCorta"
								, ord."IdTipoPrecio"
								, ord."IdCondicionDeLiquidacion"
								, ord."IdTipoOperacion"
								, ord."IdCliente"
								, ord."NombreCompleto"
								, ord."DocumentoIdentidad"
								, ord."IdPortafolio"
								, ord."CodigoPortafolio"
								, ord."IdTipoCanalIngreso"
								, ord."IdLibroOrdenes"
								, ord."IdBolsa"
								, ord."IdInstrumento"
								, ord."Instrumento"
								, ord."IdTipoInstrumento"
								, ord."IdClasificacionTipoInstrumento"
								, ord."IdInstrumentoSubyacente"
								, ord."IdMonedaOrden"
								, ord."AccionOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN (ord."CantidadDisponibleOrden" + ord."CantidadAsignadaOrden") ELSE ord."CantidadDisponibleOrden" END AS "CantidadDisponibleOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE ord."CantidadAsignadaOrden" END AS "CantidadAsignadaOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN (ord."MontoDisponibleOrden" + ord."MontoAsignadoOrden") ELSE ord."MontoDisponibleOrden" END AS "MontoDisponibleOrden"
								, CASE WHEN p_IdTipo = 'REFACTURACION' or p_IdTipo = 'REASIGNACION' THEN 0 ELSE ord."MontoAsignadoOrden" END AS "MontoAsignadoOrden"
							FROM :p_Ordenes  AS p_ord INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrden" AS ord ON (ord."IdOrden" = p_ord."IdOrden") 
							ORDER BY "FechaOperacionOrden" asc;
			
			p_out_SimAsignacion =	SELECT "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion".NEXTVAL as "IdInterno"
									, tran."IdMovimiento" 		as "IdMovimiento"
									,v_FechaSistemaCreacion 	as "FechaSimulacion"
									,CURRENT_UTCTIMESTAMP 		as "FechaIngresoRegistro"
									, tran."IdTransaccion"					as "IdTransaccion"
									, tran."FolioTransaccion"				as "FolioTransaccion"
									, tran."FechaTransaccion"				as "FechaTransaccion"
									, 0										as "FolioCartera"
									, ord."IdPortafolio"					as "IdPortafolio"
									, ord."CodigoPortafolio"				as "CodigoPortafolio"
									, ord."IdCliente"						as "IdCliente"
									, ord."IdLibroOrdenes"					as "IdLibroOrdenes"
									, ord."IdInstrumento" 					as "IdInstrumento"
									, ord."Instrumento"						as "Instrumento"
									, ord."IdTipoInstrumento"				as "IdTipoInstrumento"
									, tran."IdMonedaInstrumento"			as "IdMonedaInstrumento"
									, ord."IdOrden"							as "IdOrden"
									, ord."NombreCompleto"					as "NombreCompleto"
									, DOC."DocumentoIdentidad"				as "DocumentoIdentidad"
									, cast(0 as DECIMAL(26,8))				as "CantidadAsignacion"
									, cast(0 as DECIMAL(26,8))				as "ValorRescateAsignacion"
									, cast(0 as DECIMAL(26,8))				as "PorcentajeAsignacion"
									, tran."PrecioTransaccion"				as "PrecioSimulacion"
									, tran."IdTipoProducto"					as "IdTipoProducto"
									, tran."IdTipoOperacion"				as "IdTipoOperacion"
									, ord."IdTipoCanalIngreso"				as "IdTipoCanalIngreso"
									, ord."IdBolsa"							as "IdBolsa"
								FROM :v_infoTransaciones AS tran INNER JOIN :v_infoOrdenes AS ord ON (
										tran."IdClasificacionTipoInstrumento" = ord."IdClasificacionTipoInstrumento" and
										tran."IdTipoInstrumento" 		= ord."IdTipoInstrumento" and
										tran."IdInstrumento" 			= ord."IdInstrumento" and
										(tran."IdBolsa" 				= ord."IdBolsa" OR ord."IdBolsa" = '0') and
										tran."IdTipoOperacion" 			= ord."IdTipoOperacion" and
										tran."IdCondicionDeLiquidacion" = ord."IdCondicionDeLiquidacion" and
										tran."EsVentaCorta" 			= ord."EsVentaCorta" and
										tran."FechaOperacionTransaccion"		>= CASE WHEN v_asignacionCronologicaObligatoria = 'S' THEN ord."FechaOperacionOrden" ELSE tran."FechaOperacionTransaccion" END  AND
								
										(
											--REQ ORDENES RF PERU:
											(tran."IdClasificacionTipoInstrumento" = 'RF' 
												and tran."Tasa" = ord."Tasa" 
												and tran."Plazo" = ord."Plazo" 
												--and tran."CantidadDisponibleTransaccion" = ord."CantidadDisponibleOrden"
											) or								
											(tran."IdTipoOperacion" = 'COMSIM' and /*tran."MontoDisponibleTransaccion" <= ord."MontoDisponibleOrden" and*/ tran."Tasa" >= ord."Tasa" and tran."Plazo" <= ord."Plazo") or
											(tran."IdTipoOperacion" = 'VENSIM' and tran."CantidadDisponibleTransaccion" = ord."CantidadDisponibleOrden" and tran."Tasa" <= ord."Tasa" and tran."Plazo" >= ord."Plazo" and tran."IdInstrumentoSubyacente" = ord."IdInstrumentoSubyacente") or
											(tran."IdTipoOperacion" != 'COMSIM' and tran."IdTipoOperacion" != 'VENSIM' and 
													(	ord."IdTipoPrecio" <> 'L' or
														(ord."IdTipoPrecio" = 'L' and ord."AccionOrden" = 'COMPRA' and ord."PrecioOrden" >= tran."PrecioTransaccion") or
														(ord."IdTipoPrecio" = 'L' and ord."AccionOrden" = 'VENTA' and ord."PrecioOrden" <= tran."PrecioTransaccion")
													)
											)
										)
									)
								LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON ord."IdCliente" = CLI."IdCliente" 
								LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona"  = PER."IdPersona" 
								LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad"  = DOC."IdDocumentoIdentidad" 
								;
			
		
END;
GO
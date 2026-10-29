SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CalcularNetoMM";
CREATE PROCEDURE
--  Proposito: 
--  Autor: - 
--  Fecha Creacion: 

 "BSCL.Orden.BD.Servicios::CalcularNetoMM" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_Modo						NVARCHAR(40) DEFAULT 'CREAR_ORDEN', --CALCULAR_NETO_Y_COBROS,CREAR_ORDEN, CREAR_ASIGNACION, ETC
	IN p_Operaciones				TABLE(
										"IdInterno"						BIGINT,
										"IdInstrumento"					BIGINT,
										"IdTipoOperacion"				NVARCHAR(6),
										"CantidadOperacionOrigen"		DECIMAL(26,8),
										"CantidadOperacion"				DECIMAL(26,8),
										"MontoNetoOperacionOrigen"		DECIMAL(26,8),
										"MontoNetoOperacion"			DECIMAL(26,8),
										"ValorRescateOperacionOrigen"	DECIMAL(26,8),
										"ValorRescateOperacion"			DECIMAL(26,8),
										"EsFungible"					NVARCHAR(1),
										"Precio"						DECIMAL(26,8)
										),

	
	OUT p_out_Operaciones			TABLE(
										"IdInterno"							BIGINT,
										"IdMonedaOperacion"					NVARCHAR(3),
										"ProporcionOperacion"				NVARCHAR(3),
										"CantidadOperacion"					DECIMAL(26,8),
										"ValorRescateOperacion"				DECIMAL(26,8),
										"MontoNetoOperacion"				DECIMAL(26,8)
										)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
			
	p_out_Operaciones = SELECT ope."IdInterno",
								CASE 
									WHEN ope."IdTipoOperacion" in ('COMSIM','VENSIM') THEN 'CLP'
									ELSE ins."IdMoneda" 
								END as "IdMonedaOperacion",
								CASE
									WHEN :p_Modo = 'CALCULAR_NETO_Y_COBROS' THEN 1
									WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'S' THEN
										ope."ValorRescateOperacion" / ope."ValorRescateOperacionOrigen"
									ELSE
										ope."CantidadOperacion" / ope."CantidadOperacionOrigen"
								END AS "ProporcionOperacion",
								CASE --cantidad
									WHEN ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion"  = 'COMSIM' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion"  = 'VENSIM' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion"  != 'VENSIM' and ope."IdTipoOperacion"  != 'COMSIM' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'DV' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'RF' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'N' THEN
										ope."CantidadOperacion"
									WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'S' THEN
										1
									ELSE
										0
								END as "CantidadOperacion",
							CASE 
								WHEN ins."IdClasificacionTipoInstrumento" = 'RV' THEN
									0
								WHEN ins."IdClasificacionTipoInstrumento" = 'DV' THEN
									0
								WHEN ins."IdClasificacionTipoInstrumento" = 'RF' THEN
									0
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'N' THEN
									ope."ValorRescateOperacionOrigen"
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'S' THEN
									ope."ValorRescateOperacion"
								ELSE
									0
							END as "ValorRescateOperacion",
							CASE 
								WHEN :p_Modo = 'CALCULAR_NETO_Y_COBROS' and ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion" = 'VENSIM' THEN
									ope."Precio" * ope."CantidadOperacion"
								WHEN :p_Modo = 'CALCULAR_NETO_Y_COBROS' and ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion" = 'COMSIM' THEN
									ope."MontoNetoOperacion"
								
								WHEN :p_Modo = 'CREAR_ORDEN' and ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion" = 'VENSIM' THEN
									ope."Precio" * ope."CantidadOperacion"
								WHEN :p_Modo = 'CREAR_ORDEN' and ins."IdClasificacionTipoInstrumento" = 'RV' and ope."IdTipoOperacion" = 'COMSIM' THEN
									ope."MontoNetoOperacion"
								
								WHEN ins."IdClasificacionTipoInstrumento" = 'RV' THEN
									ope."Precio" * ope."CantidadOperacion"
								WHEN ins."IdClasificacionTipoInstrumento" = 'DV' THEN
									ope."Precio" * ope."CantidadOperacion" * ins."Tamano"
								WHEN ins."IdClasificacionTipoInstrumento" = 'RF' THEN
									(ope."CantidadOperacion" / ope."CantidadOperacionOrigen") * ope."MontoNetoOperacionOrigen"
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'N' THEN
									(ope."CantidadOperacion" / ope."CantidadOperacionOrigen") * ope."MontoNetoOperacionOrigen"
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ope."EsFungible" = 'S' THEN
									(ope."ValorRescateOperacion" / ope."ValorRescateOperacionOrigen") * ope."MontoNetoOperacionOrigen"
								ELSE
									0
							END as "MontoNetoOperacion"
							
							
							FROM :p_Operaciones ope
							INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON ins."IdInstrumento" = ope."IdInstrumento"
							--INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tipoope ON tipoope."IdTipoOperacion" = ope."IdTipoOperacion" and tipoope."IdTipoInstrumento" = ins."IdTipoInstrumento"
							;
	

	/*p_out_Operaciones = SELECT ope."IdInterno",ope."IdMonedaOperacion",ope."ProporcionOperacion",ope."MontoNetoOperacion",  ope."CantidadOperacion", ope."ValorRescateOperacion"
							FROM :v_detalleOperaciones ope;*/


END;


SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearOrdenesasdf";

CREATE PROCEDURE
--  Proposito: crear orden masiva
--  Autor: - TEAM ORDENES 
--  Fecha Creacion: 20/07/2015  
"BSCL.Orden.BD.Servicios::CrearOrdenesasdf" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.

	IN p_Ordenes					TABLE(
										
										"PlazoSimultanea"					BIGINT,
										"PlazoVentaCorta"					BIGINT,
										"EsVentaCorta"						NVARCHAR(1),
										"EsOD"								NVARCHAR(1),
										"EsFungible"						NVARCHAR(1),
										"IdCorredorCorresponsal"			BIGINT,
										"MontoPlazo"						DECIMAL(26,8),
										"IdPersonaAutorizadoDarOrdenes"		BIGINT,
										"IdTipoCanalDistribucion"			NVARCHAR(3)
										),

										
										
 
    									
 
    
    OUT p_out_InformacionContador 			INT,
	OUT p_out_InformacionDetalle 			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) default empty,
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000))  default empty,	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))  default empty
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" default select 0 as "IdInterno",0 as "IdOperacion",'' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" from dummy where 1=2;
	
	/*DECLARE v_out_ErroresContador_CrearGarantiasOrdenes			INT;
	DECLARE v_out_AdvertenciasContador_CrearGarantiasOrdenes	INT;
	DECLARE v_out_AdvertenciasDetalle_CrearGarantiasOrdenes		
		TABLE ("IdGenerarMovimientoCustodia" BIGINT,"Codigo"	NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion"	NVARCHAR(5000)) ;
	DECLARE v_out_ErroresDetalle_CrearGarantiasOrdenes			TABLE ("IdGenerarMovimientoCustodia" BIGINT,"Codigo"	NVARCHAR(100),"Descripcion"	NVARCHAR(5000)) ;
	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :v_Errores,
		
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		
		p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
								SELECT "IdGenerarMovimientoCustodia" as "IdInterno", "IdGenerarMovimientoCustodia" as "IdOperacion", "Codigo","Descripcion"
								FROM :v_out_ErroresDetalle_CrearGarantiasOrdenes;
		
		p_out_AdvertenciasDetalle = SELECT * FROM :p_out_AdvertenciasDetalle UNION ALL
								SELECT "IdGenerarMovimientoCustodia" as "IdInterno", "IdGenerarMovimientoCustodia" as "IdOperacion", "RequiereAutorizacion","Codigo","Descripcion"
								FROM :v_out_AdvertenciasDetalle_CrearGarantiasOrdenes;
				
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
		
		drop TABLE #ORDENESSS;
		DROP TABLE #COBROS;
		
		call "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR ORDENES','FIN','FIN');
	END;*/

	p_out_AdvertenciasContador							:= 0;
	p_out_ErroresContador								:= 0;
	--v_out_ErroresContador_CrearGarantiasOrdenes			:= 0;
	--v_out_AdvertenciasContador_CrearGarantiasOrdenes	:= 0;
		
	
	
END;

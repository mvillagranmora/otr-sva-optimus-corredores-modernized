SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::RetirarOfertasOrdenes";
GO
CREATE PROCEDURE
-- 
--  Proposito: Ofertar un conjunto de ordenes
--  Autor: rulloa
--  Fecha Creacion: 07/01/2016
--  
"BSCL.Orden.BD.Servicios::RetirarOfertasOrdenes" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3), 
	
	--ENUMERAR VARIABLES DE ENTRADA
	IN p_Ordenes						  table ("IdOrden" BIGINT),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT, "Codigo" nvarchar(40),"Descripcion" nvarchar(520))  DEFAULT EMPTY
	
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT  SELECT 0 as "IdInterno",0 as "IdOperacion", '' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	       	
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
		
		p_out_ErroresContador = 0;
		
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	
	BEGIN
		
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		--CHECK ORDENES SIN ASIGNACIONES, 
		CREATE LOCAL TEMPORARY TABLE #RESULTADO_RetirarOfertasOrdenes (
			"IdOrden" BIGINT,
			"Resultado" 	NVARCHAR(40),
			"Codigo" nvarchar(40)
		);
		
		
				
		INSERT INTO #RESULTADO_RetirarOfertasOrdenes
		SELECT ord."IdOrden",'RECHAZO' AS "Resultado" ,CASE WHEN COUNT(asig."IdAsignacion") > 0 THEN 'ORDEN_POSEE_ASIGNACIONES' ELSE 'OK' END AS "Codigo"
		FROM :p_Ordenes p_ord 
		INNER JOIN "BSCL.Orden.BD::ORD.Orden" ord ON p_ord."IdOrden" = ord."IdOrden"
		LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" asig ON asig."IdOrden" = ord."IdOrden" and asig."IdEstadoAsignacion" != 'ANU'
		GROUP BY ord."IdOrden";
		
		v_ordenes_temp = 
		SELECT p_ord."IdOrden" as "IdInterno",p_ord."IdOrden"
		FROM :p_Ordenes p_ord
        INNER JOIN #RESULTADO_RetirarOfertasOrdenes res on res."IdOrden" = p_ord."IdOrden" AND "Codigo" = 'OK';
		
		call "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
		p_Ordenes 				=> :v_ordenes_temp,
		p_NuevoEstado 			=> 'ING',
		p_out_SalidaDetalle		=> v_resultado,
		p_out_SalidaResumen		=> v_out_SalidaResumen);
		
		/** Test Update Fecha/IdBolsaDistribucion **/
		UPDATE ORD
		SET
			ORD."FechaOferta" = '0001-01-01 00:00:00.0',
			ORD."IdBolsaDistribucion" = ''
		FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
		INNER JOIN :v_ordenes_temp as ORDTEMP ON (ORDTEMP."IdOrden" = ORD."IdOrden");

		
		v_Errores = SELECT "IdOrden" AS "IdInterno","IdOrden" AS "IdOperacion","Resultado","Codigo", 'IdOrden' AS "CodigoDato1",CAST("IdOrden" AS NVARCHAR(50)) AS "ValorDato1" , '' AS "CodigoDato2" , '' AS "ValorDato2" 
					FROM #RESULTADO_RetirarOfertasOrdenes WHERE "Codigo" != 'OK'
					UNION ALL 
					SELECT "IdOrden" AS "IdInterno","IdOrden" AS "IdOperacion",'INFORMACION' AS "Resultado",'OFERTA_RETIRADA' AS "Codigo", 'IdOrden' AS "CodigoDato1",CAST("IdOrden" AS NVARCHAR(50)) AS "ValorDato1" , '' AS "CodigoDato2" , '' AS "ValorDato2" 
					FROM #RESULTADO_RetirarOfertasOrdenes WHERE "Codigo" = 'OK';
		DROP TABLE #RESULTADO_RetirarOfertasOrdenes;
		SIGNAL SALIDA_CONTROLADA;
		
	END;
END;
GO

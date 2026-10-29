SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Tesoreria.CrystalReports.Servicios::LiquidacionBilateral";
 
CREATE PROCEDURE
-- 
--  Proposito: Obtiene data para reporte de liquidacion bilateral(pago a corredores contrapartes).
--             La salida debe ser por ResulSet y no por variable output para que el Cristal Report lo tome.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/08/2016
--
"BSCL.Tesoreria.CrystalReports.Servicios::LiquidacionBilateral" (

    IN p_IdEmpresa             				BIGINT,
    IN p_IdUsuario              			BIGINT,
    IN p_IdModulo               			NVARCHAR(3),

    IN p_FechaLiquidacion	    			NVARCHAR(10),
    IN p_ClasificacionTipoInstrumento  		NVARCHAR(3),       -- RV,RF,IF,.. (Ex Mercado)
    IN p_TipoInstrumento               		NVARCHAR(3),       
    IN p_CondicionDeLiquidacion        		NVARCHAR(2)        -- T0,T1,T2,T3	 
 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
     	 
AS
  BEGIN

	
	--------------------
	--LOGICA
	--------------------
	BEGIN

		DECLARE v_AdvertenciasContador  INTEGER;
		DECLARE v_ErroresContador       INTEGER;			
	    DECLARE v_ClasificacionTipoInstrumento  		TABLE  ("IdClasificacionTipoInstrumento" 	NVARCHAR(3));  -- RV,RF,IF.. (Ex Mercado)
	    DECLARE v_TipoInstrumento               		TABLE  ("IdTipoInstrumento"        NVARCHAR(3));           -- ACN..
	    DECLARE v_CondicionDeLiquidacion        		TABLE  ("IdCondicionDeLiquidacion" NVARCHAR(2));           -- T0,T1,T2,T3


		v_ClasificacionTipoInstrumento 	   =  SELECT '' as "IdClasificacionTipoInstrumento"  FROM DUMMY WHERE 1=2;	
		v_TipoInstrumento 				   =  SELECT '' as "IdTipoInstrumento"               FROM DUMMY WHERE 1=2;	
		v_CondicionDeLiquidacion 		   =  SELECT '' as "IdCondicionDeLiquidacion"        FROM DUMMY WHERE 1=2;
		
		
		-------------------------------------------------------------
		-- OBTIENE DATOS DE CORREDORA 
		-------------------------------------------------------------
		v_datosGenerales = select
				"RazonSocial" AS "RazonCorredora",
				"RUT" AS "RutCorredora",
				"Direccion" AS "DireccionCorredora",
				"Telefono" AS "TelefonoCorredora"
				from "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
				WHERE "IdEmpresa" = :p_IdEmpresa;
		v_logo = select TOP 1
				"Binario" AS "LogoCorredora"
				from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
				WHERE "NombreImagen" = 'LOGO_CORREDOR' AND "IdEmpresa" = :p_IdEmpresa ORDER BY "Version" DESC;	
				
		v_datosCorredora = 
			 SELECT *
			 FROM :v_logo
			 CROSS JOIN :v_datosGenerales;


		----------------------------------------------------------------------------------
        -- FORMATEA PARAMETROS DE CONSULTA DE LIQUIDACION BILATERAL
		----------------------------------------------------------------------------------

		-- Convierte arreglo(ClasificacionTipoInstrumento) en tabla
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitString"
		(
			p_String      	=> :p_ClasificacionTipoInstrumento,
			p_Delimitador   => ',',
			p_out_Elementos => v_SplitClasificacionTipoInstrumento
		);
		v_ClasificacionTipoInstrumento = SELECT "Item" as "IdClasificacionTipoInstrumento"  FROM :v_SplitClasificacionTipoInstrumento;


		-- Convierte arreglo(TipoInstrumento) en tabla
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitString"
		(
			p_String      	=> :p_TipoInstrumento,
			p_Delimitador   => ',',
			p_out_Elementos => v_SplitTipoInstrumento
		);
		v_TipoInstrumento = SELECT "Item" as "IdTipoInstrumento"  FROM :v_SplitTipoInstrumento;
		
		
		-- Convierte arreglo(CondicionDeLiquidacion) en tabla
		CALL "BSCL.Plataforma.BD.Utilitarios::SplitString"
		(
			p_String      	=> :p_CondicionDeLiquidacion,
			p_Delimitador   => ',',
			p_out_Elementos => v_SplitCondicionDeLiquidacion
		);
		v_CondicionDeLiquidacion = SELECT "Item" as "IdCondicionDeLiquidacion"  FROM :v_SplitCondicionDeLiquidacion;
		


		----------------------------------------------------------------------------------
        -- OBTIENE DETALLE DE LIQUIDACION BILATERAL
		----------------------------------------------------------------------------------
		CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateral"
		( 
		     p_IdEmpresa 						=> :p_IdEmpresa
		    ,p_IdUsuario 						=> :p_IdUsuario
		    ,p_IdModulo 						=> :p_IdModulo
		    ,p_FechaLiquidacion					=> :p_FechaLiquidacion
		    ,p_ClasificacionTipoInstrumento 	=> :p_ClasificacionTipoInstrumento
		    ,p_TipoInstrumento       			=> :p_TipoInstrumento
		    ,p_CondicionDeLiquidacion       	=> :p_CondicionDeLiquidacion
		    ,p_out_LiquidacionBilateralDetalle 	=> v_LiquidacionBilateralDetalle
		    ,p_out_LiquidacionBilateralResumen 	=> v_LiquidacionBilateralResumen
			,p_out_ErroresContador 				=> v_ErroresContador
			,p_out_ErroresDetalle 				=> v_ErroresDetalle
		);



		----------------------------------------------------------------------------------
        -- SALIDA PARA REPORTE (Debe ser por ResulSet y no por variable tipo output)
		----------------------------------------------------------------------------------
		SELECT
			 "DocumentoIdentidadCorredoraContraparte" AS "DocumentoIdentidadContraparte"
			,"NombreCompletoCorredoraContraparte" AS "NombreContraparte"
			,"CodigoInscripcionCorredoraContraparte" AS "CodigoContraparte"
			,"IdPersonaCorredoraContraparte" AS "IdPersonaContraparte"
			,"IdBolsa"
			,"IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento"
			,"FechaSistemaCreacion"
			,"Nemotecnico"
			,"FolioTransaccion"
			,"CantidadTransaccion"
			,"PrecioTransaccion"
			,"IdCondicionDeLiquidacion"
			,"CodigoCondicionDeLiquidacion"
			,"MontoCompra"
			,"MontoVenta"
			,"AccionTipoOperacion"
			,"IdMonedaTransaccion"
			,"IdComprobanteTesoreria"
			,"NombreTipoOperacion"
			,"IdMonedaLiquidacion"
			,"RazonCorredora"
			,"RutCorredora"
			,"DireccionCorredora"
			,"TelefonoCorredora"
			,"LogoCorredora"
		FROM :v_LiquidacionBilateralDetalle
		CROSS JOIN :v_datosCorredora;
                                                                      
											 
  	END;
    
END;

  

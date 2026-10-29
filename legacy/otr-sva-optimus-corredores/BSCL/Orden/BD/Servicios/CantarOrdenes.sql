SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CantarOrdenes";
GO 
CREATE PROCEDURE
-- 
--  Proposito: Cantar un conjunto de ordenes
--  Autor: rulloa
--  Fecha Creacion: 07/01/2016
--  
"BSCL.Orden.BD.Servicios::CantarOrdenes" ( 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3), 
	
	IN p_Ordenes						  table ("IdInterno" BIGINT,"IdOrden" BIGINT,"IdBolsa" NVARCHAR(3)),
	
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle";
	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
	
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
		

		v_out_SalidaResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		
		DROP TABLE #ORD;
	
	END;
	
	BEGIN
		DECLARE v_FechaOperacionSecondate 	SECONDDATE;
		DECLARE	v_resultadoRegla   NVARCHAR(40);
		
		v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'' AS "Resultado",'' AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM DUMMY WHERE 1=2;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaOperacionSecondate);	
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalleBasicos,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion",'RECHAZO' AS "Resultado","Codigo" AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" 
					FROM :v_out_ErroresDetalleBasicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
    	CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
    		p_Ordenes				=> :p_Ordenes,	
    		p_NuevoEstado			=> 'OFE',
			p_out_SalidaDetalle		=> v_out_Resultado,
			p_out_SalidaResumen		=> v_out_SalidaResumen
		);
		
			
		v_res_ok = SELECT RS."Resultado",RS."IdInterno",ORD."IdOrden",TBO."Nemotecnico"
				   FROM :v_out_Resultado AS RS
				   LEFT JOIN :p_Ordenes AS ORD ON ORD."IdInterno" = RS."IdInterno"
				   LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS TBO ON TBO."IdOrden"=ORD."IdOrden"
				   WHERE RS."Resultado" ='OK';
		
		
		-- INSERTA TAG PARA REFRESCAR GRILLA DE NEGOCIACION DE ORDENES
		INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"
		("IdEmpresa","FechaRegistro","Evento","DATA","EsProcesado")
		SELECT
		:p_IdEmpresa,
		CURRENT_UTCTIMESTAMP,
		'ORDEN_OFERTADA',
		'',
		'N'  
		from :v_res_ok;
		
		update ord 
		set "IdTipoCanalDistribucion" = 'MAN', "IdBolsaDistribucion" = "IdBolsa","FechaOferta" = :v_FechaOperacionSecondate
		FROM "BSCL.Orden.BD::ORD.Orden" ord 
		INNER JOIN :v_out_Resultado res on ord."IdOrden" = res."IdOperacion" and "Resultado" = 'OK';
		
		--------------------------------------------------------------------------------------------------------------------
		--Notificar cambio de estado ordenes
		--------------------------------------------------------------------------------------------------------------------
		v_ordenes_notificar = SELECT "IdOperacion" AS "IdOrden" FROM :v_out_Resultado;
		/*
		CALL "BSCL.Orden.BD.Servicios::NotificarEstadoOrden"(
		    p_IdEmpresa		=> :p_IdEmpresa,	
    		p_IdUsuario		=> :p_IdUsuario,
			p_IdModulo		=> :p_IdModulo,
			p_Ordenes		=> :v_ordenes_notificar
		);
		*/
		
	END;
END;

SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ConsultarContratoCustodiaCliente";
GO 

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ConsultarContratoCustodiaCliente" (


	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa  			BIGINT 			default 0,
    IN p_IdUsuario  			BIGINT 			default 0,
    IN p_IdModulo   			NVARCHAR(3) 	default '',
    
	IN p_IdCliente				BIGINT 			default 0,
	IN p_IdPortafolio 			BIGINT 			DEFAULT 0,
	IN p_IdMercado 				NVARCHAR(3) 	DEFAULT 'RV',  --Mientras aun no se implementa VC RF
	
	OUT p_out_TieneContrato 	NVARCHAR(1)
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_TieneContrato = 'N';
	END;
  	
	BEGIN
	
		SELECT IFNULL(indicador."ValorIndicador", 'N') INTO p_out_TieneContrato
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indicador
		WHERE indicador."IdCliente" = p_IdCliente AND indicador."CodigoTipoIndicador" = 'CC';


/*		SELECT top 1 IFNULL("ValorIndicador",'N') INTO p_out_TieneContrato
		FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO 
		LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
		WHERE 
			"CodigoTipoIndicador" = CASE WHEN :p_IdMercado = 'RF' THEN 'ORDOCUSTRF' ELSE 'ORDOCUSTRV' END  
			AND ATR."IdTipoEntidad" = 'PORTAFOLIO' 
			AND ATR."IdEntidad" = TO_VARCHAR(p_IdPortafolio);	  
*/           
	END;
END; 
go
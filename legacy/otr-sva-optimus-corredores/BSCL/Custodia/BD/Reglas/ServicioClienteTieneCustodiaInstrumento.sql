SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Reglas::ServicioClienteTieneCustodiaInstrumento";

CREATE PROCEDURE
-- 
--  Proposito: Validar los requisitos para la generación de comprobantes de tesoreria
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
--     
"BSCL.Custodia.BD.Reglas::ServicioClienteTieneCustodiaInstrumento"(
	-- Parametros entrada  
	IN p_IdEmpresa 			BIGINT, 
	IN p_IdCliente			BIGINT,
	IN p_IdPortafolio 		BIGINT default 0,
	IN p_IdInstrumento 		BIGINT,

	OUT p_out_TieneCustodia VARCHAR(1)  -- S/N

) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_IdCartera 			BIGINT := 0;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_TieneCustodia := 'N';
	END;
	

/*	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
								
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		
	END; 
*/

 	p_out_TieneCustodia := 'N';	
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;

	
	BEGIN
	
		SELECT top 1 "IdCartera" INTO v_IdCartera 
		from "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos"
		WHERE "IdEmpresa" = p_IdEmpresa
		and "IdCliente" = p_IdCliente
		and "IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end
		and "IdInstrumento" = p_IdInstrumento;
		
		if coalesce(v_IdCartera,0) = 0 then
			p_out_TieneCustodia := 'N';
		else
			p_out_TieneCustodia := 'S';	
		end if;
	

    END;	
END;

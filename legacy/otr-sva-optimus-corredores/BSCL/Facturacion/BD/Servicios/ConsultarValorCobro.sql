SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ConsultarValorCobro";
GO 
CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::ConsultarValorCobro" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa  			BIGINT 			default 0,
    IN p_IdUsuario  			BIGINT 			default 0,
    IN p_IdModulo   			NVARCHAR(3) 	default '',
	IN p_IdCobro				NVARCHAR(40)	default '',
	
	OUT p_out_ValorCobro 		BIGINT
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_Sistema                     NVARCHAR(2);

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_ValorCobro = 0;
	END;
  	
	BEGIN
	
	v_Sistema:= '';
		
    SELECT "Valor" INTO v_Sistema  
    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.LocalizacionSistema';	
        
	IF (v_Sistema = 'CL') THEN 	
		SELECT top 1 IFNULL("ValorCobro",0) INTO p_out_ValorCobro
		FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarCobro"  
		WHERE "IdCobro" = p_IdCobro;
	END IF;
	
	IF (v_Sistema = 'PE' AND p_IdCobro = 'IVA') THEN
		SELECT top 1 IFNULL("PorcentualFijo_ValorCobro",0) INTO p_out_ValorCobro 
		FROM "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DEF
		LEFT JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro" CLASE ON DEF."IdClaseCobro" = CLASE."IdClaseCobro"
		INNER JOIN "BSCL.Plataforma.BD::COM_2.TipoCobro" TIPO ON DEF."IdTipoCobro" = TIPO."IdTipoCobro"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO ON DEF."IdTipoProducto" = PRO."IdTipoProducto"
		WHERE "CodigoDefinicionCobro" = 'IGV_OC';
	END IF;
	
	SELECT p_out_ValorCobro FROM dummy;
		
	END;
END; 
go
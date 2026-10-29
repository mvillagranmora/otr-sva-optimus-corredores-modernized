SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionUsuario";

-- 
--  Proposito: 
--  Fecha Creacion: 08/12/2015
--  

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionUsuario" (
	IN p_IdEmpresa		BIGINT,		
	IN p_in_Usuarios	TABLE ("IdUsuario" BIGINT),
									
	--Salida
   	OUT P_OUT_ERROR				NVARCHAR(500),
   	OUT p_out_configuracion		TABLE ("IdUsuario" BIGINT, "Codigo" NVARCHAR(100),"Valor" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresDetalle	TABLE ("IdInterno"	NVARCHAR(40), "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN SEQUENTIAL EXECUTION

	--- Controlado
	DECLARE TipoComprobanteDefecto NVARCHAR(3);
	DECLARE SALIDA_CONTROLADA CONDITION;    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
	END;	
			
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		P_OUT_ERROR := SUBSTRING(::SQL_ERROR_MESSAGE,1,500);
	END;
	
	p_out_configuracion =		
		SELECT
			Usuario."IdUsuario",
			'PERMITEDISTRI' as "Codigo",
			CASE WHEN COALESCE(Rol."NombreRol",'') = '' THEN 'N' ELSE 'S' END as "Valor",
			CASE WHEN COALESCE(Rol."NombreRol",'') = '' THEN 'UI_SIN_ROL_DISTRIBUCION' ELSE '' END as "Descripcion"
		FROM :p_in_Usuarios Usuario
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" Configuracion
			ON Configuracion."CodigoParametro" = 'ROLPERMITEDISTRI'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario" Rol
			ON Usuario."IdUsuario" = Rol."IdUsuario" AND Configuracion."Valor" = Rol."NombreRol"
		
    	UNION ALL
    	
		SELECT
			Usuario."IdUsuario",
			'PERMITEINTERB' as "Codigo",
			CASE WHEN COALESCE(Rol."NombreRol",'') = '' THEN 'N' ELSE 'S' END as "Valor",
			CASE WHEN COALESCE(Rol."NombreRol",'') = '' THEN 'UI_SIN_ROL_INTERBANCARIO' ELSE '' END as "Descripcion"
		FROM :p_in_Usuarios Usuario
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" Configuracion
			ON Configuracion."CodigoParametro" = 'ROLPERMITEINTERB'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario" Rol
			ON Usuario."IdUsuario" = Rol."IdUsuario" AND Configuracion."Valor" = Rol."NombreRol"
		
		UNION ALL
		
		SELECT
			Usuario."IdUsuario",
			'ESTADOMESA' as "Codigo",
			COALESCE(Mesa."IdEstadoMesa",'C')as "Valor",
			CASE WHEN COALESCE(Mesa."IdEstadoMesa",'C') = 'C' THEN 'UI_MESA_CERRADA' ELSE '' END as "Descripcion"
		FROM :p_in_Usuarios Usuario
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" Mesa 
	    	ON Mesa."IdTipoProducto" = 'MEME' AND Mesa."IdEstadoRegistro" = 'ACT'
	    
	    UNION ALL
	    
		SELECT
			Usuario."IdUsuario",
			Configuracion."CodigoParametro" as "Codigo",
			"Valor",
			'' as "Descripcion"
		FROM :p_in_Usuarios Usuario		
		LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" Configuracion on 1=1
		WHERE "CodigoParametro" = 'MARGENBIDASK';
	
	
    p_out_ErroresDetalle =
	    SELECT
	    	 "IdUsuario" as "IdInterno"
	    	,"Codigo"
	    	,"Descripcion"
	    FROM :p_out_configuracion
	    WHERE "Descripcion" != '';
    		
    		
	SIGNAL SALIDA_CONTROLADA;
	
END;
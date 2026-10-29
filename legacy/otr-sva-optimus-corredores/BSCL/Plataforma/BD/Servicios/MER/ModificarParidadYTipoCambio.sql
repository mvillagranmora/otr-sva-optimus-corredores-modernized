SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ModificarParidadYTipoCambio";

CREATE PROCEDURE
--  Proposito: Modificar paridades
--  Autor: - TEAM ORDENES 
--  Fecha Creacion: 20/07/2015 
 
"BSCL.Plataforma.BD.Servicios::MER.ModificarParidadYTipoCambio" ( 
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
	
	--"Fecha","IdMoneda","TipoRegistro"
	
	IN p_Registros			TABLE(
								"Fecha"					DATE,
								"IdMoneda"				NVARCHAR(3),
								"IdTipoRegistro"		NVARCHAR(40),
								"ParidadMonedaCambio"	DECIMAL(26,8)							
		
	),
    
    OUT p_out_Resultado		TABLE(
    							"Fecha"					DATE,
								"IdMoneda"				NVARCHAR(3),
								"IdTipoRegistro"		NVARCHAR(40),
								"ParidadMonedaCambio"	DECIMAL(26,8)	
    ),						
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	
	p_out_AdvertenciasContador = 0;
	p_out_ErroresContador = 0;
		
	UPDATE par SET 
		"ParidadMonedaCambio" = COALESCE(reg."ParidadMonedaCambio",par."ParidadMonedaCambio"),
		"IdUsuarioUltimaModificacion" = :p_IdUsuario,
		"FechaUltimaModificacion" = CURRENT_TIMESTAMP
	FROM "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" par
	INNER JOIN :p_Registros reg on par."Fecha" = reg."Fecha" AND par."IdMoneda" = reg."IdMoneda" AND par."IdTipoRegistro" = reg."IdTipoRegistro"
	;
	
	
	
	p_out_Resultado = select "Fecha","IdMoneda","IdTipoRegistro","ParidadMonedaCambio" from :p_registros;
	
	
	
END;

CALL "BSCL"."BSCL.Plataforma.BD::LIM.Servicios.ConsultarLineaRiesgo" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS
    1,--IN p_IdEmpresa          BIGINT,
    1,--IN p_IdUsuario          BIGINT,
    'LIM',--IN p_IdModulo           NVARCHAR(3),
    'S',--IN p_UsaReglasNegocio	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
    
    --ENUMERAR VARIABLES DE ENTRADA
    '',--IN p_CodigoAgente		NVARCHAR(40),
    '',--IN p_IdTipoOperacion	NVARCHAR(6),
    0,--IN p_IdPortafolio		BIGINT,

	--ENUMERAR VARIABLES DE SALIDA	
	?,--OUT p_out_linea_riesgo	TABLE ( "IdTipoLinea" NVARCHAR(40),"MontoDisponible" DECIMAL(26,8),
		--							"MontoUtilizado" DECIMAL(26,8)),

	-- Salidas para mensajes de advertencias y errores
	?,--OUT p_out_AdvertenciasContador int,
	?,--OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	?,--OUT p_out_ErroresContador int,
	?--OUT p_out_ErroresDetalle table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
)   
commit

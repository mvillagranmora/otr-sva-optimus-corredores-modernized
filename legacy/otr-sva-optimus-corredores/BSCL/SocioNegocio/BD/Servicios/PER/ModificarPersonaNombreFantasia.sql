SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaNombreFantasia" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de Nombres de Fantasia.
--  Autor: CAponte - BCS
--  Fecha Creacion: 08/02/2016
-- 

"BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaNombreFantasia" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

    IN  p_IdPersona                 BIGINT,
    IN  p_IdNombreFantasia          BIGINT,
	IN	p_NombreFantasia			NVARCHAR(200),
	IN  p_IdEstado	 				NVARCHAR(3),
	-- Parametros salida del servicio publico
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNombreFantasia			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='MODNFA';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
    	---------------------------------------------------

		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
		  CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
		  
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------
          p_DetalleLog =
            SELECT 
                :p_IdUsuario AS "SubRegistro",
                'NombreFantasia' AS "CampoRegistro",
                "DescripcionNombreFantasia" AS "ValorAntiguo",
                :p_NombreFantasia AS "ValorNuevo"
             FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" WHERE "IdNombreFantasia" = p_IdNombreFantasia AND "IdPersona" = p_IdPersona 
             UNION ALL
             SELECT
                :p_IdUsuario AS "SubRegistro",
                'Estado' AS "CampoRegistro",
                "IdEstado" AS "ValorAntiguo",
                :p_IdEstado AS "ValorNuevo"
             FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" WHERE "IdNombreFantasia" = p_IdNombreFantasia AND "IdPersona" = p_IdPersona;
            
            CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
                p_IdTipoLog         => 'MODIFICACION',
                p_IdModulo          => :p_IdModulo,
                p_IdUsuario         => :p_IdUsuario,
                p_IdEmpresa         => :p_IdEmpresa,
                p_FechaSistemaLog   => TO_VARCHAR(v_FechaIngreso, 'YYYY-MM-DD'),
                p_IdUsuarioAtendido => :p_IdUsuario,
                p_IdPersona			=> :p_IdPersona,
                p_IdTipoRegistro    => 'SOC',
                p_DetalleLog        => :p_DetalleLog
            );
            
            
          UPDATE "BSCL.SocioNegocio.BD::PER.NombreFantasia"
          SET "DescripcionNombreFantasia" = p_NombreFantasia, "IdEstado" = p_IdEstado, "FechaModificacion" = CURRENT_UTCTIMESTAMP
          WHERE "IdNombreFantasia" = p_IdNombreFantasia AND "IdPersona" = p_IdPersona;
          
          
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
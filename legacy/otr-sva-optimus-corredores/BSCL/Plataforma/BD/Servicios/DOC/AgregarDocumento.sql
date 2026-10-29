SET SCHEMA "BSCL";

DROP PROCEDURE   "BSCL.Plataforma.BD.Servicios::DOC.AgregarDocumento";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para agregar un Tipo de documento a la lista disponible en Mantenedor Documentos.
--  Autor: - CAPONTE
--  
"BSCL.Plataforma.BD.Servicios::DOC.AgregarDocumento" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	
		  
	-- Parametros entrada del servicio publico
	IN	p_IdCliente					BIGINT,
	IN	p_IdTipoDocumento      		NVARCHAR(40),
	IN	p_NumeroFirmantesEmpresa    INT,
	IN	p_NumeroFirmantesCliente    INT,
	IN  p_FechaVigencia				NVARCHAR(10),
	IN  p_NombreFirmanteEmpresa1	NVARCHAR(100) default '',
	IN  p_NombreFirmanteEmpresa2	NVARCHAR(100) default '',
	IN  p_NombreFirmanteEmpresa3	NVARCHAR(100) default '',
	IN  p_NombreFirmanteCliente1	NVARCHAR(100) default '',
	IN  p_NombreFirmanteCliente2	NVARCHAR(100) default '',
	IN  p_NombreFirmanteCliente3	NVARCHAR(100) default '',
	
	
	--IN	p_IdEstado								NVARCHAR(3),
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
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
		DECLARE v_IdTipoAtributo		 		bigint:=0;
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
					
		DECLARE v_countAtributo	 		   bigint:=0;
		--------------------
		--Validaciones
		--------------------
		

	BEGIN

			---Verifica si el Atributo no este registrado
			/*SELECT COUNT(1) INTO v_countAtributo
			FROM "BSCL.Plataforma.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = :p_IdTipoDocumento 
				AND "NombreDocumento" = :p_NombreDocumento
				AND "IdEmpresa"= :p_IdEmpresa;
			IF(v_countAtributo>0) THEN
				p_out_ErroresDetalle =  select 'DOCUMENTO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;		*/					
	END;		

		
		
				
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			
			    -- recuperamos numero de secuencia que usaremos para el id del documento
	
			p_out_RefId:=v_IdTipoAtributo;
			
		 	------------------------------------------
			--Crear Anexo
			------------------------------------------
			
			
					insert into "BSCL.Plataforma.BD::DOC.Anexo"
					(
						"IdAnexo",
						"IdDocumento",
						"IdEstado",
						"NumeroFirmantesEmpresa",
						"NumeroFirmantesCliente",
						"NombreFirmanteEmpresa1", 
						"NombreFirmanteEmpresa2",
						"NombreFirmanteEmpresa3",
						"NombreFirmanteCliente1",
						"NombreFirmanteCliente2",
						"NombreFirmanteCliente3",
						"FechaIngreso",
						"FechaModificacion",
						"FechaVigencia",
						"DocumentoFisicoReferencia"	
					)
					values(
					"BSCL.Plataforma.BD.Secuencias::Anexo".NEXTVAL,
					"BSCL.Plataforma.BD.Secuencias::Documento".NEXTVAL,
					'PEN',
					p_NumeroFirmantesEmpresa,
					p_NumeroFirmantesCliente,
					p_NombreFirmanteEmpresa1,
					p_NombreFirmanteEmpresa2,
					p_NombreFirmanteEmpresa3,
					p_NombreFirmanteCliente1,
					p_NombreFirmanteCliente2,
					p_NombreFirmanteCliente3,
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					TO_SECONDDATE(p_FechaVigencia,'DD-MM-YYYY'),
					'');
					
					insert into "BSCL.Plataforma.BD::DOC.Documento" 
					(
						"IdDocumento",
						"IdTipoDocumento",
						"IdEmpresa",
						"IdCliente",
						"IdUltimoAnexo"
					)
					values( "BSCL.Plataforma.BD.Secuencias::Documento".CURRVAL,
							p_IdTipoDocumento,
							p_IdEmpresa,
							p_IdCliente,
							"BSCL.Plataforma.BD.Secuencias::Anexo".CURRVAL);
					
			------------------------------------------
			--Crear Documento
			------------------------------------------
			
			
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
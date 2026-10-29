SET SCHEMA "BSCL";
GO
DROP  PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.ModificarAutorizadoEtapa";
GO
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.ModificarAutorizadoEtapa" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN  p_IdUsuario				BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  	--ID del Modulo que lo invoca	
	IN  p_SimularOperacion		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos cASos)
	IN  p_UsaReglasNegocio		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en cASo de servicios anidados.
		  
	-- Parametros entrada del servicio publico	
	IN p_IdAutorizadoEtapa      BIGINT,
    IN p_IdUsuarioEtapa        	BIGINT DEFAULT 0, -- //Usuario autorizador de la etapa
    IN p_IdRol                 	BIGINT DEFAULT 0, -- //Rol autorizador de la etapa
    IN p_IdUsuarioReemplaza    	BIGINT DEFAULT 0, -- //Usuario autorizador de la etapa en reemplazo de
    IN p_ReemplazaDesde        	NVARCHAR(10) DEFAULT '0001-01-01',
    IN p_ReemplazaHasta        	NVARCHAR(10) DEFAULT '0001-01-01',
    IN p_IdEstado              	NVARCHAR(3)  DEFAULT 0, --   //[FK]AUT.AutorizadoEtapa;IdEstado;PLA.Estado;IdEstado//
    IN p_SoloBloquear           NVARCHAR(1)  DEFAULT 'N', -- Solo cambia el estado del Autorizador a BLOQUEADO (no modifica nada mas)
	       
	-- Parametros salida del servicio publico
	--
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_errorhAShmsg 	TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 	NVARCHAR(520);
	DECLARE v_Secuencia 	BIGINT;	  
	DECLARE v_out_Resultado CHAR(1);
    DECLARE v_out_Mensaje 	NVARCHAR(520); 
    DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);
    DECLARE v_Descripcion 	NVARCHAR(100);
    DECLARE v_FechaValida 	CHAR(1);
    DECLARE v_ReemplazaDesde NVARCHAR(10);
	DECLARE v_ReemplazaHasta NVARCHAR(10);
	DECLARE v_Cantidad		INT;
	DECLARE v_NombreEtapa 	NVARCHAR(100);
	DECLARE v_CodigoUsuario	NVARCHAR(100);
	DECLARE v_NombreRol		NVARCHAR(100);
	DECLARE v_CountExiste	BIGINT:=0;
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	 
	DECLARE SALIDA_CONTROLADA CONDITION;  	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------

		-------------------------
		--Validaciones básicas 
		-------------------------
			
															
		v_CountExiste := 0;
		SELECT COUNT(1) INTO v_CountExiste
			FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "IdUsuario" = :p_IdUsuarioEtapa;

		IF  v_CountExiste = 0 THEN	
			p_out_ErroresDetalle =  SELECT 'Usuario' AS "Codigo",'NO_EXISTE_USUARIO_ETAPA' AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 
		END IF;		

		v_CountExiste := 0;
		IF p_IdRol <> 0 THEN
			
			SELECT COUNT(1) INTO v_CountExiste
			FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "IdRol" = :p_IdRol;
	
			IF  v_CountExiste = 0 THEN
				p_out_ErroresDetalle =  SELECT 'Rol' AS "Codigo",'NO_EXISTE_ROL' AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
			END IF;		

		END IF;

		--Valida IdUsuario que esta como reemplazante en la etapa
		--v_out_Resultado:='N'; v_CodigoError:=0; v_MensajeError:='';

		IF p_IdUsuarioReemplaza <> 0 THEN
			v_CountExiste := 0;
			SELECT COUNT(1) INTO v_CountExiste
			FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "IdUsuario" = :p_IdUsuarioReemplaza;
	
			IF  v_CountExiste = 0 THEN
				p_out_ErroresDetalle =  SELECT 'UsuarioReemplaza' AS "Codigo",'USUARIO_REEMPLAZA_NO_EXISTE' AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
			END IF;	
			
			v_FechaValida := 'S';
			IF "BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_ReemplazaDesde)  = 'N' THEN
				v_FechaValida := 'N'; 
			END IF;
			
			IF "BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_ReemplazaHasta)  = 'N' THEN
				v_FechaValida := 'N';
			END IF;
			 
			IF 	v_FechaValida = 'N' THEN	
				v_MensajeError:='';
				v_hashmsg = SELECT 'Fecha' AS "Codigo", p_ReemplazaDesde || ' ' || p_ReemplazaHasta  AS "Valor" FROM dummy;    	
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('FECHA_NO_VALIDA', :v_hashmsg, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'Fecha' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle;
			END IF;
		
			v_ReemplazaDesde := p_ReemplazaDesde;
			v_ReemplazaHasta := p_ReemplazaHasta;
			
		ELSE
			v_ReemplazaDesde := '0001-01-01';
			v_ReemplazaHasta := '0001-01-01';
		END IF;


		v_CountExiste := 0;
		SELECT COUNT(1) INTO v_CountExiste
			FROM "BSCL.Plataforma.BD::PLA.Estado" WHERE "IdEstado" = :p_IdEstado;

		IF  v_CountExiste = 0 THEN
			p_out_ErroresDetalle =  SELECT 'Estado' AS "Codigo",'ESTADO_NO_EXISTE' AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle; 
		END IF;		

		--Si existen errores en las validaciones debe salir
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;		
				
		------------------------
		--Validaciones via Motor (solo complejAS)
		------------------------		
		--
	
		--------------------
		--ReglAS Negocio 
		--------------------		
		--
					
		--------------------
		--Negocio Aqui
		--------------------
		BEGIN

			IF (:p_SoloBloquear = 'S') THEN
				UPDATE "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"
				SET   "IdEstado" 		  = p_IdEstado       -- BLO: BLOQUEADO
				WHERE "IdAutorizadoEtapa" = p_IdAutorizadoEtapa;
			ELSE
				UPDATE "BSCL.Plataforma.BD::AUT.AutorizadoEtapa"
				SET "IdUsuario" 		     = p_IdUsuarioEtapa,          
				        "IdRol" 			 = p_IdRol,                 
				        "IdUsuarioReemplaza" = p_IdUsuarioReemplaza,
				        "ReemplazaDesde"	 = v_ReemplazaDesde ,      
				        "ReemplazaHasta" 	 = v_ReemplazaHasta,       
				        "IdEstado" 			 = p_IdEstado              
				WHERE "IdAutorizadoEtapa" = p_IdAutorizadoEtapa;			
			END IF;
		  	    		

		END;

						
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

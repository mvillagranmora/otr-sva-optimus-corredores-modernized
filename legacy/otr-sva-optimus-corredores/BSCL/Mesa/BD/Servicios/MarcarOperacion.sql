SET SCHEMA "BSCL"; 
--*
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MarcarOperacion";
--*
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MarcarOperacion" (

	--* -----------------------------------------------------------------------
	--* Proposito ..: Marca o Libera la operacion de su edicion
	--* Autor ......: Miguel Sandoval
	--* Fecha ......: Enero 2017
	--* -----------------------------------------------------------------------

    --* VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    IN p_IdUsuario 				BIGINT,
    IN p_IdModulo 				NVARCHAR(3)
    
    --* VARIABLES DEL PROCESO OBLIGATORIAS
	,IN p_Operaciones			
		TABLE (	"Accion"						VARCHAR(1) -- B: Bloquear, L: Liberar
				,"IdOperacion"					BIGINT
			   )

    --* SALIDA DEL SP
	,OUT p_out_Operaciones
		TABLE (	"IdOperacion"					BIGINT
				,"FechaEdicion"					DATETIME
				,"IdUsuarioEdicion"				BIGINT
				,"EstadoEdicion"				NVARCHAR(10)
			   )

	--* Salidas para mensajes de advertencias y errores
	,OUT p_out_ErroresContador 	INT
	,OUT p_out_ErroresDetalle 	TABLE (	"IdAplicacion"	BIGINT,
										"Codigo" 		NVARCHAR(100),
										"Descripcion"	NVARCHAR(5000)
									  )
	
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN
-------------------------------------------------------------


	--* ---------------------------------
	--* DECLARE Manejo de Errores
	--* ---------------------------------
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE	v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" 
				(::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = 
				SELECT * FROM :p_out_ErroresDetalle UNION ALL  
				SELECT 0, v_errorExt, ::SQL_ERROR_MESSAGE FROM DUMMY;				
			SELECT 	COUNT(1) 
			INTO 	p_out_ErroresContador 
			FROM	:p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	--* --------------
	--* Proceso
	--* --------------
	BEGIN 	
		--* ---------------------------------------------------
		--* Declaraciones Variables
		--* ---------------------------------------------------
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_CountErr BIGINT;
		DECLARE v_sizeTblBloquearS BIGINT;
		DECLARE v_sizeTblBloquearE BIGINT;
		DECLARE v_esAccionB BIGINT;
		DECLARE	v_FechaOperacionSecondate	SECONDDATE;
		DECLARE v_out_ErroresDetalle  		TABLE ("IdAplicacion" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
					DEFAULT SELECT 0 AS "IdAplicacion",'' AS  "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" FROM dummy WHERE 1=2;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
		BEGIN
			--salida/error/advertencia controlado por este servicio
					
			v_TablaCodigosMensajes = SELECT 
									"IdAplicacion" AS "IdInterno"
									,"Codigo"
									,"NombreParametro" AS "CodigoDato1"
									,"ValorParametro" AS "ValorDato1"
									,'' AS "CodigoDato2"
									,'' AS "ValorDato2"
									FROM :v_out_ErroresDetalle;

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = 	SELECT errores."IdAplicacion",
										errores."Codigo",
										msj."Descripcion"
									FROM :v_out_ErroresDetalle errores
									INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
									
			
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;
					 					 	
		END;
		BEGIN
		
			p_out_ErroresContador := 0;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
				( :p_IdEmpresa, v_FechaOperacionSecondate );		
			
			--* ---------------------------------------------------
			--* Validaciones basicas (Codigos,Existencias,etc)
			--* ---------------------------------------------------
			-- Supervisor?
			v_TblBloquearS = 	SELECT
									TblOpe."IdOperacion"
									,CASE WHEN TblOpe."IdUsuarioEdicion" = :p_IdUsuario OR TblOpe."IdUsuarioEdicion" = 0 THEN :p_IdUsuario ELSE "IdUsuarioEdicion" END AS "IdUsuarioEdicion"
									,CASE WHEN TblOpe."IdUsuarioEdicion" = :p_IdUsuario OR TblOpe."IdUsuarioEdicion" = 0 THEN :v_FechaOperacionSecondate ELSE "FechaEdicion" END  AS "FechaEdicion"
									,CASE 
										WHEN :p_IdUsuario = TblOpe."IdUsuarioCreacion" THEN 'OK'
										WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario = IFNULL(usuSup."IdUsuario",0) THEN 'OK'
										WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario != IFNULL(usuSup."IdUsuario",0) THEN 'NOK' 
										WHEN TblOpe."IdUsuarioEdicion" != 0 AND TblOpe."IdUsuarioEdicion" != :p_IdUsuario  THEN 'NOK'
										ELSE 'OK' 
									 END AS "EstadoEdicion"
									,CASE 
										WHEN :p_IdUsuario = TblOpe."IdUsuarioCreacion" THEN ''
										WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario = IFNULL(usuSup."IdUsuario",0) THEN ''
										WHEN TblOpe."IdUsuarioEdicion" != 0 AND TblOpe."IdUsuarioEdicion" != :p_IdUsuario  THEN 'OPERACION_BLOQUEADA'									
										WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario != IFNULL(usuSup."IdUsuario",0) THEN 'USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION' 
										ELSE '' 
									 END AS "DescripcionEstadoEdicion"
									 ,"Accion"
								FROM	:p_Operaciones ope
										LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" TblOpe ON (TblOpe."IdOperacion" = ope."IdOperacion")
										INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (TblOpe."IdUsuarioCreacion" = usu."IdUsuario" AND usu."TipoEntidad" = 'EMP')
										LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON (EJE."IdEmpleadoEjecutivo" = usu."IdEntidad" AND EJE."IdEstado" = 'ACT' )
									    LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuSup ON (EJE."IdEmpleadoSupervisor" = usuSup."IdEntidad" AND usuSup."TipoEntidad" = 'EMP')
								WHERE	"Accion" = 'B' AND usuSup."IdUsuario" = :p_IdUsuario;
						
			-- Mismo empleado?
			v_TblBloquearE = 	SELECT
									TblOpe."IdOperacion"
									,CASE WHEN TblOpe."IdUsuarioEdicion" = :p_IdUsuario OR TblOpe."IdUsuarioEdicion" = 0 OR USRMRF."IdUsuario" = :p_IdUsuario 
										  THEN :p_IdUsuario ELSE "IdUsuarioEdicion" END AS "IdUsuarioEdicion"
									,CASE WHEN TblOpe."IdUsuarioEdicion" = :p_IdUsuario OR TblOpe."IdUsuarioEdicion" = 0 OR USRMRF."IdUsuario" = :p_IdUsuario 
										  THEN :v_FechaOperacionSecondate ELSE "FechaEdicion" END  AS "FechaEdicion"
									,CASE 
										  WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario = IFNULL(usuSup."IdUsuario",0) THEN 'OK'
										  WHEN :p_IdUsuario = TblOpe."IdUsuarioCreacion" THEN 'OK'
										  WHEN TblOpe."IdUsuarioEdicion" != 0 AND :p_IdUsuario  != TblOpe."IdUsuarioEdicion" THEN 'NOK'									
										  WHEN :p_IdUsuario = USRMRF."IdUsuario" AND USRMRF."IdEntidad" = AGE."IdAgenteOperador" THEN 'OK'
										  WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" THEN 'NOK' 
										  ELSE 'OK' 
									 END AS "EstadoEdicion"
									,CASE 
										  WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" AND :p_IdUsuario = IFNULL(usuSup."IdUsuario",0) THEN ''
										  WHEN :p_IdUsuario = TblOpe."IdUsuarioCreacion" THEN ''
										  WHEN TblOpe."IdUsuarioEdicion" != 0 AND :p_IdUsuario  != TblOpe."IdUsuarioEdicion"  THEN 'OPERACION_BLOQUEADA'									
										  WHEN :p_IdUsuario = USRMRF."IdUsuario" AND USRMRF."IdEntidad" = AGE."IdAgenteOperador" THEN ''  -- Es Autorizado por Mant.Mesa?
										  WHEN :p_IdUsuario != TblOpe."IdUsuarioCreacion" THEN 'USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION' 
										  ELSE '' 
									 END AS "DescripcionEstadoEdicion"
									 ,"Accion"
								FROM	:p_Operaciones ope
										LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" TblOpe ON (TblOpe."IdOperacion" = ope."IdOperacion")
										----------------------------------------------------------------------------------------------------------
										LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USRMRF ON USRMRF."IdUsuario" = :p_IdUsuario
										LEFT JOIN "BSCL.Mesa.BD::MES.AgenteOperadorMesa" AGE ON USRMRF."IdEntidad" = AGE."IdAgenteOperador" 
													AND TblOpe."IdMesaOrigen" = AGE."IdMesa" AND AGE."ModificaOperacionOtroUsuario" = 'S'
										INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (TblOpe."IdUsuarioCreacion" = usu."IdUsuario" )
										LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE ON (EJE."IdEmpleadoEjecutivo" = usu."IdEntidad" AND EJE."IdEstado" = 'ACT' )
										LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuSup ON (EJE."IdEmpleadoSupervisor" = usuSup."IdEntidad" AND usuSup."TipoEntidad" = 'EMP' AND usuSup."IdUsuario" = :p_IdUsuario)
								WHERE	"Accion" = 'B' ;

							
			-- libera 
			v_TblLiberarS =     SELECT
                                    TblOpe."IdOperacion"
                                    ,0 AS "IdUsuarioEdicion"
                                    ,:v_FechaOperacionSecondate AS "FechaEdicion"
                                    ,'OK' AS "EstadoEdicion"
                                    ,'' AS "DescripcionEstadoEdicion"
                                    ,"Accion"
                                FROM    :p_Operaciones ope
                                        LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" TblOpe ON (TblOpe."IdOperacion" = ope."IdOperacion")
                                WHERE    "Accion" = 'L' AND TblOpe."IdUsuarioEdicion" = :p_IdUsuario;
						
			-- Mismo empleado?
			v_TblLiberarE = 	SELECT
									TblOpe."IdOperacion"
									,0 AS "IdUsuarioEdicion"
									,CASE WHEN TblOpe."IdUsuarioEdicion" = :p_IdUsuario OR TblOpe."IdUsuarioEdicion" = 0 THEN :v_FechaOperacionSecondate ELSE "FechaEdicion" END  AS "FechaEdicion"
									,'OK' AS "EstadoEdicion"
									,'' AS "DescripcionEstadoEdicion"
									,"Accion"
								FROM	:p_Operaciones ope
										LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" TblOpe ON (TblOpe."IdOperacion" = ope."IdOperacion")
										INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (TblOpe."IdUsuarioCreacion" = usu."IdUsuario" AND usu."TipoEntidad" = 'EMP')
								WHERE	"Accion" = 'L' AND usu."IdUsuario" = :p_IdUsuario;

							
			v_out_ErroresDetalle = SELECT "IdOperacion" AS "IdAplicacion", "DescripcionEstadoEdicion" AS "Codigo", "IdOperacion" AS "ValorParametro",'IdOperacion' AS "NombreParametro" FROM :v_TblBloquearS WHERE "EstadoEdicion" = 'NOK'
								   UNION ALL
								   SELECT "IdOperacion" AS "IdAplicacion", "DescripcionEstadoEdicion" AS "Codigo", "IdOperacion" AS "ValorParametro",'IdOperacion' AS "NombreParametro" FROM :v_TblBloquearE WHERE "EstadoEdicion" = 'NOK'
								   UNION ALL
				 				   SELECT "IdOperacion" AS "IdAplicacion", "DescripcionEstadoEdicion" AS "Codigo", "IdOperacion" AS "ValorParametro",'IdOperacion' AS "NombreParametro" FROM :v_TblLiberarS WHERE "EstadoEdicion" = 'NOK'
								   UNION ALL
								   SELECT "IdOperacion" AS "IdAplicacion", "DescripcionEstadoEdicion" AS "Codigo", "IdOperacion" AS "ValorParametro",'IdOperacion' AS "NombreParametro" FROM :v_TblLiberarE WHERE "EstadoEdicion" = 'NOK';

			--Si Accion = B, no es supervisor, ni empleado creador de la operacion, error de permisos		  
			SELECT count(*) INTO v_sizeTblBloquearS FROM :v_TblBloquearS WHERE "Accion" = 'B';
			SELECT count(*) INTO v_sizeTblBloquearE FROM :v_TblBloquearE WHERE "Accion" = 'B';	
			SELECT count(*) INTO v_esAccionB FROM (SELECT "Accion" FROM :p_Operaciones WHERE "Accion" = 'B');
			
			IF(v_sizeTblBloquearS = 0 AND v_sizeTblBloquearE = 0 AND v_esAccionB > 0) THEN
				v_out_ErroresDetalle = SELECT "IdOperacion" AS "IdAplicacion", 'USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION' AS "Codigo", "IdOperacion" AS "ValorParametro", 'IdOperacion' AS "NombreParametro" FROM :p_Operaciones;
			END IF;
			
			SELECT count(*) INTO v_CountErr FROM :v_out_ErroresDetalle;
			IF (v_CountErr > 0) THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			UPDATE 	Operacion
			SET	 "IdUsuarioEdicion" = Tmp."IdUsuarioEdicion"
				,"FechaEdicion" = Tmp."FechaEdicion"
			FROM	"BSCL.Mesa.BD::MES.Operacion" Operacion
				INNER JOIN :v_TblBloquearS Tmp 
					ON	Tmp."IdOperacion" = Operacion."IdOperacion" AND Tmp."EstadoEdicion"='OK';
			
			UPDATE 	Operacion
			SET	 "IdUsuarioEdicion" = Tmp."IdUsuarioEdicion"
				,"FechaEdicion" = Tmp."FechaEdicion"
			FROM	"BSCL.Mesa.BD::MES.Operacion" Operacion
				INNER JOIN :v_TblBloquearE Tmp 
					ON	Tmp."IdOperacion" = Operacion."IdOperacion" AND Tmp."EstadoEdicion"='OK'; 
		
			UPDATE 	Operacion
			SET	 "IdUsuarioEdicion" = Tmp."IdUsuarioEdicion"
				,"FechaEdicion" = Tmp."FechaEdicion"
			FROM	"BSCL.Mesa.BD::MES.Operacion" Operacion
				INNER JOIN :v_TblLiberarS Tmp 
					ON	Tmp."IdOperacion" = Operacion."IdOperacion" AND Tmp."EstadoEdicion"='OK';
				
			UPDATE 	Operacion
			SET	 "IdUsuarioEdicion" = Tmp."IdUsuarioEdicion"
				,"FechaEdicion" = Tmp."FechaEdicion"
			FROM	"BSCL.Mesa.BD::MES.Operacion" Operacion
				INNER JOIN :v_TblLiberarE Tmp 
					ON	Tmp."IdOperacion" = Operacion."IdOperacion" AND Tmp."EstadoEdicion"='OK';
	
	
			p_out_Operaciones = 
				SELECT	"IdOperacion"
						,"FechaEdicion"
						,"IdUsuarioEdicion"
						,"EstadoEdicion"
				FROM	:v_TblBloquearS WHERE "EstadoEdicion"='OK'
				UNION ALL
				SELECT	"IdOperacion"
						,"FechaEdicion"
						,"IdUsuarioEdicion"
						,"EstadoEdicion"
				FROM	:v_TblBloquearE WHERE "EstadoEdicion"='OK'
				UNION ALL
				SELECT	"IdOperacion"
						,"FechaEdicion"
						,"IdUsuarioEdicion"
						,"EstadoEdicion"
				FROM	:v_TblLiberarS WHERE "EstadoEdicion"='OK'
				UNION ALL
				SELECT	"IdOperacion"
						,"FechaEdicion"
						,"IdUsuarioEdicion"
						,"EstadoEdicion"
				FROM	:v_TblLiberarE WHERE "EstadoEdicion"='OK';
		END;	
	END;  	
END;


SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConfirmaEliminacionRolUsuarioMasivo";

CREATE PROCEDURE 
-- 
--  Proposito: Autorizar eliminacion de rol al usuario
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 21/01/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmaEliminacionRolUsuarioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	nvarchar(3),    --ID del Modulo que lo invoca		  
	-- Parametros entrada 
	IN p_RolUsuario						TABLE ("IdRolUsuario"	BIGINT)			--ID RolUsuario que se desea eliminar
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	declare v_IdPersona bigint;
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	DECLARE v_fechaHoraSistema NVARCHAR(40);
	DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar eliminar Rol';
	
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;
        
		--Agregar log
	        v_DetalleLog = select RU."IdRol" AS "IdCabeceraLog",'Eliminar Rol' as "SubRegistro", 'Rol' as "CampoRegistro", R."Nombre" as "ValorAntiguo",'' as "ValorNuevo"
		               from "BSCL.Plataforma.BD::ACC.RolUsuario" RU
		               inner join :p_RolUsuario TU ON RU."IdRolUsuario" = TU."IdRolUsuario"
					   inner join "BSCL.Plataforma.BD::ACC.Rol" R ON R."IdRol" = RU."IdRol"
					   union
					   select RU."IdRol" AS "IdCabeceraLog", 'Eliminar Perfil' as "SubRegistro", 'Perfil' as "CampoRegistro", P."Nombre" as "ValorAntiguo", '' as "ValorNuevo"
					   from "BSCL.Plataforma.BD::ACC.RolUsuario" RU
		               inner join :p_RolUsuario TU ON RU."IdRolUsuario" = TU."IdRolUsuario"
					   inner join "BSCL.Plataforma.BD::ACC.RolPerfil" RP ON  RP."IdRol" =  RU."IdRol" 
					   inner join "BSCL.Plataforma.BD::ACC.Perfil" P on RP."IdPerfil" = P."IdPerfil";
		
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);
		
		v_log = SELECT  RU."IdRol" AS "IdCabeceraLog"
						,'ELIMINACION' AS "IdTipoLog"
						,'PLA' AS "IdModulo"
					    ,p_IdUsuario AS "IdUsuario"
						,p_IdEmpresa AS "IdEmpresa"
						,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaSistemaLog"
						,RU."IdUsuario" AS "IdUsuarioAtendido" 
						,0 AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente" 
						,'' AS "IdTipoDocumentoIdentidad" 
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'ROL' AS "IdTipoRegistro" 
						,0 AS "IdRegistro"
						,0 AS "EstadoRegistro"
						,'' AS "IdTipoOperacion" 
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,0 AS "TipoCambio"
						,'' AS "Nemo"
						,RU."IdRol" AS "IdProceso"
						,0 AS "EstadoProceso"
						,v_NombreProceso AS "NombreProceso"
				from "BSCL.Plataforma.BD::ACC.RolUsuario" RU
		        inner join :p_RolUsuario TU ON RU."IdRolUsuario" = TU."IdRolUsuario"; 
		       
		--Eliminar el registro de la tabla rolUsuario
		DELETE 
		FROM "BSCL.Plataforma.BD::ACC.RolUsuario"
		WHERE "IdRolUsuario" IN (SELECT "IdRolUsuario" FROM :p_RolUsuario); 

		--Grabar log
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo"(
			p_Log => :v_log,
			p_LogDetalle => :v_DetalleLog
		);     
	END;				    
END;

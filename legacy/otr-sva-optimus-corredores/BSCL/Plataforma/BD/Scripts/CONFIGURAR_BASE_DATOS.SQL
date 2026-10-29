DO
BEGIN
	-- COPIA UNA NUEVA EMPRESA A PARTIR DE UNA EXISTE	
	--Parametros de setup
	DECLARE param_OldIdEmpresa BIGINT  DEFAULT '1'; 
	DECLARE param_NewIdEmpresa BIGINT  DEFAULT '2'; 
	DECLARE param_DocumentoIdentidad nvarchar(100) DEFAULT '0-0';
	
	--variables
	DECLARE v_IdDocumentoIdentidad,v_IdPersona,v_OldIdEmpleado,v_OldIdUsuario  BIGINT;
	DECLARE v_socneg,v_emp,v_usu,v_rol,v_rolusu BIGINT;
	DECLARE v_count  BIGINT;
	
	
	--obtiene datos
	SELECT "IdDocumentoIdentidad","IdPersona" INTO v_IdDocumentoIdentidad,v_IdPersona  
	 FROM  "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
	WHERE  "DocumentoIdentidad" = param_DocumentoIdentidad;
	
	SELECT "IdEmpleado"  INTO v_OldIdEmpleado
	  FROM BSCL."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 
	 WHERE "IdPersona" = v_IdPersona AND "IdEmpresa" = param_OldIdEmpresa;
	 	 
	SELECT "IdUsuario" INTO v_OldIdUsuario
	  FROM BSCL."BSCL.Plataforma.BD::ACC.Usuario" 
	WHERE "IdEmpresa" = param_OldIdEmpresa AND "TipoEntidad" = 'EMP' AND "IdEntidad" = v_OldIdEmpleado;	 

	--keys
	SELECT  "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL, 
			"BSCL.SocioNegocio.BD.Secuencias::Empleado".NEXTVAL, 
			"BSCL.Plataforma.BD.Secuencias::Usuario".NEXTVAL, 
			"BSCL.Plataforma.BD.Secuencias::Rol".NEXTVAL,
			"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL
			INTO v_socneg,v_emp,v_usu,v_rol,v_rolusu 
	FROM DUMMY;
	
	
	SELECT  v_IdDocumentoIdentidad	 AS "IdDocumentoIdentidad",
			v_IdPersona      		 AS "IdPersona",
		    v_OldIdEmpleado  		 AS "IdEmpleado", 
		    v_OldIdUsuario  		 AS "IdUsuario", 
		    v_socneg 				 AS "NewIdSocioNegocio", 
			v_emp       			 AS "NewIdEmpleado", 
			v_usu					 AS "NewIdUsuario", 
			v_rol					 AS "NewIdRol",
			v_rolusu   				 AS "NewIdRolUsuario"					   
	FROM DUMMY;
	

		
	--Agrega empleado a la empresa
    INSERT INTO BSCL."BSCL.SocioNegocio.BD::EMP.Empleado"  ("IdEmpleado", "IdPersona", "CodigoEmpleado", "IdEmpresa", "IdEmpleadoSupervisor", "IdSucursal", "FechaInicioCargo", "FechaTerminoCargo", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdCarga", "IdEstado", "CodigoAgenteMigracion", "CodigoSupervisorAgenteMigracion", "Borrado", "EsEnrolado", "IdUsuarioCreador", "UtilizaPlantillaEmpleado", "IdPlantillaEmpleado", "CodigoTipoCargoEmpleado", "IdTipoBloqueo", "FechaBloqueo", "ComentarioBloqueo", "IdTipoDesbloqueo", "FechaDesbloqueo", "ComentarioDesbloqueo", "DetalleCargo", "IdDireccionElectronica")
	SELECT  v_emp, "IdPersona", "CodigoEmpleado",param_NewIdEmpresa,0, 0, "FechaInicioCargo", "FechaTerminoCargo", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdCarga", "IdEstado", "CodigoAgenteMigracion", "CodigoSupervisorAgenteMigracion", "Borrado", "EsEnrolado", "IdUsuarioCreador", "UtilizaPlantillaEmpleado", "IdPlantillaEmpleado", "CodigoTipoCargoEmpleado", "IdTipoBloqueo", "FechaBloqueo", "ComentarioBloqueo", "IdTipoDesbloqueo", "FechaDesbloqueo", "ComentarioDesbloqueo", "DetalleCargo", "IdDireccionElectronica"
	  FROM BSCL."BSCL.SocioNegocio.BD::EMP.Empleado"
	 WHERE "IdPersona" = v_IdPersona AND "IdEmpresa" = param_OldIdEmpresa;	
	
	--Agrega como socio negocio para la empresa
	INSERT INTO BSCL."BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdSocioDeNegocio", "IdPersona", "IdEmpresa", "IdEmpleado", "Borrado")
	VALUES(v_socneg ,v_IdPersona, param_NewIdEmpresa, v_emp, 'N');	
	
	--Crear usuario para la empresa
	INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave")
	SELECT v_usu, param_NewIdEmpresa, "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", v_emp, "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave" 
	  FROM BSCL."BSCL.Plataforma.BD::ACC.Usuario" 
	WHERE "IdEmpresa" = param_OldIdEmpresa AND "TipoEntidad" = 'EMP' AND "IdEntidad" = v_OldIdEmpleado;
		
	--Crea rol ADMIN en la empresa nueva
	INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.Rol"  ("IdRol", "IdEmpresa", "Nombre", "IdEstado")
    VALUES(v_rol, param_NewIdEmpresa, 'ADMIN', 'ACT');	
	
    --Asigna ROL ADMIN en el usuario nuevo
	INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
	VALUES(v_rolusu, v_usu, v_rol,'S');

	--Configuraciones de Plataforma
	INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
	SELECT param_NewIdEmpresa, "CodigoParametro", "Descripcion", "Valor"
	  FROM BSCL."BSCL.Plataforma.BD::PLA.Configuracion" 
	 WHERE "IdEmpresa" = param_OldIdEmpresa;

	--configura id ROL
	UPDATE BSCL."BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = v_rol
	WHERE   "IdEmpresa" = param_NewIdEmpresa AND "CodigoParametro" = 'ACC.IdRolAdmin';


	SELECT 'Configurar PLA.Empresa' AS TODO FROM dummy UNION ALL
	SELECT 'Configurar PLA.Configuracion' AS TODO FROM dummy UNION ALL
	SELECT '---' AS TODO FROM dummy;
END;
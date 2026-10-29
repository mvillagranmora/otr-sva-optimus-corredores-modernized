SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilComercial";

 CREATE PROCEDURE
 --
 --  Proposito: Modificacion de Perfil Comercial
 --  Autor: JG - BCS
 --  Fecha Creacion: 28/03/2016
 --
 "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilComercial" (
	 -- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa       					BIGINT,   		--ID Empresa destino
	 IN  p_IdUsuario       					BIGINT,   		--ID del usuario que solicita el servicio
	 IN  p_IdModulo       					NVARCHAR(3),   	--ID del Modulo que lo invoca
	 IN  p_simularOperacion     			NVARCHAR(1), 	-- S/N se usa para simular operacion (algunos casos)
	 IN  p_UsaReglasNegocio      			NVARCHAR(1), 	-- S/N se usa en caso de servicios anidados.
	 IN p_IdPerfilComercialBase    			NVARCHAR(40),
	 IN p_DescripcionPerfilComercialBase  	NVARCHAR(100),
	 IN p_IdCategoriaInversionista   		NVARCHAR(40),
	 IN p_IdEstado       					NVARCHAR(3),
	 IN p_PorDefecto       					NVARCHAR(1),
	 IN p_IdPerfilProductos     			NVARCHAR(40),
	 IN p_IdPerfilCobros      				NVARCHAR(40),
	 IN p_IdPerfilCobrosPeru     			NVARCHAR(40),
	 IN p_IdPerfilLimites     				NVARCHAR(40),
	 IN p_IdPerfilCondicionesOperativas  	NVARCHAR(40),
	 IN p_IdModeloFacturacion    			NVARCHAR(40),
	 IN p_TipoEnrolamiento                  NVARCHAR(10) DEFAULT 'FULL',
	 -- Parametros salida del servicio publico
	 OUT p_out_RefId 						BIGINT,
	 -- Salidas para mensajes de advertencias y errores
	 OUT p_out_AdvertenciasContador 		int,
	 OUT p_out_AdvertenciasDetalle 			table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	 OUT p_out_ErroresContador 				int,
	 OUT p_out_ErroresDetalle 				table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
			 --Error interno no manejado  (El log sera captado por la capa de XS control)
			 RESIGNAL;
		 END IF;
	 END;
	 p_out_AdvertenciasContador := 0;
	 p_out_ErroresContador  := 0;
	 p_out_AdvertenciasDetalle = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	 p_out_ErroresDetalle  = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	 
	 BEGIN
		 -----------------------------------------------------
		 --Declaraciones Variables
		 -----------------------------------------------------
		 DECLARE v_countPerfil BIGINT;
		 -----------------------------------------------------
		 --Validaciones basicas (Codigos,Existencias,etc)
		 -----------------------------------------------------
		 BEGIN
			 declare v_resultadoRegla nvarchar(40);
			 SELECT COUNT(1) INTO v_countPerfil from "BSCL.Plataforma.BD::COM.PerfilComercialBase"
			 WHERE "IdPerfilComercialBase" = :p_IdPerfilComercialBase;
			 if v_countPerfil = 0 then
				 v_hashmsg = select 'TipoEntidad' as "Codigo", 'persona' as "Valor" from dummy;
				 CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PERFIL NO EXISTE',:v_hashmsg, :v_erroroutmsg);
				 p_out_ErroresDetalle =  select 'PERFIL_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
				 select * from :p_out_ErroresDetalle;
				 SIGNAL SALIDA_CONTROLADA;
			 end if;
			 --Si existen errores en las validaciones debe salir
			 select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
			 IF :p_out_ErroresContador > 0 THEN
			 	SIGNAL SALIDA_CONTROLADA;
			 END IF;
		 END;
		 --------------------
		 --Reglas Negocio
		 --------------------
		 --Solo fue una simulacion del servicio... move bitch,get out the way
		 if p_simularOperacion = 'S' then
		 	SIGNAL SALIDA_CONTROLADA;
		 end if;
		 --------------------
		 --Negocio Aqui
		 --------------------
		 BEGIN
			 p_out_RefId := 0;
			 v_registros_antiguos =
			 SELECT
			 "IdPerfilComercialBase",
			 "DescripcionPerfilComercialBase",
			 "IdCategoriaInversionista",
			 "IdPerfilProductos",
			 "IdPerfilCobros",
			 "IdPerfilCobrosPeru",
			 "IdPerfilLimites",
			 "IdPerfilCondicionesOperativas",
			 "IdModeloFacturacion",
			 "FechaModificacion",
			 "IdEstado",
			 "PorDefecto",
			 "IdEmpresa",
		     "TipoEnrolamiento"
			 FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase"
			 WHERE "IdPerfilComercialBase" = :p_IdPerfilComercialBase;
			
			 UPDATE
			 	"BSCL.Plataforma.BD::COM.PerfilComercialBase"
			 SET
				 "DescripcionPerfilComercialBase"	= :p_DescripcionPerfilComercialBase,
				 "IdCategoriaInversionista"   		= :p_IdCategoriaInversionista,
				 "IdPerfilProductos"    			= :p_IdPerfilProductos,
				 "IdPerfilCobros"     				= :p_IdPerfilCobros,
				 "IdPerfilCobrosPeru"   			= :p_IdPerfilCobrosPeru,
				 "IdPerfilLimites"     				= :p_IdPerfilLimites,
				 "IdPerfilCondicionesOperativas" 	= :p_IdPerfilCondicionesOperativas,
				 "IdModeloFacturacion"    			= :p_IdModeloFacturacion,
				 "FechaModificacion"    			= CURRENT_UTCTIMESTAMP,
				 "IdEstado"       					= :p_IdEstado,
				 "PorDefecto"      					= :p_PorDefecto,
				 "IdEmpresa"      					= :p_IdEmpresa,
			     "TipoEnrolamiento"                 = :p_TipoEnrolamiento
			 WHERE
			 	"IdPerfilComercialBase" = :p_IdPerfilComercialBase;
			 	select 1 into p_out_RefId from dummy;
			 
			 -- valida que exista por categoria de inversionista, un solo modelo comercial por defecto 
			 IF (:p_PorDefecto='S') THEN
			 	BEGIN
					 UPDATE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
					 SET "PorDefecto"  = 'N'
					 WHERE "IdCategoriaInversionista" = :p_IdCategoriaInversionista
                     AND "TipoEnrolamiento" = :p_TipoEnrolamiento
					 AND "IdPerfilComercialBase" <> :p_IdPerfilComercialBase
					 AND "IdEmpresa" = :p_IdEmpresa;				 	
				END;
			 END IF;
			
			 BEGIN
				 --LOG CAMBIOS
				 --REEMPLAZAR POR LOG MASIVO
				 DECLARE p_sysDate    NVARCHAR(20);
				 DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				
				 v_valores_antiguos =
				 SELECT "IdPerfilComercialBase" AS "IdSecuencia", "Llave", "Valor"
				 FROM (
				 SELECT * ,
				 COALESCE(
				 "1" ||"DescripcionPerfilComercialBase", "2" ||"IdCategoriaInversionista", "3" ||"IdPerfilProductos", "4" ||"IdPerfilCobros", "5" ||"IdPerfilCobrosPeru", "6" ||"IdPerfilLimites",
				 "7" ||"IdPerfilCondicionesOperativas", "8" ||"IdModeloFacturacion", "9" ||"FechaModificacion", "10" ||"IdEstado",
				 "11" ||"PorDefecto", "12" ||"IdEmpresa", "13" || "IdPerfilComercialBase"
				 ) AS "Valor",
				 COALESCE(
				 "1" ||'DescripcionPerfilComercialBase', "2" ||'IdCategoriaInversionista', "3" ||'IdPerfilProductos', "4" ||'IdPerfilCobros', "5" ||'IdPerfilCobrosPeru', "6" ||'IdPerfilLimites',
				 "7" ||'IdPerfilCondicionesOperativas', "8" ||'IdModeloFacturacion', "9" ||'FechaModificacion', "10" ||'IdEstado',
				 "11" ||'PorDefecto', "12" ||'IdEmpresa', "13" || 'IdPerfilComercialBase'
				 ) AS "Llave"
				 FROM :v_registros_antiguos,"BSCL.Plataforma.BD::PLA.Identidad"
				 ) WHERE  "Valor" IS NOT NULL;
				
				 v_valores_nuevos =
				 SELECT "IdPerfilComercialBase" AS "IdSecuencia", "Llave", "Valor"
				 FROM (
				 SELECT * ,
				 COALESCE(
				 "1" ||"DescripcionPerfilComercialBase", "2" ||"IdCategoriaInversionista", "3" ||"IdPerfilProductos", "4" ||"IdPerfilCobros", "5" ||"IdPerfilCobrosPeru", "6" ||"IdPerfilLimites",
				 "7" ||"IdPerfilCondicionesOperativas", "8" ||"IdModeloFacturacion", "9" ||"FechaModificacion", "10" ||"IdEstado",
				 "11" ||"PorDefecto", "12" ||"IdEmpresa", "13" || "IdPerfilComercialBase"
				 ) AS "Valor",
				 COALESCE(
				 "1" ||'DescripcionPerfilComercialBase', "2" ||'IdCategoriaInversionista', "3" ||'IdPerfilProductos', "4" ||'IdPerfilCobros', "5" ||'IdPerfilCobrosPeru', "6" ||'IdPerfilLimites',
				 "7" ||'IdPerfilCondicionesOperativas', "8" ||'IdModeloFacturacion', "9" ||'FechaModificacion', "10" ||'IdEstado',
				 "11" ||'PorDefecto', "12" ||'IdEmpresa', "13" || 'IdPerfilComercialBase'
				 ) AS "Llave"
				 FROM (select * from "BSCL.Plataforma.BD::COM.PerfilComercialBase" where "IdPerfilComercialBase" = :p_IdPerfilComercialBase),"BSCL.Plataforma.BD::PLA.Identidad"
				 ) WHERE "Valor" IS NOT NULL;
				
				 p_DetalleLog =
				 SELECT
				 vn."IdSecuencia" as "SubRegistro",
				 vn."Llave" as "CampoRegistro",
				 TO_VARCHAR(va."Valor") as "ValorAntiguo",
				 TO_VARCHAR(vn."Valor") as "ValorNuevo"
				 FROM :v_valores_antiguos va
				 INNER JOIN :v_valores_nuevos vn on va."IdSecuencia" = vn."IdSecuencia" AND va."Llave" =vn."Llave" and va."Valor" <> vn."Valor";
				 SELECT CURRENT_DATE into p_sysDate from DUMMY;
				
				 CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'ACTUALIZACION'
					 ,p_IdModulo => 'PLA'
					 ,p_IdUsuario => p_IdUsuario
					 ,p_IdEmpresa => p_IdEmpresa
					 ,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
					 ,p_IdUsuarioAtendido => p_IdUsuario
					 ,p_IdTipoRegistro => 'MODELO COMERCIAL'
					 ,p_DetalleLog => :p_DetalleLog
				 );
			 END;
		 END;
		 --------------------
		 --Normativo
		 --------------------
		 --------------------
		 --FIN SP
		 --------------------
	 END;
 END;

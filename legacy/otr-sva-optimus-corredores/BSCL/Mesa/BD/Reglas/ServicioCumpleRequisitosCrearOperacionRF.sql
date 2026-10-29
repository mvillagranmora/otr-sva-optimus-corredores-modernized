SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearOperacionRF";

--* ------------------------------------------------------------------------
--* Nombre .....: ServicioCumpleRequisitosCrearOperacionRF.sql
--* Modulo .....: MesaRF
--* F. Creación : Agosto 2016
--* Proposito ..: Reglas de validación para el ingreso de una operación de la 
--*               Mesa RF + validación de los papeles.
--* ------------------------------------------------------------------------
CREATE PROCEDURE "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearOperacionRF"(

--- 										"IdRegistroOpe"		BIGINT
	 IN p_IdEmpresa				BIGINT
	 --IN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN",
	 /*IN p_DatosOperacion     	TABLE (  "IdRegistroOpe"	BIGINT
			                            ,"IdCliente"		BIGINT
										,"IdPortafolio"		BIGINT
										,"IdPortafolioCorredor"	BIGINT
										,"IdTipoOperacion"	NVARCHAR(06)
										,"MontoOperacion"	DECIMAL(26,8)
										,"FechaOperacion"	NVARCHAR(10)
										,"FechaLiquidacion"	NVARCHAR(10)
										,"PlazoOperacion"	BIGINT
										,"FechaVencimiento"	NVARCHAR(10)
										,"Intermesa"		NVARCHAR(1)
										,"IdMesaOrigen"		BIGINT
										,"IdMesaDestino"	BIGINT
									   	,"IdUsuario"			BIGINT),*/
	 --IN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_IN",
	 /*IN p_DatosDetalle     		TABLE (  
 										 "IdRegistroOpe"	BIGINT
 										,"IdRegistroDet"	BIGINT
			                            ,"IdCliente"		BIGINT
										,"IdPortafolio"		BIGINT
										,"IdInstrumento"	BIGINT
										,"IdClasificacionTipoInstrumento" NVARCHAR(3)
										,"Cantidad"			DECIMAL(26,8)
										,"CorteMinimo"		DECIMAL(26,8)
									   	),*/
	--IN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_OUT",
	/*OUT p_out_SalidaDetalle     TABLE (	
										"IdRegistroOpe" 		BIGINT
										,"Resultado" 		NVARCHAR(40)
										,"Codigo" 			NVARCHAR(40)
										,"ValorParametro" 	NVARCHAR(100)
										,"NombreParametro" 	NVARCHAR(40)
										),*/
	--IN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Resultado_OUT"
	/*OUT p_out_Salida        	TABLE (	
										"IdRegistroOpe" 	BIGINT 
										,"Resultado" 	NVARCHAR(40)
										) 	-- OK 	= Cumple todos los requisitos
											-- ADV	= Advertencia
											-- AUT	= Autorizacion							  	   
                                            -- ERR	= Error*/
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
AS
BEGIN

	DECLARE v_FechaSistema NVARCHAR(10);
	
	SELECT 	COALESCE("Valor",'') 
	INTO 	v_FechaSistema 
	FROM 	"BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE 	"IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
       
	v_matriz_validaciones =	SELECT ope."IdRegistroOpe"
								 --* PARA TODAS LAS OPERACIONES
								 , 1 AS "FECHA_OPERACION_DISTINTA_FECHA_PROCESO"	-- Prog.
								 , 1 AS "MESA_RF_CERRADA"
								 
								 , 1 AS "CLIENTE_EXISTE"							-- val. basica (antes)
								 , 1 AS "CLIENTE_BLOQUEADO"							-- val. basica (antes)
								 , 1 AS "CLIENTE_HABILITADO_PARA_OPERACION"
								 , 1 AS "CLIENTE_CONDICIONES_GENERALES_OK"
								 , 1 AS "CLIENTE_INSTITUCIONAL"								 
								 , 1 AS "LINEA_CLIENTE"								 								 
								 , 1 AS "PORTAFOLIO_NO_EXISTE"							-- val. basica (antes)
								 , 1 AS "PORTAFOLIO_BLOQUEADO"						-- val. basica (antes)
								 , 1 AS "PRODUCTO_NO_HABILITADO"
								 , 1 AS "USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION"
								 --* LIMITES --> OTRO SERVICIO LO HARA...
								 --* , 1 AS "LIMITE_PRECIO_Y_TASA"
								 --* , 1 AS "LIMITE_AS400"
								 --* , 1 AS "LIMITE_ARTICULO_84"
								 --* , 1 AS "LIMITE_TASA_X_FAMILIA_INSTRUMENTO"								 								 								 
								 --* , 1 AS "LIMITE_EMISORES_X_CLIENTE"
								 --* , 1 AS "LIMITE_PACTO_AGENTE_X_FAMILIA"								 

								 --* FICHA
								 , 1 AS "CONTRATO_CUSTODIA"
								 , 1 AS "MANDATO_MERCANTIL"
								 , 1 AS "ANTECEDENTES_LEGALES"								 
								 , 1 AS "DECLARACION_RESIDENCIA"
								 , 1 AS "CONTRATO_APV"								 
								 					
								 --* DATOS DE LA OPERACION
								 , 1 AS "MONTO_MAYOR_CERO"							-- Prog.
								 , 1 AS "PLAZO_MAYOR_CERO"							-- Prog.
								 , 1 AS "TASA_CERO"	
								 , 1 AS "FECHA_VENCIMIENTO_HABIL"					-- Prog.
								 , 1 AS "FECHA_LIQUIDACION_HABIL"
								 , 1 AS "FECHA_OPERACION_Y_VENCIMIENTO"				-- Prog.
								 , 1 AS "TIPO_OPERACION_VALIDA"						-- Prog.

								 --* OTROS								 
								 , 1 AS "BLOQUEO_ADMINISTRACION_LIQUIDEZ"	
								 , 1 AS "AJUSTE_SENCILLO"
								 , 1 AS "PAPEL_EN_OTRA_OPERACION"
								 , 0/*CASE det."IdClasificacionTipoInstrumento"
										WHEN 'RF' THEN 1
										WHEN 'IF' THEN 1
										ELSE 0 
									END */as  "CORTE_MINIMO_INVALIDO"
								 , CASE ope."Intermesa"
								 		WHEN 'S' THEN 1 
								 		ELSE 0
								 	END	AS "TIPO_OPERACION_INTERMESA_VALIDA"
								 , 1 AS "MODULO_CERRADO"
								 , CASE ope."Intermesa"
								 		WHEN 'S' THEN 1 
								 		ELSE 0
								 	END AS "OPERACION_INTERMESA_VALIDA"
								 ,1 AS "MESA_ORIGEN_CERRADA"
								 , CASE ope."Intermesa"
								 		WHEN 'S' THEN 1 
								 		ELSE 0
								 	END AS "MESA_DESTINO_CERRADA"
								 
									
								 								 
                                 
 							  FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope
 							  		LEFT JOIN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_IN" det on (ope."IdRegistroOpe" = det."IdRegistroOpe");
 		
 	v_PortafolioCliente = 	SELECT ope."IdRegistroOpe",ope."IdTipoOperacion", conPor.* 
 							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" conPor
 								INNER JOIN "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope ON (conPor."IdPortafolio"= ope."IdPortafolio");
 								
 	select 'v_PortafolioCliente' as "v_PortafolioCliente",* from :v_PortafolioCliente;							
 	
	v_PortafolioRequisitosOperativos = 	SELECT conPor.*,cli."DocumentoIdentidad", reqOp."IdTipoEtapa",reqOp."IdCondicionOperativa",reqOp."CodigoTipoIndicador",reqOp."IdTipoComportamiento", indCli."GrupoIndicador", indCli."NombreTipoIndicador" , indCli."ValorIndicador"
			 							FROM :v_PortafolioCliente conPor
			 								CROSS JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF" reqOp
			 								INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON (conPor."IdCliente" = cli."IdCliente")
			 								LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indCli ON (conPor."IdCliente" = indCli."IdCliente" and indCli."CodigoTipoIndicador" = reqOp."CodigoTipoIndicador" AND indCli."IdTipoEntidad" = 'CLIENTE' and indCli."IdUso" = 'REQ')
			 							WHERE reqOp."IdTipoEtapa" = 'INGRESOOPMESARF' and reqOp."IdCondicionOperativa" in ('CONTCONDGRAPACTO');
	
	select 'v_PortafolioRequisitosOperativos' as "v_PortafolioRequisitosOperativos",* from :v_PortafolioRequisitosOperativos;							
/*
 	SELECT conPor.*, reqOp."IdTipoEtapa",reqOp."IdCondicionOperativa",reqOp."CodigoTipoIndicador",reqOp."IdTipoComportamiento", indCli."GrupoIndicador", indCli."NombreTipoIndicador" , indCli."ValorIndicador"
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" conPor
		cross join "BSCL.Mesa.BD.Vistas::MRF.ConsultarRequisitosOperativosMesaRF" reqOp
		left join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" indCli on (indCli."IdCliente" = conPor."IdCliente" and indCli."CodigoTipoIndicador" = reqOp."CodigoTipoIndicador" and indCli."IdTipoEntidad" = 'CLIENTE' and indCli."IdUso" = 'REQ')
	WHERE  reqOp."IdTipoEtapa" = 'INGRESOOPMESARF' and conPor."IdCliente"=123569
	
select * FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
*/
	v_TablaRequisitos =	
		SELECT 	ope."IdRegistroOpe"
	  			,CASE WHEN val."FECHA_OPERACION_DISTINTA_FECHA_PROCESO" = 0 OR ope."FechaOperacion" = v_FechaSistema  THEN 1 
	    		ELSE 4 END 									AS "IdResultado"
	  			,'FECHA_OPERACION_DISTINTA_FECHA_PROCESO' 	AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100))	AS "ValorParametro"
	  			,'IdOperacion' 							AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope, :v_matriz_validaciones val
		WHERE 	ope."IdRegistroOpe" = val."IdRegistroOpe"

		UNION 
	
		SELECT 	ope."IdRegistroOpe"
				,CASE WHEN 	( ope."IdTipoOperacion" = 'COMTER' OR ope."IdTipoOperacion" = 'VENTER' ) THEN 1
				 	  WHEN  ( ope."PlazoOperacion" > 0 ) THEN 1 
 				 	  WHEN 	( val."PLAZO_MAYOR_CERO" = 1 AND ope."PlazoOperacion" = 0 ) AND 
 				 			( ope."IdTipoOperacion"  = 'COMPAC' OR ope."IdTipoOperacion"  = 'VENPAC') THEN 4
			   	 ELSE 4 END 								AS "IdResultado"
			 	,'PLAZO_MAYOR_CERO' 						AS "Codigo"
			 	,CAST(ope."IdRegistroOpe" AS NVARCHAR(100))	AS "ValorParametro"
			 	,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope, :v_matriz_validaciones val
	    WHERE 	ope."IdRegistroOpe" = val."IdRegistroOpe"

		UNION 
		                 
		SELECT 	ope."IdRegistroOpe"
				,CASE WHEN val."MONTO_MAYOR_CERO" = 0 OR ope."MontoOperacion" > 0 THEN 1 
				ELSE 4 END 									AS "IdResultado"
	  			,'MONTO_MAYOR_CERO' 						AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope, :v_matriz_validaciones val
		WHERE 	ope."IdRegistroOpe" = val."IdRegistroOpe"    

		UNION		                  

		SELECT ope."IdRegistroOpe"
				,CASE WHEN COALESCE(op."IdTipoOperacion", '') > '' THEN 1
				ELSE 4 END 									AS "IdResultado"
	  			,'TIPO_OPERACION_VALIDA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope 
				LEFT JOIN ( SELECT 	DISTINCT "IdTipoOperacion" 
							FROM	"BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" 
							WHERE	"IdTipoProducto" = 'MERF' ) op
				ON ope."IdTipoOperacion" = op."IdTipoOperacion"

		UNION
			 
		SELECT ope."IdRegistroOpe"
				,CASE WHEN ope."IdTipoOperacion"  = 'COMPAC' OR ope."IdTipoOperacion"  = 'VENPAC' THEN
					  CASE WHEN "FechaVencimiento" > "FechaOperacion" THEN 1 ELSE 4 END
				ELSE 1 END 									AS "IdResultado"
	  			,'FECHA_OPERACION_Y_VENCIMIENTO'			AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope, :v_matriz_validaciones val
		WHERE 	ope."IdRegistroOpe" = val."IdRegistroOpe"    

		UNION
		
		SELECT	ope."IdRegistroOpe"
				,CASE WHEN COALESCE("Tipo", 'OK') = 'OK' THEN 
					  CASE WHEN WEEKDAY("FechaOperacion") < 5 THEN 1 ELSE 4 END
				ELSE 1 END 									AS "IdResultado"
	  			,'FECHA_VENCIMIENTO_HABIL' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope
				LEFT JOIN "BSCL.Plataforma.BD::REG.DiaFeriado"
				ON ("Tipo" = 'FERIADO' AND "FechaOperacion" = "Fecha" )
		
		UNION
		
		SELECT	ope."IdRegistroOpe"
				,CASE WHEN COALESCE("Tipo", 'OK') = 'OK' THEN 
					  CASE WHEN WEEKDAY("FechaLiquidacion") < 5 THEN 1 ELSE 4 END
				ELSE 1 END 									AS "IdResultado"
	  			,'FECHA_LIQUIDACION_HABIL' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope
				LEFT JOIN "BSCL.Plataforma.BD::REG.DiaFeriado"
				ON ("Tipo" = 'FERIADO' AND "FechaLiquidacion" = "Fecha" )
		
		UNION
		SELECT
			det."IdRegistroOpe"
			,CASE 
				WHEN val."CORTE_MINIMO_INVALIDO" = 0 THEN 1
				WHEN det."CorteMinimo" = 1 THEN 1
				WHEN MOD(det."Cantidad", det."CorteMinimo") = 0 THEN 1
				ELSE 4 
			END AS "IdResultado"
			,'CORTE_MINIMO_INVALIDO' as "Codigo"
			, CAST(det."IdRegistroOpe" AS NVARCHAR(100)) 	as "ValorParametro"
			, 'IdOperacion' 							as "NombreParametro"
		FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_IN" det, :v_matriz_validaciones val
		
		UNION		                  

		SELECT ope."IdRegistroOpe"
				,CASE
					WHEN val."TIPO_OPERACION_INTERMESA_VALIDA" = 0 THEN 1 
					WHEN COALESCE(op."IdTipoOperacion", '') != '' THEN 1
					ELSE 4 END 									AS "IdResultado"
	  			,'TIPO_OPERACION_INTERMESA_VALIDA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" op
					ON (ope."IdTipoOperacion" = op."IdTipoOperacion" AND "IdTipoProducto" = 'MERF' and "Accion" = 'VENTA')
				, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
				
		UNION		                  

		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."OPERACION_INTERMESA_VALIDA" = 0 THEN 1 
					WHEN "IdMesaOrigen" != "IdMesaDestino" THEN 1
				 	WHEN "IdPortafolio" != "IdPortafolioCorredor" THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'OPERACION_INTERMESA_VALIDA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM 	"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."MODULO_CERRADO" = 0 THEN 1 
					WHEN "TipoEstadoModulo" != 'C' THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'UI_MODULO_CERRADO' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" mes ON (ope."IdMesaOrigen" = mes."IdMesa")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."MESA_ORIGEN_CERRADA" = 0 THEN 1 
					WHEN "IdEstadoMesa" != 'C' THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'MESA_ORIGEN_CERRADA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" mes ON (ope."IdMesaOrigen" = mes."IdMesa")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT ope."IdRegistroOpe"
				,CASE 
					WHEN val."MESA_DESTINO_CERRADA" = 0 THEN 1 
					WHEN "IdEstadoMesa" != 'C' THEN 1
				 	ELSE 4 END 									AS "IdResultado"
	  			,'MESA_DESTINO_CERRADA' 					AS "Codigo"
	  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
	  			,'IdOperacion' 								AS "NombreParametro"
		FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" mes ON (ope."IdMesaDestino" = mes."IdMesa")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		
		SELECT
			por."IdRegistroOpe"
			,CASE 
				WHEN val."PRODUCTO_NO_HABILITADO" = 0 THEN 1 
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' 
				    AND (IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0' OR IFNULL(perfBase."IdTipoOperacion",perfExtra."IdTipoOperacion") = '0' OR IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = por."IdTipoOperacion" OR IFNULL(perfBase."IdTipoOperacion",perfExtra."IdTipoOperacion") = por."IdTipoOperacion") THEN 1
			 	ELSE 4 END 								AS "IdResultado"
  			,'PRODUCTO_NO_HABILITADO' 					AS "Codigo"
  			,CAST(por."IdRegistroOpe" AS NVARCHAR(100)) 	AS "ValorParametro"
  			,'IdOperacion' 								AS "NombreParametro"
		FROM :v_PortafolioCliente por
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfExtra ON (por."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND perfExtra."IdTipoProducto" = 'MERF' AND perfExtra."IdEmpresa" = p_IdEmpresa AND (perfExtra."IdTipoOperacion" = por."IdTipoOperacion" OR perfExtra."IdTipoOperacion" = '0') )
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfBase ON (por."IdPerfilProductos" = perfBase."IdPerfilProductos" AND perfBase."IdTipoProducto" = 'MERF' AND perfBase."IdEmpresa" = p_IdEmpresa AND (perfBase."IdTipoOperacion" = por."IdTipoOperacion" OR perfBase."IdTipoOperacion" = '0'))
			, :v_matriz_validaciones val
		WHERE por."IdRegistroOpe" = val."IdRegistroOpe"
		
		UNION
		SELECT ope."IdRegistroOpe"
			,CASE 
				WHEN ope."IdUsuario" = cOpe."IdUsuarioCreacion" OR ope."IdUsuario" = usuSup."IdUsuario" THEN 1
				ELSE 4 END 								AS "IdResultado"
			,'USUARIO_NO_AUTORIZADO_A_MODIFICAR_OPERACION' 					AS "Codigo"
  			,CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) AS "ValorParametro"
  			,'IdOperacion' 								AS "NombreParametro"
		FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Operacion_IN" ope 
			INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" cOpe ON (ope."IdRegistroOpe" = cOpe."IdOperacion")
			INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (cOpe."IdUsuarioCreacion" = usu."IdUsuario" AND usu."TipoEntidad" = 'EMP')
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" emp ON (usu."IdEntidad" = emp."IdEmpleado")
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuSup ON (usuSup."IdEntidad" = emp."IdEmpleadoSupervisor")
			, :v_matriz_validaciones val
		WHERE ope."IdRegistroOpe" = val."IdRegistroOpe"
		
		--agrego requisitos ooperativos. se supone que debe migrar todo aca
		UNION
		SELECT ope."IdRegistroOpe"
			,CASE 
				--WHEN ope."IdCondicionOperativa" = 'CONTCUSTODIA' and ope."ValorIndicador" = 'S' THEN 1
				WHEN ope."IdCondicionOperativa" = 'CONTCONDGRAPACTO' and (ope."IdTipoOperacion" = 'COMTER' or "IdTipoOperacion"= 'VENTER') THEN 1
				WHEN ope."IdCondicionOperativa" = 'CONTCONDGRAPACTO' and (ope."IdTipoOperacion" = 'COMPAC' or "IdTipoOperacion"= 'VENPAC') and ope."ValorIndicador" = 'S' THEN 1
				ELSE 
					case 
						when "IdTipoComportamiento" = 'RECHAZO' then 4
						when "IdTipoComportamiento" = 'AUTORIZACION' then 3
						when "IdTipoComportamiento" = 'ADVERTENCIA' then 2
						else 4
					end
				END 								AS "IdResultado"
			,CASE 
				--WHEN ope."IdCondicionOperativa" = 'CONTCUSTODIA' THEN 'CLIENTE_SIN_CONTRATO_CUSTODIA'
				WHEN ope."IdCondicionOperativa" = 'CONTCONDGRAPACTO' THEN 'CLIENTE_SIN_CONTRATO_COND_GRAL_PACTOS'
				ELSE '' END								AS "Codigo"
  			,CASE 
				--WHEN ope."IdCondicionOperativa" = 'CONTCUSTODIA' THEN ope."DocumentoIdentidad"
				WHEN ope."IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN ope."DocumentoIdentidad"
				ELSE CAST(ope."IdRegistroOpe" AS NVARCHAR(100)) END AS "ValorParametro"
  			,CASE 
				--WHEN ope."IdCondicionOperativa" = 'CONTCUSTODIA' THEN 'DocumentoIdentidad'
				WHEN ope."IdCondicionOperativa" = 'CONTCONDGRAPACTO'  THEN 'DocumentoIdentidad'
				ELSE 'IdOperacion' END 								AS "NombreParametro"
		FROM :v_PortafolioRequisitosOperativos ope;


	--p_out_SalidaDetalle = 
		DELETE FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_OUT";
		INSERT INTO "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_DetalleOperacion_OUT"
		SELECT 	tb."IdRegistroOpe"
	    		,CASE 	"IdResultado"
						WHEN 1 THEN 'OK'
						WHEN 2 THEN 'ADV'
						WHEN 3 THEN 'AUT'
						WHEN 4 THEN 'ERR'
				END AS "Resultado"
				,tb."Codigo" 
				,tb."ValorParametro" 
				,tb."NombreParametro" 
		FROM 	:v_TablaRequisitos tb
		WHERE 	"IdResultado" <> 1
		ORDER BY "IdRegistroOpe";
						
	--p_out_Salida = 	
		DELETE FROM "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Resultado_OUT";
		INSERT INTO "BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosCrearOperacionRF_Resultado_OUT"
		SELECT 	"IdRegistroOpe"
				,CASE 	MAX("IdResultado")
						WHEN 1 THEN 'OK'
						WHEN 2 THEN 'ADV'
						WHEN 3 THEN 'AUT'
						WHEN 4 THEN 'ERR'
				END AS "Resultado"
		FROM 	:v_TablaRequisitos 
		GROUP BY "IdRegistroOpe"
		ORDER BY "IdRegistroOpe";
END;


          
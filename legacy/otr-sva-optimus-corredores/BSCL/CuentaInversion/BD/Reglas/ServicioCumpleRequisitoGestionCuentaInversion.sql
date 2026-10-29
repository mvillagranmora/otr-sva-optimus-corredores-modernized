SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoGestionCuentaInversion";

CREATE PROCEDURE
-- 
--  Proposito: Validar si se puede crear el movimiento de cargo o abono
--  Autor: NRM - BCS 
--  Fecha Creacion: 08/01/2016
-- 
--
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoGestionCuentaInversion" (
	IN  p_CuentaInversion			TABLE ("IdInterno"					BIGINT,
										   "IdCliente"					BIGINT,
										   "IdPortafolio"				BIGINT,
										   "IdMoneda"					NVARCHAR(3),
										   "IdCuentaInversion"			BIGINT,
										   "Nombre"						NVARCHAR(250),
										   "CuentaInversionPrincipal"	NVARCHAR(1),
										   "Accion"						NVARCHAR(1)),

	-- SalidAS para mensajes de errores
	OUT p_out_MovimientosRech		TABLE ("IdInterno"					BIGINT),
	OUT p_out_ErroresDetalle    	TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
	OUT p_out_resultadoRegla    	NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
													--ADVE = 	 								  	   
                                                   	--RECH=Alguna de lAS reglAS detienen la operacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	READS SQL DATA AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION;
	DECLARE v_erroroutmsg  			NVARCHAR(5000);
	DECLARE v_ErrorNoData  			NVARCHAR(100);

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		p_out_resultadoRegla 	:= 'RECH';
		v_ErrorNoData 			:= 'DATA_NO_EXISTE';
		v_hash					 = SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END;
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "IdInterno" FROM dummy WHERE 1=2;
	
	
	BEGIN
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),
												"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
		DECLARE v_TablaRequisitos     	TABLE  ("Prioridad" INT,"Cumple" BIGINT, "Codigo" NVARCHAR(100), 
												"CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100), 
												"ValorDato2" NVARCHAR(520),"IdInterno" BIGINT);
						
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT  0 AS "IdInterno",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",
										 '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		v_TablaRequisitos 		= SELECT  0 AS "Prioridad",0 AS "Cumple",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",
								   		 '' AS "CodigoDato2",'' AS "ValorDato2",0 AS "IdInterno" FROM dummy WHERE (1=2);
		
		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_TablaRequisitos =
		 SELECT  1 AS "Prioridad"
		       	,CASE WHEN COUNT(1) = 0 THEN 0 ELSE 1 END AS "Cumple"
		       	,'NO_LLEGAN_MOVIMIENTOS_GESTION_CUENTA_INVERSION' AS "Codigo"
		       	,'' AS "CodigoDato1"
		       	,'' AS "ValorDato1"
		       	,'' AS "CodigoDato2"
		       	,'' AS "ValorDato2"
				,0 as "IdInterno"
		   FROM :p_CuentaInversion
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;


		
		--**********************************************************************************************
		--***************************************  NEGOCIO  ********************************************
		--**********************************************************************************************
		v_CuentaInversion = 
		 SELECT  TCI."IdInterno"
			    ,TCI."IdCliente"
			    ,CLI."IdCliente" as "IdClienteReal"
			    ,CLI."NombreCompleto" as "NombreCliente"
			    ,TCI."IdPortafolio"
			    ,TCI."IdMoneda"
			    ,MON."IdMoneda" as "IdMonedaReal"
			    ,MON."Nombre" as "NombreMoneda"
			    ,MON."IdEstadoMonedaTesoreria" as "IdEstadoMoneda"
			    ,TCI."IdCuentaInversion"
			    ,TCI."Nombre"
			    ,TCI."CuentaInversionPrincipal"
			    ,TCI."Accion"
			    ,CI."IdCuentaInversion" as "IdCuentaInversionReal"
			    ,CI."IdEstado" as "IdEstadoCuentaInversion"
		   FROM :p_CuentaInversion as TCI
		   LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI 
		  		  ON CI."IdPortafolio" 		= TCI."IdPortafolio" 
		  		 AND CI."IdMoneda" 			= TCI."IdMoneda"
		   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
		   		  ON CLI."IdCliente"		= TCI."IdCliente"
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" as MON 
		  		  ON MON."IdMoneda"	 		= TCI."IdMoneda";		
		--SELECT 'v_CuentaInversion', * FROM :v_CuentaInversion;
		
		v_TablaRequisitos =
		  SELECT  2 AS "Prioridad"
		       	,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN 0
		       		  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN 0
		       		  WHEN TCI."IdEstadoMoneda" = 'INA' THEN 0
		       		  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN 0
		       		  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN 0
		       		  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN 0
		       		  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN 0
		       		  ELSE 1 
				 END AS "Cumple"
		       	,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN 'MONEDA_VACIA'
		       		  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN 'MONEDA_NO_EXISTE'
		       		  WHEN TCI."IdEstadoMoneda" = 'INA' THEN 'MONEDA_NO_ACTIVA'
		       		  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN 'CLIENTE_VACIO'
		       		  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN 'PORTAFOLIO_VACIO'
		       		  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN 'CUENTA_INVERSION_EXISTE'
		       		  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN 'CUENTA_INVERSION_NO_ACTIVA'
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN 'IdMoneda'
					  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN 'IdMoneda'
					  WHEN TCI."IdEstadoMoneda" = 'INA' THEN 'IdMoneda'
					  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN 'IdCliente'
					  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN 'IdPortafolio'
					  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN 'NombreMoneda'
					  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN 'NombreMoneda'
					  ELSE  ''
				 END AS "CodigoDato1"
				,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN TCI."IdMoneda"
					  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN TCI."IdMoneda"
					  WHEN TCI."IdEstadoMoneda" = 'INA' THEN TCI."IdMoneda"
					  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN CAST(TCI."IdCliente" as NVARCHAR)
					  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN CAST(TCI."IdPortafolio" as NVARCHAR)
					  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN TCI."NombreMoneda"
					  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN TCI."NombreMoneda"
					  ELSE  ''
				 END AS "ValorDato1"
				,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN ''
					  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN ''
					  WHEN TCI."IdEstadoMoneda" = 'INA' THEN ''
					  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN ''
					  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN ''
					  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN 'NombreCliente'
					  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN 'NombreCliente'
					  ELSE  ''
				 END AS "CodigoDato2"
				,CASE WHEN LENGTH(COALESCE(TCI."IdMoneda",'')) = 0 THEN ''
					  WHEN COALESCE(TCI."IdMonedaReal",'') = '' THEN ''
					  WHEN TCI."IdEstadoMoneda" = 'INA' THEN ''
					  WHEN COALESCE(TCI."IdCliente",0) = 0 THEN ''
					  WHEN COALESCE(TCI."IdPortafolio",0) = 0 THEN ''
					  WHEN COALESCE(TCI."IdCuentaInversionReal",0) > 0 THEN TCI."NombreCliente"
					  WHEN TCI."IdEstadoCuentaInversion" = 'INA' THEN TCI."NombreCliente"
					  ELSE  ''
				 END AS "ValorDato2"
				,TCI."IdInterno"
		   FROM :v_CuentaInversion as TCI
		  WHERE TCI."Accion" = 'I'
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		
		
		--Desactivar / Activar cuenta inversion
		v_TablaRequisitos =
		 SELECT  2 AS "Prioridad"
		       	,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 0
		       		  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN 0
		       		  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN 0
		       		  ELSE 1 
				 END AS "Cumple"
		       	,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'CUENTA_INVERSION_NO_EXISTE'
		       		  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN 'CUENTA_INVERSION_NO_ACTIVA'
		       		  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN 'MONEDA_NO_ACTIVA'
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'NombreCliente'
					  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN 'NombreMoneda'
					  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN 'IdMoneda'
					  ELSE  ''
				 END AS "CodigoDato1"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN TCI."NombreCliente"
					  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN TCI."NombreMoneda"
					  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN TCI."IdMoneda"
					  ELSE  ''
				 END AS "ValorDato1"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'NombreMoneda'
					  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN 'NombreCLiente'
					  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN ''
					  ELSE  ''
				 END AS "CodigoDato2"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN TCI."NombreMoneda"
					  WHEN TCI."Accion" = 'D' AND TCI."IdEstadoCuentaInversion" = 'INA' THEN TCI."NombreCliente"
					  WHEN TCI."Accion" = 'A' AND TCI."IdEstadoMoneda" = 'INA' THEN ''
					  ELSE  ''
				 END AS "ValorDato2"
				,TCI."IdInterno"
		   FROM :v_CuentaInversion as TCI
		  WHERE TCI."Accion" IN ('D','A')
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;


		--Modificar cuenta inversion
		v_TablaRequisitos =
		 SELECT  2 AS "Prioridad"
		       	,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 0
		       		  --WHEN COALESCE(TCI."Nombre",'') = '' THEN 0
		       		  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN 0
		       		  ELSE 1 
				 END AS "Cumple"
		       	,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'CUENTA_INVERSION_NO_EXISTE'
		       		  --WHEN COALESCE(TCI."Nombre",'') = '' THEN 'NOMBRE_EN_BLANCO'
		       		  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN 'INDICADOR_INCORRECTO'
					  ELSE  ''
				 END AS "Codigo"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'NombreCliente'
					  --WHEN COALESCE(TCI."Nombre",'') = '' THEN 'Nombre'
					  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN ''
					  ELSE  ''
				 END AS "CodigoDato1"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN TCI."NombreCliente"
					  --WHEN COALESCE(TCI."Nombre",'') = '' THEN TCI."Nombre"
					  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN ''
					  ELSE  ''
				 END AS "ValorDato1"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN 'NombreMoneda'
					  --WHEN COALESCE(TCI."Nombre",'') = '' THEN ''
					  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN ''
					  ELSE  ''
				 END AS "CodigoDato2"
				,CASE WHEN COALESCE(TCI."IdCuentaInversionReal",0) = 0 THEN TCI."NombreMoneda"
					 -- WHEN COALESCE(TCI."Nombre",'') = '' THEN ''
					  WHEN COALESCE(TCI."CuentaInversionPrincipal",'N') NOT IN ('S','N') THEN ''
					  ELSE  ''
				 END AS "ValorDato2"
				,TCI."IdInterno"
		   FROM :v_CuentaInversion as TCI
		  WHERE TCI."Accion" = 'M'
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		
		
		--select * from :v_TablaRequisitos; --BORRAR
		--**********************************************************************************************
		--***********************************  ANALISIS DE DATOS  **************************************
		--**********************************************************************************************
		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	   							
		
		p_out_MovimientosRech =
	     SELECT DISTINCT "IdInterno"
	      FROM :v_TablaRequisitos
	     WHERE "Cumple" = 0;
	   							
		v_TablaCodigosMensajes =
		SELECT  "IdInterno"
			   ,"Codigo"
			   ,"CodigoDato1"
			   ,"ValorDato1"
			   ,"CodigoDato2"
			   ,"ValorDato2" 
		  FROM :v_TablaRequisitos
		 WHERE "Cumple" = 0
		 ORDER BY "Prioridad" DESC;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalle);

	    p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   ED."Descripcion"
		  FROM :v_out_ErroresDetalle as ED;		  
    END;
END;

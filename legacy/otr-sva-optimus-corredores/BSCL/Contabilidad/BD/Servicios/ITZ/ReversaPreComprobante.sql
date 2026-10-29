SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ReversaPreComprobante";
CREATE PROCEDURE
-- 
--  Proposito: Genera Precomprobantes ingresando movimientos genericos 
--			   deacuerdo a la condicion establecida en los mantenedores
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Rogers Abreu :: RAV
--  Fecha Creacion: 13/08/2016
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.ReversaPreComprobante"(
    IN 	p_IdEmpresa   					BIGINT  
   ,IN 	p_IdPreComprobanteAnt 			DECIMAL(18,0)  
   ,IN 	p_FechaContable  				NVARCHAR(10)  
   ,IN 	p_CodigoUsuario  				NVARCHAR(40)  
   ,IN 	p_IdPreComprobante  			DECIMAL(18,0)
	-- Parametros de Salida
	,OUT p_out_AdvertenciasContador 	INT
	,OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	,OUT p_out_ErroresContador 		INT
	,OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
   )

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN                     
	DECLARE v_FechaProceso					NVARCHAR(10); 
	DECLARE v_HoraProceso					NVARCHAR(8);  
	DECLARE v_Reversado						NVARCHAR(1);  
	DECLARE v_GlosaError					NVARCHAR(600);  
	DECLARE v_IndicadorError				NVARCHAR(50);  
	DECLARE v_IdLog							DECIMAL(18);  
	DECLARE v_IdPreComprobante				DECIMAL(18);
	DECLARE v_IdPreComprobanteAsiento		DECIMAL(18);  
	DECLARE v_IdPreComprobanteAsientoAnt	DECIMAL(18);  
	DECLARE v_contadorReg					DECIMAL(18);
	DECLARE v_cantidadReg					DECIMAL(18);
 	DECLARE p_out_MsjRetorno   				NVARCHAR(600);
	DECLARE v_CantidadRegistros				BIGINT;
 	
 	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
 	
 	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;
 	
 	
	CREATE LOCAL TEMPORARY TABLE #TabPreCompAsiento (   
										"ID"						DECIMAL(18, 0)
										,"IdEmpresa"				BIGINT
										,"IdPreComprobante"			DECIMAL(18, 0)
										,"IdCuentaContable"			NVARCHAR(12)
										,"Ejercicio"				BIGINT
										,"Linea"					DECIMAL(10, 0)
										,"MontoDebe"				DECIMAL(18, 2)
										,"MontoHaber"				DECIMAL(18, 2)
										,"FechaContable"			NVARCHAR(10)
										,"Glosa"					NVARCHAR(120)
										,"Estado"					NVARCHAR(1)
										,"IdPreComprobanteAsiento"	DECIMAL(18, 0)
													);


	v_FechaProceso:= CAST(NOW() AS NVARCHAR(10));
	v_HoraProceso:= CAST(NOW() AS CHAR(8));
   
   	v_CantidadRegistros := 0;
	
	SELECT COUNT(*) INTO v_CantidadRegistros		
	FROM
		"BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante
	WHERE 
		CTB_itzPreComprobante."IdReversa" = :p_IdPreComprobanteAnt;  

/*   
	SELECT TOP 1
		'S'
	INTO
		v_Reversado
	FROM
		"BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante
	WHERE 
		CTB_itzPreComprobante."IdReversa" = :p_IdPreComprobanteAnt;  
*/	
  
	IF :v_CantidadRegistros > 0 THEN
		DROP TABLE #TabPreCompAsiento;
		v_out_ErroresDetalle = 
		SELECT :p_IdPreComprobanteAnt AS "IdInterno", 'PRECOMPROBANTE_YA_ESTA_REVERSADO' AS "Codigo", TRIM(CAST(:p_IdPreComprobanteAnt AS NVARCHAR)) AS "ValorParametro", 'IdPreComprobanteAnt' AS "NombreParametro" FROM DUMMY;
		SIGNAL SALIDA_CONTROLADA;
	END IF;
  
	--+ Leer el Pre-Voucher y lo duplica con la glosa distinta y asociado al voucher anterior  
	v_GlosaError:= 'Al Reversar Pre-Comprobante ';
	v_IndicadorError:= '300';

 	
	IF :p_IdPreComprobante > 0 THEN
		v_IdPreComprobante = p_IdPreComprobante;
	ELSE
		SELECT 
			"BSCL.Contabilidad.BD.Secuencias::IdPreComprobante".NEXTVAL
		INTO
			v_IdPreComprobante
		FROM 
			DUMMY;
	END IF;

	INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante"
        ("IdPreComprobante"
        ,"IdTipoContabilidad"
        ,"IdEmpresa"
        ,"Ejercicio"
        ,"Tipo"
        ,"FechaContable"
        ,"Glosa"
        ,"FechaProceso"
        ,"HoraProceso"
        ,"CodigoUsuario"
        ,"FolioContabilidad"
        ,"IdReversa"  
        ,"Estado")  
	SELECT 
	    :v_IdPreComprobante
	   ,CTB_itzPreComprobante."IdTipoContabilidad"
       ,CTB_itzPreComprobante."IdEmpresa"
       ,CTB_itzPreComprobante."Ejercicio"
       ,CTB_itzPreComprobante."Tipo"
       ,:p_FechaContable  
       ,'REVERSA PRE-COMPROBANTE N° ' || TRIM(CAST(:p_IdPreComprobanteAnt AS NVARCHAR))
       ,:v_FechaProceso  
       ,:v_HoraProceso  
       ,:p_CodigoUsuario  
       ,CTB_itzPreComprobante."FolioContabilidad"
       ,:p_IdPreComprobanteAnt  
       ,'V'  
	FROM 
		"BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzPreComprobante
	WHERE 
		CTB_itzPreComprobante."IdPreComprobante" = :p_IdPreComprobanteAnt;  
  
  
   --+ Leer el detalle del Prevoucher y lo Voltea  
  
   v_GlosaError:= 'Al Reversar Asientos de Pre-Comprobante ';  
   v_IndicadorError:= '400'; 

   INSERT INTO #TabPreCompAsiento
			("ID"
			,"IdEmpresa"
			,"IdPreComprobante"
			,"IdCuentaContable"
			,"Ejercicio"
			,"Linea"
			,"MontoDebe"
			,"MontoHaber"
			,"FechaContable"
			,"Glosa"  
			,"Estado"  
			,"IdPreComprobanteAsiento"  )
	SELECT 
		 ROW_NUMBER() OVER(ORDER BY CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento")
		,CTB_itzPreComprobanteAsiento."IdEmpresa"
		,:v_IdPreComprobante  
		,CTB_itzPreComprobanteAsiento."IdCuentaContable"
		,CTB_itzPreComprobanteAsiento."Ejercicio"
		,CTB_itzPreComprobanteAsiento."Linea"
		,CASE   
			WHEN CTB_itzPreComprobanteAsiento."MontoDebe" > 0 THEN 0   
			ELSE CTB_itzPreComprobanteAsiento."MontoHaber"  
		 END   --+ Se voltean los Valores del Haber hacia el Debe  
		,CASE   
			WHEN CTB_itzPreComprobanteAsiento."MontoHaber" > 0 THEN 0  
			ELSE CTB_itzPreComprobanteAsiento."MontoDebe"  
		 END  --+ Se voltean los Valores del Debe hacia al Haber  
		,:p_FechaContable  
		,'REVERSA PRE-COMPROBANTE N° ' || TRIM(CAST(:p_IdPreComprobanteAnt AS NVARCHAR)) || ' CUENTA N° ' || TRIM(CAST("IdCuentaContable" AS NVARCHAR))  
		,CTB_itzPreComprobanteAsiento."Estado"
		,CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"
	FROM 
		"BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento
	WHERE 
		CTB_itzPreComprobanteAsiento."IdPreComprobante" = :p_IdPreComprobanteAnt;  

	SELECT
		COUNT(1)
	INTO
		v_cantidadReg
	FROM
		#TabPreCompAsiento;
	
	IF :v_cantidadReg IS NULL THEN
		v_cantidadReg:= 0;
	END IF;
	
	v_contadorReg = 1;

	--+ Recorre c/u de las Facturas 
	WHILE :v_contadorReg <= :v_cantidadReg DO                  

		SELECT 
			"IdPreComprobanteAsiento"
		INTO
			v_IdPreComprobanteAsientoAnt
		FROM 
			#TabPreCompAsiento
		WHERE 
			"ID" = :v_contadorReg;

		SELECT 
			"BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento".NEXTVAL
		INTO
			v_IdPreComprobanteAsiento
		FROM
			DUMMY;

		INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
		("IdPreComprobanteAsiento"
		,"IdEmpresa"
		,"IdPreComprobante"  
		,"IdCuentaContable"  
		,"Ejercicio"
		,"Linea"
		,"MontoDebe"  
		,"MontoHaber"  
		,"FechaContable"
		,"Glosa"
		,"Estado")
		SELECT
		 :v_IdPreComprobanteAsiento
		,"IdEmpresa"
		,:v_IdPreComprobante  
		,"IdCuentaContable"  
		,"Ejercicio"
		,"Linea"
		,"MontoDebe"  
		,"MontoHaber"  
		,"FechaContable"
		,"Glosa"
		,"Estado"
		FROM 
			#TabPreCompAsiento 
		WHERE 
			"ID" = :v_contadorReg;

		
		INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento"
		("IdPreComprobanteAsiento"
		,"IdEvento"
		,"Operador"
		,"Linea"
		,"Estado"
		,"GlosaDetalle")
        SELECT  
		 :v_IdPreComprobanteAsiento
		,"IdEvento"
		,"Operador"
		,"Linea"
		,"Estado"
		,"GlosaDetalle"
		FROM 
			"BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
		WHERE
			"IdPreComprobanteAsiento" = :v_IdPreComprobanteAsientoAnt;

		v_contadorReg:= :v_contadorReg + 1;
  
	END WHILE;

	p_out_MsjRetorno:= 'OK';  
	DROP TABLE #TabPreCompAsiento;
  
	--COMMIT;
  
  
  --ROLLBACK TRAN  
  
  --SET @pMsjRetorno =   
  --  'ErrorPrg - '   
  --   + @GlosaError  
  --   + 'Empresa[' + LTRIM(RTRIM(STR(@pIdEmpresa))) + '] '  
  --   + 'NumComp[' + LTRIM(RTRIM(STR(@pIdPreComprobanteAnt))) + '] '  
  --   + 'FecCont[' + @pFechaContable + '] '  
  --   + 'Usuario[' + @pCodigoUsuario + '] '  
  --   + RTRIM(dbo.CTB_itzFnMensajeErrorSql('')) + '[' + @IndicadorError + ']'  
  
  --EXEC  [CTB_itzInsertaLog]--+Inserta en el Proceso LOG  
  --         @pIdTipoLog   = 2      
  --        ,@pIdEmpresa   = @pIdEmpresa  
  --        ,@pIdTipoProceso  = 5  
  --        ,@pCodigoUsuario  = @pCodigoUsuario    
  --        ,@pGlosa    = @GlosaError    
  --        ,@pIdPreComprobante  = @pIdPreComprobanteAnt  
  --       ,@pIndicadorError  = @IndicadorError  
  --        ,@pEstado    = 'E'  
  --        ,@pIdLog    = @IdLog  OUTPUT                   
  --        ,@pMsjRetorno   = @pMsjRetorno    
  
END
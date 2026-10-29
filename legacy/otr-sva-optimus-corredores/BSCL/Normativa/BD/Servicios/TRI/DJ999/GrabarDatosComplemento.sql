SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ999.GrabarDatosComplemento";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ999.GrabarDatosComplemento"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    -- Periodo
	IN p_Periodo					    NVARCHAR(7), --YYYY/MM			
	
    ------------ Parametros de Salida Propios ----------

    -- ID del registro.
    OUT p_out_periodo NVARCHAR(7),

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------
    DECLARE p_saldos  TABLE ("RutInstitucion" nvarchar(10), "FechaPosicion" date,"Concepto" bigint,"Moneda" bigint,"Paridad" DECIMAL(10,2), "MontoMonedaPrincipal" DECIMAL(20,2), "MontoDolar" DECIMAL(20,2));
    DECLARE v_periodoUltimoDia NVARCHAR(40):='';
 	DECLARE v_UltimoDiaHabilMes		DATE;
 	DECLARE v_FechaSistema 			DATE;
 	DECLARE v_DiaHabilResultado     NVARCHAR(1);   
 	DECLARE v_IdPaisSistema 		BIGINT;
    ------------ Bloque Comun de Inicializacion ----------

    DECLARE salida_controlada condition;
    DECLARE EXIT handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE EXIT handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle union all
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            resignal;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------
    
    --Obtener ultimo dia habil del periodo
    SELECT TO_VARCHAR (LAST_DAY(TO_DATE(p_Periodo, 'YYYY/MM')), 'YYYY-MM-DD') INTO v_periodoUltimoDia FROM DUMMY;
    
    --Obtiene fecha de sistema
    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);  
    
    IF(v_FechaSistema < v_periodoUltimoDia) THEN
    	v_periodoUltimoDia = v_FechaSistema;
    END IF;
    
    SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
    
	CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(v_periodoUltimoDia, v_IdPaisSistema, v_DiaHabilResultado);
					
	IF v_DiaHabilResultado = 'N' THEN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior"(
			 p_Fecha      		 => v_periodoUltimoDia
			,p_NumeroDias 		 => 1
			,p_EsHabil    		 => 'S'
			,p_IdPais     		 => v_IdPaisSistema
			,p_out_FechaAnterior => v_UltimoDiaHabilMes
		);
	ELSE 
		v_UltimoDiaHabilMes = v_periodoUltimoDia;
	END IF;
					 
	p_saldos =  SELECT "RutInstitucion",
					   "FechaPosicion",
					   "Concepto",
					   "Moneda",
					   COALESCE("Paridad",0) AS "Paridad",
					   COALESCE(SUM(ROUND("MontoMonedaPrincipal", 2)),0) AS "MontoMonedaPrincipal",
					   COALESCE (SUM(ROUND("MontoDolar", 2)),0) AS "MontoDolar"
				FROM ( SELECT EMP."DocumentoIdentidad" AS "RutInstitucion"
							  ,TO_NVARCHAR(v_UltimoDiaHabilMes,'YYYY-MM-DD') AS "FechaPosicion"
							  ,1 AS "Concepto"
							  ,MON."CodigoNumerico" AS "Moneda"
							  ,CASE WHEN SPT."IdMonedaPrincipal" <> 'CLP'THEN NULLIF(PAR_MON."ParidadMercado",0)
							  		WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN NULLIF(PAR_USD."TipoCambioMercado",0)
        					        ELSE 0
        					   END AS "Paridad"
							  ,SPT."MontoMonedaPrincipal"	
						 	  ,CASE WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" * NULLIF(PAR_MON."ParidadMercado",0), 0)
						      		WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / NULLIF(PAR_MON."ParidadMercado",0), 0) 
						      		WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / NULLIF(PAR_USD."TipoCambioMercado",0), 0)
						       ELSE 0 END AS "MontoDolar"
						FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
							ON Cliente."IdCliente" = SPT."IdCliente"
						INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
							ON (EMP."IdEmpresa" = :p_IdEmpresa) 
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
							ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")	
						LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
							ON (SPT."IdMonedaPrincipal" = PAR_MON."IdMoneda" AND v_UltimoDiaHabilMes = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
						LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
					    	ON ('USD' = PAR_USD."IdMoneda" AND v_UltimoDiaHabilMes = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))	
						WHERE SPT."OperacionFicticia" = 'N'
						AND SPT."Estado" = 'VIG'
						AND TO_CHAR(SPT."FechaOperacion",'YYYY/MM') = :p_Periodo)
						GROUP BY "RutInstitucion","FechaPosicion","Concepto","Moneda","Paridad"	

				UNION ALL	
	
				SELECT "RutInstitucion",
					   "FechaPosicion",
					   "Concepto",
					   "Moneda",
					   "Paridad",
					   ROUND("MontoMonedaPrincipal", 2) AS "MontoMonedaPrincipal",
					   COALESCE(SUM(ROUND("MontoDolar", 2)),0) AS "MontoDolar"
				FROM ( SELECT  EMP."DocumentoIdentidad" AS "RutInstitucion"
					      	  ,TO_NVARCHAR(v_UltimoDiaHabilMes,'YYYY-MM-DD') AS "FechaPosicion"
						      ,CASE WHEN SPT."IdTipoOperacion"='COMSPT'AND  RTRIM(LTRIM(Cliente."DocumentoIdentidad")) IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario") THEN 2
          							WHEN SPT."IdTipoOperacion"='COMSPT' AND RTRIM(LTRIM(Cliente."DocumentoIdentidad")) NOT  IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario")THEN 3
         					 		WHEN SPT."IdTipoOperacion"='VENSPT' AND RTRIM(LTRIM(Cliente."DocumentoIdentidad")) NOT  IN (SELECT RTRIM(LTRIM("DocumentoIdentidad")) FROM "BSCL.Mesa.BD::SPT.EntMercadoCambiario")THEN 4
    						   END AS "Concepto"
						      ,0 AS "Moneda"
						      ,0.0 AS "Paridad"
						      ,0.0 AS "MontoMonedaPrincipal"
						      ,CASE WHEN SPT."IdMonedaPrincipal"='USD' THEN SPT."MontoMonedaPrincipal"
        							WHEN  SPT."IdMonedaSecundaria"='USD'  THEN SPT."MontoMonedaSecundaria"
							        WHEN MON."IdTipoMoneda" = 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" * 
							            (CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0)
							        WHEN MON."IdTipoMoneda" <> 'FUE' AND SPT."IdMonedaPrincipal" <> 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
							            (CASE WHEN SPT."ParidadCierre" <> 0 THEN SPT."ParidadCierre" ELSE NULLIF(PAR_MON."ParidadMercado",0) END), 0) 
							        WHEN SPT."IdMonedaPrincipal" = 'CLP' THEN COALESCE(SPT."MontoMonedaPrincipal" / 
							            (CASE WHEN SPT."TipoCambioCierre" <> 0 THEN SPT."TipoCambioCierre" ELSE NULLIF(PAR_USD."TipoCambioMercado",0) END), 0)
							   ELSE 0 END AS "MontoDolar" 
				   		FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPT
				   		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente 
							ON Cliente."IdCliente" = SPT."IdCliente"
						INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
							ON (EMP."IdEmpresa" = :p_IdEmpresa)
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON   
							ON (SPT."IdMonedaPrincipal" = MON."IdMoneda")
						LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_MON
							ON (SPT."IdMonedaPrincipal" = PAR_MON."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_MON."Fecha",'YYYY-MM-DD'))
						LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR_USD
				    		ON ('USD' = PAR_USD."IdMoneda" AND TO_CHAR(SPT."FechaOperacion",'YYYY-MM-DD') = TO_CHAR(PAR_USD."Fecha",'YYYY-MM-DD'))
						LEFT JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" PRO
							ON Cliente."IdPersona"=PRO."IdPersona" 
						WHERE SPT."OperacionFicticia" = 'N'
						AND SPT."Estado"  = 'VIG'
						AND TO_CHAR(SPT."FechaOperacion",'YYYY/MM') = :p_Periodo)
						WHERE "Concepto" IS NOT NULL
						GROUP BY "RutInstitucion","FechaPosicion","Concepto","Moneda","Paridad","MontoMonedaPrincipal"
						ORDER BY "Concepto" ASC;

   p_out_periodo := p_Periodo;
    
   DELETE FROM "BSCL.Normativa.BD::TRI.SaldoDJ999" AS SAL 
   WHERE EXISTS (SELECT 1 FROM  "BSCL.Normativa.BD::TRI.SaldoDJ999" WHERE  SAL."Periodo" = p_Periodo AND SAL."IdEmpresa" = p_IdEmpresa);

   INSERT INTO "BSCL.Normativa.BD::TRI.SaldoDJ999"(
			"Periodo",
			"IdEmpresa",
			"IdUsuario",
		    "RutInstitucion",
			"FechaPosicion",
			"Concepto",
			"Moneda",
			"Paridad",
			"MontoMonedaPrincipal",
			"MontoDolar")
	SELECT  p_Periodo,
			p_IdEmpresa,
			p_IdUsuario,
		    "RutInstitucion",
			"FechaPosicion",
			"Concepto",
			"Moneda",
			"Paridad",
			"MontoMonedaPrincipal",
			"MontoDolar"
	FROM :p_saldos;

END;
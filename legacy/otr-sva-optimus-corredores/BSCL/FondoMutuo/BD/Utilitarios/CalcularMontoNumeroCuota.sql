SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota";
GO
CREATE PROCEDURE
--  Proposito: Calcula Monto o Numero de Cuotas para una Solicitud Aporte Rescate dependiendo del numero de cuotas.
--  Autor: Gat-Blac 
--  Fecha Creacion: 21/07/2016
--- modificaciones
--  Proposito: Se cambia IdTrabajo que estaba en duro (0,1,2,3) a la secuencia correspondiente
--  Autor: OCT - Oscar Carreno Tolmo
--  Fecha Creacion: 04/01/2018
---------------------------------------------------------------------------------------------------------------
--  Proposito: Se agrega CalculoPorMontoCuota por 'Porcentaje (p)', esta se comportara como si fuera por cuotas
--  Autor: OCT - Oscar Carreno Tolmo
--  Fecha Creacion: 22/01/2018
"BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota" (
	IN p_IdEmpresa			BIGINT,
	IN p_Series				TABLE	(
										"IdInterno"			   BIGINT,
										"Fecha"				   DATE,
										"IdSerieFondo"		   NVARCHAR(40),
									 	"CalculoPorMontoCuota" NVARCHAR(1),
									 	"Monto"				   DECIMAL(26,8),
									 	"NumeroCuotas"		   DECIMAL(26,8),
									 	"IdMonedaFondo"	   	   NVARCHAR(3),
									 	"IdMonedaInversion"	   NVARCHAR(3),
									 	"TipoSolicitud"		   NVARCHAR(3),
									 	"TipoFactorAjuste"	   NVARCHAR(2),
									 	"IdEmpresa"			   BIGINT
								 	),
	OUT p_out_Resultado		TABLE	(
										"IdInterno"			   BIGINT,
										"TipoSolicitud"		   NVARCHAR(3),
									 	"Monto"				   DECIMAL(26,8),
									 	"NumeroCuotas"		   DECIMAL(26,8),
									 	"ValorCuota"		   DECIMAL(26,8),
									 	"MontoDestino"	       DECIMAL(26,8),
									 	"ValorParidad"		   DECIMAL(26,8),
									 	"ValorFactorAjuste"	   DECIMAL(26,8),
									 	"CostoEnFactorAjuste"  DECIMAL(26,8),
									 	"IdMonedaSistema"	   NVARCHAR(3),
									 	"MontoMonedaSistema"   DECIMAL(26,8),
									 	"Resultado"			   NVARCHAR(3),
									 	"CodigoMensaje"		   NVARCHAR(100)
									)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_usa_dolar			NVARCHAR(255);
	DECLARE v_MonedaSistema		NVARCHAR(3);
	DECLARE v_IdTrabajoParidad,v_IdTrabajoParidad2  BIGINT :=0; --oct
	
	CALL "BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo" (:p_IdEmpresa,'UsaDolarObservado',v_usa_dolar);
	
	SELECT top 1 COALESCE("Valor",'') into v_MonedaSistema from "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE  "IdEmpresa" = p_IdEmpresa AND  "CodigoParametro" = 'PLA.MonedaSistema';
	
	v_series =
	   SELECT
            "Series"."IdInterno",
            "Series"."Fecha",
            "Series"."IdSerieFondo",
            "Series"."CalculoPorMontoCuota",
            "Series"."Monto",
            "Series"."NumeroCuotas",
            "Series"."IdMonedaFondo" AS "IdMonedaFondo",
            "Series"."IdMonedaInversion" AS "IdMonedaInversion",
            "Series"."TipoSolicitud",
            "Series"."TipoFactorAjuste",
            "Series"."IdEmpresa",
            TO_DECIMAL(ROUND("Series"."NumeroCuotas" * "ValorCuota"."ValorCuota",8), 26, 8) AS "MontoPorCuota",
            "ValorCuota"."ValorCuota",
            CASE WHEN "Series"."TipoSolicitud" = 'APO' THEN COALESCE("ValorTipoCambio"."ValorDolarAporte",0) 
                 WHEN "Series"."TipoSolicitud" = 'RES' THEN COALESCE("ValorTipoCambio"."ValorDolarRescate",0) 
            END AS "TipoCambio",
            "SerieFondo"."DecimalesCuota",
            CASE WHEN "Series"."IdMonedaInversion" != "Series"."IdMonedaFondo" THEN 'S' ELSE 'N' END AS "Diferido"
        FROM :p_Series AS "Series"
        LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "ValorTipoCambio" ON ("ValorTipoCambio"."IdSerieFondo" = "Series"."IdSerieFondo"
                                                                                 AND "ValorTipoCambio"."IdEmpresa" = "Series"."IdEmpresa")
        INNER JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = "Series"."IdSerieFondo"
                                                                        AND "ValorCuota"."IdEmpresa" = "Series"."IdEmpresa"
                                                                        AND "ValorCuota"."IdMoneda" = "Series"."IdMonedaFondo")
        INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "ValorCuota"."IdSerieFondo"
                                                                          AND "SerieFondo"."IdEmpresa" = "ValorCuota"."IdEmpresa");
	
	
    SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
        "IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT
        :v_IdTrabajoParidad,
        "IdInterno",
        "Fecha",
        "IdMonedaInversion",
        "IdMonedaFondo",
        0,
        CASE WHEN "CalculoPorMontoCuota" IN ('C','T','P') THEN "MontoPorCuota" ELSE "Monto" END,
        ''
    FROM :v_series
    WHERE "Diferido" = 'S';
	
	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo	=> :v_IdTrabajoParidad);
	
	v_errores =
        SELECT
            "Series"."IdInterno",
            "Series"."TipoSolicitud",
            "TipoCambio"."CodigoMensaje"
        FROM :v_series AS "Series"
        INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS "TipoCambio" ON ("TipoCambio"."IdInterno" = "Series"."IdInterno"
                                                                                                      AND "TipoCambio"."IdTrabajo" = :v_IdTrabajoParidad)
        WHERE "TipoCambio"."Resultado" = 'RECHAZO';	
					
	v_series_ok =
        SELECT
            "Series"."IdInterno",
            "Series"."Fecha",
            "Series"."IdSerieFondo",
            "Series"."CalculoPorMontoCuota",
            "Series"."Monto",
            "Series"."NumeroCuotas",
            "Series"."IdMonedaFondo",
            "Series"."IdMonedaInversion",
            "Series"."TipoSolicitud",
            "Series"."TipoFactorAjuste",
            "Series"."IdEmpresa",
            "Series"."MontoPorCuota",
            "Series"."ValorCuota",
            "Series"."TipoCambio",
            "Series"."DecimalesCuota",
            "Series"."Diferido",
            CASE WHEN "Series"."IdMonedaFondo" = "Series"."IdMonedaInversion" THEN 1 ELSE CASE :v_usa_dolar WHEN 'N' THEN "Series"."TipoCambio" ELSE "TipoCambio"."FactorCambioLocal" END END AS "ValorParidad",
            CASE WHEN "Series"."IdMonedaFondo" = "Series"."IdMonedaInversion" THEN "Series"."Monto" 
            	 ELSE CASE WHEN "Series"."IdMonedaFondo" = 'USD' AND :v_usa_dolar = 'N' THEN "Series"."Monto" / COALESCE("Series"."TipoCambio",-1)
            	  		   WHEN "Series"."IdMonedaFondo" = :v_MonedaSistema AND "Series"."IdMonedaInversion" = 'USD'  AND :v_usa_dolar = 'N' THEN "Series"."Monto" * COALESCE("Series"."TipoCambio",-1)
            	 ELSE COALESCE("TipoCambio"."MontoDestino",0) END
            END AS "MontoDestino"	
        FROM :v_series AS "Series"
        LEFT JOIN :v_errores AS "Error" ON ("Error"."IdInterno" = "Series"."IdInterno")
        LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS "TipoCambio" ON ("TipoCambio"."IdInterno" = "Series"."IdInterno"
                                                                                                     AND "TipoCambio"."IdTrabajo" = :v_IdTrabajoParidad)
        WHERE COALESCE("Error"."IdInterno",-1) = -1 AND ("Series"."IdMonedaFondo" = "Series"."IdMonedaInversion" OR :v_usa_dolar = 'S' OR COALESCE("Series"."TipoCambio",0) > 0);

	v_calculo =
        SELECT
            "Series"."IdInterno",
            "Series"."IdSerieFondo",
            "Series"."CalculoPorMontoCuota",
            "Series"."IdMonedaFondo",
            "Series"."Fecha",
            "Series"."TipoFactorAjuste",
            "Series"."TipoSolicitud",
            CASE WHEN "Series"."CalculoPorMontoCuota" IN ('C','T','P') THEN ROUND("MontoPorCuota",COALESCE("FormatoMonedaInversion"."MontoDecimales",0)) 	
                 ELSE CASE WHEN "Series"."IdMonedaFondo" != "Series"."IdMonedaInversion" THEN TO_DECIMAL(ROUND(COALESCE("Series"."MontoDestino",0),COALESCE("FormatoMonedaInversion"."MontoDecimales",0)), 26, 8) 
                           ELSE ROUND("Series"."Monto",COALESCE("FormatoMonedaInversion"."MontoDecimales",0))
                      END
            END AS "Monto",
            CASE "Series"."CalculoPorMontoCuota" WHEN 'M' THEN 
                    CASE WHEN "Series"."IdMonedaFondo" != "Series"."IdMonedaInversion" THEN TO_DECIMAL(ROUND(ROUND(COALESCE("Series"."MontoDestino",0),8)/"Series"."ValorCuota","Series"."DecimalesCuota"), 26, 8) 
                         ELSE TO_DECIMAL(ROUND("Series"."Monto"/"Series"."ValorCuota","Series"."DecimalesCuota"), 26, 8)
                    END
                ELSE "Series"."NumeroCuotas"
            END AS "NumeroCuotas",
            "Series"."ValorCuota",
            CASE WHEN "Series"."CalculoPorMontoCuota" IN ('C','T','P') THEN 
                    CASE WHEN "Series"."IdMonedaFondo" != "Series"."IdMonedaInversion" THEN TO_DECIMAL(ROUND(COALESCE("Series"."MontoDestino",0),COALESCE("FormatoMonedaInversion"."MontoDecimales",0)), 26, 8)
                         ELSE ROUND("MontoPorCuota",COALESCE("FormatoMonedaInversion"."MontoDecimales",0))
                    END
                ELSE ROUND("Series"."Monto",COALESCE("FormatoMonedaInversion"."MontoDecimales",0))
            END AS "MontoDestino",
            "Series"."ValorParidad",
            "Series"."DecimalesCuota"
        FROM :v_series_ok AS "Series"
        LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS "FormatoMonedaFondo" ON ("FormatoMonedaFondo"."IdMoneda" = "Series"."IdMonedaFondo"
                                                                                  AND "FormatoMonedaFondo"."IdUsoMonedaFormato" = 'FMM'
                                                                                  AND "FormatoMonedaFondo"."IdEstado" = 'ACT')
        LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS "FormatoMonedaInversion" ON ("FormatoMonedaInversion"."IdMoneda" = "Series"."IdMonedaInversion"
                                                                                      AND "FormatoMonedaInversion"."IdUsoMonedaFormato" = 'FMM'
                                                                                      AND "FormatoMonedaInversion"."IdEstado" = 'ACT');
	
	v_tipo_cambio_factor =
        SELECT
            ROW_NUMBER() over (ORDER BY c."IdInterno") AS "IdProceso",
            t."TipoCambio",c."IdInterno",c."Fecha",
            CASE WHEN t."TipoCambio" = 'INDICADOR' THEN 'UF' ELSE c."IdMonedaFondo" END AS "IdMonedaOrigen",
            CASE WHEN t."TipoCambio" = 'INDICADOR' THEN 'CLP'
                 WHEN t."TipoCambio" = 'SISTEMA' THEN :v_MonedaSistema
                 ELSE c."TipoFactorAjuste"
            END AS "IdMonedaDestino",
            0 AS "ValorCambioLocalModificado",
            CASE WHEN t."TipoCambio" = 'INDICADOR' THEN 1 ELSE TO_DECIMAL(c."Monto",26,8) END AS "MontoOrigen",
            '' AS "IdUso",
            CASE WHEN t."TipoCambio" != 'SISTEMA' THEN 'S'
                 WHEN c."IdMonedaFondo" = :v_MonedaSistema THEN 'N'
                 ELSE 'S'
            END AS "EsUtilizable"
	    FROM (
            SELECT 'INDICADOR' AS "TipoCambio" FROM DUMMY UNION ALL
            SELECT 'SISTEMA' AS "TipoCambio" FROM DUMMY UNION ALL
            SELECT 'FONDO' AS "TipoCambio"FROM DUMMY) AS t,:v_calculo AS c;

    --SELECT 'v_tipo_cambio_factor',* FROM :v_tipo_cambio_factor;--borrar
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad2 FROM DUMMY;
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
		"IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso"
	)
	SELECT
	   :v_IdTrabajoParidad2,"IdProceso" AS "IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino",
	   "ValorCambioLocalModificado","MontoOrigen","IdUso"
    FROM :v_tipo_cambio_factor
    WHERE "EsUtilizable" = 'S';
    
	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo	=> :v_IdTrabajoParidad2
	);	
	
	v_tipo_cambio_factor_resultado =
	   SELECT
	       t."TipoCambio",t."IdInterno",c."FactorOrigenDestino",c."MontoDestino"
	   FROM :v_tipo_cambio_factor AS t
	   LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS c ON (c."IdInterno" = t."IdProceso"
	                                                                                     AND c."IdTrabajo" = :v_IdTrabajoParidad2);
	   
	p_out_Resultado =
        SELECT
            "Calculo"."IdInterno",
            "TipoSolicitud",
            TO_DECIMAL("Calculo"."Monto",26,8) AS "Monto",
            TO_DECIMAL(SUBSTR("Calculo"."NumeroCuotas",0,instr("Calculo"."NumeroCuotas",'.')) || SUBSTR(SUBSTR("Calculo"."NumeroCuotas",instr("Calculo"."NumeroCuotas",'.')+1,LENGTH("Calculo"."NumeroCuotas")),0,"Calculo"."DecimalesCuota"),26,8) AS "NumeroCuotas",
            "Calculo"."ValorCuota",
            TO_DECIMAL("Calculo"."MontoDestino",26,8) AS "MontoDestino",
            "Calculo"."ValorParidad",
            COALESCE("Indicador"."FactorOrigenDestino",0) AS "ValorFactorAjuste",
            COALESCE("Tipo_Cambio"."MontoDestino",0) AS "CostoEnFactorAjuste",
            :v_MonedaSistema AS "IdMonedaSistema",
            CASE WHEN "Calculo"."IdMonedaFondo" != :v_MonedaSistema THEN COALESCE("Sistema"."MontoDestino",0) ELSE "Monto" END AS "MontoMonedaSistema",
            'OK' AS "Resultado",
            'OK' AS "CodigoMensaje"
        FROM :v_calculo AS "Calculo"
        LEFT JOIN :v_tipo_cambio_factor_resultado AS "Tipo_Cambio" ON ("Tipo_Cambio"."TipoCambio" = 'FONDO'
                                                                   AND "Tipo_Cambio"."IdInterno" = "Calculo"."IdInterno")
        LEFT JOIN :v_tipo_cambio_factor_resultado AS "Indicador" ON ("Indicador"."TipoCambio" = 'INDICADOR'
                                                                 AND "Indicador"."IdInterno" = "Calculo"."IdInterno")
        LEFT JOIN :v_tipo_cambio_factor_resultado AS "Sistema" ON ("Sistema"."TipoCambio" = 'SISTEMA'
                                                               AND "Sistema"."IdInterno" = "Calculo"."IdInterno")
        UNION ALL
        SELECT
            "IdInterno","TipoSolicitud",0 AS "Monto",0 AS "NumeroCuotas",0 AS "ValorCuota",0 AS "MontoDestino",0 AS "ValorParidad",0 AS "ValorFactorAjuste",
            0 AS "CostoEnFactorAjuste",:v_MonedaSistema AS "IdMonedaSistema",0 AS "MontoMonedaSistema",'ERR' AS "Resultado","CodigoMensaje" AS "CodigoMensaje"
        FROM :v_errores;
END;
GO
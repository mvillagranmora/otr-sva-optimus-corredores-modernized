SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Contabilidad.CrystalReports.Servicios::ConsultarCarteraLeasing";
GO
CREATE PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::ConsultarCarteraLeasing" (
    IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_FechaConsulta NVARCHAR(20),
    IN p_IdCliente BIGINT,
    IN p_IdPortafolio BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
AS
BEGIN
	DECLARE varExistenDatos INT;

DECLARE v_FechaSistema NVARCHAR(10);

DECLARE lt_Paridades TABLE (
        "IdParidadYTipoCambio" BIGINT,
        "IdMoneda" NVARCHAR(3),
        "NombreMoneda" NVARCHAR(50),
        "ParidadMercado" DECIMAL(26,8),
        "ParidadBID" DECIMAL(26,8),
        "ParidadASK" DECIMAL(26,8),
        "LimiteSuperiorParidad" DECIMAL(26,8),
        "LimiteInferiorParidad" DECIMAL(26,8),
        "ParidadSBIF" DECIMAL(26,8),
        "TipoCambioMercado" DECIMAL(26,8),
        "LimiteSuperiorTipoCambio" DECIMAL(26,8),
        "LimiteInferiorTipoCambio" DECIMAL(26,8),
        "PrecioSpotCompra" DECIMAL(26,8),
        "PrecioSpotVenta" DECIMAL(26,8),
        "PrecioSpotPromedio" DECIMAL(26,8),
        "MultiplicaODivide" NVARCHAR(1),
        "UsuarioModificador" NVARCHAR(100)
    );

DECLARE v_Advertencias INT;

DECLARE v_Errores INT;

DECLARE lt_Advertencias TABLE ("IdInterno" BIGINT,
"Codigo" NVARCHAR(100),
"RequiereAutorizacion" NVARCHAR(40),
"Descripcion" NVARCHAR(520));

DECLARE lt_Errores TABLE ("IdInterno" BIGINT,
"Codigo" NVARCHAR(100),
"Descripcion" NVARCHAR(520));

DECLARE v_TipoCambio_DO DECIMAL(26,8);

DECLARE v_TipoCambio_UF DECIMAL(26,8);

DECLARE cartera_detalle TABLE (
        "TipoCambioDO" DECIMAL(26,8),
        "TipoCambioUF" DECIMAL(26,8),
        "Instrumento" NVARCHAR(100),
       "Vencimiento" NVARCHAR(10),
        "Emisor" NVARCHAR(100),
        "Cantidad" DECIMAL(26,8),
        "MontoCompra" DECIMAL(26,8),
        "TasaCompra" DECIMAL(26,8),
        "TasaMercado" DECIMAL(26,8),
        "HoraActual" NVARCHAR(10),
        "FechaActual" NVARCHAR(10),
        "FechaConsulta" NVARCHAR(20),
        "Portafolio" NVARCHAR(20),
        "CodigoPortafolio" NVARCHAR(50),
    	"Plazo" NVARCHAR(50),
    	"Rango0a90MontoCompra" DECIMAL(26,8),
        "Rango0a90ValorMercado" DECIMAL(26,8),
        "Rango0a90DiferenciaMercado" DECIMAL(26,8),
        "Rango91a180MontoCompra" DECIMAL(26,8),
        "Rango91a180ValorMercado" DECIMAL(26,8),
        "Rango91a180DiferenciaMercado" DECIMAL(26,8),
        "Rango181a360MontoCompra" DECIMAL(26,8),
        "Rango181a360ValorMercado" DECIMAL(26,8),
        "Rango181a360DiferenciaMercado" DECIMAL(26,8),
        "Rango1a3MontoCompra" DECIMAL(26,8),
        "Rango1a3ValorMercado" DECIMAL(26,8),
        "Rango1a3DiferenciaMercado" DECIMAL(26,8),
        "Rango3a7MontoCompra" DECIMAL(26,8),
        "Rango3a7ValorMercado" DECIMAL(26,8),
        "Rango3a7DiferenciaMercado" DECIMAL(26,8),
        "RangoMas7MontoCompra" DECIMAL(26,8),
        "RangoMas7ValorMercado" DECIMAL(26,8),
        "RangoMas7DiferenciaMercado" DECIMAL(26,8) ,
        "MontoMercado" DECIMAL(26,8),
        "DiferenciaMercado" DECIMAL(26,8),
        "ValorMercado" DECIMAL(26,8),
        "TotalMontoCompra" DECIMAL(26,8),
        "TotalValorMercado" DECIMAL(26,8),
        "TotalDiferenciaMercado" DECIMAL(26,8),
		"TotalMontoCompraRF" DECIMAL(26,8),
        "TotalValorMercadoRF" DECIMAL(26,8),
        "TotalDiferenciaMercadoRF" DECIMAL(26,8)
        );

DECLARE v_logo TABLE("LogoCorredora" BLOB);

DECLARE v_DatosCorredora TABLE(
        "RazonSocial" NVARCHAR(100),
        "RUT" NVARCHAR(20),
        "Direccion" NVARCHAR(200),
        "Telefono" NVARCHAR(50),
        "Email" NVARCHAR(100),
        "CodigoPortafolio" NVARCHAR(50)
    );

DECLARE v_DatosCliente TABLE(
        "Nombres" NVARCHAR(100),
        "ApellidoPaterno" NVARCHAR(100),
        "ApellidoMaterno" NVARCHAR(100),
        "DocumentoIdentidad" NVARCHAR(20)
    );

CALL BSCL."BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambio" (
        :p_IdEmpresa,
        :p_IdUsuario,
        TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),
        lt_Paridades,
        v_Advertencias,
        lt_Advertencias,
        v_Errores,
        lt_Errores
    );

IF	EXISTS (SELECT	1 FROM :lt_Paridades WHERE "IdMoneda" = 'DO')  THEN
	 	SELECT	"TipoCambioMercado" INTO	v_TipoCambio_DO
     	FROM	:lt_Paridades
     	WHERE	"IdMoneda" = 'DO';
ELSE
	v_TipoCambio_DO := 0;
END IF;

IF EXISTS (SELECT	1 FROM	:lt_Paridades WHERE	"IdMoneda" = 'UF') THEN
		SELECT	"TipoCambioMercado" INTO v_TipoCambio_UF
		FROM :lt_Paridades
		WHERE "IdMoneda" = 'UF';
ELSE
	v_TipoCambio_UF := 0;
END IF;

CALL BSCL."BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (
        p_IdEmpresa => :p_IdEmpresa,
        p_out_FechaSistema => v_FechaSistema
    );

v_logo = SELECT	TOP 1 "Binario" AS "LogoCorredora"
		 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		 WHERE	"NombreImagen" = 'LOGO_CORREDOR_LOGIN'
		 ORDER BY "Version" DESC;

v_DatosCorredora = 	SELECT TOP 1 COR1."RazonSocial",COR1."RUT",COR1."Direccion",COR1."Telefono",COR1."Email",COR3."CodigoPortafolio"
				  	FROM	"BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" COR1
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" COR2  ON	COR2."DocumentoIdentidad" = COR1."RUT"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" COR3 ON	COR3."IdPortafolio" = :p_IdPortafolio
					WHERE COR1."IdEmpresa" = :p_IdEmpresa;

v_DatosCliente =SELECT	TOP 1 PER."Nombres",PER."ApellidoPaterno",	PER."ApellidoMaterno",	DI."DocumentoIdentidad"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
				JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON	CLI."IdPersona" = PER."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON	PER."IdPersona" = DI."IdPersona"
				WHERE	CLI."IdCliente" = :p_IdCliente;

IF TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD') >= TO_DATE(v_FechaSistema, 'YYYY-MM-DD') THEN
	INSERT	INTO :cartera_detalle("TipoCambioDO","TipoCambioUF","Instrumento","Vencimiento","Emisor","Cantidad","MontoCompra","TasaCompra","TasaMercado","HoraActual","FechaActual",
		"FechaConsulta","Portafolio","CodigoPortafolio","Plazo","Rango0a90MontoCompra","Rango0a90ValorMercado","Rango0a90DiferenciaMercado","Rango91a180MontoCompra",
		"Rango91a180ValorMercado","Rango91a180DiferenciaMercado","Rango181a360MontoCompra","Rango181a360ValorMercado","Rango181a360DiferenciaMercado","Rango1a3MontoCompra",
		"Rango1a3ValorMercado","Rango1a3DiferenciaMercado","Rango3a7MontoCompra","Rango3a7ValorMercado","Rango3a7DiferenciaMercado","RangoMas7MontoCompra","RangoMas7ValorMercado",
		"RangoMas7DiferenciaMercado","MontoMercado","DiferenciaMercado","ValorMercado","TotalMontoCompra","TotalValorMercado","TotalDiferenciaMercado","TotalMontoCompraRF",
		"TotalValorMercadoRF","TotalDiferenciaMercadoRF")
	WITH Carteras AS (
	    SELECT
		:v_TipoCambio_DO AS "TipoCambioDO",
		:v_TipoCambio_UF AS "TipoCambioUF",
		CASE WHEN car."NemoSVS" IS NULL OR car."NemoSVS"='' THEN res."NemotecnicoDetalle"
				ELSE car."Nemotecnico"
		END AS "Instrumento",
		TO_VARCHAR(car."FechaVencimientoInstrumento", 'DD-MM-YYYY') AS "Vencimiento",
		CASE WHEN car."IdEmisor" IS NULL OR car."IdEmisor" ='' THEN res."IdEmisorDetalle"
	         ELSE car."IdEmisor"
	    END AS "Emisor",
		CASE WHEN   car."IdClasificacionTipoInstrumento" = 'IF'THEN RES."CantidadDetalle"
			  ELSE  car."Cantidad"
			  END AS "Cantidad",
		car."INGMontoATasaCompraActual" AS "MontoCompra",
		car."TasaCompra",
		car."INGTasaMercadoActual" AS "TasaMercado",
		TO_VARCHAR(CURRENT_TIME,'HH24:MI')AS "HoraActual",
		TO_VARCHAR(CURRENT_DATE, 'DD-MM-YYYY') AS "FechaActual",
		TO_VARCHAR(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), 'DD-MM-YYYY') AS "FechaConsulta",
		:p_IdPortafolio AS "Portafolio",
		PFOR."CodigoPortafolio" AS "CodigoPortafolio",
		CASE WHEN CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int )>0 THEN
	           TO_VARCHAR( CAST((CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int )/360)AS int))||','||
					CASE WHEN ROUND ((MOD( DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento"),360 )/30),0) >9 THEN
							TO_VARCHAR( CAST( ROUND ((MOD( DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento"),360 )/30),0)AS int))
						ELSE
							'0'|| TO_VARCHAR( CAST( ROUND ((MOD( DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento"),360 )/30),0)AS int))
					END
			ELSE '0,00'
	       END AS "Plazo",
		-- Valores para rango 0-90 días
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90 THEN car."INGMontoATasaCompraActual"
	           ELSE 0
	        END) OVER () AS "Rango0a90MontoCompra",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90  THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
	   END) OVER () AS "Rango0a90ValorMercado",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90  THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
	   END) OVER () AS "Rango0a90DiferenciaMercado",
		-- Rango 91-180 días
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180    THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "Rango91a180MontoCompra",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango91a180ValorMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango91a180DiferenciaMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360 THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "Rango181a360MontoCompra",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360 THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango181a360ValorMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango181a360DiferenciaMercado",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080   THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "Rango1a3MontoCompra",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango1a3ValorMercado",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango1a3DiferenciaMercado",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520 THEN car."INGMontoATasaCompraActual"
	    ELSE 0
	    END) OVER () AS "Rango3a7MontoCompra",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520 THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango3a7ValorMercado",
		SUM(CASE
	    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520
	    THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango3a7DiferenciaMercado",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520 THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "RangoMas7MontoCompra",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "RangoMas7ValorMercado",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "RangoMas7DiferenciaMercado",

		CASE
			WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN
			   CASE	WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate",	0)  THEN
			        ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate",0)) * car."INGMontoATasaMercadoActual")/ NULLIF(car."INGValorRescate",0)
				ELSE car."INGMontoATasaMercadoActual"
			   END
			ELSE
			   CASE	WHEN COALESCE(resTot."IngCantidad",	0) > 0	AND car."Cantidad" > 0 THEN
			         ((car."Cantidad" - COALESCE(resTot."IngCantidad",0)) * car."INGMontoATasaMercadoActual")/ NULLIF(car."Cantidad",0)
				ELSE car."INGMontoATasaMercadoActual"
			    END
		END AS "MontoMercado",
		CASE
			WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                              THEN car."INGMontoMercadoActualMonedaInstrumento"
	                                   - car."INGMontoCompraActualMonedaInstrumento"
			ELSE ( CASE
				WHEN COALESCE(car."INGMontoATasaMercadoActual",
				0)= 0
	                                 THEN COALESCE(PREMER."PrecioCierre",
				0) * car."Cantidad"
				ELSE car."INGMontoATasaMercadoActual"
			END) - car."INGMontoATasaCompraActual"
		END AS "DiferenciaMercado",
		CASE
			WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                              THEN COALESCE(PRECIO_BCS."Precio",
			PREMER."PrecioCierre",
			car."INGPrecioMercadoActual",
			0)
	                                   * car."INGCantidad"
			WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                              THEN CASE
				WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate",	0)
	                                        THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate",0))
	                                              * car."INGMontoMercadoActualMonedaInstrumento")
	                                            / NULLIF(car."INGValorRescate",	0)
				ELSE car."INGMontoMercadoActualMonedaInstrumento"
			END
			ELSE CASE
				WHEN COALESCE(resTot."IngCantidad",
				0) > 0
				AND car."INGCantidad" > 0
	                                THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad",0))
	                                      * car."INGMontoMercadoActualMonedaInstrumento")
	                                    / NULLIF(car."INGCantidad",	0)
				ELSE car."INGMontoMercadoActualMonedaInstrumento"
			END
		END AS "ValorMercado",
		SUM(car."INGMontoATasaCompraActual") OVER () AS "TotalMontoCompra",
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
						COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0)* car."INGCantidad"
	               WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
	               		CASE WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0) THEN
	               			((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0))  * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                     ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                    END
	               ELSE
	               		CASE WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0 THEN
	               				((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                    END
	               END ) OVER () AS "TotalValorMercado",
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN
					car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	              ELSE ( CASE WHEN COALESCE(car."INGMontoATasaMercadoActual", 0)= 0 THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                          ELSE car."INGMontoATasaMercadoActual"
	                     END) - car."INGMontoATasaCompraActual"
	                    END ) OVER () AS "TotalDiferenciaMercado",
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN
	           CASE WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0 THEN
	               		((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento" END
				END ) OVER () AS "TotalValorMercadoRF",
		SUM(CASE  WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN  car."INGMontoATasaCompraActual"
				END ) OVER () AS "TotalMontoCompraRF" ,
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" IN ('RF') THEN
					car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
				END ) OVER () AS "TotalDiferenciaMercadoRF"
	FROM	"BSCL"."BSCL.Custodia.BD::CUS.Cartera" car
	LEFT JOIN (	SELECT	"IdInstrumento","IngFecha",  SUM("IngCantidad") AS "IngCantidad",  SUM("IngValorRescate") AS "IngValorRescate"
		       FROM	"BSCL"."BSCL.Custodia.BD::CUS.Reserva"
		       GROUP BY	"IdInstrumento","IngFecha" ) resTot   ON car."IdInstrumento" = resTot."IdInstrumento" AND car."FechaOperacion" = resTot."IngFecha"
	LEFT JOIN (	SELECT	"IdInstrumento","PrecioCierre",	ROW_NUMBER() OVER (PARTITION BY "IdInstrumento"	ORDER BY "Fecha" DESC) AS rn
				FROM	"BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ) PREMER  ON car."IdInstrumento" = PREMER."IdInstrumento"	AND PREMER.rn = 1
	LEFT JOIN (	SELECT "IdInstrumento",	"Precio",		ROW_NUMBER() OVER (PARTITION BY "IdInstrumento"	ORDER BY "FechaSistema" DESC) AS rn
				FROM "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento"  ) PRECIO_BCS ON	car."IdInstrumento" = PRECIO_BCS."IdInstrumento" AND PRECIO_BCS.rn = 1
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PFOR ON	PFOR."IdPortafolio" = car."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" RES ON RES."IdOperacion" = car."IdOperacion"
	WHERE	car."IdCliente" = :p_IdCliente
	AND car."IdPortafolio" = :p_IdPortafolio
	AND car."IdEstado" = 'VIG'
	AND car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	)
	SELECT "TipoCambioDO","TipoCambioUF","Instrumento","Vencimiento","Emisor",SUM("Cantidad") AS "Cantidad",SUM("MontoCompra") AS "MontoCompra","TasaCompra",
		"TasaMercado","HoraActual","FechaActual","FechaConsulta","Portafolio","CodigoPortafolio","Plazo","Rango0a90MontoCompra","Rango0a90ValorMercado","Rango0a90DiferenciaMercado",
		"Rango91a180MontoCompra","Rango91a180ValorMercado","Rango91a180DiferenciaMercado","Rango181a360MontoCompra","Rango181a360ValorMercado","Rango181a360DiferenciaMercado",
		"Rango1a3MontoCompra","Rango1a3ValorMercado","Rango1a3DiferenciaMercado","Rango3a7MontoCompra","Rango3a7ValorMercado","Rango3a7DiferenciaMercado","RangoMas7MontoCompra",
		"RangoMas7ValorMercado","RangoMas7DiferenciaMercado",SUM("MontoMercado") AS "MontoMercado",SUM("DiferenciaMercado") AS "DiferenciaMercado",SUM("ValorMercado") AS "ValorMercado",
		"TotalMontoCompra","TotalValorMercado","TotalDiferenciaMercado","TotalMontoCompraRF","TotalValorMercadoRF","TotalDiferenciaMercadoRF"
	FROM Carteras
	GROUP BY
		"TipoCambioDO",	"TipoCambioUF",	"Instrumento",	"Vencimiento",	"Emisor",	"TasaCompra",	"TasaMercado",	"HoraActual",
		"FechaActual",	"FechaConsulta",	"Portafolio",	"CodigoPortafolio",	"Plazo",	"Rango0a90MontoCompra",	"Rango0a90ValorMercado",
		"Rango0a90DiferenciaMercado",	"Rango91a180MontoCompra",	"Rango91a180ValorMercado",	"Rango91a180DiferenciaMercado",
		"Rango181a360MontoCompra",	"Rango181a360ValorMercado",	"Rango181a360DiferenciaMercado",	"Rango1a3MontoCompra",	"Rango1a3ValorMercado",
		"Rango1a3DiferenciaMercado",	"Rango3a7MontoCompra",	"Rango3a7ValorMercado",	"Rango3a7DiferenciaMercado",	"RangoMas7MontoCompra",
		"RangoMas7ValorMercado",	"RangoMas7DiferenciaMercado",	"TotalMontoCompra",	"TotalValorMercado",	"TotalDiferenciaMercado",	"TotalMontoCompraRF",
		"TotalValorMercadoRF",	"TotalDiferenciaMercadoRF";
ELSE

	INSERT INTO :cartera_detalle ("TipoCambioDO","TipoCambioUF","Instrumento","Vencimiento","Emisor","Cantidad","MontoCompra","TasaCompra","TasaMercado","HoraActual","FechaActual",
		"FechaConsulta","Portafolio","CodigoPortafolio","Plazo","Rango0a90MontoCompra","Rango0a90ValorMercado","Rango0a90DiferenciaMercado","Rango91a180MontoCompra",
		"Rango91a180ValorMercado","Rango91a180DiferenciaMercado","Rango181a360MontoCompra","Rango181a360ValorMercado","Rango181a360DiferenciaMercado","Rango1a3MontoCompra",
		"Rango1a3ValorMercado","Rango1a3DiferenciaMercado","Rango3a7MontoCompra","Rango3a7ValorMercado","Rango3a7DiferenciaMercado","RangoMas7MontoCompra","RangoMas7ValorMercado",
		"RangoMas7DiferenciaMercado","MontoMercado","DiferenciaMercado","ValorMercado","TotalMontoCompra","TotalValorMercado","TotalDiferenciaMercado","TotalMontoCompraRF",
		"TotalValorMercadoRF","TotalDiferenciaMercadoRF")
	WITH CarterasHistoricas AS (
		SELECT
		:v_TipoCambio_DO AS "TipoCambioDO",
		:v_TipoCambio_UF AS "TipoCambioUF",
		CASE WHEN car."NemoSVS" IS NULL OR car."NemoSVS"='' THEN res."NemotecnicoDetalle"
				ELSE car."Nemotecnico"
		END AS "Instrumento",
		TO_VARCHAR(car."FechaVencimientoInstrumento", 'DD-MM-YYYY') AS "Vencimiento",
		CASE WHEN car."IdEmisor" IS NULL OR car."IdEmisor" ='' THEN res."IdEmisorDetalle"
	         ELSE car."IdEmisor"
	    END AS "Emisor",
		CASE WHEN   car."IdClasificacionTipoInstrumento" = 'IF'THEN RES."CantidadDetalle"
			  ELSE  car."Cantidad"
			  END AS "Cantidad",
		car."INGMontoATasaCompraActual" AS "MontoCompra",
		car."TasaCompra",
		car."INGTasaMercadoActual" AS "TasaMercado",
	    TO_VARCHAR(CURRENT_TIME,'HH24:MI')AS "HoraActual",
		TO_VARCHAR(CURRENT_DATE, 'DD-MM-YYYY') AS "FechaActual",
		TO_VARCHAR(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), 'DD-MM-YYYY') AS "FechaConsulta",
		:p_IdPortafolio AS "Portafolio",
		PFOR."CodigoPortafolio" AS "CodigoPortafolio",
		CASE WHEN CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int )>0 THEN
	    			TO_VARCHAR( CAST((CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int )/360)AS int))||','||
					TO_VARCHAR( FLOOR((TO_DECIMAL( MOD( CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int),360)/30 ) -
					FLOOR(TO_DECIMAL( MOD( CAST (DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'),car."FechaVencimientoInstrumento")AS int),360)/30 ))) * 100),'00')
			 ELSE '0,00'
	       END AS "Plazo",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90   THEN car."INGMontoATasaCompraActual"
	    ELSE 0
	    END) OVER () AS "Rango0a90MontoCompra",
		SUM(CASE WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
	    END) OVER () AS "Rango0a90ValorMercado",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 0 AND 90 THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
	    END) OVER () AS "Rango0a90DiferenciaMercado",
		-- Rango 91-180 días
		SUM(CASE WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180 THEN car."INGMontoATasaCompraActual"
	    ELSE 0
	    END) OVER () AS "Rango91a180MontoCompra",
		SUM(CASE WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180 THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	                WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0) THEN
	                        ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango91a180ValorMercado",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 91 AND 180     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango91a180DiferenciaMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360     THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "Rango181a360MontoCompra",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
	    END) OVER () AS "Rango181a360ValorMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 181 AND 360   THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
	     END) OVER () AS "Rango181a360DiferenciaMercado",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080
	    THEN car."INGMontoATasaCompraActual"
	    ELSE 0
	    END) OVER () AS "Rango1a3MontoCompra",
		SUM(CASE   WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
	    END) OVER () AS "Rango1a3ValorMercado",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 361 AND 1080    THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
		END) OVER () AS "Rango1a3DiferenciaMercado",
		SUM(CASE    WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520
	    THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "Rango3a7MontoCompra",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520     THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "Rango3a7ValorMercado",
		SUM(CASE  WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") BETWEEN 1081 AND 2520
	    THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
	    END) OVER () AS "Rango3a7DiferenciaMercado",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520
	    THEN car."INGMontoATasaCompraActual"
	    ELSE 0
		END) OVER () AS "RangoMas7MontoCompra",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520
	    THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" = 'RV'
	                THEN COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	            WHEN car."IdClasificacionTipoInstrumento" = 'IF'
	                THEN CASE
	                        WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0)
	                            THEN ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                        ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                     END
	            ELSE CASE
	                    WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0
	                        THEN ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                 END
	        END
	    ELSE 0
		END) OVER () AS "RangoMas7ValorMercado",
		SUM(CASE     WHEN DAYS_BETWEEN(TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD'), car."FechaVencimientoInstrumento") > 2520
	    THEN
	        CASE
	            WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	                THEN car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	            ELSE
	                (CASE
	                    WHEN COALESCE(car."INGMontoATasaMercadoActual", 0) = 0
	                        THEN COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad"
	                    ELSE car."INGMontoATasaMercadoActual"
	                 END) - car."INGMontoATasaCompraActual"
	        END
	    ELSE 0
	END) OVER () AS "RangoMas7DiferenciaMercado",
		CASE
			WHEN car."IdClasificacionTipoInstrumento" = 'IF'	AND car."EsFungible" = 'S' THEN
	           CASE	WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate",	0) THEN
	                   ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate",0)) * car."INGMontoATasaMercadoActual") / NULLIF(car."INGValorRescate",0)
				ELSE
	               car."INGMontoATasaMercadoActual"
				END
			ELSE
	           CASE	WHEN COALESCE(resTot."IngCantidad",	0) > 0	AND car."Cantidad" > 0 THEN
	                                ((car."Cantidad" - COALESCE(resTot."IngCantidad",0)) * car."INGMontoATasaMercadoActual") / NULLIF(car."Cantidad",0)
				ELSE
	                                car."INGMontoATasaMercadoActual"
			END
		END AS "MontoMercado",
		CASE
			WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN
	               car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
			ELSE
	           (CASE WHEN COALESCE(car."INGMontoATasaMercadoActual",0) = 0 THEN
	                   COALESCE(PREMER."PrecioCierre",0) * car."Cantidad"
				ELSE
	               car."INGMontoATasaMercadoActual"
			END) - car."INGMontoATasaCompraActual"
		END AS "DiferenciaMercado",
		CASE
			WHEN car."IdClasificacionTipoInstrumento" = 'RV' THEN
	             COALESCE(PRECIO_BCS."Precio",	PREMER."PrecioCierre",	car."INGPrecioMercadoActual",	0) * car."INGCantidad"
			WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN
	             CASE	WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate",	0) THEN
	                          ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate",	0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate",	0)
				ELSE
	               car."INGMontoMercadoActualMonedaInstrumento"
				END
			ELSE
	            CASE WHEN COALESCE(resTot."IngCantidad",0) > 0	AND car."INGCantidad" > 0 THEN
	                                ((car."INGCantidad" - COALESCE(resTot."IngCantidad",0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad",	0)
				ELSE
	               car."INGMontoMercadoActualMonedaInstrumento"
			END
		END AS "ValorMercado",
		SUM(car."INGMontoATasaCompraActual") OVER () AS "TotalMontoCompra",
		SUM(CASE WHEN car."IdClasificacionTipoInstrumento" = 'RV'  THEN
		           COALESCE(PRECIO_BCS."Precio", PREMER."PrecioCierre", car."INGPrecioMercadoActual", 0) * car."INGCantidad"
	             WHEN car."IdClasificacionTipoInstrumento" = 'IF'  THEN
	                CASE  WHEN car."INGValorRescate" > COALESCE(resTot."IngValorRescate", 0) THEN
	                    ((car."INGValorRescate" - COALESCE(resTot."IngValorRescate", 0))  * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGValorRescate", 0)
	                ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                END
	              ELSE CASE
	                WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0  THEN
	                              ((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento")/ NULLIF(car."INGCantidad", 0)
	                ELSE car."INGMontoMercadoActualMonedaInstrumento"
	                END
	             END  ) OVER () AS "TotalValorMercado",
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN
				car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
	          ELSE ( CASE WHEN COALESCE(car."INGMontoATasaMercadoActual", 0)= 0  THEN
	          COALESCE(PREMER."PrecioCierre", 0) * car."Cantidad" ELSE car."INGMontoATasaMercadoActual"  END) - car."INGMontoATasaCompraActual"
	          END  ) OVER () AS "TotalDiferenciaMercado",
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN
	           CASE WHEN COALESCE(resTot."IngCantidad", 0) > 0 AND car."INGCantidad" > 0 THEN
	               		((car."INGCantidad" - COALESCE(resTot."IngCantidad", 0)) * car."INGMontoMercadoActualMonedaInstrumento") / NULLIF(car."INGCantidad", 0)
	                    ELSE car."INGMontoMercadoActualMonedaInstrumento" END
				END ) OVER () AS "TotalValorMercadoRF",
		SUM(CASE  WHEN car."IdClasificacionTipoInstrumento" = 'RF' THEN  car."INGMontoATasaCompraActual"
			END ) OVER () AS "TotalMontoCompraRF" ,
		SUM( CASE  WHEN car."IdClasificacionTipoInstrumento" IN ('RF') THEN
					car."INGMontoMercadoActualMonedaInstrumento" - car."INGMontoCompraActualMonedaInstrumento"
			END ) OVER () AS "TotalDiferenciaMercadoRF"
	FROM "BSCL"."BSCL.Custodia.BD::HIS.Cartera" AS car
	LEFT JOIN (	SELECT	"IdInstrumento",	"IngFecha",		SUM("IngCantidad") AS "IngCantidad",	SUM("IngValorRescate") AS "IngValorRescate"
		 		FROM "BSCL"."BSCL.Custodia.BD::CUS.Reserva"
				GROUP BY"IdInstrumento","IngFecha" ) AS resTot ON car."IdInstrumento" = resTot."IdInstrumento"	AND car."FechaOperacion" = resTot."IngFecha"
	LEFT JOIN (	SELECT	"IdInstrumento","PrecioCierre",	ROW_NUMBER() OVER (PARTITION BY "IdInstrumento"ORDER BY	"Fecha" DESC) AS rn
				FROM"BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" ) AS PREMER ON	car."IdInstrumento" = PREMER."IdInstrumento"	AND PREMER.rn = 1
	LEFT JOIN (	SELECT	"IdInstrumento",	"Precio",	ROW_NUMBER() OVER (PARTITION BY "IdInstrumento"	ORDER BY	"FechaSistema" DESC) AS rn
				FROM"BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" ) AS PRECIO_BCS ON	car."IdInstrumento" = PRECIO_BCS."IdInstrumento"	AND PRECIO_BCS.rn = 1
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PFOR  ON PFOR."IdPortafolio" = car."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" RES ON RES."IdOperacion" = car."IdOperacion"
	WHERE	car."IdCliente" = :p_IdCliente
	AND car."IdPortafolio" = :p_IdPortafolio
	AND car."FechaRespaldo" = TO_DATE(:p_FechaConsulta, 'YYYY-MM-DD')
	AND car."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	)
	SELECT "TipoCambioDO","TipoCambioUF","Instrumento","Vencimiento","Emisor",SUM("Cantidad") AS "Cantidad",SUM("MontoCompra") AS "MontoCompra","TasaCompra",
		"TasaMercado","HoraActual","FechaActual","FechaConsulta","Portafolio","CodigoPortafolio","Plazo","Rango0a90MontoCompra","Rango0a90ValorMercado","Rango0a90DiferenciaMercado",
		"Rango91a180MontoCompra","Rango91a180ValorMercado","Rango91a180DiferenciaMercado","Rango181a360MontoCompra","Rango181a360ValorMercado","Rango181a360DiferenciaMercado",
		"Rango1a3MontoCompra","Rango1a3ValorMercado","Rango1a3DiferenciaMercado","Rango3a7MontoCompra","Rango3a7ValorMercado","Rango3a7DiferenciaMercado","RangoMas7MontoCompra",
		"RangoMas7ValorMercado","RangoMas7DiferenciaMercado",SUM("MontoMercado") AS "MontoMercado",SUM("DiferenciaMercado") AS "DiferenciaMercado",SUM("ValorMercado") AS "ValorMercado",
		"TotalMontoCompra","TotalValorMercado","TotalDiferenciaMercado","TotalMontoCompraRF","TotalValorMercadoRF","TotalDiferenciaMercadoRF"
	FROM CarterasHistoricas
	GROUP BY
		"TipoCambioDO",	"TipoCambioUF",	"Instrumento",	"Vencimiento",	"Emisor",	"TasaCompra",	"TasaMercado",	"HoraActual",
		"FechaActual",	"FechaConsulta",	"Portafolio",	"CodigoPortafolio",	"Plazo",	"Rango0a90MontoCompra",	"Rango0a90ValorMercado",
		"Rango0a90DiferenciaMercado",	"Rango91a180MontoCompra",	"Rango91a180ValorMercado",	"Rango91a180DiferenciaMercado",
		"Rango181a360MontoCompra",	"Rango181a360ValorMercado",	"Rango181a360DiferenciaMercado",	"Rango1a3MontoCompra",	"Rango1a3ValorMercado",
		"Rango1a3DiferenciaMercado",	"Rango3a7MontoCompra",	"Rango3a7ValorMercado",	"Rango3a7DiferenciaMercado",	"RangoMas7MontoCompra",
		"RangoMas7ValorMercado",	"RangoMas7DiferenciaMercado",	"TotalMontoCompra",	"TotalValorMercado",	"TotalDiferenciaMercado",	"TotalMontoCompraRF",
		"TotalValorMercadoRF",	"TotalDiferenciaMercadoRF";
END IF;
--------------------------------------
SELECT	COUNT(1)INTO varExistenDatos
FROM :cartera_detalle;
-- Verifica que existan datos
IF :varExistenDatos = 0 THEN
	v_Dummy =
    SELECT
	CORD."RazonSocial",
	CORD."RUT",
	CORD."Direccion",
	CORD."Telefono",
	CORD."Email",
	CORD."CodigoPortafolio" AS "PortCorredora",
	LOGO."LogoCorredora",
	0 AS "TipoCambioDO",
	0 AS "TipoCambioUF",
	'' AS "Instrumento",
	'' AS "Vencimiento",
	'' AS "Emisor",
	0 AS "Cantidad",
	0 AS "MontoCompra",
	0 AS "TasaCompra",
	0 AS "TasaMercado",
	TO_VARCHAR(CURRENT_TIME,'HH24:MI')AS "HoraActual",
	CURRENT_DATE AS "FechaActual",
	:p_FechaConsulta AS "FechaConsulta",
	CAST(:p_IdPortafolio AS NVARCHAR(20)) AS "Portafolio",
	CLI."Nombres",
	CLI."ApellidoPaterno",
	CLI."ApellidoMaterno",
	CLI."DocumentoIdentidad",
	CORD."CodigoPortafolio" AS "CodigoPortafolio",
	'0' AS "Plazo",
	0 AS "Rango0a90MontoCompra",
	0 AS "Rango0a90ValorMercado",
	0 AS "Rango0a90DiferenciaMercado",
	0 AS "Rango91a180MontoCompra",
	0 AS "Rango91a180ValorMercado",
	0 AS "Rango91a180DiferenciaMercado",
	0 AS "Rango181a360MontoCompra",
	0 AS "Rango181a360ValorMercado",
	0 AS "Rango181a360DiferenciaMercado",
	0 AS "Rango1a3MontoCompra",
	0 AS "Rango1a3ValorMercado",
	0 AS "Rango1a3DiferenciaMercado",
	0 AS "Rango3a7MontoCompra",
	0 AS "Rango3a7ValorMercado",
	0 AS "Rango3a7DiferenciaMercado",
	0 AS "RangoMas7MontoCompra",
	0 AS "RangoMas7ValorMercado",
	0 AS "RangoMas7DiferenciaMercado",
	0 AS "MontoMercado",
	0 AS "DiferenciaMercado",
	0 AS "ValorMercado",
	0 AS "TotalMontoCompra",
	0 AS "TotalValorMercado",
	0 AS "TotalDiferenciaMercado",
	0 AS "TotalMontoCompraRF",
	0 AS "TotalValorMercadoRF",
	0 AS "TotalDiferenciaMercadoRF"
FROM	DUMMY
CROSS JOIN :v_DatosCorredora AS CORD
CROSS JOIN :v_logo AS LOGO
CROSS JOIN :v_DatosCliente AS CLI;

END IF;
-- Salida del SP
IF :varExistenDatos > 0 THEN
    SELECT	CORD."RazonSocial",
	    CORD."RUT",
	    CORD."Direccion",
	    CORD."Telefono",
	    CORD."Email",
	    CORD."CodigoPortafolio" AS "PortCorredora",
	    LOGO."LogoCorredora",
	    CLI."Nombres",
		CLI."ApellidoPaterno",
		CLI."ApellidoMaterno",
		CLI."DocumentoIdentidad",
		CAR_DET.*
    FROM :cartera_detalle CAR_DET
   	CROSS JOIN :v_DatosCorredora AS CORD
	CROSS JOIN :v_logo AS LOGO
	CROSS JOIN :v_DatosCliente AS CLI;
ELSE
    SELECT	* FROM	:v_Dummy;
END IF;

END;
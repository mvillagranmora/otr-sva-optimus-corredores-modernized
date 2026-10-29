SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::InformeVencimientoAnticiposPactos";
GO
CREATE PROCEDURE "BSCL.Contabilidad.CrystalReports.Servicios::InformeVencimientoAnticiposPactos" ( 
    --VARIABLES OBLIGATORIAS
   	IN p_IdEmpresa							BIGINT	
    ,IN p_FechaConsulta					    DATE
    ,IN p_FechaSistema					    DATE
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_logo TABLE("LogoCorredora" BLOB);
    DECLARE v_count           INT;
	DECLARE v_FechaSistemaUTC    NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL       NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaSistema       SECONDDATE;
	DECLARE v_FechaHoraUTC     NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL     NVARCHAR(40);
   
  	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> 1,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaSistema,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
    v_logo = Select TOP 1 "Binario" AS "LogoCorredora"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' 
			ORDER BY "Version" Desc;    
	v_DatosCorredora =
		SELECT cord."RazonSocial" ,cord."RUT" 
		FROM  "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" cord 
	    WHERE cord."IdEmpresa" = :p_IdEmpresa;	
    IF :p_FechaSistema > :p_FechaConsulta THEN
        v_VencimientosAnticiposPactos = 
        WITH OperacionesFiltradas AS (
            SELECT *
            FROM "BSCL.Mesa.BD::MES.Operacion"
            WHERE "IdTipoProducto" = 'MERF'
            AND "IdTipoOperacion" IN ('COMPAC','VENPAC')
            AND "IdEstado" IN ('VIG', 'ANT', 'LIQ')AND (("IdEstado" = 'ANT' AND "FechaCondicionLiquidacionAnticipo" = :p_FechaConsulta) OR ("IdEstado" <> 'ANT' AND "FechaVencimiento" = :p_FechaConsulta))
        ),
        SumatoriaDetalle AS (
            SELECT res."IdOperacion", SUM(res."MontoLiquidacionOperacion") AS "MontoTotalLiquidacion"
            FROM "BSCL.Custodia.BD::CUS.Reserva" res
            INNER JOIN OperacionesFiltradas ope ON res."IdOperacion" = ope."IdOperacion"
            GROUP BY res."IdOperacion" 
        )
        SELECT 
        TIPOPE."Nombre" AS "Operacion",
        CASE WHEN OPE."IdEstado" = 'ANT' AND OPE."FechaCondicionLiquidacionAnticipo" = :p_FechaConsulta THEN 'ANTICIPO' ELSE 'VENCIMIENTO' END AS "Tipo",
        OPE."FolioOperacion" AS "Folio",
        CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
        POR."CodigoPortafolio" AS "Portafolio",
        CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "Nombre",
        RES."INGMontoAPlazo" AS "Monto Final",
        ROUND(RES."MontoLiquidacionOperacion",0) AS "Valor Titulo Anticipo",
        ROUND(RES."MontoLiquidacionOperacion",0) AS "Valor Titulo Vencimiento",
        ROUND(OPE."MontoOperacionFinalAnticipo" * (RES."MontoLiquidacionOperacion" / SUMDET."MontoTotalLiquidacion"), 0) AS "Valor Pacto",
        COALESCE(CARINS."INGMontoCompraActualMonedaInstrumento", CARINSPRE."INGMontoCompraActualMonedaInstrumento") AS "Valor Presente",
        RES."IngValorRescate" AS "Cantidad",
        COALESCE(NULLIF(CARORI."NemoSVS", ''), RES."Nemotecnico") AS "Nemo",
        RES."IdEmisor" AS "Emisor",
        RES."IdTipoReajuste" AS "Tipo Reajuste",
        OPE."FechaVencimiento" AS "Fecha Vencimiento",
        :p_FechaConsulta AS "Fecha Liquidacion",
        RES."Tasa" AS "Tasa TIR",
        TIPINS."Nombre" AS "Tipo Instrumento",
        CASE WHEN TEMI."Nombre" IS NOT NULL THEN  REPLACE(TEMI."Nombre", '(Centralizacion) ', '')
        ELSE  'Otras Empresas'
        END AS "Familia",
        COR."RazonSocial",
        COR."RUT",
        LOGO."LogoCorredora" AS "LogoCorredora",
        TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY') AS "FechaSistema",
        TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI') AS "HoraImpresion"
        FROM OperacionesFiltradas AS OPE
        INNER JOIN SumatoriaDetalle AS SUMDET 
            ON OPE."IdOperacion" = SUMDET."IdOperacion" 
        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI 
            ON OPE."IdCliente" = CLI."IdCliente"
        INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR 
            ON POR."IdPortafolio" = OPE."IdPortafolio"
        INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOPE 
            ON OPE."IdTipoProducto" = TIPOPE."IdTipoProducto" 
            AND OPE."IdTipoOperacion" = TIPOPE."IdTipoOperacion"
        LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES 
            ON OPE."IdOperacion" = RES."IdOperacion"
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPINS
            ON  TIPINS."IdTipoInstrumento" = RES."IdTipoInstrumento"
        LEFT JOIN (
                SELECT "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico" 
                FROM "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" 
                GROUP BY "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico"
            ) AS TIPEMI 
		  	ON TIPEMI."IdTipoInstrumento" = RES."IdTipoInstrumento"
		   	AND TIPEMI."Nemotecnico" = CASE WHEN RES."IdClasificacionTipoInstrumento"  = 'IF' AND RES."Nemotecnico" IS NOT NULL
		    THEN RES."Nemotecnico" ELSE '' END
		LEFT JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" AS TEMI 
            ON TEMI."Codigo" = TIPEMI."CodigoTipoEmisor"
        LEFT JOIN (
                SELECT "Nemotecnico", "NemoSVS", "IdClasificacionTipoInstrumento", "IdCartera", "FolioCartera", "FechaRespaldo",
                ROW_NUMBER() OVER (PARTITION BY "IdCartera" ORDER BY "FechaRespaldo" DESC) AS rn
                FROM "BSCL.Custodia.BD::HIS.Cartera" WHERE "FechaRespaldo" <= :p_FechaConsulta 
            ) AS CARORI 
            ON CARORI."IdCartera" = RES."IdCarteraOrigen" 
            AND CARORI.rn = 1
        LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CARCUS 
            ON RES."IdCarteraCustodia" = CARCUS."IdCartera" 
            AND CARCUS."FechaRespaldo" = CARORI."FechaRespaldo" 
        LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento" AS CARINS
            ON CARINS."Estado" = 'DISPONIBLE'
            AND :p_FechaConsulta >= CARINS."FechaLiquidacion"
            AND CARORI."NemoSVS" = CARINS."NemoSVS"
            AND CARINS."INGCantidad" = CASE WHEN CARORI."IdClasificacionTipoInstrumento" = 'IF' THEN RES."IngValorRescate" ELSE RES."IngCantidad" END
            AND CARINS."IdCliente" = OPE."IdClienteCorredor"
            AND CARINS."Tasa" = RES."Tasa"
        LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARPRE
            ON RES."IdCarteraOrigen" = CARPRE."IdCartera"
        LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento" AS CARINSPRE
            ON CARINSPRE."Estado" = 'DISPONIBLE'
            AND :p_FechaConsulta >= CARINSPRE."FechaLiquidacion"
            AND CARPRE."NemoSVS" = CARINSPRE."NemoSVS"
            AND CARINSPRE."INGCantidad" = CASE WHEN CARPRE."IdClasificacionTipoInstrumento" = 'IF' THEN RES."IngValorRescate" ELSE RES."IngCantidad" END
            AND CARINSPRE."IdCliente" = OPE."IdClienteCorredor"
            AND CARINSPRE."Tasa" = RES."Tasa"
        CROSS JOIN :v_logo As LOGO
        CROSS JOIN :v_DatosCorredora AS COR
        ORDER BY  OPE."FolioOperacion";
    ELSE
        v_VencimientosAnticiposPactos = 
        WITH OperacionesFiltradas AS (
        SELECT *
        FROM "BSCL.Mesa.BD::MES.Operacion"
        WHERE "IdTipoProducto" = 'MERF'
            AND "IdTipoOperacion" IN ('COMPAC','VENPAC')
            AND "IdEstado" IN ('VIG', 'ANT', 'LIQ')
            AND (("IdEstado" = 'ANT' AND "FechaCondicionLiquidacionAnticipo" = :p_FechaConsulta) OR ("IdEstado" <> 'ANT' AND "FechaVencimiento" = :p_FechaConsulta))
        ),
        SumatoriaDetalle AS (
            SELECT res."IdOperacion", SUM(res."MontoLiquidacionOperacion") AS "MontoTotalLiquidacion"
            FROM "BSCL.Custodia.BD::CUS.Reserva" res
            INNER JOIN OperacionesFiltradas ope ON res."IdOperacion" = ope."IdOperacion"
            GROUP BY res."IdOperacion" 
        )
        SELECT
        TIPOPE."Nombre" AS "Operacion",
        CASE WHEN OPE."IdEstado" = 'ANT' AND OPE."FechaCondicionLiquidacionAnticipo" = :p_FechaConsulta THEN 'ANTICIPO' ELSE 'VENCIMIENTO' END AS "Tipo",
        OPE."FolioOperacion" AS "Folio",
        CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
        POR."CodigoPortafolio" AS "Portafolio",
        CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "Nombre",
        RES."INGMontoAPlazo" AS "Monto Final",
        ROUND(RES."MontoLiquidacionOperacion",0) AS "Valor Titulo Anticipo",
        ROUND(RES."MontoLiquidacionOperacion",0) AS "Valor Titulo Vencimiento",
        ROUND(OPE."MontoOperacionFinalAnticipo" * (RES."MontoLiquidacionOperacion" / SUMDET."MontoTotalLiquidacion"), 0) AS "Valor Pacto",
        CARINS."INGMontoCompraActualMonedaInstrumento" AS "Valor Presente",
        RES."IngValorRescate" AS "Cantidad",
        COALESCE(NULLIF(CARORI."NemoSVS", ''), RES."Nemotecnico") AS "Nemo",
        RES."IdEmisor" AS "Emisor",
        RES."IdTipoReajuste" AS "Tipo Reajuste",
        OPE."FechaVencimiento" AS "Fecha Vencimiento",
        :p_FechaConsulta AS "Fecha Liquidacion",
        RES."Tasa" AS "Tasa TIR",
        TIPINS."Nombre" AS "Tipo Instrumento",
        CASE WHEN TEMI."Nombre" IS NOT NULL THEN  REPLACE(TEMI."Nombre", '(Centralizacion) ', '')
        ELSE 'Otras Empresas'
        END AS "Familia",
        COR."RazonSocial",
        COR."RUT",
        LOGO."LogoCorredora" AS "LogoCorredora",
        TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY') AS "FechaSistema",
        TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI') AS "HoraImpresion"
        FROM OperacionesFiltradas AS OPE
        INNER JOIN SumatoriaDetalle AS SUMDET 
            ON OPE."IdOperacion" = SUMDET."IdOperacion" 
        INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI 
            ON OPE."IdCliente" = CLI."IdCliente"
        INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR 
            ON POR."IdPortafolio" = OPE."IdPortafolio"
        INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOPE 
            ON OPE."IdTipoProducto" = TIPOPE."IdTipoProducto" 
            AND OPE."IdTipoOperacion" = TIPOPE."IdTipoOperacion"
        LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES 
            ON OPE."IdOperacion" = RES."IdOperacion"
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIPINS 
            ON  TIPINS."IdTipoInstrumento" = RES."IdTipoInstrumento"
        LEFT JOIN (
                SELECT "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico" 
                FROM "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" 
                GROUP BY "IdTipoInstrumento","CodigoTipoEmisor","Nemotecnico"
            ) AS TIPEMI 
		  	ON TIPEMI."IdTipoInstrumento" = RES."IdTipoInstrumento"
		   	AND TIPEMI."Nemotecnico" = CASE  WHEN RES."IdClasificacionTipoInstrumento"  = 'IF' AND RES."Nemotecnico" IS NOT NULL
		    THEN RES."Nemotecnico" ELSE '' END
        LEFT JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" AS TEMI 
            ON TEMI."Codigo" = TIPEMI."CodigoTipoEmisor"
        LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARORI 
            ON RES."IdCarteraOrigen" = CARORI."IdCartera"
        LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARCUS 
            ON RES."IdCarteraCustodia" = CARCUS."IdCartera"
		LEFT JOIN "BSCL.Mesa.BD.Vistas::MRF.ConsultarCarteraInstrumento" AS CARINS 
            ON CARINS."Estado" = 'DISPONIBLE'
            AND  :p_FechaConsulta >= CARINS."FechaLiquidacion"
            AND CARORI."NemoSVS" = CARINS."NemoSVS"
            AND CARINS."INGCantidad" = CASE WHEN CARORI."IdClasificacionTipoInstrumento" = 'IF' THEN RES."IngValorRescate" ELSE RES."IngCantidad" END AND CARINS."IdCliente" = OPE."IdClienteCorredor"
            AND CARINS."Tasa" = RES."Tasa"
        CROSS JOIN :v_logo As LOGO
        CROSS JOIN :v_DatosCorredora AS COR
        ORDER BY  OPE."FolioOperacion";
    END IF;
    SELECT COUNT(*) INTO v_count FROM :v_VencimientosAnticiposPactos;
    --Salida del SP
    IF (v_count > 0) THEN
    SELECT
        COALESCE("Operacion", '')                  AS "Operacion",
        COALESCE("Tipo", '')                       AS "Tipo",
        COALESCE("Folio", 0)                       AS "Folio",
        COALESCE("DocumentoIdentidad", '')         AS "DocumentoIdentidad",
        COALESCE("Portafolio", 0)                  AS "Portafolio",
        COALESCE("Nombre", '')                     AS "Nombre",
        COALESCE("Monto Final", 0)                 AS "Monto Final",
        COALESCE("Valor Titulo Anticipo", 0)       AS "Valor Titulo Anticipo",
        COALESCE("Valor Titulo Vencimiento", 0)    AS "Valor Titulo Vencimiento",
        COALESCE("Valor Pacto", 0)                 AS "Valor Pacto",
        COALESCE("Valor Presente", 0)              AS "Valor Presente",
        COALESCE("Cantidad", 0)                    AS "Cantidad",
        COALESCE("Nemo", '')                       AS "Nemo",
        COALESCE("Emisor", '')                     AS "Emisor",
        COALESCE("Tipo Reajuste", '')              AS "Tipo Reajuste",
        COALESCE(TO_VARCHAR(CAST("Fecha Vencimiento" AS DATE), 'DD-MM-YYYY'), '01-01-01') AS "Fecha Vencimiento",
        COALESCE(TO_VARCHAR(CAST("Fecha Liquidacion" AS DATE), 'DD-MM-YYYY'), '01-01-01') AS "Fecha Liquidacion",
        COALESCE("Tasa TIR", 0)                    AS "Tasa TIR",
        COALESCE("Tipo Instrumento", '')           AS "Tipo Instrumento",
        COALESCE("Familia", '')                    AS "Familia",
        COALESCE("RazonSocial", '')                AS "RazonSocial",
        COALESCE("RUT", '')                        AS "RUT",
        COALESCE("LogoCorredora", '')              AS "LogoCorredora",
        COALESCE("FechaSistema", '01-01-01')       AS "FechaSistema",
        COALESCE("HoraImpresion", '01-01-01')      AS "HoraImpresion"
    FROM :v_VencimientosAnticiposPactos;
    ELSE
        SELECT 	
            ''  AS "Operacion",
            ''  AS "Tipo",
            0   AS "Folio",
            ''  AS "DocumentoIdentidad",
            0   AS "Portafolio",
            ''  AS "Nombre",
            CAST(0 AS DECIMAL(20,2))   AS "Monto Final",
            CAST(0 AS DECIMAL(20,2))   AS "Valor Titulo Anticipo",
            CAST(0 AS DECIMAL(20,2))   AS "Valor Titulo Vencimiento",
            CAST(0 AS DECIMAL(20,2))   AS "Valor Pacto",
            CAST(0 AS DECIMAL(20,2))   AS "Valor Presente",
            CAST(0 AS DECIMAL(20,2))   AS "Cantidad",
            ''  AS "Nemo",
            ''  AS "Emisor",
            ''  AS "Tipo Reajuste",
            '01-01-0001'  AS "Fecha Vencimiento",
            TO_VARCHAR(p_FechaConsulta, 'DD-MM-YYYY') AS "Fecha Liquidacion",
            CAST(0 AS DECIMAL(10,4))   AS "Tasa TIR",
            ''  AS "Tipo Instrumento",
            ''  AS "Familia",
            cord."RazonSocial",
            cord."RUT",
            Logo."LogoCorredora",
            TO_VARCHAR(:v_FechaHoraLOCAL,'DD-MM-YYYY') AS "FechaSistema",
            TO_VARCHAR(:v_FechaHoraLOCAL,'HH24:MI') AS "HoraImpresion"
        FROM DUMMY
        CROSS JOIN :v_logo AS Logo
        CROSS JOIN :v_DatosCorredora AS cord;
    END IF;
END;
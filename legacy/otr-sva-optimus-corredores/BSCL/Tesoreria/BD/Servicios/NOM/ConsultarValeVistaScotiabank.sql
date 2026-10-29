SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarValeVistaScotiabank";
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarValeVistaScotiabank" (
    IN p_IdEmpresa BIGINT,
    IN p_IdNominaTransferencia BIGINT,
    IN p_idBanco BIGINT,
    IN p_idMoneda NVARCHAR(5),
    IN p_idMedioDePago NVARCHAR(5),
    OUT p_out_Data_TipoRegistro1 TABLE (
        "TipoRegistroOptimus" INTEGER,
        "TipoRegistro" NVARCHAR(1),
        "Fecha" NVARCHAR(8),
        "TotalMonto" BIGINT,
        "TotalRegistros" INTEGER
    ),
    OUT p_out_Data_TipoRegistro2 TABLE (
        "TipoRegistroOptimus" INTEGER,
        "IdInterno" BIGINT,
        "TipoRegistro" NVARCHAR(1),
        "Fecha" NVARCHAR(8),
        "Monto" BIGINT,
        "RutCliente" NVARCHAR(9),
        "Corredora" NVARCHAR(35),
        "PaternoCliente" NVARCHAR(16),
        "MaternoCliente" NVARCHAR(16),
        "NombreCliente" NVARCHAR(28),
        "Sucursal" NVARCHAR(3),
        "RutCorredora" NVARCHAR(40)
    )
) LANGUAGE SQLSCRIPT SQL SECURITY INVOKER DEFAULT SCHEMA "BSCL" AS 
BEGIN 
    BEGIN 
        p_out_Data_TipoRegistro1 =
        SELECT
            1 AS "TipoRegistroOptimus",
            '1' AS "TipoRegistro",
            COALESCE(TO_NVARCHAR(NT."FechaLiquidacion", 'DDMMYYYY'),'') AS "Fecha",
            TO_BIGINT(COALESCE(NT."Monto", 0)) AS "TotalMonto",
            COUNT(NTD."IdNominaTransferenciaDetalle") AS "TotalRegistros"
        FROM
            "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
            INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD ON NTD."IdNominaTransferencia" = NT."IdNominaTransferencia"
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON DGEN."IdEmpresa" = NT."IdEmpresa"
        WHERE
            NT."IdNominaTransferencia" = :p_IdNominaTransferencia
        GROUP BY
            NT."FechaLiquidacion", NT."Monto";

        p_out_Data_TipoRegistro2 =
        SELECT
            2 AS "TipoRegistroOptimus",
            C."IdComprobante" AS "IdInterno",
            '2' AS "TipoRegistro",
            COALESCE(TO_NVARCHAR(NT."FechaLiquidacion",'DDMMYYYY'),'') as "Fecha",
            TO_BIGINT(COALESCE(CFP."Monto", 0)) AS "Monto",
            LPAD(LEFT(REPLACE( C."DocumentoIdentidad",'-',''),9),9,'0')AS "RutCliente",
            'SCOTIA SUDAMERICANO C. DE BOLSA S.A' AS "Corredora",
            COALESCE(CP."ApellidoPaterno", '') AS "PaternoCliente",
            COALESCE(CP."ApellidoMaterno", '') AS "MaternoCliente",
            COALESCE(CP."Nombres", '') AS "NombreCliente",
            COALESCE(SUBSTRING(SUC."CodigoSucursal", 1, 3), '') AS "Sucursal",
            REPLACE(DGEN."RUT", '-', '') AS "RutCorredora"
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
        INNER JOIN
            "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
        INNER JOIN
            "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
        INNER JOIN
            "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = CFP."IdComprobante" 
        LEFT JOIN 
            "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS CP ON CP."IdPersona" = C."IdPersona"
        LEFT JOIN 
            "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON DGEN."IdEmpresa" = NT."IdEmpresa"
        LEFT JOIN 
        	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdPersona" = CP."IdPersona"
 		LEFT JOIN 
 			"BSCL.Plataforma.BD::PLA.Sucursal" AS SUC ON (SUC."IdSucursal" = CLI."IdSucursalEjecutivo" AND SUC."IdEmpresa" = CLI."IdEmpresa")
        WHERE 
            NT."IdNominaTransferencia" = :p_IdNominaTransferencia;
    END;
END;
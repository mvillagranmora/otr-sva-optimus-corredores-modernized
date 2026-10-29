SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasScotiabank";
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ConsultarTransferenciasScotiabank" (
    IN p_IdEmpresa BIGINT,  
    IN p_IdNominaTransferencia BIGINT,
    IN p_idBanco BIGINT,
    IN p_idMoneda NVARCHAR(5),
    IN p_idMedioDePago NVARCHAR(5),
    
    OUT p_out_Data_Cabecera TABLE (
        "TipoRegistroOptimus" INTEGER,
        "TipoRegistro" NVARCHAR(1),
        "Tipo" NVARCHAR(2),
        "RutCorredor" INTEGER,
        "DigitoVeri" NVARCHAR(1),
        "Codigo1" INTEGER,
        "Fecha" NVARCHAR(8),
        "NumeroRegs" INTEGER,
        "TotalMonto" BIGINT,
        "Codigo2" NVARCHAR(5),
        "Relleno" NVARCHAR(218)
    ),
    OUT p_out_Data_Detalle TABLE (
        "TipoRegistroOptimus" INTEGER,
        "TipoRegistro" NVARCHAR(1),
        "Tipo" NVARCHAR(2),
        "RutCliente" NVARCHAR(40),
        "Nombre" NVARCHAR(110),
        "Numero" INTEGER,
        "Correo" NVARCHAR(40),
        "TipoTrx" NVARCHAR(2),
        "Cuenta" NVARCHAR(16),
        "CodigoBco" INTEGER,
        "Monto" BIGINT,
        "TipoCuenta" NVARCHAR(40),
        "Indicador" NVARCHAR(1),
        "SaltoLinea"  NVARCHAR(2),
        "Tipo2" NVARCHAR(2),
        "Tipo3" NVARCHAR(2),
        "Numero2" BIGINT,
        "Nemo" NVARCHAR(42),
        "Monto2" BIGINT,
        "Relleno" NVARCHAR(145)
    )
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" AS
BEGIN
    BEGIN
        p_out_Data_Cabecera = 
        SELECT
            1 AS "TipoRegistroOptimus",
            '1' AS "TipoRegistro",
            '00' AS "Tipo",
            TO_INTEGER(SUBSTRING(DGEN."RUT", 1, LENGTH(DGEN."RUT") - 2)) AS "RutCorredor",
            RIGHT(DGEN."RUT", 1) AS "DigitoVeri",
            2 AS "Codigo1",
            TO_NVARCHAR(NT."FechaLiquidacion",'YYYYMMDD') as "Fecha",
             1+2*COUNT(NTD."IdNominaTransferenciaDetalle") AS "NumeroRegs",
            TO_BIGINT(NT."Monto") AS "TotalMonto",
            '1005N' AS "Codigo2",
            ' ' AS "Relleno"
        FROM 
            "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
        LEFT JOIN 
            "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD ON NTD."IdNominaTransferencia" = NT."IdNominaTransferencia"
        LEFT JOIN 
            "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON (DGEN."IdEmpresa" = NT."IdEmpresa")
        WHERE 
            NT."IdNominaTransferencia" = :p_IdNominaTransferencia
        GROUP BY 
            DGEN."RUT", NT."FechaLiquidacion", NT."Monto";
        p_out_Data_Detalle =
        SELECT 
            2 AS "TipoRegistroOptimus",
            '2' AS "TipoRegistro",
            '10' AS "Tipo",			 
		    LPAD(LEFT(REPLACE( C."DocumentoIdentidad",'-',''),10),10,'0')  AS "RutCliente",
            COALESCE(C."NombrePersona", '') AS "Nombre",
            0 AS "Numero",
            LEFT( COALESCE(PDE."Direccion", ''),40 )AS "Correo",
            CASE WHEN CFP."IdBancoSocio" = 12 THEN 'OB' ELSE 'CC' END AS "TipoTrx",
            COALESCE(LEFT(TO_VARCHAR(REPLACE(CFP."NumeroCuentaBancariaSocio",'-','')), 16), '') AS "Cuenta",
            0 AS "CodigoBco",
            COALESCE(TO_BIGINT(CFP."Monto"), 0) AS "Monto",
            CASE WHEN BAN."CodigoBanco" = '014' THEN 'Cuenta Corriente SCOTIABANK SUDAMERICANO' ELSE LEFT(( UPPER(CFP."NombreTipoCuentaBancariaCorredora") ||' ' ||ban."NombreBanco"),40)   END AS "TipoCuenta",
            'N' AS "Indicador",
            '\n' AS "SaltoLinea",
            '20' AS "Tipo2",
            'DI' AS "Tipo3",
            10000000000 AS "Numero2",
            COALESCE(COM."Nemotecnico", '') AS "Nemo",
            COALESCE(TO_BIGINT(CFP."Monto"), 0) AS "Monto2",
            ' ' AS "Relleno"
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
        LEFT JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
        LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
        LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = CFP."IdComprobante"
        LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON (PER."IdPersona" = C."IdPersona")
        LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS PDE ON (PDE."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal") 
        LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON DGEN."IdEmpresa" = NT."IdEmpresa"
        LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" as COM ON COM."IdComprobante"= C."IdComprobante"
        LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON BAN."IdBanco" = CFP."IdBancoSocio"
        WHERE NT."IdNominaTransferencia" = :p_IdNominaTransferencia;
    END;
END;
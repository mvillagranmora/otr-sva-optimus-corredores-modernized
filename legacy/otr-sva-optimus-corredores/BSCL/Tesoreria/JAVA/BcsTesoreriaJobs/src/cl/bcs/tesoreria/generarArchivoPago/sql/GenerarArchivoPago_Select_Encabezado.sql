 SELECT 
           1                             AS "TipoRegistroOptimus"
          ,'1'                           AS "TipoRegistro"
          ,' '                           AS "Relleno"
          ,NT."IdNominaTransferencia"    AS "NumeroNomina"
          ,DGEN."RUT"                    AS "RutOrdenate"
          ,DGEN."RazonSocial"            AS "NombreOrdenate"
          ,' '                           AS "NumeroConvenio"
          ,NT."NumeroCuentaBancaria"     AS "CuentaCargo"
          ,NT."IdMoneda"                 AS "MonedaNomina"
          ,NT."FechaLiquidacion"         AS "FechaOperacion" 
          ,'USU-PROC-FINAN'              AS "NombreUsuario"
          ,NT."NombreBanco"				 AS "NombreBanco" 
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" AS DGEN ON (DGEN."IdEmpresa" = NT."IdEmpresa")
	WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia;
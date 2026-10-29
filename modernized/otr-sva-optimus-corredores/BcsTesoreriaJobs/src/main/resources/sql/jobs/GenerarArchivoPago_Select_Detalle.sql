 SELECT 
          2                               AS "TipoRegistroOptimus"
         ,NTD."IdComprobante"             AS "IdInterno"  
         ,'2'                             AS "TipoRegistro"
         ,(CASE 	WHEN NT."IdMedioDePago" = 'TDP' THEN  'DCV'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" = "IdBancoSocio" THEN 'CCT'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoCorredora" != "IdBancoSocio" THEN 'ITF'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 AND NTD."DocumentoIdentidad" = '97029000-1' THEN 'CSBC'
					WHEN NT."IdMedioDePago" = 'TRA' AND NTD."IdBancoSocio" = 14 THEN 'AVAP'
					ELSE ' ' END)            AS "TipoDePago"
         ,' '                             AS "RefDetallePago"
         ,NTD."DocumentoIdentidad"        AS "RutBeneficiario"
         ,NTD."NombrePersona"             AS "NombreBeneficiario"
         ,NTD."Monto"                     AS "MontoRemesa"
         ,NTD."NumeroCuentaBancariaSocio" AS "CtaCteBeneficiario"
         ,BNC."CodigoIso"                 AS "CodigoIsoBancoBeneficiario"
         ,' '                             AS "InformacionRemesa"
    	 ,NTD."CuentaDCVDestino"          AS "CodigoDepDcvComprador"
	     ,NTD."CuentaDCVOrigen"			  AS "CodigoDepDcvVendedor"
		 ,NTD."ClaveDCV"				  AS "ClaveMedioPago"
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
    INNER JOIN "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS BNC ON (NTD."NombreBancoSocio" = BNC."NombreBanco")
	WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia AND NTD."IdEstado" <> 'ANU';
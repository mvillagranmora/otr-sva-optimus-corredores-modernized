 SELECT
           3                        AS "TipoRegistroOptimus"   		  							
		  ,NTD."IdComprobante"      AS "IdInterno"        			
		  ,'3'                      AS "TipoRegistro"                   			
          ,to_decimal(count(NTD."IdNominaTransferencia"),5,0)                 AS "TotalDetalles"
          ,' '                      AS "relleno1"
          ,' '                      AS "relleno2"
          ,' '                      AS "relleno3"
          ,NT."Monto"	            AS "MontoNomina"
    FROM "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT
    INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferenciaDetalle" AS NTD ON (NT."IdNominaTransferencia" = NTD."IdNominaTransferencia")
	WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
    GROUP BY NTD."IdNominaTransferencia",NTD."IdComprobante",NT."Monto";
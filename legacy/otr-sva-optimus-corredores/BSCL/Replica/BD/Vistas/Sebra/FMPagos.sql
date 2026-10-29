SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.FMPagos"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.FMPagos" AS

SELECT 
    COALESCE(CPO."IdComprobantePagoOperacion", 0)               AS ID, 
    COALESCE(CASE 
				WHEN CPO."IdTipoProducto" = 'MEME' THEN OP."IdOperacionSpot"
				WHEN CPO."IdTipoProducto" = 'MEFW' THEN CAR."IdOperacion"
				ELSE CPO."IdOperacion" END
	, 0)                              AS FolioTransaccion, 
    COALESCE(CASE 
        	WHEN CPO."RecibePaga" = 'R' THEN 'C' ELSE CPO."RecibePaga" 
    		END, '')                                                        AS IndPagoCobro, 
    1                                               			            AS FolioPago,
    COALESCE(CASE 
				WHEN CPO."IdTipoProducto" = 'MEFW' THEN CAR."FolioCartera"
				ELSE CPO."IdOperacion" END
	, 0)                               AS FolioOperacion,
    COALESCE(CASE 
        		WHEN CPO."IdMoneda" = 'USD' THEN '13' ELSE '999' 
    			END, '')                                                    AS CodMonPago,
    COALESCE(CASE PP."IdMedioDePago"
        WHEN '' THEN '0'
        WHEN 'VVI' THEN '1' --VALE VISTA
        WHEN 'CHE' THEN '2' --CHEQUE
        WHEN 'CTA' THEN '3' --CUENTA CORRIENTE
        WHEN 'CCC' THEN '3' --CARGO CUENTA CORRIENTE
        WHEN 'EFE' THEN '4' --EFECTIVO
        WHEN 'TRA' THEN '8' --Transferencia Hoy                                 
        WHEN 'TRA' THEN '9' --Transferencia 24                              
        WHEN 'TRA' THEN '10' --Transferencia 48   
        WHEN 'TDP' THEN '21' --TRANSFERENCIA DVP
        WHEN 'TLR' THEN '12' --TRANSFERENCIA LBTR
        WHEN 'POR' THEN '28' --POR DEFINIR
        WHEN 'VVE' THEN '779' --VALE VISTA ELECTRONICO
        WHEN 'CIN' THEN '993' --Cargo Cuenta Inversiones
        ELSE PP."IdMedioDePago"
    END, '0')                                             		              AS CodFormaPago,
    COALESCE(CPO."Monto",0)                                    	              AS Monto,
    COALESCE(CASE 
    			WHEN CPO."IdTipoProducto" LIKE 'MEFW' THEN CAR."FechaOperacion" --FechaOpFoward
    			ELSE OP."FechaOperacion" 										--FechaOpSpot
    		 END, '1900-01-01') 											  AS FechaMovto,
    COALESCE(CPO."FechaLiquidacionComprobantePagoOperacion", '1900-01-01')    AS FechaPago,
    COALESCE(CASE 
    			WHEN CPO."IdTipoProducto" LIKE 'MEFW' THEN CAR."FechaOperacion"
    			ELSE OP."FechaOperacion" 										
    		 END, '1900-01-01') 											  AS FechaValor,
    'IM'                                            			              AS OrigenPago,
	''                                              			              AS NomCheque,
    COALESCE(CASE CPO."IdEstado"
        		WHEN 'ANU' THEN 'A'
        		WHEN 'PEN' THEN 'P'
        		WHEN 'VIG' THEN 'I'
        		ELSE CPO."IdEstado"
    		 END, '')                               AS Estado,
    ''                                              AS ObservacionFO,
    '' 												AS ObservacionBO,
    0 												AS CodBancoCorresp,
    '' 												AS NroCuentaCorriente,
    0 												AS CodBancoGirador,
    LEFT(COALESCE(USR."CodigoUsuario",''),10) 				AS UsuarioCreador,
    COALESCE(PP."FechaCreacion",'1900-01-01')		AS FechaCreacion,
    0 												AS CodBancoCorrespCli,
    '' 												AS NroCuentaCorrienteCli,
    0 												AS IdGrupo,
    0 												AS CodBancoCorr,
    '' 												AS NroCuentaCorr,
    ''												AS CodBancoCli,
    '' 												AS NroCuentaCli,
    '' 												AS CodSucursal
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
INNER JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" FPM      ON (CPO."IdFormaDePago" = FPM."IdFormaDePago" OR CONCAT(CPO."IdFormaDePago",'_0') = FPM."IdFormaDePago")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USR         ON FPM."IdUsuarioCreador" = USR."IdUsuario" 
INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP ON FPM."IdPreferenciaPago" = PP."IdPreferenciaPago"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR 			ON CPO."IdOperacion" = CAR."IdCartera"
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OP 			ON CPO."IdOperacion" = OP."IdOperacionSpot"
WHERE CPO."IdTipoProducto" IN ('MEME','MEFW')


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.FMPagos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
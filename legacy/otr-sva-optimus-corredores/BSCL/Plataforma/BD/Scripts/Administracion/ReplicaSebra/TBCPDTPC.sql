SELECT ms.RUT_CLI,dt.*,ms.* FROM BSCL.TBCPDTPC DT LEFT JOIN TBCPMSPC MS ON MS.SEC_COMPROM = DT.SEC_COMPROM AND ms.COD_PER = dt.COD_PER
where DT.cod_per = '2021-03' AND MS.estado in ('A','E') AND MS.FEC_LIQUID > '2021-03-31';

SELECT * FROM BSCL.TBCPMSPC


SELECT cod_per,estado,fec_liquid,* FROM "BSCL"."BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC" OPT where cod_per = '2021-04' AND estado in ('A','E') AND FEC_LIQUID > '2021-03-31';

SELECT *  FROM "BSCL.Custodia.BD::CUS.Subyacente" RES

SELECT * FROM "BSCL.Custodia.BD::CUS.Reserva" RES
		--INNER JOIN  "BSCL.Mesa.BD::MES.Operacion" o ON (res."IdOperacion" = o."IdOperacion")


BBNS-N0712      


SELECT rut_cli FROM "BSCL"."BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" 
ORDER BY rut_cli


SELECT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"EsFungible"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"IdCuentaCustodioPortafolio"
		   ,"NumeroCuentaBoveda"
		   ,"Cantidad"
		   ,"ValorCompra"
		   ,"MontoCompra"
		   ,"INGPrecioMercadoActual"
		   ,"INGTasaMercadoActual"
		   ,"ValorMercado"
		   ,"MontoMercado"
		   ,0 AS "ValorPPP"
		   ,"IdMonedaReajuste"
		   ,"IdMonedaEmision"
		FROM
		(
		
			SELECT sby."Nemotecnico",*
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PAC."FolioCartera" = ultSub."FolioCarteraRelacionada")
			INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
			WHERE sby."Nemotecnico" = 'BBNS-N0712'
			--where PAC."IdTipoInstrumento" = 'PAC' AND PAC."IdTipoOperacion" = 'COMPAC'
		
			)
		WHERE RANK = 1;
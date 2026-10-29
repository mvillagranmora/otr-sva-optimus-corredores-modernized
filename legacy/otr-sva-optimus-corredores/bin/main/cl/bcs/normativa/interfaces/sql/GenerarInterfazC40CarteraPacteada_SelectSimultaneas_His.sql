-- ***************************************************************************************************************
-- Cartera Simultanea - Interfaz GenerarInterfazC40CarteraPacteada consulta historica
-- ***************************************************************************************************************

SELECT 
CASE WHEN SIM."IdTipoOperacion"= 'COMSIM' THEN 'C'
    WHEN SIM."IdTipoOperacion" = 'VENSIM' THEN 'V'
    END 																	AS TIPO_COMPROM,
COALESCE (TO_VARCHAR(SIM."INGFechaOperacion", 'DD-MM-YYYY'),'' )  			AS FEC_TRANS,
COALESCE (TO_VARCHAR(SIM."FechaVencimientoOperacion", 'DD-MM-YYYY'),'' )  	AS FEC_VENC,   
COALESCE (TO_VARCHAR(SIM."FechaLiquidacion" , 'DD-MM-YYYY'),'' )  		    AS FEC_LIQUID, 
TO_NVARCHAR(DAYS_BETWEEN(:FiltroFecha, SIM."FechaVencimientoOperacion" ))	AS PLAZO,
ROUND(SIM."PrecioInicial"  *  SIM."INGCantidad" )  							AS MONTO_CAPITAL,
ROUND(ROUND("PrecioInicial" * (1 + (SIM."INGTasaCompra" * DAYS_BETWEEN("FechaInversion", :FiltroFecha)) / 3000), 3) * SIM."INGCantidad" , 0)  AS VALOR_PTE,
SIM."INGTasaCompra"															AS TASA
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis"		AS SIM
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 	AS SOC ON  SOC."IdCliente"  = SIM."IdCliente" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"  			AS EMP ON  EMP."IdSocioDeNegocio"  = SOC."IdSocioDeNegocio" 
WHERE SIM."INGFechaOperacion" 		    <= :FiltroFecha
AND SIM."FechaVencimientoOperacion" 	> :FiltroFecha
AND EMP."IdEmpresa" 					= :IdEmpresa
AND SIM."IdEstado" 					    = 'VIG'
AND SIM."FechaRespaldo"  				= :FiltroFecha
ORDER BY
FEC_VENC
SELECT
	 CMD."IdCargaMasivaDetalle"							AS "IdCargaMasivaDetalle"	
	,CM."IdTipoCargaMasiva" 							AS "IdTipoCargaMasiva"
	,CMD."IdCargaMasiva"								AS "IdCargaMasiva"
	,CMD."CodigoPortafolio"								AS "CodigoPortafolio"
	,CMD."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"
	,CMD."IdBoveda"										AS "IdBoveda"
	,CMD."IdConcepto"									AS "IdConcepto"
	,INS."IdInstrumento"								AS "IdInstrumento"	
	,CMD."Nemotecnico"									AS "Nemotecnico"
	,INS."IdTipoInstrumento"							AS "IdTipoInstrumento"
	,INS."IdEmisor"										AS "IdEmisor"
	,INS."CorteMinimo"									AS "CorteMinimo"
	,INS."IdTipoReajuste"								AS "IdTipoReajuste"
	,CAST(INS."FechaEmision"		AS VARCHAR(10))		AS "FechaEmision"
	,CAST(INS."FechaVencimiento"	AS VARCHAR(10))		AS "FechaVencimientoInstrumento"
	,CAST(CMD."FechaVencimiento" 	AS VARCHAR(10)) 	AS "FechaVencimiento"	
	,CMD."Cantidad"										AS "Cantidad"
	,CMD."Precio"										AS "Precio"
	,CMD."Tasa"											AS "Tasa"
	,CMD."Monto"										AS "Monto"
	,CMD."ValorRescate"									AS "ValorRescate"
	,CMD."NemoGarantizado"								AS "NemoGarantizado"
	,INS."IdMoneda"										AS "Moneda"
	,CMD."Fungible"										AS "EsFungible"
	,CMD."Bolsa"										AS "IdBolsa"
	,CMD."Glosa"										AS "Glosa"
	,CMD."IdEstado"										AS "IdEstado"
	,CMD."ObservacionValidacion"						AS "ObservacionValidacion"
	,CMD."CuentaCustodio"								AS "CuentaCustodio"
	,CMD."CorrelativoReporte"							AS "CorrelativoReporte"
	,CMD."TipoOperacion"								AS "IdTipoOperacion"
	,CMD."ClasificacionInstrumento"						AS "ClasificacionInstrumento"
	,CMD."Plazo"
	,CAST(CMD."FechaLiquidacion" 	AS VARCHAR(10)) 	AS "FechaLiquidacion"
	,CAST(CMD."FechaOperacion"  	AS VARCHAR(10)) 	AS "FechaOperacion"
	,CMD."TipoCambio"									AS "TipoCambio"
	,CMD."MontoContado"									AS "MontoContado"
	,CMD."MontoPlazo"									AS "MontoPlazo"
	,CMD."EsNominal"									AS "EsNominal"
	,CMD."ReferenciaReportado"							AS "ReferenciaReportado"
	,CP."DocumentoIdentidad" 							AS "DocumentoIdentidad"
	,CP."IdCliente"										AS "IdCliente"
	,CP."IdPersona"										AS "IdPersona"
	,CP."IdPortafolio"									AS "IdPortafolio"
	,CTA."IdCuentaCustodioPortafolio"					AS "IdCuentaCustodioPortafolio"
	,TIP."IdInstrumento"								AS "IdInstrumentoSubyacente"
	,TIP."Nemotecnico"									AS "NemotecnicoSubyacente"
	,CMD."AsociadoCorelativo"                           AS "AsociadoCorelativo"
					
FROM "BSCL.Custodia.BD::CUS.CargaMasivaDetalle"                             AS CMD 
    INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasiva"                          AS CM
        ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"         AS CC
        ON CC."DocumentoIdentidadAdicional" = CMD."DocumentoIdentidad"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"    AS CP 
        ON (CP."DocumentoIdentidad" = CC."DocumentoIdentidad" 
        AND CP."CodigoPortafolio"   = CMD."CodigoPortafolio")       
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento"      AS INS
        ON (CMD."Nemotecnico" = INS."Nemotecnico"
        AND CMD."ClasificacionInstrumento" = INS."IdClasificacionTipoInstrumento")
    LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"            AS CTA
        ON (CTA."IdPortafolio"  = CP."IdPortafolio"
        AND CTA."IdBoveda"      = CMD."IdBoveda")
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"        AS TIP
        ON (TIP."IdTipoOperacion"   = LEFT(CMD."TipoOperacion",3)|| 'R' || CMD."ClasificacionInstrumento"
        AND TIP."IdTipoInstrumento" = 'R'|| CMD."ClasificacionInstrumento")     

WHERE   CM."IdEmpresa"      = :IdEmpresa 
    AND CMD."IdCargaMasiva" = :IdCargaMasiva
    AND CTA."IdEstado"      = 'ACT';

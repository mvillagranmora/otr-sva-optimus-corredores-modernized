SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacionesPactosVigentes";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacionesPactosVigentes" AS
	SELECT DISTINCT
		'2'	AS "TipoRegistro"	
		,CASE WHEN CODPD."IdTipoOperacion" = 'COMPAC' THEN 'CRV'	
			WHEN CODPD."IdTipoOperacion" = 'VENPAC' THEN 'VRC'ELSE '' END AS "TipoCompromiso"
		,CODPD."IndicadorCobertura"	AS "IndicadorCobertura"
		,CODPD."FechaOperacion"	AS "FechaOperacion"
		,ODP."RazonSocial"	AS "NombreCliente"	
		,ODP."DocumentoIdentidad"	AS "RutCliente"
		,CASE WHEN CODPD."RelacionCorredora" = 'Relacionado' THEN 'S' ELSE 'N' END AS "Relacionado"
		,CODPD."Mercado"	AS "Mercado"
		,CODPD."Nemotecnico"	AS "Nemotecnico"
		,CODPD."IdEmisor"	AS "Emisor"
		,CODPD."IdTipoReajuste" AS "TipoReajusteInstrumento"
		,CODPD."Cantidad"	AS "Unidades" 
		,CODPD."FolioOperacion"	AS "NumeroOperacion"
		,TO_VARCHAR(CODPD."FechaVencimiento", 'YYYY-MM-DD')	AS "FechaVencimientoPacto"	
		,TO_VARCHAR(CODPD."FechaVencimientoInstrumento", 'DD-MM-YYYY')	AS "FechaVencimientoInstrumento"
		,ODP."MontoCompromiso"	AS "MontoPacto"
		,CODPD."IdTipoReajuste" AS "TipoMonedaPacto"
		,ODP."MontoOperacion"	AS "MontoOperacion"
		,ODP."Tasa"	AS "TasaPacto"

	FROM "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactosDetalle" CODPD
	LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactos" ODP 
	ON (ODP."FolioOperacion" = CODPD."FolioOperacion")
	WHERE ODP."IdEstado" = 'VIG'
;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarOperacionesPactosVigentes" TO BCS_DESARROLLADOR WITH GRANT OPTION;

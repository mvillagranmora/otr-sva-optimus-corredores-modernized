SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCEDVRV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCEDVRV" AS

select YEAR(EVC."FechaPago") AS EJERCICIO,
	CASE EVC."IdTipoInstrumento" 
		WHEN 'ACN' THEN 'AC' 
		WHEN 'ACX' THEN 'AO' 
		WHEN 'ETN' THEN 'EN'
		WHEN 'ETX' THEN 'ET' 
		WHEN 'MIL' THEN 'ST' 
		WHEN 'CFI' THEN 'CF' 
		WHEN 'CFX' THEN 'CF' 
		ELSE 'AC' END AS MERCADO,'D' AS TIPO_REG,
		COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad"),'0-0') AS RUT_CLI,
		CAST(POR."CodigoPortafolio" AS INT) AS SEC_RUT_CLI,
		EVCC."Nemo" AS NEMO,
		EVCC."FechaPago" AS FECHA,
		EVCC."Secuencia" AS SEC_EVE,
		COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(EMIPER."DocumentoIdentidad"),'0-0') AS RUT_SOC,
		0 AS NUM_CER_CLI,
		SUM(EVCC."CantidadDerecho" )AS CANTIDAD,
		'COR' AS COD_BOVEDA,
		'S' AS IND_INSCRITO,
		'N' AS IND_PRI_EMI,
		SUM(EVCC."Total") AS MONTO,
		CASE WHEN STRING_AGG(COALESCE(CTA."IdCargoAbonoMovimiento",0),',') LIKE '%,%' THEN '0' 
			ELSE STRING_AGG(COALESCE(CTA."IdCargoAbonoMovimiento",0),',') END AS SEC_MOVTO,
		'' as TIPO_FONDO,
		EVC."IdMoneda" AS MONEDA
from "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVCC
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" evc on evc."IdEventoCapital" = EVCC."IdEventoCapital"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por on por."IdPortafolio" = EVCC."IdPortafolio"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI on EMI."IdEmisor" = EVC."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS EMIPER ON EMIPER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CTA ON CTA."IdOperacion" = EVCC."IdEventoCapitalCliente" and cta."IdConcepto"  = 'CIECDV'
WHERE EVCC."IdTipoEventoCapital" IN ('DV') AND "TipoSocioNegocio" = 'CLI'
AND "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0
AND EVC."Secuencia" <= 99999999
GROUP BY EVC."IdTipoInstrumento",EVC."FechaPago",PER."DocumentoIdentidad",POR."CodigoPortafolio",EVCC."Nemo",EVCC."FechaPago",EVCC."Secuencia",
EMIPER."DocumentoIdentidad",EVC."IdMoneda"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCEDVRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

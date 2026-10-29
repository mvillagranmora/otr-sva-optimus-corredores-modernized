SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBPLTBRF";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaTBPLTBRF" as
SELECT
	TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD:HH24:MI:SS.FF7') AS FEC_TRANS, --char](26) NOT NULL, *********
	TRA."Folio" AS FOLIO_TRANS, --char](8) NOT NULL, *********
	TRA."IdBolsa" AS IND_BOLSA, --char](3) NOT NULL,
	substr(TOPE."AccionOperacion",1,1) AS TIPO_OPERAC, --char](1) NOT NULL, C/V
	TO_VARCHAR (TRA."FechaOperacion",'YYYY-MM-DD') AS FECHA, --char](10) NOT NULL, *********
	TRA."Nemotecnico" AS NEMO, --char](16) NOT NULL,  *********
	'' AS CAN_ASIG_ACUM, --decimal](14, 2) NOT NULL,
	'' AS CAN_CP, --decimal](14, 2) NOT NULL,
	TRA."Cantidad" AS CANTIDAD, --decimal](14, 2) NOT NULL, *********
	TRA."CodigoOperadorContraparte" AS COD_CORR_CONTRA, --decimal](3, 0) NOT NULL, ********* 82
	CORRES."CodigoCorredor" AS COD_OPE_EXT, --decimal](3, 0) NOT NULL, ********* 42
	'' AS EMISOR_NO_INS, --char](10) NOT NULL,
	'' AS FEC_DIG, --char](10) NOT NULL,
	'' AS IND_BOLSON, --char](1) NOT NULL,
	'' AS IND_COMPROMISO, --char](1) NOT NULL,
	'' AS IND_CTA_PRO, --char](1) NOT NULL,
	TRA."IndicadorDCV" AS IND_DCV, --char](1) NOT NULL,  ********* D
	CL."Codigo" AS IND_LIQUID, --char](2) NOT NULL, *********  PM
	'' AS IND_MODIF, --char](1) NOT NULL,
	'' AS IND_OD, --char](2) NOT NULL,
	'' AS IND_RESERVADO, --char](1) NOT NULL,
	TRA."Monto" AS MONTO, --decimal](17, 2) NOT NULL, *********
	'' AS PLAZO, --decimal](4, 0) NOT NULL,
	'' AS PLAZO_CORA_PRTP, --decimal](2, 0) NOT NULL,
	TRA."Precio" AS PRECIO, --decimal](5, 2) NOT NULL, *********
	'' AS RUEDA, --char](2) NOT NULL, *********  T1
	'' AS TIPO_CALCULO, --char](2) NOT NULL,
	TRA."Tasa" AS TIR, --decimal](5, 2) NOT NULL, *********
	TRA."CodigoOperadorCorredor" AS OPE_INT, --decimal](3, 0) NOT NULL, *********  3
	'' AS SEC_MOVTO_FAC, --decimal](8, 0) NOT NULL,
	'' AS FEC_VCTO_TP, --char](10) NOT NULL,
	'' AS IND_SIMUL, --char](2) NOT NULL,
	'' AS TASA_SIMUL, --decimal](7, 4) NOT NULL,
	'' AS FOLIO_SIMUL, --char](8) NOT NULL,
	'' AS CODCATIFRSID, --tinyint] NOT NULL,
	'' AS PARIDAD_MORIGEN, --decimal](15, 6) NOT NULL,
	'' AS CAPTACION_MORIGEN, --decimal](18, 2) NOT NULL,
	'' AS MON_MORIGEN, --char](3) NOT NULL,
	'' AS FCREOF_COM, --char](26) NOT NULL,
	'' AS FCREOF_VEN, --char](26) NOT NULL,
	'' AS CAN_COM, --decimal](9, 0) NOT NULL,
	'' AS PRECIO_COM, --decimal](15, 4) NOT NULL,
	'' AS BOLSA_COM, --char](3) NOT NULL,
	'' AS CAN_VEN, --decimal](9, 0) NOT NULL,
	'' AS PRECIO_VEN, --decimal](15, 4) NOT NULL,
	'' AS BOLSA_VEN --char](3) NOT NULL,
FROM "BSCL.Orden.BD::ORD.Transaccion"  TRA 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  CL."IdCondicionDeLiquidacion" = TRA."IdCondicionDeLiquidacion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  TOPE."IdTipoOperacion" = TRA."IdTipoOperacion" 
LEFT JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsal" AS CORRES ON CORRES."IdCorredorCorresponsal" = TRA."IdCorredorCorresponsal"
WHERE TRA."IdClasificacionTipoInstrumento" = 'RF'
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBPLTBRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO

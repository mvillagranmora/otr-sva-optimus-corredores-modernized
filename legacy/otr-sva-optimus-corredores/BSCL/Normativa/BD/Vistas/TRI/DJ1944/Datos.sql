SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"Numero"	
	,"EjecutadoComplementoIPC"
	,"TipoCartera"
	,"RutCliente"
	,"IdCliente"
	,"RutInstitucion"
	,"FechaOperacion"
	,"SalAhorroNetoEjerNEGT1"
	,"SalArrastreEjerSIGT1"
	,"SalAhorroNetoEjerT2"
	,"SalArrastreEjerSigNegT2"
	,"MtoRetiro57BisT2"
	,"SalAhorroNetoEjeNegT3"
	,"SalArrastreEjeSigNegT3"
	,"MtoRetiroAnoT3"
	,"MtoRenRetFFMMPOS"
	,"MtoRenRetFFMMNEG"
	,"MtoRenRetOTROSPOS"
	,"MtoRenRetOTROSNEG"
	,"NumeroCertificado"

) AS

SELECT 
     MAX("ID") AS "ID"
    ,"IdDocumento"
    ,MAX("VboUsuario") as "VboUsuario"
    ,MAX("Observaciones") AS "Observaciones"
    ,MAX("FuenteDatos") AS "FuenteDatos"
   	,MAX("Numero") AS "Numero" 	
	,MAX("EjecutadoComplementoIPC") AS "EjecutadoComplementoIPC"
	,MAX("TipoCartera") AS "TipoCartera"
	,"RutCliente"
	,"IdCliente"
	,"RutInstitucion"
	,MAX("FechaOperacion") AS "FechaOperacion"
	,SUM("SalAhorroNetoEjerNEGT1") AS "SalAhorroNetoEjerNEGT1"
	,SUM("SalArrastreEjerSIGT1") AS "SalArrastreEjerSIGT1"
	,SUM("SalAhorroNetoEjerT2") AS "SalAhorroNetoEjerT2"
	,SUM("SalArrastreEjerSigNegT2") AS "SalArrastreEjerSigNegT2"
	,SUM("MtoRetiro57BisT2") AS "MtoRetiro57BisT2"
	,SUM("SalAhorroNetoEjeNegT3") AS "SalAhorroNetoEjeNegT3"
	,SUM("SalArrastreEjeSigNegT3") AS "SalArrastreEjeSigNegT3"
	,SUM("MtoRetiroAnoT3") AS "MtoRetiroAnoT3"
	,SUM("MtoRenRetFFMMPOS") AS "MtoRenRetFFMMPOS"
	,SUM("MtoRenRetFFMMNEG") AS "MtoRenRetFFMMNEG"
	,SUM("MtoRenRetOTROSPOS") AS "MtoRenRetOTROSPOS"
	,SUM("MtoRenRetOTROSNEG") AS "MtoRenRetOTROSNEG"
	,"NumeroCertificado"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1944"
GROUP BY
"RutCliente",
"RutInstitucion",
"IdDocumento",
"IdCliente",
"NumeroCertificado"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos" TO bcs_desarrollador WITH GRANT OPTION;
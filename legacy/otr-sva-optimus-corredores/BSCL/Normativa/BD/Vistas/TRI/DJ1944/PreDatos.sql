SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.PreDatos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.PreDatos" (
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
     d."ID"
    ,d."IdDocumento"
    ,d."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion", d."Observaciones") AS "Observaciones"
    ,d."FuenteDatos"
   	,d."Numero"	
	,d."EjecutadoComplementoIPC"
	,d."TipoCartera"
	,d."RutCliente"
	,d."IdCliente"
	,d."RutInstitucion"
	,d."FechaOperacion"
	,d."SalAhorroNetoEjerNEGT1"
	,d."SalArrastreEjerSIGT1"
	,d."SalAhorroNetoEjerT2"
	,d."SalArrastreEjerSigNegT2"
	,d."MtoRetiro57BisT2"
	,d."SalAhorroNetoEjeNegT3"
	,d."SalArrastreEjeSigNegT3"
	,d."MtoRetiroAnoT3"
	,d."MtoRenRetFFMMPOS"
	,d."MtoRenRetFFMMNEG"
	,d."MtoRenRetOTROSPOS"
	,d."MtoRenRetOTROSNEG"
	,d."NumeroCertificado"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1944" AS d	
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"RutCliente","IdCliente", "RutInstitucion","NumeroCertificado"
			FROM (
			    SELECT 
			        R."CodigoValidacion", 
			        DT."IdDocumento",
			        DT."RutCliente", 
			        DT."IdCliente",
			        DT."RutInstitucion",
			        DT."NumeroCertificado",
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."IdCliente" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1944" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t
			WHERE t.rn = 1
        ) AS VAL ON  d."IdDocumento" = VAL."IdDocumento" AND d."RutCliente" = VAL."RutCliente" 
        	AND d."IdCliente" = VAL."IdCliente" AND d."RutInstitucion" = VAL."RutInstitucion" AND d."NumeroCertificado" = VAL."NumeroCertificado";
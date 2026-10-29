SET SCHEMA "BSCL";

drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
 "IdParametroFinanciero"	NVARCHAR(40),
"FechaConsulta"					NVARCHAR(10)
    
);

INSERT INTO #TT 
SELECT 'UTM','2017-01-01' FROM DUMMY;
INSERT INTO #TT 
SELECT 'UTM','2017-01-01' FROM DUMMY;
INSERT INTO #TT 
SELECT 'UTM','2017-01-02' FROM DUMMY;
INSERT INTO #TT 
SELECT 'IPC','2017-01-02' FROM DUMMY;
INSERT INTO #TT 
SELECT 'IPC2','2017-01-02' FROM DUMMY;
INSERT INTO #TT 
SELECT 'IPC','2012-01-02' FROM DUMMY;


CALL "BSCL.Plataforma.BD.Servicios::MER.ConsultarParametroFinanciero" (
	p_ParametrosConsulta 					=> #TT,
	p_out_ParametrosResultado				=> ?,
	p_out_ErroresDetalle					=> ?

);


WITH ASDF AS (
			SELECT
				par."IdParametroFinanciero",defpar."Nombre",par."Valor",defpar."IdMonedaValor" as "IdMoneda",par."Fecha",ent."FechaConsulta",
			ROW_NUMBER() OVER (partition by par."IdParametroFinanciero",ent."FechaConsulta" order by par."IdParametroFinanciero",ent."FechaConsulta",par."Fecha" DESC) AS "Prioridad"
			FROM #TT ent
			INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" par ON par."IdParametroFinanciero" = ent."IdParametroFinanciero" AND par."Fecha" <= ent."FechaConsulta"
			INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" defpar ON defpar."IdParametroFinanciero" = par."IdParametroFinanciero"
		)
		SELECT TEMP."IdParametroFinanciero",TEMP."FechaConsulta",ASDF."Valor",ASDF."IdMoneda",ASDF."Fecha"
		FROM #TT TEMP
		LEFT JOIN ASDF ON ASDF."IdParametroFinanciero" = TEMP."IdParametroFinanciero" AND ASDF."FechaConsulta" = TEMP."FechaConsulta" AND "Prioridad" = 1;
		
		
SELECT* FROM 		"BSCL.Plataforma.BD::MER.ParametroFinanciero" par
INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" defpar ON defpar."IdParametroFinanciero" = par."IdParametroFinanciero"
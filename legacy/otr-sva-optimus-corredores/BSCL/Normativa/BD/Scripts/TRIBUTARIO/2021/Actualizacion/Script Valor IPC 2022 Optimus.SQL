--SEBRA
	--SELECT  ano_mes,isnull(Ind_Ipc,0) as Ind_Ipc FROM   rue_tbiepfme with(nolock) WHERE  left (ano_mes ,4)=2022 order by 2 desc
		/*
		ano_mes Ind_Ipc
		------- ---------------------------------------
		2022-12 129.0200000000
		2022-11 128.6500000000
		2022-10 127.4100000000
		2022-09 126.7500000000
		2022-08 125.6700000000
		2022-07 124.1600000000
		2022-06 122.4800000000
		2022-05 121.3500000000
		2022-04 119.9100000000
		2022-03 118.2600000000
		2022-02 116.1000000000
		2022-01 115.7700000000

(12 row(s) affected)
		
		*/
		
		
--OPTIMUS		

---respaldo
SELECT * FROM BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE left("AgnoMes",4)='2022'  AND "IdParametroFinanciero" = 'IPC' ORDER BY 2 DESC


DELETE FROM BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE left("AgnoMes",4)='2022'  AND "IdParametroFinanciero" = 'IPC';

INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-12-01', '2022-12', '', 129.02000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-11-01', '2022-11', '', 128.65000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-10-01', '2022-10', '', 127.41000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-09-01', '2022-09', '', 126.75000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-08-01', '2022-08', '', 125.67000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-07-01', '2022-07', '', 124.16000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-06-01', '2022-06', '', 122.48000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-05-01', '2022-05', '', 121.35000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-04-01', '2022-04', '', 119.91000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-03-01', '2022-03', '', 118.26000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-02-01', '2022-02', '', 116.10000000, '2023-01-23 18:00:00.000');
INSERT INTO BSCL."BSCL.Plataforma.BD::MER.ParametroFinanciero"
("IdParametroFinanciero", "Fecha", "AgnoMes", "Agno", "Valor", "FechaActualizacion")
VALUES('IPC', '2022-01-01', '2022-01', '', 115.77000000, '2023-01-23 18:00:00.000');


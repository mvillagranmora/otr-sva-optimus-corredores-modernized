SELECT "FechaOperacion","Comentario"
FROM BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodiaExclusiones" m
LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" c ON c."IdMovimientoCarteraEnTransito" = m."IdMovimientoCarteraEnTransito"
ORDER BY "FechaOperacion"


--PARA ACTUALIZAR EL SALDO CUSTODIA DESDE OPTIMUS (recordar vertificar que el saldo desde el respaldo coincide)
DELETE FROM BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaRespaldo" ;
INSERT INTO BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaRespaldo" 
("FechaSistema", "DocumentoIdentidad", "IdPortafolio", "CodigoPortafolio", "IdClasificacionTipoInstrumento", "IdInstrumento", "Nemotecnico", "Saldo")
SELECT '2022-06-14' AS "FechaSistema", POR."DocumentoIdentidad",CUS."IdPortafolio",POR."CodigoPortafolio",TINS."IdClasificacionTipoInstrumento",
CUS."IdInstrumento",INS."Nemotecnico",SUM(CUS."INGCantidad")  AS "Saldo"
FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" CUS
LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CUS."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" POR ON CUS."IdPortafolio" = POR."IdPortafolio"
WHERE TINS."IdClasificacionTipoInstrumento" = 'RV' AND CUS."IdEstado" = 'VIG'
--AND CUS."IdPortafolio" = 2555
GROUP BY POR."DocumentoIdentidad",POR."CodigoPortafolio",CUS."IdPortafolio",CUS."IdInstrumento",INS."Nemotecnico",TINS."IdClasificacionTipoInstrumento"

--SELECT DISTINCT "DocumentoIdentidad" FROM (
SELECT cus.*,COALESCE(res."Saldo",0) AS "SaldoOptimus" 
FROM BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaCalculado" cus
LEFT JOIN BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaRespaldo" res ON res."IdPortafolio" = cus."IdPortafolio" 
	AND cus."IdInstrumento" = res."IdInstrumento" AND cus."FechaSistema" = res."FechaSistema"
WHERE cus."FechaSistema" = '2022-06-14' AND cus."IdClasificacionTipoInstrumento" = 'RV'
AND COALESCE(cus."Saldo",0) != COALESCE(res."Saldo",0)
AND cus."DocumentoIdentidad" != '96921130-0'
--AND cus."DocumentoIdentidad" NOT IN('76513680-6','76756963-7','76023598-9')
ORDER BY "DocumentoIdentidad","CodigoPortafolio","Nemotecnico"
--)

ARREGLADO--1 2022-05-05	12855315-0	6074	0	RV	222	BSANTANDER	50000.00000000	0.00000000	0.00000000	0.00000000	250000.00000000 DESCUADRADO POR PRESTAMOS, NO RETIRARON, DEBERIA TENER 50.000, LE SACARON TODO
ARREGLADO--2 2022-05-05	12855315-0	6074	0	RV	356	CHILE	0.00000000	0.00000000	0.00000000	0.00000000	180000.00000000 DESCUADRADO POR VENC PRESTAMOS, NO RETIRARON, SE ARREGLO
ARREGLADO--3 2022-05-05	12855315-0	6074	0	RV	2042	SQM-B	319.00000000	0.00000000	0.00000000	0.00000000	382.00000000 DESCUADRE POR 63, VENTAS DEL 11/02 SOLO REBAJO 6, LE SACARON TODO
ARREGLADO--4 2022-05-05	15380738-8	5967	0	RV	253	CAP	11575.00000000	0.00000000	312.00000000	0.00000000	0.00000000  DESCUADRADO, LA CUSTODIA DESAPARECE EL 22/02 Y NO TIENE NINGUN MOVIMIENTO ASOCIADO
ARREGLADO--5 2022-05-05	16609906-4	5959	0	RV	260537	CFIFTRLP-E	18850.00000000	0.00000000	0.00000000	0.00000000	0.00000000 DESAPARECIERON LAS CARTERAS EL 4/3/2022
ARREGLADO--6 2022-05-06	5549427-4	4353	1	RV	260537	CFIFTRLP-E	281000.00000000	0.00000000	0.00000000	0.00000000	0.00000000 DESAPARECIERON LAS CARTERAS EL 4/3/2022
ARREGLADO--7 2022-05-06	76327976-6	4173	0	RV	4966	CFIMBIGL-A	25.00000000	-25.00000000	0.00000000	0.00000000	0.00000000 ESTA OK (SE NETEA GARANTIA Y SALDO, QUEDA EN 0)
ARREGLADO--8 2022-05-06	76362185-5	955	0	RV	211556	CFIMBIDT-B	3115.00000000	0.00000000	0.00000000	1116.00000000	1999.00000000 ESTA OK, EL ANTICIPO DEL 2022-03-30 FUE REALMENTE POR 4527
ARREGLADO--9 2022-05-06	76414943-2	2804	0	RV	260537	CFIFTRLP-E	397800.00000000	0.00000000	0.00000000	0.00000000	0.00000000 DESAPARECIERON LAS CARTERAS EL 4/3/2022
ARREGLADO--10 2022-05-11	76697482-1	1127	0	RV	2169	VAPORES	4573632.00000000	180000.00000000	996368.00000000	0.00000000	5173632.00000000 ESTA OK
ARREGLADO--11 2022-05-11	76756963-7	1860	0	RV	2042	SQM-B	-1737.00000000	-6623.00000000	0.00000000	-8360.00000000	0.00000000	ESTA OK
ARREGLADO--12 2022-05-11	78539300-7	4632	0	RV	253	CAP	-17401.00000000	0.00000000	6140.00000000	0.00000000	0.00000000 ESTA OK

--SALDO INICIAL
SELECT * FROM TBCTCURV WHERE rut_cli like  '%76669690%' AND SEC_RUT_CLI = 0 AND nemo like '%AGUAS-A' 

custodia 0
com plazo 312978.00
ven plazo  0
can garantia  0


--SALDO CALCULADO
SELECT * FROM BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaCalculado" 
WHERE "DocumentoIdentidad" like '%15380738%' AND "CodigoPortafolio" = 0 AND "Nemotecnico" = 'CAP'
ORDER BY "FechaSistema"

SELECT * FROM BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodia" 
WHERE "DocumentoIdentidad" like '%15380738%' AND "CodigoPortafolio" = 0 AND "Nemotecnico" = 'CAP'  ORDER BY "FechaSistema","IdTipoOperacion"

SELECT ex."Comentario",tr."FechaOperacion",tr."IdTipoOperacion","IdConcepto","Cantidad",tr.* 
FROM BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodiaExclusiones" ex
LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" tr ON ex."IdMovimientoCarteraEnTransito" = tr."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" POR ON tr."IdPortafolio" = POR."IdPortafolio"
WHERE "DocumentoIdentidad" like '%76023598%' AND "CodigoPortafolio" = 0 AND "Nemotecnico" = 'CHILE'


SELECT * 
FROM BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" mc
LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.Comprobante" c ON c."IdComprobante" = mc."IdComprobante"
LEFT JOIN "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" tr and
WHERE "IdMovimientoComprobante" = 283925
ORDER BY "IdMovimientoComprobante" DESC

			
SELECT CUS."FechaOperacion",POR."DocumentoIdentidad",CUS."IdPortafolio",POR."CodigoPortafolio",CUS."IdClasificacionTipoInstrumento",
			SUB."IdInstrumento",SUB."Nemotecnico",CUS."INGCantidad" AS "Cantidad",
			CASE CUS."Nemotecnico" WHEN 'FINES GENERALES' THEN 'FNGRLS' WHEN 'EMPRESTITO' THEN 'EMPRES' 
			WHEN 'VENTA CORTA' THEN 'VTACTA'
			END AS "IdTipoOperacion",CUS."IdCartera",'CUS'
			FROM (
				SELECT *
				FROM (
					SELECT row_number() OVER (PARTITION BY "FolioCartera" ORDER BY "IdCartera" asc) AS NN,* 
					FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CUS
					) WHERE NN = 1
				) AS CUS
			LEFT JOIN (
					SELECT "FolioCarteraRelacionada","Nemotecnico","IdInstrumento","IdSubyacente","FolioSubyacente" FROM (
						SELECT row_number() OVER (PARTITION BY "FolioCarteraRelacionada" ORDER BY "IdSubyacente" asc) AS NN,"Nemotecnico","FolioCarteraRelacionada","IdInstrumento","IdSubyacente","FolioSubyacente"
						FROM "BSCL"."BSCL.Custodia.BD::CUS.Subyacente")
					WHERE nn = 1
				) AS SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera"
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" POR ON CUS."IdPortafolio" = POR."IdPortafolio"
			WHERE (CUS."Nemotecnico" IN ('FINES GENERALES','EMPRESTITO','VENTA CORTA') )
			AND CUS."IdEstado" != 'ANU'
			AND SUB."Nemotecnico" IS NOT NULL
			AND "EsCustodiaExterna" = 'N'
			AND "DocumentoIdentidad" = '96519800-8' AND sub."Nemotecnico" = 'BSANTANDER'


SELECT "IdTipoOperacion","INGIdTipoOperacion","INGCantidad","IdEstado","EGRIdTipoOperacion","EGRIdMovimientoCarteraEnTransito","FechaOperacion","EGRFechaOperacion","EGRCantidad",* 
FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" WHERE "EGRIdTipoComprobante" = 'FAE' AND "EGRFolioComprobante" = 341160 AND "Nemotecnico" = 'CAP'
ORDER BY "FolioCartera","IdCartera"

SELECT "IdTipoOperacion","INGIdTipoOperacion","INGCantidad","IdEstado","EGRIdTipoOperacion","EGRIdMovimientoCarteraEnTransito","FechaOperacion","EGRFechaOperacion","EGRCantidad",* 
FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" WHERE "FolioCartera" = 119767
ORDER BY "FolioCartera","IdCartera"

SELECT "IdTipoOperacion","INGIdTipoOperacion","INGCantidad","IdEstado","EGRIdTipoOperacion","EGRIdMovimientoCarteraEnTransito","FechaOperacion","EGRFechaOperacion","EGRCantidad",* 
FROM "BSCL"."BSCL.Custodia.BD::CUS.Cartera" WHERE "EGRIdMovimientoCarteraEnTransito" = 7064

SELECT "IdTipoComprobante","IdCarga","IdTipoOperacion","INGCantidad","IdEstado","EGRIdTipoOperacion","FechaOperacion","EGRFechaOperacion","EGRCantidad",* 
FROM "BSCL"."BSCL.Custodia.BD::HIS.Cartera" WHERE "FolioCartera" = 69435
ORDER BY "FechaRespaldo","FolioCartera","IdCartera"




SET SCHEMA "BSCL"; 
-----------------------------------------------------------------------------------------------------------------------------------------
----- CARGA INSTRUMENTOS DESDE RUEBAS ------
-----------------------------------------------------------------------------------------------------------------------------------------
CALL "BSCL"."BSCL.Plataforma.BD.Servicios::INS.CargarInstrumentos"(1,'2015-11-13',1,'',?,?);


--- INGRESAR INSTRUMENTOS QUE NO ESTAN EN RUEBAS
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL,'SIMULTANEA COMPRA','',0.00000000,'S','N','SIMULTANEA','','2013-01-16','0001-01-01',0,'','','N','','SIM','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'SIMULTANEA VENTA','',0.00000000,'S','N','SIMULTANEA','','2013-01-16','0001-01-01',0,'','','N','','SIM','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PRESTAMO VENTA CORTA','',0.00000000,'N','N','PRTVTC','','1900-01-01','1900-01-01',0,'','','N','CLP','PRT','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;                  

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PRESTAMO FINES GENERALES','',0.00000000,'N','N','PRTFIN','','1900-01-01','1900-01-01',0,'','','N','CLP','PRT','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PRESTAMO ADMINISTRATIVO','',0.00000000,'N','N','PRTADM','','1900-01-01','1900-01-01',0,'','','N','CLP','PRT','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'EMPRESTITO','',0.00000000,'N','N','EMPRES','','1900-01-01','1900-01-01',0,'','','N','CLP','EMP','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PDBC','',5000000.00000000,'S','N','PAGARE DESCONTABLE BANCO CENTRAL','','0001-01-01','0001-01-01',0,'CENTRAL','NR','N','CLP','PAG','ACT',2,1,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PRBC','',0.00000000,'S','N','PAGARE REAJUSTABLE BANCO CENTRAL','','0001-01-01','0001-01-01',0,'CENTRAL','','S','','PAG','ACT',2,1,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PDT','',1000.00000000,'S','N','PAGARE DESCONTABLE DE LA TESORERIA','','0001-01-01','0001-01-01',0,'TESORERIA','NR','N','CLP','PAG','ACT',2,1,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PRT','',0.00000000,'S','N','PAGARE REAJUSTABLE DE LA TESORERIA','','0001-01-01','0001-01-01',0,'TESORERIA','','S','','PAG','ACT',2,1,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PAGARE R','',0.00000000,'S','N','PAGARE REAJUSTABLE','','0001-01-01','0001-01-01',0,'','','S','','PAG','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") 
select "BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL, 'PAGARE NR','',0.00000000,'S','N','PAGARE REAJUSTABLE','','0001-01-01','0001-01-01',0,'','NR','N','CLP','PAG','ACT',2,0,1,0,'0001-01-01','0001-01-01','00:00:00' from dummy;
 
/* COPIAR DESDE UNA BASA HANA A OTRA
select ' INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" ( "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","Lista","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio","IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre") values ('
             || TO_ALPHANUM(INS."IdInstrumento") ||
		 ',' || '''' || INS."Nemotecnico" || '''' ||
		 ',' || '''' || INS."ISIN" || '''' ||
         ',' || TO_ALPHANUM(INS."CorteMinimo") ||
		 ',' || '''' || INS."EstaInscrito" || '''' ||
		 ',' || '''' || INS."EsDivisible" || '''' ||
		 ',' || '''' || INS."RazonSocial" || ''''
		 ',' || '''' || INS."Lista" || ''''||
		 ',' || '''' || INS."FechaEmision" || ''''||
		 ',' || '''' || INS."FechaVencimiento" || ''''||
		 ',' || TO_ALPHANUM(INS."IdInstrumentoRelacionado") || 
		 ',' || '''' || INS."IdEmisor" || ''''||
		 ',' || '''' || INS."IdTipoReajuste" || ''''||
		 ',' || '''' || INS."Reajustable" || ''''||		 
		 ',' || '''' || INS."IdMoneda" || ''''||	
		 ',' || '''' || INS."IdTipoInstrumento" || ''''||
		 ',' || '''' || INS."IdEstado" || ''''||	
		 ',' || TO_ALPHANUM(INS."DecimalesPrecio") || 		 
		 ',' || TO_ALPHANUM(INS."IdClasificacionRiesgo") || 		 		 
		 ',' || TO_ALPHANUM(INS."Tamano") || 		 		 
		 ',' || TO_ALPHANUM(INS."DiasAlVencimiento") || 		 		 
		 ',' || '''' || INS."FechaFijaCierre" || ''''		
		 ',' || '''' || INS."FechaInicioVigencia" || ''''||		
		 ',' || '''' || INS."HoraFijaCierre" || ''''||
		 ' ) ' || '&'				 		  		 
 as cadena 
 from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"	INS		
-- where "Nemotecnico" in ('SIMULTANEA COMPRA','SIMULTANEA VENTA','PRESTAMO VENTA CORTA','PRESTAMO FINES GENERALES','PRESTAMO ADMINISTRATIVO','EMPRESTITO','PDBC','PRBC','PDT','PRT','PAGARE NR','PAGARE R');
*/

---- REGULARIZA FUTUROS
--select *	from "BSCL.Plataforma.BD::INS.Instrumento" a WHERE a."RazonSocial" like '%FU%'
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'FUI', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'FUTURO IPSA' WHERE  "Nemotecnico" = 'IPSAM-1215';
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'USD',"IdTipoInstrumento" = 'FUD', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'FUTURO DOLAR' WHERE  "Nemotecnico" = 'DOLAR-1215';
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'FIC', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'FUTURO ICP' WHERE  "Nemotecnico" = 'ICP-1215';
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'USD',"IdTipoInstrumento" = 'FUB', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'FUTURO BCU' WHERE  "Nemotecnico" = 'BCU-1215';

---- REGULARIZA SIMULTANEAS
-- select *	from "BSCL.Plataforma.BD::INS.Instrumento" a WHERE a."Nemotecnico" like '%SIM%'
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= '',"IdTipoInstrumento" = 'SIM', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'SIMULTANEA' WHERE  "Nemotecnico" = 'SIMULTÁNEA COMPRA';
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= '',"IdTipoInstrumento" = 'SIM', "DecimalesPrecio" = 2,  "ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'S', "EsDivisible" = 'N', "RazonSocial" = 'SIMULTANEA' WHERE  "Nemotecnico" = 'SIMULTÁNEA VENTA';

---- REGULARIZA PRESTAMOS
--select *	from "BSCL.Plataforma.BD::INS.Instrumento" a WHERE a."Nemotecnico" like '%PRES%'
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'PRT', "DecimalesPrecio" = 2,  
													"ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'N', "EsDivisible" = 'N', "RazonSocial" = 'PRTVTC'
											 WHERE  "Nemotecnico" = 'PRESTAMO VENTA CORTA';
											 
UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'PRT', "DecimalesPrecio" = 2,  
													"ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'N', "EsDivisible" = 'N', "RazonSocial" = 'PRTFIN'
											 WHERE  "Nemotecnico" = 'PRESTAMO FINES GENERALES';

UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'PRT', "DecimalesPrecio" = 2,  
													"ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'N', "EsDivisible" = 'N', "RazonSocial" = 'PRTADM'
											 WHERE  "Nemotecnico" = 'PRESTAMO ADMINISTRATIVO';

UPDATE "BSCL.Plataforma.BD::INS.Instrumento"  SET "IdMoneda"= 'CLP',"IdTipoInstrumento" = 'EMP', "DecimalesPrecio" = 2,  
													"ISIN" = '', "CorteMinimo"=0, "EstaInscrito" = 'N', "EsDivisible" = 'N', "RazonSocial" = 'EMPRES'
											 WHERE  "Nemotecnico" = 'EMPRESTITO';
	
---- REGULARIZA PAGARES										 											 
/*select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" INS where "Nemotecnico" in('PDBC','PRBC','PDT','PRT','PAGARE NR','PAGARE R');
select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" INS where "IdTipoInstrumento" in('PDB','PRC','PDT','TPR','PAG');
select * from "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" ;
*/
UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 5000000
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE DESCONTABLE BANCO CENTRAL'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'CENTRAL'
		, "IdTipoReajuste" = 'CLP'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2	
--from "BSCL.Plataforma.BD::INS.Instrumento" INS
where "Nemotecnico" = 'PDBC'

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE BANCO CENTRAL'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'CENTRAL'
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PRBC'

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 1000
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE DESCONTABLE DE LA TESORERIA'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'TESORERIA'
		, "IdTipoReajuste" = 'CLP'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PDT'

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE DE LA TESORERIA'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'TESORERIA'
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PRT'

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  
	SET
	 "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = ''
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
WHERE  "Nemotecnico" = 'PAGARE R';

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  
	SET
	 "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE'
		, "Lista"       = ''
		, "FechaEmision"= '0001-01-01'
		, "FechaVencimiento" = '0001-01-01'
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = ''
		, "IdTipoReajuste" = 'CLP'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
WHERE  "Nemotecnico" = 'PAGARE NR';


UPDATE "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" SET "IdClasificacionTipoInstrumento" = 'IF' where "IdTipoInstrumento" in('PDB','PRC','PDT','TPR');

---- INGRESAR BOLETAS DE GARANTIAS
insert into "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
select "BSCL"."BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL,'BOLGTIA','',0,"EstaInscrito","EsDivisible",'Boleta de Garantia',"Lista","FechaEmision","FechaVencimiento"
,"IdInstrumentoRelacionado",'',"IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento","IdEstado","DecimalesPrecio"
,"IdClasificacionRiesgo","Tamano","DiasAlVencimiento","FechaFijaCierre","FechaInicioVigencia","HoraFijaCierre","TasaFiscal","IndArt104"
--select *
from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
where "IdTipoInstrumento" = 'PAG' and "Nemotecnico" = 'PDBC'

select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" = 'BOLGTIA'

/*
insert into "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" 
select 328683,"IdTipoInstrumento","IdTipoReajuste",0,"IdMoneda"
from "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" where "IdInstrumento" = 2223
*/

